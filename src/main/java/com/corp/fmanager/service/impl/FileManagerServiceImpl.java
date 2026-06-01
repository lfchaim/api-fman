package com.corp.fmanager.service.impl;

import com.corp.fmanager.config.FileManagerConfig;
import com.corp.fmanager.dto.request.CreateFolderRequest;
import com.corp.fmanager.dto.request.MoveRequest;
import com.corp.fmanager.dto.request.RenameRequest;
import com.corp.fmanager.dto.response.FileMetadataResponse;
import com.corp.fmanager.exception.FileManagerException;
import com.corp.fmanager.model.FileAudit;
import com.corp.fmanager.repository.FileAuditRepository;
import com.corp.fmanager.service.FileManagerService;
import com.corp.fmanager.util.FileManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação concreta do FileManagerService.
 * Encapsula toda lógica de negócio de operações sobre o sistema de arquivos.
 */
@Service
@Transactional
public class FileManagerServiceImpl implements FileManagerService {

    private static final Logger log = LoggerFactory.getLogger(FileManagerServiceImpl.class);

    private final FileManagerConfig   config;
    private final FileManagerUtils    utils;
    private final FileAuditRepository auditRepo;

    public FileManagerServiceImpl(FileManagerConfig config, FileManagerUtils utils,
                                  FileAuditRepository auditRepo) {
        this.config    = config;
        this.utils     = utils;
        this.auditRepo = auditRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> listDirectory(String relativePath) {
        log.info("Listando diretório: '{}'", relativePath);
        Path dir = utils.resolveSafePath(relativePath);
        if (!Files.exists(dir))
            throw new FileManagerException("Diretório não encontrado: " + relativePath, HttpStatus.NOT_FOUND);
        if (!Files.isDirectory(dir))
            throw new FileManagerException("O caminho não é um diretório: " + relativePath, HttpStatus.BAD_REQUEST);

        List<FileMetadataResponse> items = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) items.add(buildMetadata(entry));
        } catch (IOException e) {
            throw new FileManagerException("Erro ao listar diretório", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        audit(FileAudit.Operation.LIST, relativePath, dir.getFileName().toString(), null, null, true, null);
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataResponse getMetadata(String relativePath) {
        Path path = utils.resolveSafePath(relativePath);
        if (!Files.exists(path))
            throw new FileManagerException("Item não encontrado: " + relativePath, HttpStatus.NOT_FOUND);
        return buildMetadata(path);
    }

    @Override
    public FileMetadataResponse uploadFile(String relativePath, MultipartFile file) {
        log.info("Upload de '{}' para '{}'", file.getOriginalFilename(), relativePath);
        String originalName = sanitizeFileName(file.getOriginalFilename());
        utils.validateFileExtension(originalName);
        utils.validateFileSize(file.getSize());

        Path dir    = utils.resolveSafePath(relativePath);
        if (!Files.exists(dir))
            throw new FileManagerException("Diretório de destino não existe: " + relativePath, HttpStatus.NOT_FOUND);

        Path target = dir.resolve(originalName).normalize();
        utils.resolveSafePath(getRelativePath(target));

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            audit(FileAudit.Operation.UPLOAD, relativePath, originalName, file.getSize(), null, false, e.getMessage());
            throw new FileManagerException("Erro ao salvar arquivo", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        String mimeType = utils.detectMimeType(target);
        audit(FileAudit.Operation.UPLOAD, relativePath, originalName, file.getSize(), mimeType, true, null);
        return buildMetadata(target);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(String relativePath) {
        Path filePath = utils.resolveSafePath(relativePath);
        if (!Files.exists(filePath) || Files.isDirectory(filePath))
            throw new FileManagerException("Arquivo não encontrado: " + relativePath, HttpStatus.NOT_FOUND);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.isReadable())
                throw new FileManagerException("Arquivo não legível", HttpStatus.FORBIDDEN);
            audit(FileAudit.Operation.DOWNLOAD, relativePath, filePath.getFileName().toString(), null, null, true, null);
            return resource;
        } catch (Exception e) {
            if (e instanceof FileManagerException fme) throw fme;
            throw new FileManagerException("Erro ao preparar download", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public FileMetadataResponse createFolder(CreateFolderRequest request) {
        log.info("Criando pasta '{}' em '{}'", request.getFolderName(), request.getParentPath());
        Path parent    = utils.resolveSafePath(request.getParentPath());
        Path newFolder = parent.resolve(request.getFolderName()).normalize();
        utils.resolveSafePath(getRelativePath(newFolder));

        if (Files.exists(newFolder))
            throw new FileManagerException("Pasta já existe: " + request.getFolderName(), HttpStatus.CONFLICT);

        try {
            Files.createDirectories(newFolder);
        } catch (IOException e) {
            audit(FileAudit.Operation.MKDIR, request.getParentPath(), request.getFolderName(), null, null, false, e.getMessage());
            throw new FileManagerException("Erro ao criar pasta", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        audit(FileAudit.Operation.MKDIR, request.getParentPath(), request.getFolderName(), null, null, true, null);
        return buildMetadata(newFolder);
    }

    @Override
    public FileMetadataResponse rename(RenameRequest request) {
        log.info("Renomeando '{}' para '{}'", request.getCurrentPath(), request.getNewName());
        Path current = utils.resolveSafePath(request.getCurrentPath());
        Path renamed = current.resolveSibling(request.getNewName()).normalize();

        if (!Files.exists(current))
            throw new FileManagerException("Item não encontrado: " + request.getCurrentPath(), HttpStatus.NOT_FOUND);
        if (Files.exists(renamed))
            throw new FileManagerException("Já existe um item com o nome: " + request.getNewName(), HttpStatus.CONFLICT);

        try {
            Files.move(current, renamed, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            audit(FileAudit.Operation.RENAME, request.getCurrentPath(), request.getNewName(), null, null, false, e.getMessage());
            throw new FileManagerException("Erro ao renomear", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        audit(FileAudit.Operation.RENAME, request.getCurrentPath(), request.getNewName(), null, null, true, null);
        return buildMetadata(renamed);
    }

    @Override
    public FileMetadataResponse move(MoveRequest request) {
        log.info("Movendo '{}' para '{}'", request.getSourcePath(), request.getDestinationPath());
        Path source      = utils.resolveSafePath(request.getSourcePath());
        Path destination = utils.resolveSafePath(request.getDestinationPath());

        if (!Files.exists(source))
            throw new FileManagerException("Origem não encontrada: " + request.getSourcePath(), HttpStatus.NOT_FOUND);
        if (Files.exists(destination))
            throw new FileManagerException("Destino já existe: " + request.getDestinationPath(), HttpStatus.CONFLICT);

        try {
            Files.createDirectories(destination.getParent());
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            audit(FileAudit.Operation.MOVE, request.getSourcePath(), source.getFileName().toString(), null, null, false, e.getMessage());
            throw new FileManagerException("Erro ao mover", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        audit(FileAudit.Operation.MOVE, request.getSourcePath(), source.getFileName().toString(), null, null, true, null);
        return buildMetadata(destination);
    }

    @Override
    public void delete(String relativePath) {
        log.info("Removendo: '{}'", relativePath);
        Path path = utils.resolveSafePath(relativePath);
        if (!Files.exists(path))
            throw new FileManagerException("Item não encontrado: " + relativePath, HttpStatus.NOT_FOUND);

        Path base = Paths.get(config.getBasePath()).normalize().toAbsolutePath();
        if (path.equals(base))
            throw new FileManagerException("Não é permitido deletar a pasta-base", HttpStatus.FORBIDDEN);

        try {
            deleteRecursively(path);
        } catch (IOException e) {
            audit(FileAudit.Operation.DELETE, relativePath, path.getFileName().toString(), null, null, false, e.getMessage());
            throw new FileManagerException("Erro ao remover", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        audit(FileAudit.Operation.DELETE, relativePath, path.getFileName().toString(), null, null, true, null);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                    Files.delete(f); return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                    Files.delete(d); return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.delete(path);
        }
    }

    private FileMetadataResponse buildMetadata(Path path) {
        FileMetadataResponse meta = new FileMetadataResponse();
        meta.setName(path.getFileName().toString());
        meta.setRelativePath(getRelativePath(path));
        meta.setDirectory(Files.isDirectory(path));
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            long size = attrs.isDirectory() ? 0L : attrs.size();
            meta.setSizeBytes(size);
            meta.setSizeHuman(utils.formatFileSize(size));
            meta.setLastModified(OffsetDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
        } catch (IOException e) {
            log.warn("Não foi possível ler atributos de '{}': {}", path, e.getMessage());
        }
        if (!Files.isDirectory(path)) {
            meta.setMimeType(utils.detectMimeType(path));
            meta.setExtension(utils.extractExtension(path.getFileName().toString()));
        }
        return meta;
    }

    private String getRelativePath(Path absolutePath) {
        Path base = Paths.get(config.getBasePath()).normalize().toAbsolutePath();
        return base.relativize(absolutePath.normalize().toAbsolutePath()).toString();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank())
            throw new FileManagerException("Nome de arquivo inválido", HttpStatus.BAD_REQUEST);
        String clean = Paths.get(fileName).getFileName().toString()
                .replaceAll("[\x00-\x1F\x7F<>:\"/\\|?*]", "_");
        if (clean.isBlank())
            throw new FileManagerException("Nome inválido após sanitização", HttpStatus.BAD_REQUEST);
        return clean;
    }

    private void audit(FileAudit.Operation op, String path, String name,
                       Long size, String mime, boolean success, String errMsg) {
        if (!config.isAuditEnabled()) return;
        try {
            FileAudit a = new FileAudit(op, path, name, success);
            a.setFileSize(size); a.setMimeType(mime); a.setErrorMessage(errMsg);
            auditRepo.save(a);
        } catch (Exception e) {
            log.error("Falha ao persistir auditoria de {}: {}", op, e.getMessage());
        }
    }
}

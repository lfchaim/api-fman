package com.corp.fmanager.controller;

import com.corp.fmanager.dto.request.CreateFolderRequest;
import com.corp.fmanager.dto.request.MoveRequest;
import com.corp.fmanager.dto.request.RenameRequest;
import com.corp.fmanager.dto.response.ApiResponse;
import com.corp.fmanager.dto.response.FileMetadataResponse;
import com.corp.fmanager.service.FileManagerService;
import com.corp.fmanager.util.FileManagerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

/**
 * Controller REST para gerenciamento de arquivos e pastas.
 * Expõe endpoints em /api/v1/fman.
 */
@RestController
@RequestMapping("/api/v1/fman")
@Tag(name = "File Manager", description = "Operações CRUD de arquivos e pastas corporativas")
public class FileManagerController {

    private static final Logger log = LoggerFactory.getLogger(FileManagerController.class);

    private final FileManagerService service;
    private final FileManagerUtils   utils;

    public FileManagerController(FileManagerService service, FileManagerUtils utils) {
        this.service = service; this.utils = utils;
    }

    @Operation(summary = "Listar diretório", description = "Retorna arquivos e pastas no caminho relativo informado.")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FileMetadataResponse>>> listDirectory(
            @Parameter(description = "Caminho relativo à pasta-base", example = "documentos")
            @RequestParam(defaultValue = "") String path) {
        log.info("GET /list  path='{}'", path);
        List<FileMetadataResponse> items = service.listDirectory(path);
        return ResponseEntity.ok(ApiResponse.success("Diretório listado — " + items.size() + " item(ns)", items));
    }

    @Operation(summary = "Obter metadados", description = "Retorna informações detalhadas de arquivo ou pasta.")
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> getInfo(
            @Parameter(description = "Caminho relativo do item", example = "documentos/relatorio.pdf")
            @RequestParam String path) {
        log.info("GET /info  path='{}'", path);
        return ResponseEntity.ok(ApiResponse.success("Metadados obtidos", service.getMetadata(path)));
    }

    @Operation(summary = "Upload de arquivo", description = "Envia um arquivo para o diretório de destino.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileMetadataResponse>> upload(
            @RequestParam(defaultValue = "") String path,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /upload  path='{}', arquivo='{}', tamanho={}", path, file.getOriginalFilename(), file.getSize());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Arquivo enviado com sucesso", service.uploadFile(path, file)));
    }

    @Operation(summary = "Download de arquivo", description = "Realiza o download de um arquivo como stream binário.")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @Parameter(description = "Caminho relativo do arquivo", example = "documentos/relatorio.pdf")
            @RequestParam String path) {
        log.info("GET /download  path='{}'", path);
        Resource resource = service.downloadFile(path);
        String   mime     = utils.detectMimeType(Paths.get(path));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(mime))
                .body(resource);
    }

    @Operation(summary = "Criar pasta", description = "Cria um novo diretório dentro do caminho pai.")
    @PostMapping("/mkdir")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> mkdir(
            @Valid @RequestBody CreateFolderRequest request) {
        log.info("POST /mkdir  parent='{}', nome='{}'", request.getParentPath(), request.getFolderName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Pasta criada com sucesso", service.createFolder(request)));
    }

    @Operation(summary = "Renomear arquivo ou pasta")
    @PutMapping("/rename")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> rename(
            @Valid @RequestBody RenameRequest request) {
        log.info("PUT /rename  atual='{}', novoNome='{}'", request.getCurrentPath(), request.getNewName());
        return ResponseEntity.ok(ApiResponse.success("Item renomeado com sucesso", service.rename(request)));
    }

    @Operation(summary = "Mover arquivo ou pasta")
    @PutMapping("/move")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> move(
            @Valid @RequestBody MoveRequest request) {
        log.info("PUT /move  origem='{}', destino='{}'", request.getSourcePath(), request.getDestinationPath());
        return ResponseEntity.ok(ApiResponse.success("Item movido com sucesso", service.move(request)));
    }

    @Operation(summary = "Remover arquivo ou pasta", description = "Remove permanentemente um arquivo ou pasta (recursivo).")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Caminho relativo do item", example = "temp/arquivo.txt")
            @RequestParam String path) {
        log.info("DELETE /delete  path='{}'", path);
        service.delete(path);
        return ResponseEntity.ok(ApiResponse.success("Item removido com sucesso"));
    }
}

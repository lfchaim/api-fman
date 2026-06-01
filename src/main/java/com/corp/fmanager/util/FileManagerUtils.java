package com.corp.fmanager.util;

import com.corp.fmanager.config.FileManagerConfig;
import com.corp.fmanager.exception.FileManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitários compartilhados: resolução segura de caminhos, formatação
 * de tamanho, detecção de MIME type e validação de extensões.
 */
@Component
public class FileManagerUtils {

    private static final Logger log = LoggerFactory.getLogger(FileManagerUtils.class);
    private final FileManagerConfig config;

    public FileManagerUtils(FileManagerConfig config) { this.config = config; }

    /** Resolve e valida caminho relativo contra a pasta-base (anti path-traversal). */
    public Path resolveSafePath(String relativePath) {
        Path base     = Paths.get(config.getBasePath()).normalize().toAbsolutePath();
        Path resolved = base.resolve(relativePath).normalize().toAbsolutePath();
        if (!resolved.startsWith(base)) {
            log.error("Tentativa de path traversal: '{}' => '{}'", relativePath, resolved);
            throw new FileManagerException("Acesso negado: caminho inválido ou fora da pasta-base", HttpStatus.FORBIDDEN);
        }
        return resolved;
    }

    /** Formata bytes para unidade legível (B, KB, MB, GB). */
    public String formatFileSize(long bytes) {
        if (bytes < 1024)          return bytes + " B";
        if (bytes < 1_048_576)     return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1_073_741_824) return String.format("%.1f MB", bytes / 1_048_576.0);
        return String.format("%.2f GB", bytes / 1_073_741_824.0);
    }

    /** Detecta MIME type pelo conteúdo do arquivo. */
    public String detectMimeType(Path filePath) {
        try {
            String mime = Files.probeContentType(filePath);
            return mime != null ? mime : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /** Extrai extensão do nome do arquivo (lowercase, sem ponto). */
    public String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    /** Lança exceção se a extensão estiver na lista de bloqueados. */
    public void validateFileExtension(String fileName) {
        String ext = extractExtension(fileName);
        if (!ext.isEmpty() && config.getBlockedExtensionList().contains(ext)) {
            throw new FileManagerException("Extensão de arquivo não permitida: ." + ext, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /** Lança exceção se o tamanho exceder o limite configurado. */
    public void validateFileSize(long sizeBytes) {
        if (sizeBytes > config.getMaxFileSizeBytes()) {
            throw new FileManagerException(
                    "Arquivo excede o limite de " + formatFileSize(config.getMaxFileSizeBytes()),
                    HttpStatus.PAYLOAD_TOO_LARGE);
        }
    }
}

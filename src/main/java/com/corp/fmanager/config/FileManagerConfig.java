package com.corp.fmanager.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Configuração da pasta-base e parâmetros do gerenciador de arquivos.
 * Lê as propriedades do prefixo file-manager do application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "file-manager")
public class FileManagerConfig {

    private static final Logger log = LoggerFactory.getLogger(FileManagerConfig.class);

    private String basePath;
    private String blockedExtensions;
    private long maxFileSizeBytes;
    private boolean auditEnabled;
    private List<String> blockedExtensionList;

    @PostConstruct
    public void init() throws IOException {
        Path base = Paths.get(basePath);
        if (!Files.exists(base)) {
            Files.createDirectories(base);
            log.info("Pasta-base criada em: {}", base.toAbsolutePath());
        } else {
            log.info("Pasta-base existente: {}", base.toAbsolutePath());
        }
        this.blockedExtensionList = Arrays.stream(blockedExtensions.split(","))
                .map(String::trim).map(String::toLowerCase).toList();
        log.debug("Extensões bloqueadas: {}", blockedExtensionList);
    }

    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    public String getBlockedExtensions() { return blockedExtensions; }
    public void setBlockedExtensions(String blockedExtensions) { this.blockedExtensions = blockedExtensions; }
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
    public boolean isAuditEnabled() { return auditEnabled; }
    public void setAuditEnabled(boolean auditEnabled) { this.auditEnabled = auditEnabled; }
    public List<String> getBlockedExtensionList() { return blockedExtensionList; }
}

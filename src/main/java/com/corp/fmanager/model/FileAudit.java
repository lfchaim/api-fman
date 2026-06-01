package com.corp.fmanager.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entidade JPA que representa um registro de auditoria de operação
 * sobre arquivo ou pasta no sistema de arquivos.
 */
@Entity
@Table(name = "file_audit", indexes = {
        @Index(name = "idx_file_audit_operation",   columnList = "operation"),
        @Index(name = "idx_file_audit_operated_at", columnList = "operated_at DESC"),
        @Index(name = "idx_file_audit_path",        columnList = "relative_path")
})
public class FileAudit {

    public enum Operation { UPLOAD, DOWNLOAD, DELETE, RENAME, MOVE, MKDIR, LIST }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 20)
    private Operation operation;

    @Column(name = "relative_path", nullable = false, length = 1024)
    private String relativePath;

    @Column(name = "file_name", nullable = false, length = 512)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "operated_at", nullable = false)
    private OffsetDateTime operatedAt = OffsetDateTime.now();

    @Column(name = "operated_by", nullable = false, length = 255)
    private String operatedBy = "system";

    public FileAudit() {}

    public FileAudit(Operation operation, String relativePath, String fileName, boolean success) {
        this.operation    = operation;
        this.relativePath = relativePath;
        this.fileName     = fileName;
        this.success      = success;
        this.operatedAt   = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }
    public String getRelativePath() { return relativePath; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public OffsetDateTime getOperatedAt() { return operatedAt; }
    public void setOperatedAt(OffsetDateTime operatedAt) { this.operatedAt = operatedAt; }
    public String getOperatedBy() { return operatedBy; }
    public void setOperatedBy(String operatedBy) { this.operatedBy = operatedBy; }
}

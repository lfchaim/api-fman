package com.corp.fmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/** DTO de resposta com metadados de um arquivo ou pasta. */
@Schema(description = "Metadados de um arquivo ou pasta")
public class FileMetadataResponse {

    private String name;
    private String relativePath;
    private boolean directory;
    private long sizeBytes;
    private String sizeHuman;
    private String mimeType;
    private OffsetDateTime lastModified;
    private String extension;

    public FileMetadataResponse() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelativePath() { return relativePath; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    public boolean isDirectory() { return directory; }
    public void setDirectory(boolean directory) { this.directory = directory; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getSizeHuman() { return sizeHuman; }
    public void setSizeHuman(String sizeHuman) { this.sizeHuman = sizeHuman; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public OffsetDateTime getLastModified() { return lastModified; }
    public void setLastModified(OffsetDateTime lastModified) { this.lastModified = lastModified; }
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
}

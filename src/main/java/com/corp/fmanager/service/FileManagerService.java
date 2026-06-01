package com.corp.fmanager.service;

import com.corp.fmanager.dto.request.CreateFolderRequest;
import com.corp.fmanager.dto.request.MoveRequest;
import com.corp.fmanager.dto.request.RenameRequest;
import com.corp.fmanager.dto.response.FileMetadataResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato do serviço de gerenciamento de arquivos (DIP — Inversão de Dependência).
 */
public interface FileManagerService {
    List<FileMetadataResponse> listDirectory(String relativePath);
    FileMetadataResponse getMetadata(String relativePath);
    FileMetadataResponse uploadFile(String relativePath, MultipartFile file);
    Resource downloadFile(String relativePath);
    FileMetadataResponse createFolder(CreateFolderRequest request);
    FileMetadataResponse rename(RenameRequest request);
    FileMetadataResponse move(MoveRequest request);
    void delete(String relativePath);
}

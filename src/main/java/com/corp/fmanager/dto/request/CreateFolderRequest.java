package com.corp.fmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO de requisição para criação de pasta. */
@Schema(description = "Dados necessários para criar uma nova pasta")
public class CreateFolderRequest {

    @Schema(description = "Caminho pai relativo à pasta-base", example = "documentos/projetos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O caminho pai não pode ser vazio")
    @Size(max = 1024, message = "O caminho pai não pode exceder 1024 caracteres")
    private String parentPath;

    @Schema(description = "Nome da nova pasta", example = "relatorios-2024", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome da pasta não pode ser vazio")
    @Size(min = 1, max = 255, message = "O nome deve ter entre 1 e 255 caracteres")
    @Pattern(regexp = "^[^/\\\\:*?\"<>|.][^/\\\\:*?\"<>|]*$",
             message = "Nome de pasta inválido: não use /, \\, :, *, ?, \", <, >, | ou ponto inicial")
    private String folderName;

    public String getParentPath() { return parentPath; }
    public void setParentPath(String parentPath) { this.parentPath = parentPath; }
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
}

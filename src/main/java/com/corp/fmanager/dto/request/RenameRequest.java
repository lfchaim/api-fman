package com.corp.fmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO de requisição para renomear arquivo ou pasta. */
@Schema(description = "Dados para renomear um arquivo ou pasta existente")
public class RenameRequest {

    @Schema(description = "Caminho relativo do item a ser renomeado", example = "documentos/relatorio-antigo.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O caminho atual não pode ser vazio")
    @Size(max = 1024, message = "O caminho não pode exceder 1024 caracteres")
    private String currentPath;

    @Schema(description = "Novo nome do arquivo ou pasta", example = "relatorio-2024.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O novo nome não pode ser vazio")
    @Size(min = 1, max = 255, message = "O nome deve ter entre 1 e 255 caracteres")
    @Pattern(regexp = "^[^/\\\\:*?\"<>|.][^/\\\\:*?\"<>|]*$",
             message = "Nome inválido")
    private String newName;

    public String getCurrentPath() { return currentPath; }
    public void setCurrentPath(String currentPath) { this.currentPath = currentPath; }
    public String getNewName() { return newName; }
    public void setNewName(String newName) { this.newName = newName; }
}

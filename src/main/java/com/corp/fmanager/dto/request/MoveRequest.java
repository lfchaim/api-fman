package com.corp.fmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de requisição para mover arquivo ou pasta. */
@Schema(description = "Dados para mover um arquivo ou pasta para outro local")
public class MoveRequest {

    @Schema(description = "Caminho relativo atual do item", example = "temp/arquivo.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O caminho de origem não pode ser vazio")
    @Size(max = 1024, message = "O caminho não pode exceder 1024 caracteres")
    private String sourcePath;

    @Schema(description = "Caminho relativo do destino", example = "documentos/arquivo.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O caminho de destino não pode ser vazio")
    @Size(max = 1024, message = "O caminho não pode exceder 1024 caracteres")
    private String destinationPath;

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getDestinationPath() { return destinationPath; }
    public void setDestinationPath(String destinationPath) { this.destinationPath = destinationPath; }
}

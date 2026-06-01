package com.corp.fmanager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Wrapper padrão de resposta da API.
 * @param <T> tipo dos dados retornados
 */
@Schema(description = "Envelope padrão de resposta da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "Código HTTP da resposta", example = "200")
    private int status;

    @Schema(description = "Mensagem descritiva do resultado")
    private String message;

    @Schema(description = "Dados retornados pela operação")
    private T data;

    @Schema(description = "Timestamp da resposta")
    private OffsetDateTime timestamp;

    private ApiResponse() { this.timestamp = OffsetDateTime.now(); }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>(); r.status = 200; r.message = message; r.data = data; return r;
    }
    public static <T> ApiResponse<T> success(String message) { return success(message, null); }
    public static <T> ApiResponse<T> created(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>(); r.status = 201; r.message = message; r.data = data; return r;
    }
    public static <T> ApiResponse<T> error(int status, String message) {
        ApiResponse<T> r = new ApiResponse<>(); r.status = status; r.message = message; return r;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public OffsetDateTime getTimestamp() { return timestamp; }
}

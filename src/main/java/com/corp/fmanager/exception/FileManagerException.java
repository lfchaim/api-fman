package com.corp.fmanager.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de domínio para erros do gerenciador de arquivos.
 * Carrega o HttpStatus correspondente para resposta HTTP correta.
 */
public class FileManagerException extends RuntimeException {

    private final HttpStatus httpStatus;

    public FileManagerException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public FileManagerException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
}

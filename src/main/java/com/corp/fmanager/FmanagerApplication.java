package com.corp.fmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Ponto de entrada da aplicação Spring Boot — File Manager Corporativo.
 */
@SpringBootApplication
public class FmanagerApplication {

    private static final Logger log = LoggerFactory.getLogger(FmanagerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FmanagerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("=============================================================");
        log.info("  File Manager API iniciado com sucesso!");
        log.info("  Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("  API Docs  : http://localhost:8080/v3/api-docs");
        log.info("=============================================================");
    }
}

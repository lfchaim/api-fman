package com.corp.fmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger / OpenAPI 3.
 * Define metadados da API, servidores e esquema de segurança.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI fileManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Manager API")
                        .description("API REST para gerenciamento corporativo de arquivos e pastas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Infraestrutura Corporativa")
                                .email("infra@corp.com")
                                .url("https://intranet.corp.com/fmanager"))
                        .license(new License()
                                .name("Licença Corporativa Interna")
                                .url("https://intranet.corp.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Desenvolvimento"),
                        new Server().url("https://fmanager.corp.com").description("Produção")))
                .externalDocs(new ExternalDocumentation()
                        .description("Wiki do Projeto")
                        .url("https://intranet.corp.com/wiki/fmanager"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT de autenticação")));
    }
}

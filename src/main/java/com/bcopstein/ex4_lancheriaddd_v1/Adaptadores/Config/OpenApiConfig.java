package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuração do Swagger/OpenAPI para documentação automática da API.
 *
 * A documentação fica disponível em:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - JSON spec: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pizzariaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pizzaria ECA - API")
                        .description("API REST da Pizzaria ECA — Projeto de Arquitetura de Software (DDD / Clean Architecture)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Anthony Antonelli")
                                .email("anthony@example.com")));
    }
}

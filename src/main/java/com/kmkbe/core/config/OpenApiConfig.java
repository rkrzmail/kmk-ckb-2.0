package com.kmkbe.core.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class OpenApiConfig {

    @Bean
    public OpenAPI provideOpenAPI() {
        /*String homeURL = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        log.info("OpenApiConfig homeURL: {}", homeURL);*/

        Server server = new Server();
        server.setUrl("http://localhost:8080");
        //server.setUrl("https://kmk.nikitagenerator.com");
        server.setDescription("Server");

        Contact contact = new Contact();
        contact.setName("NikitaGenerator");
        contact.setUrl("https://nikitagenerator.com");
        contact.setEmail("khesa@nikitagenerator.com");

        Info info = new Info();
        info.setTitle("Kmk Digital 2.0 API");
        info.setDescription("Endpoints from NikitaGenerator");
        info.setContact(contact);
        info.setVersion("1.0.0");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .description("JWT Auth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .in(SecurityScheme.In.HEADER)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")

                        )
                )
                .servers(List.of(server));
    }
}

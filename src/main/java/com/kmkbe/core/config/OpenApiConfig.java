package com.kmkbe.core.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI provideOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Local Server");

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
                .addSecurityItem(new SecurityRequirement().addList("JavaInUseSecurityScheme"))
                .components(new Components()
                        .addSecuritySchemes(
                                "JavaInUseSecurityScheme",
                                new SecurityScheme()
                                        .name("JavaInUseSecurityScheme")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .servers(List.of(server));
    }
}

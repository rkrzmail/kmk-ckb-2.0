package com.kmkbe.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
      .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
      .components(new Components()
        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
          .name(securitySchemeName)
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")))
      .info(
        new Info()
          .title("com.kmk.ckb")
          .version(appVersion)
          .license(new License().name("Apache 2.0").url("http://springdoc.org"))
      );
  }

  @Bean
  public OperationCustomizer addCustomGlobalHeader() {
    return (operation, handlerMethod) -> {
      Parameter rtokenHeader = new Parameter()
        .in("header")
        .name("rtoken")
        .description("Refresh Token")
        .schema(new StringSchema())
        .required(false);

      operation.addParametersItem(rtokenHeader);

      return operation;
    };
  }
}

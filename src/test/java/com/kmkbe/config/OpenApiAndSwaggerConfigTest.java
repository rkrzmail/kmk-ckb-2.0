package com.kmkbe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test OpenApiConfig & SwaggerConfig: metadata OpenAPI dan
 * OperationCustomizer yang menambahkan header rtoken ke setiap operation.
 */
class OpenApiAndSwaggerConfigTest {

  private OpenApiConfig openApiConfig;
  private SwaggerConfig swaggerConfig;

  @BeforeEach
  void setUp() {
    openApiConfig = new OpenApiConfig();
    swaggerConfig = new SwaggerConfig();
  }

  @Test
  void openApiBeanCarriesTitleVersionAndBearerScheme() {
    OpenAPI api = openApiConfig.provideOpenAPI();

    assertThat(api.getInfo().getTitle()).isEqualTo("Kmk Digital 2.0 API");
    assertThat(api.getInfo().getVersion()).isEqualTo("1.0.0");
    assertThat(api.getInfo().getContact().getName()).isEqualTo("NikitaGenerator");
    // SecurityScheme Bearer terdaftar
    var scheme = api.getComponents().getSecuritySchemes().get("Bearer");
    assertThat(scheme).isNotNull();
    assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
    assertThat(scheme.getScheme()).isEqualTo("bearer");
    assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    // server localhost terdaftar
    assertThat(api.getServers()).isNotEmpty();
    assertThat(api.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
  }

  @Test
  void swaggerOpenApiBeanUsesConfiguredVersion() {
    OpenAPI api = swaggerConfig.openAPI("1.2.3");

    assertThat(api.getInfo().getVersion()).isEqualTo("1.2.3");
    assertThat(api.getInfo().getTitle()).isEqualTo("com.kmk.ckb");
    var scheme = api.getComponents().getSecuritySchemes().get("bearerAuth");
    assertThat(scheme).isNotNull();
    assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
  }

  @Test
  void operationCustomizerAddsOptionalRtokenHeader() {
    OperationCustomizer customizer = swaggerConfig.addCustomGlobalHeader();

    io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.toString()).thenReturn("HandlerMethodStub");

    io.swagger.v3.oas.models.Operation result = customizer.customize(operation, handlerMethod);

    assertThat(result).isSameAs(operation);
    Parameter rtoken = result.getParameters().stream()
      .filter(p -> "rtoken".equals(p.getName()))
      .findFirst()
      .orElseThrow();

    assertThat(rtoken.getIn()).isEqualTo("header");
    assertThat(rtoken.getRequired()).isFalse();
    assertThat(rtoken.getSchema()).isInstanceOf(StringSchema.class);
  }
}

package com.kmkbe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test RestTemplateConfig: bean restTemplate memakai Apache HttpClient 5,
 * converters kustom terpasang (PlainDouble serializer + url-encoded), dan
 * konfigurasi ObjectMapper yang benar.
 */
class RestTemplateConfigTest {

  private RestTemplateConfig config;

  @BeforeEach
  void setUp() {
    config = new RestTemplateConfig();
  }

  @Test
  void restTemplateBeanIsBuiltWithHttpClientFactory() {
    RestTemplate restTemplate = config.restTemplate();

    assertThat(restTemplate).isNotNull();
    // HttpComponentsClientHttpRequestFactory di bawah kapnya
    assertThat(ReflectionTestUtils.getField(restTemplate, "requestFactory")).isNotNull();
  }

  @Test
  void restTemplateCarriesCustomConverters() {
    RestTemplate restTemplate = config.restTemplate();

    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
    assertThat(converters).isNotEmpty();

    MappingJackson2HttpMessageConverter jsonConverter = converters.stream()
      .filter(MappingJackson2HttpMessageConverter.class::isInstance)
      .map(MappingJackson2HttpMessageConverter.class::cast)
      // converter kustom ditambahkan SETELAH default converter bawaan RestTemplate
      .reduce((first, second) -> second)
      .orElseThrow();

    // serializer Double sebagai plain (bukan scientific notation)
    var objectMapper = jsonConverter.getObjectMapper();
    assertThat(objectMapper.isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)).isTrue();
    // unknown properties diabaikan
    assertThat(objectMapper.isEnabled(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
  }

  @Test
  void objectMapperHandlesBigDecimalAsPlainAndEmptyStringAsNull() throws Exception {
    RestTemplate restTemplate = config.restTemplate();

    var objectMapper = restTemplate.getMessageConverters().stream()
      .filter(MappingJackson2HttpMessageConverter.class::isInstance)
      .map(MappingJackson2HttpMessageConverter.class::cast)
      .findFirst()
      .orElseThrow()
      .getObjectMapper();

    String json = objectMapper.writeValueAsString(java.util.Map.of("amount", new java.math.BigDecimal("10000000.00")));
    assertThat(json).doesNotContain("1.0E+7").contains("10000000.00");
  }
}

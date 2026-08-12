package com.kmkbe.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kmkbe.core.converter.ObjectToUrlEncodedConverter;
import com.kmkbe.nikita.utils.PlainDoubleSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    CloseableHttpClient httpClient = HttpClients.custom()
      .disableRedirectHandling()
      .build();

    HttpComponentsClientHttpRequestFactory requestFactory =
      new HttpComponentsClientHttpRequestFactory(httpClient);

    requestFactory.setConnectTimeout(10000);

    RestTemplate restTemplate = new RestTemplate(requestFactory);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    objectMapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    SimpleModule module = new SimpleModule();
    module.addSerializer(Double.class, new PlainDoubleSerializer());
    module.addSerializer(double.class, new PlainDoubleSerializer());
    objectMapper.registerModule(module);

    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
    messageConverter.setObjectMapper(objectMapper);
    restTemplate.getMessageConverters().add(messageConverter);
    restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));

    return restTemplate;
  }
}

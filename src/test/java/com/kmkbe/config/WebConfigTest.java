package com.kmkbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebConfigTest {

  @Test
  void configureMessageConvertersSetsExpectedConverters() {
    ObjectMapper mapper = new ObjectMapper();
    WebConfig config = new WebConfig(mapper);
    List<HttpMessageConverter<?>> converters = new ArrayList<>();

    config.configureMessageConverters(converters);

    assertThat(converters).hasSize(3);
    assertThat(converters.get(0)).isInstanceOf(ByteArrayHttpMessageConverter.class);
    assertThat(converters.get(1)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
    assertThat(converters.get(2)).isInstanceOf(ResourceHttpMessageConverter.class);
  }

  @Test
  void getTaskExecutorAndAsyncSupport() {
    WebConfig config = new WebConfig(new ObjectMapper());
    ConcurrentTaskExecutor executor = config.getTaskExecutor();
    assertThat(executor).isNotNull();

    AsyncSupportConfigurer asyncConfigurer = mock(AsyncSupportConfigurer.class);
    config.configureAsyncSupport(asyncConfigurer);
    verify(asyncConfigurer).setTaskExecutor(any(ConcurrentTaskExecutor.class));
  }

  @Test
  void addCorsMappingsRegistersAllPaths() {
    WebConfig config = new WebConfig(new ObjectMapper());
    CorsRegistry registry = mock(CorsRegistry.class);
    CorsRegistration registration = mock(CorsRegistration.class);
    when(registry.addMapping("/**")).thenReturn(registration);
    when(registration.allowedMethods(any(String[].class))).thenReturn(registration);

    config.addCorsMappings(registry);

    verify(registry).addMapping("/**");
    verify(registration).allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
  }

  @Test
  void addResourceHandlersRegistersUploads() {
    WebConfig config = new WebConfig(new ObjectMapper());
    ReflectionTestUtils.setField(config, "uploadRoot", "uploads");

    ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
    ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
    org.springframework.web.servlet.config.annotation.ResourceChainRegistration chain = mock(org.springframework.web.servlet.config.annotation.ResourceChainRegistration.class);
    when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
    when(registration.addResourceLocations(anyString())).thenReturn(registration);
    when(registration.setCachePeriod(3600)).thenReturn(registration);
    when(registration.resourceChain(true)).thenReturn(chain);
    when(chain.addResolver(any())).thenReturn(chain);

    config.addResourceHandlers(registry);

    verify(registry).addResourceHandler("/uploads/**");
    verify(registration).addResourceLocations(anyString());
    verify(registration).setCachePeriod(3600);
    verify(registration).resourceChain(true);
    verify(chain).addResolver(any());
  }
}

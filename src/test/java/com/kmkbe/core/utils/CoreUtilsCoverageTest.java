package com.kmkbe.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CoreUtilsCoverageTest {
  static {
    System.setProperty("security.aes.key", "0".repeat(32));
  }


  @Test void aesRoundTripAndInvalid() {
    String enc = AESUtils.encrypt("secret");
    assertThat(enc).isNotBlank();
    assertThat(AESUtils.decrypt(enc)).isEqualTo("secret");
    assertThat(AESUtils.decrypt("not-base64!!")).isNull();
    assertThat(CommonFormattingUtils.cleanBase64(enc.replace('+','-').replace('/','_'))).isEqualTo(enc);
  }

  @Test void auditMasking() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("password", "x");
    node.put("API_KEY", "y");
    ObjectNode nested = mapper.createObjectNode();
    nested.put("token", "z");
    nested.put("visible", "ok");
    node.set("nested", nested);
    node.withArray("items").add(mapper.createObjectNode().put("pin","1234"));

    ObjectNode masked = (ObjectNode) AuditMaskingUtils.mask(node);
    assertThat(masked.path("password").asText()).isEqualTo("******");
    assertThat(masked.path("API_KEY").asText()).isEqualTo("******");
    assertThat(masked.path("nested").path("token").asText()).isEqualTo("******");
    assertThat(masked.path("nested").path("visible").asText()).isEqualTo("ok");
    assertThat(masked.path("items").get(0).path("pin").asText()).isEqualTo("******");
    assertThat(node.path("password").asText()).isEqualTo("x");
    assertThat(AuditMaskingUtils.mask(null)).isNull();
    assertThat(AuditMaskingUtils.mask(mapper.nullNode()).isNull()).isTrue();
    assertThat(AuditMaskingUtils.mask(com.fasterxml.jackson.databind.node.TextNode.valueOf("plain")).asText()).isEqualTo("plain");
  }

  @Test void objectUtils() throws Exception {
    assertThat(ObjectUtils.strToJson(null)).isNull();
    assertThat(ObjectUtils.strToJson("")).isNull();
    assertThat(ObjectUtils.strToJson("bad")).isNull();
    Map<String,Object> json = ObjectUtils.strToJson("{\"a\":1}");
    assertThat(json).containsEntry("a",1);

    record Bean(String name, int age) {}
    assertThat(ObjectUtils.jsonToStr(new Bean("a",1))).contains("\"name\"");
    assertThat(ObjectUtils.jsonToStr(new Bean("a",1), false)).contains("\"name\"");
    assertThat(ObjectUtils.objectToJson(new Bean("a",1))).containsEntry("age",1);

    Pojo target = new Pojo();
    Map<String,Object> values = new java.util.LinkedHashMap<>();
    values.put("name","n"); values.put("id",UUID.randomUUID().toString()); values.put("age",7);
    assertThat(ObjectUtils.castObjectFromMap(values, target)).isSameAs(target);
    assertThat(target.name).isEqualTo("n");
    assertThat(target.id).isNotNull();
    assertThat(target.age).isEqualTo(7);
    assertThat(ObjectUtils.castObjectToMap(target)).containsKey("name");
  }

  static class Pojo { String name; UUID id; int age; }

  @Test void fileUtils() throws Exception {
    MultipartFile file = new MockMultipartFile("f", "a.pdf", "application/pdf", "x".getBytes());
    assertThat(FileUtils.getUploadFileExtension(file)).isEqualTo("pdf");
    assertThatThrownBy(() -> FileUtils.getUploadFileExtension(null)).isInstanceOf(Exception.class);
    assertThatThrownBy(() -> FileUtils.getUploadFileExtension(new MockMultipartFile("f", null, null, new byte[0]))).isInstanceOf(Exception.class);
    assertThat(FileUtils.getFileNameExtension("a.txt")).isEqualTo("txt");
    assertThat(FileUtils.getFileNameExtension("abc")).isNull();
    assertThat(FileUtils.getFileNameExtension(" ")).isNull();
    assertThat(FileUtils.getFilePathFromFullPath("a/b/c.txt")).isEqualTo("a/b");
    assertThat(FileUtils.getFilePathFromFullPath(null)).isNull();
  }

  @Test void uriUtils() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
    req.setScheme("http");
    req.setServerName("localhost");
    req.setServerPort(8080);
    req.addHeader("Authorization", "Bearer tok");
    assertThat(UriUtils.getBaseUrl(req)).contains("localhost");
    assertThat(UriUtils.getDomainUrl(req)).doesNotContain("http://");
    assertThat(UriUtils.fileUlr(req, 1, UriUtils.DocType.loan))
      .contains("/api/v1/documents/download/loan/1?token=tok")
      .doesNotContain("rtoken=null");
    req.addHeader("rtoken", "r1");
    assertThat(UriUtils.fileUlr(req, 2, UriUtils.DocType.agreement)).contains("&rtoken=r1");
  }

  @Test void redisUtils() {
    @SuppressWarnings("unchecked")
    RedisTemplate<String,String> template = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String,String> valueOps = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ListOperations<String,String> listOps = mock(ListOperations.class);
    RedisUtils<String> utils = new RedisUtils<>(template, valueOps, listOps);

    utils.putValue("k","v");
    verify(valueOps).set("k","v");
    utils.putValue("k","v",1000);
    verify(valueOps).set("k","v", Duration.ofMillis(1000));
    when(valueOps.get("k")).thenReturn("v");
    assertThat(utils.getValue("k")).isEqualTo("v");
    when(valueOps.getAndExpire("k", Duration.ofMillis(10))).thenReturn("v");
    assertThat(utils.getValueAndExpire("k",10)).isEqualTo("v");
    when(valueOps.getAndDelete("k")).thenReturn("v");
    assertThat(utils.getValueAndDelete("k")).isEqualTo("v");
    when(template.delete("k")).thenReturn(true);
    assertThat(utils.deleteKey("k")).isTrue();
    utils.setExpire("k",1, TimeUnit.SECONDS);
    verify(template).expire("k",1,TimeUnit.SECONDS);
    when(listOps.range("list",0,-1)).thenReturn(List.of("a"));
    assertThat(utils.getValueFromList("list")).containsExactly("a");
    utils.addValueToList("list","b");
    verify(listOps).rightPush("list","b");

    RedisConnection connection = mock(RedisConnection.class);
    @SuppressWarnings("unchecked")
    Cursor<byte[]> cursor = mock(Cursor.class);
    @SuppressWarnings("unchecked") Iterator<byte[]>[] it = new Iterator[]{Arrays.asList("k1".getBytes(StandardCharsets.UTF_8), "k2".getBytes(StandardCharsets.UTF_8)).iterator()};
    when(cursor.hasNext()).thenAnswer(i -> it[0].hasNext());
    when(cursor.next()).thenAnswer(i -> it[0].next());
    when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
    when(template.execute(any(RedisCallback.class))).thenAnswer(inv -> {
      RedisCallback<List<String>> cb = inv.getArgument(0);
      return cb.doInRedis(connection);
    });
    assertThat(utils.getAllKeys()).containsExactly("k1","k2");
    it[0] = Arrays.asList("k1".getBytes(StandardCharsets.UTF_8), "k2".getBytes(StandardCharsets.UTF_8)).iterator();
    when(valueOps.get("k1")).thenReturn("v1");
    when(valueOps.get("k2")).thenReturn("v2");
    assertThat(utils.getAllEntirePairs()).containsExactly("v1","v2");
    verify(cursor, times(2)).close();
  }
}

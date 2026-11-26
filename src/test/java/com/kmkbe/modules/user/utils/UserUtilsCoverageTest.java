package com.kmkbe.modules.user.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class UserUtilsCoverageTest {
  @Test
  void stringHelpers() {
    assertThat(Utils.trims("a  b   c")).isNotBlank();
    assertThat(Utils.titleCase("hello world")).isEqualTo("Hello World");
    assertThat(Utils.titleCase("")).isEmpty();
    assertThat(Utils.formatNumber("1,234")).isEqualTo("1234");
    assertThat(Utils.formatCurrency("1234567")).isEqualTo("1,234,567");
    assertThat(Utils.formatCurrency("1234567.89")).isEqualTo("1,234,567.89");
    assertThat(Utils.insertStringRev("123456", ",", 3)).isEqualTo("123,456");
    assertThat(Utils.getNumberOnly("a1b2c3")).isEqualTo("123");
    assertThat(Utils.repeat("ab", 3)).isEqualTo("ababab");
    assertThat(Utils.mid("text", 1, 2)).isEqualTo("text");
    assertThat(Utils.left("text", 1)).isEqualTo("text");
    assertThat(Utils.right("text", 1)).isEqualTo("text");
    assertThat(Utils.rsubstring("text", 1, 2)).isEqualTo("text");
    assertThat(Utils.revstring("text")).isEqualTo("text");
    assertThat(Utils.replace("a-b-c", "-", "+")).isEqualTo("a+b+c");
    assertThat(Utils.splitList("a,b,c", ",")).containsExactly("a", "b", "c");
    assertThat(Utils.lTrim("  abc  ")).isEqualTo("abc  ");
    assertThat(Utils.rTrim("  abc  ")).isEqualTo("  abc");
    assertThat(Utils.valueOf(null)).isEmpty();
    assertThat(Utils.valueOf(123)).isEqualTo("123");
    assertThat(Utils.charFixLength("ab", 5, 1)).isEqualTo("   ab");
    assertThat(Utils.charFixLength("ab", 5, -1)).isEqualTo("ab   ");
    assertThat(Utils.charFixLength("ab", 5, 0)).isEqualTo(" ab  ");
    assertThat(Utils.charFixLength("abcdef", 3, 0)).isEqualTo("abc");
  }

  @Test
  void numericAndDateHelpers() {
    assertThat(Utils.getInt("12")).isEqualTo(12);
    assertThat(Utils.getLong("12")).isEqualTo(12L);
    assertThat(Utils.getDouble(12)).isEqualTo(12.0);
    assertThat(Utils.getFloat("12")).isEqualTo(12f);
    assertThat(Utils.getNumberAsInt("12")).isEqualTo(12);
    assertThat(Utils.getNumberAsLong("12")).isEqualTo(12L);
    assertThat(Utils.getNumberAsDouble("12")).isEqualTo(12.0);
    assertThat(Utils.getNumberAsFloat("12")).isEqualTo(12f);
    assertThat(Utils.getNumber("12.5")).isEqualTo(12.5);
    assertThat(Utils.getNumber("bad")).isEqualTo(0);
    assertThat(Utils.isNumeric("-12.5")).isTrue();
    assertThat(Utils.isNumeric("x")).isFalse();
    assertThat(Utils.isLongIntegerNumber("-12")).isTrue();
    assertThat(Utils.isLongIntegerNumber("12.5")).isFalse();
    assertThat(Utils.isEmptyOrNull(null)).isTrue();
    assertThat(Utils.isEmptyOrNull("null")).isTrue();
    assertThat(Utils.isEmptyOrNull("x")).isFalse();
    assertThat(Utils.dateAdd(new Date(0), java.util.Calendar.DATE, 1)).isAfter(new Date(0));
    assertThat(Utils.NowTz()).isNotBlank();
    assertThat(Utils.Now()).isNotBlank();
    assertThat(Utils.NowTime()).isPositive();
    assertThat(Utils.NowDate()).isNotNull();
    assertThat(Utils.getTimeZone()).isNotBlank();
  }

  @Test
  void hashAndCollectionHelpers() {
    assertThat(Utils.MD5("abc")).hasSize(32);
    assertThat(Utils.MD5("")).isEmpty();
    assertThat(Utils.hashNikita("abc")).hasSize(32);
    assertThat(Utils.hashNikita("key", "a", "b")).hasSize(32);
    assertThat(Utils.SHA1("abc")).hasSize(40);
    assertThat(Utils.SHA1("")).isEmpty();
    assertThat(Utils.SHA256("abc")).hasSize(64);
    assertThat(Utils.SHA256("")).isEmpty();
    assertThat(Utils.RND()).isNotBlank();
    assertThat(Utils.UUID()).isNotBlank();
    assertThat(Utils.getKeys(Map.of("a", 1))).containsExactly("a");
    assertThat(Utils.getKeysAsString(Map.of("a", 1))).containsExactly("a");
    assertThat(new Utils().classExists("java.lang.String")).isTrue();
    assertThat(new Utils().classExists("no.such.Class")).isFalse();
    assertThat(Utils.base64Decode(Utils.base64Encode("abc"))).isEqualTo("abc");
    assertThat(Utils.base64Decode("%%%")).isEmpty();
    assertThat(Utils.toBase36("123")).isEqualTo("3F");
    assertThat(Utils.toBase36("bad")).isNull();
    assertThat(Utils.fromBase36("3F")).isEqualTo("123");
    assertThat(Utils.fromBase36("!")).isNull();
    assertThat(Utils.listArrays(List.of("a", "b"))).containsExactly("a", "b");
    assertThat(Utils.listFromArrays(new String[]{"a", "b"})).containsExactly("a", "b");
    assertThat(Utils.proc("a", "b")).isEqualTo("a");
    List<String> seen = new ArrayList<>();
    Utils.each(List.of("a", "b"), o -> seen.add((String) o));
    assertThat(seen).containsExactly("a", "b");
    List<String> deleted = new ArrayList<>();
    Utils.forEach(List.of("a"), (value, list) -> list.add(value), deleted::add);
    Utils.forEach(List.of("b"), value -> value, deleted::add);
    assertThat(deleted).containsExactly("a");
  }

  @Test
  void streamHelpers(@TempDir java.nio.file.Path tempDir) throws Exception {
    File file = tempDir.resolve("input.txt").toFile();
    Files.writeString(file.toPath(), "content");
    assertThat(Utils.readInputStream(file.getAbsolutePath())).isNotNull();
    assertThat(Utils.readInputStream("missing-file")).isNull();
    assertThat(Utils.readInputStreamAsString(file.getAbsolutePath())).isEqualTo("content");
    assertThat(Utils.readInputStreamAsString("missing-file")).isEmpty();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Utils.copyStream(new ByteArrayInputStream("copy".getBytes()), out);
    assertThat(out.toString()).isEqualTo("copy");
    InputStream broken = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("boom");
      }
    };
    Utils.copyStream(broken, new ByteArrayOutputStream());

    assertThat((Object) ReflectionTestUtils.invokeMethod(Utils.class, "escapeStringForMySQL",
      "a\\b\nc\rd\te\u001a\u0000'\"%_")).isNotNull();
    assertThat((String) ReflectionTestUtils.invokeMethod(Utils.class, "escapeWildcardsForMySQL",
      "a\\b\nc\rd\te\u001a\u0000'\"%_")).contains("\\%");
    assertThat((Object) ReflectionTestUtils.invokeMethod(Utils.class, "humanReadableByteCount", 1500L, true))
      .isEqualTo("1.5 KB");
    assertThat((Object) ReflectionTestUtils.invokeMethod(Utils.class, "humanReadableByteCount", 1500L, false))
      .isNotNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(Utils.class, "humanReadableByteCount", 10L, true))
      .isEqualTo("10 B");
    assertThat((Object) ReflectionTestUtils.invokeMethod(Utils.class, "isDecimalNumber", "12.3")).isEqualTo(true);
  }

  @Test
  void substringErrorBranches() {
    assertThat(Utils.substring("abc", 0, 0)).isEqualTo("abc");
    assertThat(Utils.substring("abc", 1, 2)).isEqualTo("b");
    assertThat(Utils.substring("abc", 5, 9)).isEmpty();
    assertThat(Utils.substring(null, 0, 0)).isEmpty();
  }
}

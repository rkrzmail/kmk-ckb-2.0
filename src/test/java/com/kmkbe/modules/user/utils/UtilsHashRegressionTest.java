package com.kmkbe.modules.user.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresi test untuk security issue #12 fase 1 (SECURITY.md):
 * method hash legacy (MD5/SHA1) ditandai @Deprecated dan silent-catch
 * diperbaiki. Test ini memastikan hasil digest TIDAK berubah sehingga
 * tidak ada efek samping bagi kode legacy yang masih menyisakan pemanggil.
 * Vektor dari RFC 1321 (MD5) dan NIST FIPS 180 (SHA-1/SHA-256), input "abc".
 */
class UtilsHashRegressionTest {

  private static final String INPUT = "abc";

  @Test
  void md5MatchesKnownVector() {
    assertThat(Utils.MD5(INPUT)).isEqualTo("900150983cd24fb0d6963f7d28e17f72");
  }

  @Test
  void sha1MatchesKnownVector() {
    assertThat(Utils.SHA1(INPUT)).isEqualTo("a9993e364706816aba3e25717850c26c9cd0d89d");
  }

  @Test
  void sha256MatchesKnownVector() {
    assertThat(Utils.SHA256(INPUT)).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
  }

  @Test
  void md5AndSha1ReturnEmptyStringForEmptyInput() {
    // perilaku legacy yang dipertahankan: input kosong -> string kosong
    assertThat(Utils.MD5("")).isEmpty();
    assertThat(Utils.SHA1("")).isEmpty();
    assertThat(Utils.SHA256("")).isEmpty();
  }

  @Test
  void hashNikitaWrapsKeyAroundPayload() {
    // hashNikita(key, parts...) = MD5(key + parts + key)
    String expected = Utils.MD5("K" + "a" + "b" + "K");

    assertThat(Utils.hashNikita("K", "a", "b")).isEqualTo(expected);
    assertThat(Utils.hashNikita("KabK")).isEqualTo(expected);
  }
}

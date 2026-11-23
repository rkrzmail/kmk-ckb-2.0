package com.kmkbe.helpers.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorConstantTest {

  @Test
  void errorConstants() {
    assertThat(ErrorConstant.CREATOR).isEqualTo("SYSTEM");
    assertThat(ErrorConstant.ERROR_CODE_404).isEqualTo(404);
    assertThat(ErrorConstant.ERROR_CODE_409).isEqualTo(409);
    assertThat(ErrorConstant.ERROR_CODE_500).isEqualTo(500);
    assertThat(ErrorConstant.ERROR_CODE_80).isEqualTo(80);
    assertThat(ErrorConstant.ERROR_CODE_81).isEqualTo(81);
    assertThat(ErrorConstant.ERROR_CODE_82).isEqualTo(82);
    assertThat(ErrorConstant.ERROR_CODE_83).isEqualTo(83);
    assertThat(ErrorConstant.ERROR_CODE_84).isEqualTo(84);

    assertThat(ErrorConstant.ERROR_MESSAGE_404).contains("Tidak ditemukan");
    assertThat(ErrorConstant.ERROR_MESSAGE_409).contains("Terjadi kesalahan");
    assertThat(ErrorConstant.ERROR_MESSAGE_500).contains("Terjadi error");
    assertThat(ErrorConstant.ERROR_MESSAGE_80).contains("Error prosess bisnis");
    assertThat(ErrorConstant.ERROR_MESSAGE_81).contains("Data tidak ditemukan");
    assertThat(ErrorConstant.ERROR_MESSAGE_82).contains("Captcha tidak valid");
    assertThat(ErrorConstant.ERROR_MESSAGE_83).contains("Invalid API-KEY");
    assertThat(ErrorConstant.ERROR_MESSAGE_84).contains("Data sudah ada");
  }
}

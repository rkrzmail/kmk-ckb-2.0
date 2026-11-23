package com.kmkbe.helpers.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusConstantsTest {

  @Test
  void financingConstants() {
    assertThat(StatusConstants.FINANCING_STEP_NEW).isEqualTo("NEW");
    assertThat(StatusConstants.FINANCING_STEP_ASSIGNMENT).isEqualTo("ASSIGNMENT");
    assertThat(StatusConstants.FINANCING_STEP_INPROCESS).isEqualTo("INPROCESS");
    assertThat(StatusConstants.FINANCING_STEP_SIGNING).isEqualTo("SIGNING");
    assertThat(StatusConstants.FINANCING_STEP_SIGNED).isEqualTo("SIGNED");
    assertThat(StatusConstants.FINANCING_STEP_GOLIVE).isEqualTo("GOLIVE");
    assertThat(StatusConstants.FINANCING_STEP_PAID).isEqualTo("PAID");
    assertThat(StatusConstants.FINANCING_FINAL_STEPS).containsExactly("GOLIVE", "PAID");

    assertThat(StatusConstants.FINANCING_STATUS_NEW).isEqualTo("NEW");
    assertThat(StatusConstants.FINANCING_STATUS_INPROCESS).isEqualTo("INPROCESS");
    assertThat(StatusConstants.FINANCING_STATUS_LIVE).isEqualTo("LIVE");
    assertThat(StatusConstants.FINANCING_STATUS_PAID).isEqualTo("PAID");
    assertThat(StatusConstants.FINANCING_FINAL_STATUS).containsExactly("LIVE", "PAID");
  }
}

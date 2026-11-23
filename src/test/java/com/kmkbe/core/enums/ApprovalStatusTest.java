package com.kmkbe.core.enums;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class ApprovalStatusTest {
  @Test void safeValueOfValid() {
    assertThat(ApprovalStatus.OPEN.safeValueOf("approved")).isEqualTo(ApprovalStatus.APPROVED);
    assertThat(ApprovalStatus.OPEN.safeValueOf(" REJECTED ")).isEqualTo(ApprovalStatus.REJECTED);
  }
  @Test void safeValueOfNullOrEmpty() {
    assertThat(ApprovalStatus.OPEN.safeValueOf(null)).isNull();
    assertThat(ApprovalStatus.OPEN.safeValueOf("  ")).isNull();
  }
  @Test void safeValueOfUnknown() {
    assertThat(ApprovalStatus.OPEN.safeValueOf("NOPE")).isNull();
  }
}

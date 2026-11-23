package com.kmkbe.core.constants;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CommonConstantsTest {
  @Test void monthInMinutes() { 
    assertThat(new CommonConstants()).isNotNull();
    assertThat(CommonConstants.MONTH_IN_MINUTES).isEqualTo(43800); 
  }
}

package com.kmkbe.core.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class CoreExceptionsTest {
  @Test void apiBusinessException() {
    var ex = new ApiBusinessException(400, "msg", Map.of("k","v"), Map.of("r","1"));
    assertThat(ex.getCode()).isEqualTo(400);
    assertThat(ex.getMessage()).isEqualTo("msg");
  }
  @Test void debtorCreationException() {
    var ex = new DebtorCreationException("msg", Map.of("k","v"));
    assertThat(ex.getMessage()).isEqualTo("msg");
  }
  @Test void illegalApiKeyException() {
    assertThat(new IllegalApiKeyException("x").getMessage()).isEqualTo("x");
    assertThat(new IllegalApiKeyException().getMessage()).contains("Credentials");
  }
  @Test void loanDocMandatoryException() {
    assertThat(new LoanDocMandatoryException("doc").getMessage()).isEqualTo("doc");
  }

  @Test void commonInvalidExceptionFactoriesAndPayload() {
    for (CommonInvalidException ex : new CommonInvalidException[]{
      CommonInvalidException.invalidAttack(),
      CommonInvalidException.invalidEmailOrPin(),
      CommonInvalidException.invalidEmail(),
      CommonInvalidException.notRegistered(),
      CommonInvalidException.alreadyRegistered(),
      CommonInvalidException.invalidPin(),
      CommonInvalidException.invalidRole(),
      CommonInvalidException.invalidPassword(),
      CommonInvalidException.notActive(),
      CommonInvalidException.invalidInternalUser(),
      CommonInvalidException.invalidInternalUserEmployee(),
      CommonInvalidException.cannotAccessResource()
    }) {
      assertThat(ex.getTitle()).isNotBlank();
      assertThat(ex.getMessage()).isNotBlank();
      assertThat(ex.getPayload()).isNotNull();
      assertThat(ex.getPayload()).containsKey("title");
      assertThat(ex.getPayload()).containsKey("message");
    }
    // branch: headerMessage null/empty and action null
    CommonInvalidException noAction = CommonInvalidException.cannotAccessResource();
    assertThat(noAction.getAction()).isNull();
    assertThat(noAction.getHeaderMessage()).isNull();
    assertThat(noAction.getPayload()).doesNotContainKey("action");
  }
}

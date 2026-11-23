package com.kmkbe.feign.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VendorNotFoundExceptionTest {

  @Test
  void constructor_setsMessageProperly() {
    VendorNotFoundException ex = new VendorNotFoundException("Vendor ABC not found");

    assertThat(ex.getMessage()).isEqualTo("Vendor ABC not found");
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}

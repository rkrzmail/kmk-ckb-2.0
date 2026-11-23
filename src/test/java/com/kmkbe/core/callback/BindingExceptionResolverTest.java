package com.kmkbe.core.callback;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class BindingExceptionResolverTest {
  @Test void canInstantiate() { assertThat(new BindingExceptionResolver()).isNotNull(); }
}

package com.kmkbe.core.callback;

import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class FlywayCallbackTest {
  private final FlywayCallback cb = new FlywayCallback();
  @Test void supportsAndCanHandle() {
    Context ctx = mock(Context.class);
    assertThat(cb.supports(Event.BEFORE_EACH_MIGRATE, ctx)).isTrue();
    assertThat(cb.canHandleInTransaction(Event.AFTER_EACH_MIGRATE, ctx)).isTrue();
  }
  @Test void handleAllBranches() {
    Context ctx = mock(Context.class);
    assertThatCode(() -> cb.handle(Event.BEFORE_EACH_MIGRATE, ctx)).doesNotThrowAnyException();
    assertThatCode(() -> cb.handle(Event.AFTER_EACH_MIGRATE, ctx)).doesNotThrowAnyException();
    assertThatCode(() -> cb.handle(Event.BEFORE_MIGRATE, ctx)).doesNotThrowAnyException();
  }
  @Test void callbackName() { assertThat(cb.getCallbackName()).isEqualTo("FlywayCallback"); }
}

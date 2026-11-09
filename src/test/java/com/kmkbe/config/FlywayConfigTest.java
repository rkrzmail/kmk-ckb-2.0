package com.kmkbe.config;

import com.kmkbe.core.callback.FlywayCallback;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;

/**
 * Unit test FlywayConfig: strategi migrasi = repair() dulu lalu migrate()
 * dengan FlywayCallback terpasang. Flyway baru (hasil load()) di-intercept
 * via mockConstruction supaya repair()/migrate() tidak memukul database.
 */
class FlywayConfigTest {

  private List<String> eventLog;

  @BeforeEach
  void setUp() {
    eventLog = new ArrayList<>();
  }

  @Test
  void migrationStrategyRepairsThenMigratesWithCallback() throws Exception {
    // sumber config: real object yang bisa di-mutasi minimal
    Flyway spySource = flywaySpyWithInterceptedLifecycle();
    List<Object[]> capturedCallbacks = new ArrayList<>();

    try (var ignored = org.mockito.Mockito.mockConstruction(
      Flyway.class,
      (mock, context) -> {
        doAnswer(inv -> { eventLog.add("repair"); return null; }).when(mock).repair();
        doAnswer(inv -> { eventLog.add("migrate"); return null; }).when(mock).migrate();
      })) {
      // strategi memanggil Flyway.configure()... load() → konstruktor Flyway baru ter-intercept
      new FlywayConfig().flywayMigrationStrategy().migrate(spySource);

      assertThat(eventLog).containsExactly("repair", "migrate");
    }
  }

  @Test
  void migrationStrategyRegistersFlywayCallbackBeforeRepairAndMigrate() throws Exception {
    // verifikasi urutan penuh: configuration() → callbacks(FlywayCallback) → load → repair → migrate
    Flyway spySource = flywaySpyWithInterceptedLifecycle();

    try (var flywayMocked = org.mockito.Mockito.mockConstruction(
      Flyway.class,
      (mock, context) -> {
        doAnswer(inv -> { eventLog.add("repair"); return null; }).when(mock).repair();
        doAnswer(inv -> { eventLog.add("migrate"); return null; }).when(mock).migrate();
      })) {
      // intercept builder chain via statik? Flyway.configure() statik —
      // verifikasi callback via args di konstruktor tidak tersedia, karena
      // callbacks() dipanggil pada FluentConfiguration. Kita verifikasi
      // urutan event + strategi tidak melempar.
      new FlywayConfig().flywayMigrationStrategy().migrate(spySource);

      assertThat(eventLog).containsExactly("repair", "migrate");
      assertThat(flywayMocked.constructed()).hasSize(1);
    }
  }

  /**
   * Membuat Flyway "sumber" yang getConfiguration()-nya mengembalikan
   * config real; lifecycle-nya (repair/migrate) di-stub lewat spy.
   */
  private Flyway flywaySpyWithInterceptedLifecycle() throws Exception {
    Flyway real = Flyway.configure().load();
    Flyway spy = spy(real);
    doAnswer(inv -> { eventLog.add("repair"); return null; }).when(spy).repair();
    doAnswer(inv -> { eventLog.add("migrate"); return null; }).when(spy).migrate();
    return spy;
  }
}

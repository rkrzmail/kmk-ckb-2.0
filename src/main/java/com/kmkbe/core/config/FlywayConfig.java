package com.kmkbe.core.config;

import com.kmkbe.core.callback.FlywayCallback;
import jakarta.activation.DataSource;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (flywayOld) -> {
            Flyway flyway = Flyway.configure()
                    .configuration(flywayOld.getConfiguration())
                    .callbacks(new FlywayCallback())
                    .load();

            flyway.repair();
            flyway.migrate();
        };
    }
}

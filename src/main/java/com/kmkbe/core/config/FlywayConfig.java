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

    @Value("${spring.datasource.url}")
    private String dataSource;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /*@PostConstruct
    public void migrate() {
        Flyway flyway = flywayMigrate();
        flyway.migrate();
    }*/

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (flywayOld) -> {
            Flyway flyway = Flyway.configure()
                    .configuration(flywayOld.getConfiguration())
                    .callbacks(new FlywayCallback())
                    .load();

            flyway.migrate();
        };
    }

   /* public Flyway flywayMigrate() {
        return Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource, username, password)
                //.locations("db/migration", "db/callbacks")
                .locations("classpath:db/migration/prod/{vendor}")
                .callbacks(new FlywayCallback())
                .load();
    }*/
}

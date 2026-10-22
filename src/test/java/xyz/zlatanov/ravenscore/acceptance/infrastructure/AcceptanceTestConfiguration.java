package xyz.zlatanov.ravenscore.acceptance.infrastructure;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@TestConfiguration(proxyBeanMethods = false)
public class AcceptanceTestConfiguration {

    // The production V1 migration uses PostgreSQL-only array syntax, so H2 gets an equivalent V1 schema first.
    // Flyway then baselines the non-empty schema at version 1 (baseline-on-migrate) and applies V2+ unchanged.
    @Bean
    FlywayMigrationStrategy createH2InitialSchemaThenMigrate() {
        return flyway -> {
            new ResourceDatabasePopulator(new ClassPathResource("db/h2/V1__initial_DDL_h2.sql"))
                    .execute(flyway.getConfiguration().getDataSource());
            flyway.migrate();
        };
    }
}

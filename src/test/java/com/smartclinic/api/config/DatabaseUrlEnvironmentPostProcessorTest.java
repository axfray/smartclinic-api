package com.smartclinic.api.config;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void convertsPostgresqlUrlToJdbcWithoutCredentials() {
        Optional<String> result = DatabaseUrlEnvironmentPostProcessor.toJdbcUrl(
                "postgresql://user:pass@localhost:5432/smartclinic_db");

        assertEquals(Optional.of("jdbc:postgresql://localhost:5432/smartclinic_db"), result);
    }

    @Test
    void acceptsPostgresSchemeAndAppliesDefaultPort() {
        Optional<String> result = DatabaseUrlEnvironmentPostProcessor.toJdbcUrl(
                "postgres://user:pass@db.internal/smartclinic_db");

        assertEquals(Optional.of("jdbc:postgresql://db.internal:5432/smartclinic_db"), result);
    }

    @Test
    void preservesQueryString() {
        Optional<String> result = DatabaseUrlEnvironmentPostProcessor.toJdbcUrl(
                "postgresql://u:p@host:5432/db?sslmode=require");

        assertEquals(Optional.of("jdbc:postgresql://host:5432/db?sslmode=require"), result);
    }

    @Test
    void returnsEmptyForMissingOrInvalidValues() {
        assertTrue(DatabaseUrlEnvironmentPostProcessor.toJdbcUrl(null).isEmpty());
        assertTrue(DatabaseUrlEnvironmentPostProcessor.toJdbcUrl("").isEmpty());
        assertTrue(DatabaseUrlEnvironmentPostProcessor.toJdbcUrl("jdbc:postgresql://localhost/db").isEmpty());
        assertTrue(DatabaseUrlEnvironmentPostProcessor.toJdbcUrl("postgresql://host").isEmpty());
    }
}

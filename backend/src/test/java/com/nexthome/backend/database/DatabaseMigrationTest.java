package com.nexthome.backend.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class DatabaseMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"));

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
    }

    @Test
    void createsCoreTablesAndPostgisExtension() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            assertThat(extensionExists(statement, "postgis")).isTrue();
            assertThat(tableExists(statement, "region")).isTrue();
            assertThat(tableExists(statement, "apartment")).isTrue();
            assertThat(tableExists(statement, "trade")).isTrue();
            assertThat(tableExists(statement, "region_grade")).isTrue();
            assertThat(tableExists(statement, "alert_condition")).isTrue();
            assertThat(tableExists(statement, "push_subscription")).isTrue();
        }
    }

    @Test
    void storesRegionBoundariesAndApartmentLocationsAsSpatialColumns() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            assertThat(columnType(statement, "region", "boundary")).isEqualTo("geometry");
            assertThat(columnType(statement, "apartment", "location")).isEqualTo("geography");
        }
    }

    private boolean extensionExists(Statement statement, String extension) throws Exception {
        try (ResultSet result = statement.executeQuery(
                "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = '" + extension + "')")) {
            result.next();
            return result.getBoolean(1);
        }
    }

    private boolean tableExists(Statement statement, String table) throws Exception {
        try (ResultSet result = statement.executeQuery(
                "SELECT to_regclass('public." + table + "') IS NOT NULL")) {
            result.next();
            return result.getBoolean(1);
        }
    }

    private String columnType(Statement statement, String table, String column) throws Exception {
        try (ResultSet result = statement.executeQuery("""
                SELECT udt_name
                FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = '%s' AND column_name = '%s'
                """.formatted(table, column))) {
            result.next();
            return result.getString(1);
        }
    }
}

package com.nexthome.backend.region;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class VworldBoundaryImportServiceTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"));

    @Test
    void matchesSigunguCodesAndStores4326MultiPolygons() throws Exception {
        try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            statement.execute("CREATE TABLE region(id bigint primary key, code text, name text, boundary geometry(MultiPolygon,4326), updated_at timestamptz default now())");
            statement.execute("INSERT INTO region VALUES (1,'11680','강남구',NULL)");
        }
        String geoJson = """
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","properties":{"sig_cd":"11680"},
                   "geometry":{"type":"Polygon","coordinates":[[[127,37],[128,37],[128,38],[127,38],[127,37]]] }},
                  {"type":"Feature","properties":{"sig_cd":"99999"},
                   "geometry":{"type":"Polygon","coordinates":[[[126,36],[127,36],[127,37],[126,37],[126,36]]] }}
                ]}
                """;
        var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        int updated = new VworldBoundaryImportService(new JdbcTemplate(dataSource)).importGeoJson(geoJson);

        assertThat(updated).isEqualTo(1);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT ST_GeometryType(boundary), ST_SRID(boundary) FROM region WHERE code='11680'")) {
            result.next();
            assertThat(result.getString(1)).isEqualTo("ST_MultiPolygon");
            assertThat(result.getInt(2)).isEqualTo(4326);
        }
    }
}

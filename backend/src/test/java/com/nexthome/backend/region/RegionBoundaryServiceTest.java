package com.nexthome.backend.region;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Testcontainers
class RegionBoundaryServiceTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"));

    @Test
    void returnsEveryBoundaryAndMarksRegionsWithoutGradesAsNoData() throws Exception {
        try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            statement.execute("CREATE TABLE region(id bigint primary key, code text, name text, boundary geometry(MultiPolygon,4326))");
            statement.execute("CREATE TABLE region_grade(region_id bigint, year smallint, grade smallint, average_price_per_pyeong numeric, trade_count int)");
            statement.execute("INSERT INTO region VALUES (1,'11680','강남구',ST_Multi(ST_GeomFromText('POLYGON((127 37,128 37,128 38,127 38,127 37))',4326))), (2,'00000','경계없음',NULL), (3,'28720','옹진군',ST_Multi(ST_GeomFromText('POLYGON((126 37,127 37,127 38,126 38,126 37))',4326)))");
            statement.execute("INSERT INTO region_grade VALUES (1,2026,1,90000000,42), (1,2025,2,80000000,30)");
        }
        var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        String geoJson = new RegionBoundaryService(new JdbcTemplate(dataSource)).findByYear(2026);

        assertThat(geoJson).contains("FeatureCollection", "강남구", "\"grade\": 1", "옹진군", "\"grade\": null", "coordinates");
        assertThat(geoJson).doesNotContain("경계없음", "\"grade\": 2");
    }
}

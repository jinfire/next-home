package com.nexthome.backend.geocoding;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class GeocodingPersistenceTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"));
    static JdbcTemplate jdbc;

    @BeforeAll
    static void setUpDatabase() {
        Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load().migrate();
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
    }

    @Test
    void atomicallyStopsAtTheConfiguredLimit() {
        JdbcApiUsageCounter counter = new JdbcApiUsageCounter(jdbc);
        LocalDate day = LocalDate.of(2026, 7, 20);

        assertThat(counter.incrementWithinLimit("DAY", day, 2)).isTrue();
        assertThat(counter.incrementWithinLimit("DAY", day, 2)).isTrue();
        assertThat(counter.incrementWithinLimit("DAY", day, 2)).isFalse();
    }

    @Test
    void storesAndReusesCoordinatesByNormalizedAddress() {
        JdbcGeocodingCache cache = new JdbcGeocodingCache(jdbc);
        GeocodingResult result = new GeocodingResult(
                "서울 마포구 월드컵로 1", "서울 마포구 월드컵로 1",
                new BigDecimal("126.90000000"), new BigDecimal("37.50000000"));

        cache.save(result);

        assertThat(cache.find(result.normalizedAddress())).contains(result);
        Integer spatialRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM geocoding_cache WHERE ST_SRID(location::geometry) = 4326", Integer.class);
        assertThat(spatialRows).isEqualTo(1);
    }
}

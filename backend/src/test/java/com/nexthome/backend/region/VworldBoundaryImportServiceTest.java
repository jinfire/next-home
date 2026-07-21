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
    void createsCapitalAreaHierarchyAndStores4326MultiPolygons() throws Exception {
        try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            statement.execute("CREATE TABLE region(id bigserial primary key, code text unique, name text, parent_id bigint references region(id), level smallint, boundary geometry(MultiPolygon,4326), created_at timestamptz default now(), updated_at timestamptz default now())");
        }
        String geoJson = """
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","properties":{"sig_cd":"11680","sig_kor_nm":"강남구","full_nm":"서울특별시 강남구"},
                   "geometry":{"type":"Polygon","coordinates":[[[127,37],[128,37],[128,38],[127,38],[127,37]]] }},
                  {"type":"Feature","properties":{"sig_cd":"41135","sig_kor_nm":"분당구","full_nm":"경기도 성남시 분당구"},
                   "geometry":{"type":"Polygon","coordinates":[[[127,36],[128,36],[128,37],[127,37],[127,36]]] }},
                  {"type":"Feature","properties":{"sig_cd":"26110","sig_kor_nm":"중구","full_nm":"부산광역시 중구"},
                   "geometry":{"type":"Polygon","coordinates":[[[126,36],[127,36],[127,37],[126,37],[126,36]]] }}
                ]}
                """;
        var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        int updated = new VworldBoundaryImportService(new JdbcTemplate(dataSource)).importGeoJson(geoJson);

        assertThat(updated).isEqualTo(2);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT child.name, parent.name, child.level, ST_GeometryType(child.boundary), ST_SRID(child.boundary)
                     FROM region child JOIN region parent ON parent.id=child.parent_id
                     WHERE child.code='41135'
                     """)) {
            result.next();
            assertThat(result.getString(1)).isEqualTo("성남시 분당구");
            assertThat(result.getString(2)).isEqualTo("경기도");
            assertThat(result.getInt(3)).isEqualTo(2);
            assertThat(result.getString(4)).isEqualTo("ST_MultiPolygon");
            assertThat(result.getInt(5)).isEqualTo(4326);
        }
        assertThat(new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM region", Integer.class)).isEqualTo(4);
    }
}

package com.nexthome.backend.region;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class VworldBoundaryImportService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VworldBoundaryImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int importGeoJson(String geoJson) {
        try {
            JsonNode features = objectMapper.readTree(geoJson).path("features");
            if (!features.isArray()) throw new IllegalArgumentException("VWorld 응답에 features 배열이 없습니다.");
            int updated = 0;
            for (JsonNode feature : features) {
                String code = feature.path("properties").path("sig_cd").asText();
                JsonNode geometry = feature.path("geometry");
                if (code.isBlank() || geometry.isMissingNode() || geometry.isNull()) continue;
                updated += jdbc.update("""
                        UPDATE region
                        SET boundary = ST_Multi(ST_CollectionExtract(ST_MakeValid(
                            ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)
                        ), 3)), updated_at = CURRENT_TIMESTAMP
                        WHERE code = ?
                        """, geometry.toString(), code);
            }
            return updated;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("VWorld 경계 GeoJSON을 처리할 수 없습니다.", exception);
        }
    }
}

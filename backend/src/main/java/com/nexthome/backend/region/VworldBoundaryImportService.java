package com.nexthome.backend.region;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VworldBoundaryImportService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VworldBoundaryImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public int importGeoJson(String geoJson) {
        try {
            JsonNode features = objectMapper.readTree(geoJson).path("features");
            if (!features.isArray()) throw new IllegalArgumentException("VWorld 응답에 features 배열이 없습니다.");
            int updated = 0;
            for (JsonNode feature : features) {
                String code = feature.path("properties").path("sig_cd").asText();
                String fullName = feature.path("properties").path("full_nm").asText();
                JsonNode geometry = feature.path("geometry");
                CapitalProvince province = CapitalProvince.fromRegionCode(code);
                if (province == null || fullName.isBlank() || geometry.isMissingNode() || geometry.isNull()) continue;
                Long parentId = jdbc.queryForObject("""
                        INSERT INTO region(code, name, level)
                        VALUES (?, ?, 1)
                        ON CONFLICT (code) DO UPDATE SET name=EXCLUDED.name, level=1, updated_at=CURRENT_TIMESTAMP
                        RETURNING id
                        """, Long.class, province.code(), province.koreanName());
                String districtName = fullName.startsWith(province.koreanName())
                        ? fullName.substring(province.koreanName().length()).trim()
                        : feature.path("properties").path("sig_kor_nm").asText();
                updated += jdbc.update("""
                        INSERT INTO region(code, name, parent_id, level, boundary)
                        VALUES (?, ?, ?, 2, ST_Multi(ST_CollectionExtract(ST_MakeValid(
                            ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)
                        ), 3)))
                        ON CONFLICT (code) DO UPDATE SET
                            name=EXCLUDED.name,
                            parent_id=EXCLUDED.parent_id,
                            level=2,
                            boundary=EXCLUDED.boundary,
                            updated_at=CURRENT_TIMESTAMP
                        """, code, districtName, parentId, geometry.toString());
            }
            return updated;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("VWorld 경계 GeoJSON을 처리할 수 없습니다.", exception);
        }
    }

    private enum CapitalProvince {
        SEOUL("11", "서울특별시"),
        INCHEON("28", "인천광역시"),
        GYEONGGI("41", "경기도");

        private final String code;
        private final String name;

        CapitalProvince(String code, String name) {
            this.code = code;
            this.name = name;
        }

        String code() { return code; }
        String koreanName() { return name; }

        static CapitalProvince fromRegionCode(String regionCode) {
            for (CapitalProvince province : values()) {
                if (regionCode.startsWith(province.code)) return province;
            }
            return null;
        }
    }
}

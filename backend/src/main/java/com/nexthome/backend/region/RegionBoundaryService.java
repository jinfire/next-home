package com.nexthome.backend.region;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RegionBoundaryService {
    private final JdbcTemplate jdbc;

    public RegionBoundaryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String findByYear(int year) {
        return jdbc.queryForObject("""
                SELECT jsonb_build_object(
                    'type', 'FeatureCollection',
                    'features', COALESCE(jsonb_agg(jsonb_build_object(
                        'type', 'Feature',
                        'id', r.id,
                        'geometry', ST_AsGeoJSON(r.boundary)::jsonb,
                        'properties', jsonb_build_object(
                            'regionId', r.id,
                            'regionCode', r.code,
                            'regionName', r.name,
                            'grade', rg.grade,
                            'averagePricePerPyeong', rg.average_price_per_pyeong,
                            'tradeCount', rg.trade_count
                        )
                    ) ORDER BY rg.grade, r.name), '[]'::jsonb)
                )::text
                FROM region r
                JOIN region_grade rg ON rg.region_id = r.id AND rg.year = ?
                WHERE r.boundary IS NOT NULL
                """, String.class, year);
    }
}

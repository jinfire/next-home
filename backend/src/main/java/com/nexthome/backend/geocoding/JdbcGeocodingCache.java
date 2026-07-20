package com.nexthome.backend.geocoding;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcGeocodingCache implements GeocodingCache {
    private final JdbcTemplate jdbc;

    JdbcGeocodingCache(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<GeocodingResult> find(String normalizedAddress) {
        return jdbc.query("""
                SELECT normalized_address, road_address, longitude, latitude
                FROM geocoding_cache WHERE normalized_address = ?
                """, (rs, row) -> new GeocodingResult(
                        rs.getString("normalized_address"), rs.getString("road_address"),
                        rs.getBigDecimal("longitude"), rs.getBigDecimal("latitude")), normalizedAddress)
                .stream().findFirst();
    }

    @Override
    public void save(GeocodingResult result) {
        jdbc.update("""
                INSERT INTO geocoding_cache (
                    normalized_address, requested_address, road_address, longitude, latitude, location)
                VALUES (?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography)
                ON CONFLICT (normalized_address) DO UPDATE SET
                    road_address = EXCLUDED.road_address,
                    longitude = EXCLUDED.longitude,
                    latitude = EXCLUDED.latitude,
                    location = EXCLUDED.location,
                    updated_at = CURRENT_TIMESTAMP
                """, result.normalizedAddress(), result.normalizedAddress(), result.roadAddress(),
                result.longitude(), result.latitude(), result.longitude(), result.latitude());
    }
}

package com.nexthome.backend.geocoding;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcApartmentLocationStore implements ApartmentLocationStore {
    private final JdbcTemplate jdbc;

    JdbcApartmentLocationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ApartmentAddress> findWithoutLocation(int limit) {
        return jdbc.query("""
                SELECT a.id, CONCAT_WS(' ', parent.name, region.name, a.address) AS address
                FROM apartment a
                JOIN region ON region.id=a.region_id
                LEFT JOIN region parent ON parent.id=region.parent_id
                WHERE a.location IS NULL OR a.road_address IS NULL
                ORDER BY a.id LIMIT ?
                """, (rs, row) -> new ApartmentAddress(rs.getLong("id"), rs.getString("address")), limit);
    }

    @Override
    public void updateLocation(long apartmentId, String roadAddress, BigDecimal longitude, BigDecimal latitude) {
        jdbc.update("""
                UPDATE apartment
                SET road_address = NULLIF(?, ''),
                    location = ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, roadAddress, longitude, latitude, apartmentId);
    }
}

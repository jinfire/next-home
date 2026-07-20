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
                SELECT id, address FROM apartment
                WHERE location IS NULL ORDER BY id LIMIT ?
                """, (rs, row) -> new ApartmentAddress(rs.getLong("id"), rs.getString("address")), limit);
    }

    @Override
    public void updateLocation(long apartmentId, BigDecimal longitude, BigDecimal latitude) {
        jdbc.update("""
                UPDATE apartment
                SET location = ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, longitude, latitude, apartmentId);
    }
}

package com.nexthome.backend.geocoding;

import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcApiUsageCounter implements ApiUsageCounter {
    private final JdbcTemplate jdbc;

    JdbcApiUsageCounter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean incrementWithinLimit(String periodType, LocalDate periodStart, int limit) {
        return !jdbc.query("""
                INSERT INTO external_api_usage (provider, api_name, period_type, period_start, call_count)
                VALUES ('NAVER', 'GEOCODING', ?, ?, 1)
                ON CONFLICT (provider, api_name, period_type, period_start)
                DO UPDATE SET call_count = external_api_usage.call_count + 1, updated_at = CURRENT_TIMESTAMP
                WHERE external_api_usage.call_count < ?
                RETURNING call_count
                """, (rs, row) -> rs.getInt("call_count"), periodType, periodStart, limit).isEmpty();
    }
}

package com.nexthome.collector.trade;

import java.time.YearMonth;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionCoverageStore {
    private final JdbcTemplate jdbc;

    public CollectionCoverageStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isComplete(String regionCode, YearMonth month) {
        Boolean complete = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM trade_collection_coverage coverage
                    JOIN region ON region.id = coverage.region_id
                    WHERE region.code = ? AND coverage.contract_month = ?::date
                )
                """, Boolean.class, regionCode, month.atDay(1).toString());
        return Boolean.TRUE.equals(complete);
    }

    public void markComplete(String regionCode, YearMonth month) {
        jdbc.update("""
                INSERT INTO trade_collection_coverage(region_id, contract_month, completed_at)
                SELECT id, ?::date, CURRENT_TIMESTAMP FROM region WHERE code = ?
                ON CONFLICT (region_id, contract_month)
                DO UPDATE SET completed_at = EXCLUDED.completed_at
                """, month.atDay(1).toString(), regionCode);
    }
}

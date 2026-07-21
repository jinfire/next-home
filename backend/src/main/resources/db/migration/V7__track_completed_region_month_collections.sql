CREATE TABLE trade_collection_coverage (
    region_id BIGINT NOT NULL REFERENCES region(id),
    contract_month DATE NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (region_id, contract_month),
    CONSTRAINT ck_trade_collection_month_start CHECK (EXTRACT(DAY FROM contract_month) = 1)
);

CREATE INDEX idx_trade_collection_coverage_month
    ON trade_collection_coverage(contract_month);

-- 2026-06 was previously collected successfully for all capital-area districts.
INSERT INTO trade_collection_coverage(region_id, contract_month)
SELECT id, DATE '2026-06-01'
FROM region
WHERE level = 2
ON CONFLICT DO NOTHING;

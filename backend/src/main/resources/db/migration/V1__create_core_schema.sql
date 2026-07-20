CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES region(id),
    level SMALLINT NOT NULL CHECK (level BETWEEN 1 AND 4),
    boundary geometry(MultiPolygon, 4326),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_region_parent ON region(parent_id);
CREATE INDEX idx_region_boundary ON region USING GIST(boundary);

CREATE TABLE apartment (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT NOT NULL REFERENCES region(id),
    external_id VARCHAR(100),
    name VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    location geography(Point, 4326),
    build_year SMALLINT,
    household_count INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_apartment_region_address_name UNIQUE(region_id, address, name)
);

CREATE INDEX idx_apartment_region ON apartment(region_id);
CREATE INDEX idx_apartment_location ON apartment USING GIST(location);

CREATE TABLE trade (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartment(id),
    source_key VARCHAR(200) NOT NULL UNIQUE,
    contract_date DATE NOT NULL,
    price_krw BIGINT NOT NULL CHECK (price_krw > 0),
    exclusive_area_sqm NUMERIC(10, 4) NOT NULL CHECK (exclusive_area_sqm > 0),
    floor SMALLINT,
    contract_type VARCHAR(30),
    buyer_type VARCHAR(30),
    seller_type VARCHAR(30),
    cancellation_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trade_apartment_date ON trade(apartment_id, contract_date DESC);
CREATE INDEX idx_trade_contract_date ON trade(contract_date DESC);

CREATE TABLE region_grade (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT NOT NULL REFERENCES region(id),
    year SMALLINT NOT NULL,
    average_price_per_pyeong NUMERIC(18, 2) NOT NULL CHECK (average_price_per_pyeong >= 0),
    grade SMALLINT NOT NULL CHECK (grade BETWEEN 1 AND 10),
    trade_count INTEGER NOT NULL CHECK (trade_count >= 0),
    calculated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_region_grade_year UNIQUE(region_id, year)
);

CREATE INDEX idx_region_grade_year_grade ON region_grade(year, grade);

CREATE TABLE alert_condition (
    id BIGSERIAL PRIMARY KEY,
    browser_id UUID NOT NULL,
    current_region_id BIGINT NOT NULL REFERENCES region(id),
    target_region_id BIGINT REFERENCES region(id),
    target_grade SMALLINT CHECK (target_grade BETWEEN 1 AND 10),
    target_gap_percent NUMERIC(7, 3),
    historical_gap_percentile NUMERIC(5, 2),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_alert_target CHECK (target_region_id IS NOT NULL OR target_grade IS NOT NULL)
);

CREATE INDEX idx_alert_condition_enabled ON alert_condition(enabled) WHERE enabled = TRUE;
CREATE INDEX idx_alert_condition_browser ON alert_condition(browser_id);

CREATE TABLE push_subscription (
    id BIGSERIAL PRIMARY KEY,
    browser_id UUID NOT NULL,
    endpoint TEXT NOT NULL UNIQUE,
    p256dh_key TEXT NOT NULL,
    auth_key TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_push_subscription_browser ON push_subscription(browser_id);

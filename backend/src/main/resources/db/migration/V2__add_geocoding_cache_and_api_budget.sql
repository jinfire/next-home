CREATE TABLE external_api_usage (
    provider VARCHAR(40) NOT NULL,
    api_name VARCHAR(60) NOT NULL,
    period_type VARCHAR(10) NOT NULL CHECK (period_type IN ('DAY', 'MONTH')),
    period_start DATE NOT NULL,
    call_count INTEGER NOT NULL DEFAULT 0 CHECK (call_count >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (provider, api_name, period_type, period_start)
);

CREATE TABLE geocoding_cache (
    normalized_address VARCHAR(300) PRIMARY KEY,
    requested_address VARCHAR(300) NOT NULL,
    road_address VARCHAR(300),
    longitude NUMERIC(11, 8) NOT NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    location geography(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_geocoding_cache_location ON geocoding_cache USING GIST(location);

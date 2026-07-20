package com.nexthome.backend.geocoding;

import java.util.Optional;

public interface GeocodingCache {
    Optional<GeocodingResult> find(String normalizedAddress);
    void save(GeocodingResult result);
}

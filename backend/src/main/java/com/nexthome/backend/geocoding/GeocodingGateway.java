package com.nexthome.backend.geocoding;

import java.util.Optional;

public interface GeocodingGateway {
    Optional<GeocodingResult> geocode(String normalizedAddress);
}

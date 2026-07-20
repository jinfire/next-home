package com.nexthome.backend.geocoding;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {
    private final GeocodingCache cache;
    private final GeocodingBudget budget;
    private final GeocodingGateway gateway;
    private final AddressNormalizer normalizer;

    GeocodingService(
            GeocodingCache cache,
            GeocodingBudget budget,
            GeocodingGateway gateway,
            AddressNormalizer normalizer) {
        this.cache = cache;
        this.budget = budget;
        this.gateway = gateway;
        this.normalizer = normalizer;
    }

    public Optional<GeocodingResult> geocode(String address) {
        String normalized = normalizer.normalize(address);
        Optional<GeocodingResult> cached = cache.find(normalized);
        if (cached.isPresent()) return cached;
        budget.reserve();
        Optional<GeocodingResult> result = gateway.geocode(normalized);
        result.ifPresent(cache::save);
        return result;
    }
}

package com.nexthome.backend.region;

public record RegionSummary(Long id, String code, String name, int level) {
    static RegionSummary from(Region region) {
        return new RegionSummary(region.id(), region.code(), region.name(), region.level());
    }
}

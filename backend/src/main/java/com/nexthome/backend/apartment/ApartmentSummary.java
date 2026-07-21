package com.nexthome.backend.apartment;

public record ApartmentSummary(Long id, String name, String roadAddress, Long regionId, String regionName, Integer buildYear) {
    static ApartmentSummary from(Apartment apartment) {
        return new ApartmentSummary(apartment.id(), apartment.name(), apartment.roadAddress(),
                apartment.region().id(), apartment.region().name(), apartment.buildYear());
    }
}

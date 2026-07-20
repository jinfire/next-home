package com.nexthome.backend.apartment;

public record ApartmentSummary(Long id, String name, String address, Long regionId, String regionName, Integer buildYear) {
    static ApartmentSummary from(Apartment apartment) {
        return new ApartmentSummary(apartment.id(), apartment.name(), apartment.address(),
                apartment.region().id(), apartment.region().name(), apartment.buildYear());
    }
}

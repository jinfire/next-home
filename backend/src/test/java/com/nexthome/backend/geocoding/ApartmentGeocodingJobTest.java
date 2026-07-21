package com.nexthome.backend.geocoding;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ApartmentGeocodingJobTest {
    @Test
    void geocodesApartmentsWithoutLocationsAndPersistsTheirCoordinates() {
        ApartmentLocationStore store = mock(ApartmentLocationStore.class);
        GeocodingService geocoding = mock(GeocodingService.class);
        ApartmentAddress apartment = new ApartmentAddress(7, "서울 마포구 월드컵로 1");
        GeocodingResult result = new GeocodingResult(apartment.address(), apartment.address(),
                new BigDecimal("126.9"), new BigDecimal("37.5"));
        when(store.findWithoutLocation(50)).thenReturn(List.of(apartment));
        when(geocoding.geocode(apartment.address())).thenReturn(Optional.of(result));

        new ApartmentGeocodingJob(store, geocoding, 50).backfill();

        verify(store).updateLocation(7, result.roadAddress(), result.longitude(), result.latitude());
    }
}

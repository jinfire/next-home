package com.nexthome.backend.geocoding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.naver.geocoding.backfill-enabled", havingValue = "true", matchIfMissing = true)
public class ApartmentGeocodingJob {
    private final ApartmentLocationStore store;
    private final GeocodingService geocoding;
    private final int batchSize;

    ApartmentGeocodingJob(
            ApartmentLocationStore store,
            GeocodingService geocoding,
            @Value("${app.naver.geocoding.batch-size:50}") int batchSize) {
        this.store = store;
        this.geocoding = geocoding;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.naver.geocoding.backfill-interval-ms:3600000}",
            initialDelayString = "${app.naver.geocoding.backfill-initial-delay-ms:120000}")
    public void backfill() {
        for (ApartmentAddress apartment : store.findWithoutLocation(batchSize)) {
            geocoding.geocode(apartment.address()).ifPresent(result ->
                    store.updateLocation(apartment.apartmentId(), result.roadAddress(), result.longitude(), result.latitude()));
        }
    }
}

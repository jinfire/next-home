package com.nexthome.backend.geocoding;

import java.math.BigDecimal;
import java.util.List;

public interface ApartmentLocationStore {
    List<ApartmentAddress> findWithoutLocation(int limit);
    void updateLocation(long apartmentId, BigDecimal longitude, BigDecimal latitude);
}

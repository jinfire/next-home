package com.nexthome.collector.trade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nexthome.collector.apartment.Apartment;
import com.nexthome.collector.apartment.ApartmentRepository;
import com.nexthome.collector.molit.MolitTradeItem;
import com.nexthome.collector.region.Region;
import com.nexthome.collector.region.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeCollectionService {
    private final RegionRepository regions;
    private final ApartmentRepository apartments;
    private final TradeRepository trades;

    public TradeCollectionService(RegionRepository regions, ApartmentRepository apartments, TradeRepository trades) {
        this.regions = regions;
        this.apartments = apartments;
        this.trades = trades;
    }

    @Transactional
    public CollectionResult store(String regionCode, String regionName, List<MolitTradeItem> items) {
        var existingRegion = regions.findByCode(regionCode);
        boolean regionMissing = existingRegion.isEmpty();
        Region region = existingRegion.orElseGet(() -> regions.save(Region.create(regionCode, regionName, 2)));
        int createdApartments = 0, savedTrades = 0, duplicates = 0;
        Map<String, Apartment> pageApartments = new HashMap<>();
        Set<String> pageTradeKeys = new HashSet<>();
        for (MolitTradeItem item : items) {
            String apartmentKey = item.address() + "|" + item.apartmentName();
            Apartment apartment = pageApartments.get(apartmentKey);
            if (apartment == null) {
                var existing = apartments.findByRegionAndAddressAndName(region, item.address(), item.apartmentName());
                apartment = existing.orElseGet(() -> apartments.save(Apartment.create(region, item.apartmentSequence(),
                        item.apartmentName(), item.address(), item.buildYear())));
                if (existing.isEmpty()) createdApartments++;
                pageApartments.put(apartmentKey, apartment);
            }
            String sourceKey = item.sourceKey(regionCode);
            if (!pageTradeKeys.add(sourceKey) || trades.existsBySourceKey(sourceKey)) {
                duplicates++;
                continue;
            }
            trades.save(Trade.from(apartment, sourceKey, item));
            savedTrades++;
        }
        return new CollectionResult(regionMissing ? 1 : 0, createdApartments, savedTrades, duplicates);
    }
}

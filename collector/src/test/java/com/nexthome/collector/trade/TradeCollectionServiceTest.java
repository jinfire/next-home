package com.nexthome.collector.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.nexthome.collector.apartment.Apartment;
import com.nexthome.collector.apartment.ApartmentRepository;
import com.nexthome.collector.molit.MolitTradeItem;
import com.nexthome.collector.region.Region;
import com.nexthome.collector.region.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeCollectionServiceTest {

    @Mock RegionRepository regionRepository;
    @Mock ApartmentRepository apartmentRepository;
    @Mock TradeRepository tradeRepository;
    private TradeCollectionService service;

    @BeforeEach
    void setUp() {
        service = new TradeCollectionService(regionRepository, apartmentRepository, tradeRepository);
    }

    @Test
    void createsRegionApartmentAndTradeOnceForDuplicateItemsInSamePage() {
        MolitTradeItem item = item("테스트 아파트", 1_250_000_000L);
        when(regionRepository.findByCode("11110")).thenReturn(Optional.empty());
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apartmentRepository.findByRegionAndAddressAndName(any(), any(), any())).thenReturn(Optional.empty());
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeRepository.existsBySourceKey(any())).thenReturn(false);

        CollectionResult result = service.store("11110", "종로구", List.of(item, item));

        assertThat(result).isEqualTo(new CollectionResult(1, 1, 1, 1));
        verify(regionRepository).save(any(Region.class));
        verify(apartmentRepository).save(any(Apartment.class));
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void skipsTradeAlreadyStoredInDatabase() {
        Region region = Region.create("11110", "종로구", 2);
        Apartment apartment = Apartment.create(region, "11110-100", "테스트 아파트", "청운동 10", 2015);
        MolitTradeItem item = item("테스트 아파트", 1_250_000_000L);
        when(regionRepository.findByCode("11110")).thenReturn(Optional.of(region));
        when(apartmentRepository.findByRegionAndAddressAndName(region, "청운동 10", "테스트 아파트"))
                .thenReturn(Optional.of(apartment));
        when(tradeRepository.existsBySourceKey(any())).thenReturn(true);

        CollectionResult result = service.store("11110", "종로구", List.of(item));

        assertThat(result.savedTrades()).isZero();
        assertThat(result.duplicates()).isEqualTo(1);
        verify(tradeRepository, never()).save(any());
    }

    private MolitTradeItem item(String name, long price) {
        return new MolitTradeItem("11110-100", name, "청운동", "10", price,
                new BigDecimal("84.91"), LocalDate.of(2026, 1, 5), 12, 2015, null);
    }
}

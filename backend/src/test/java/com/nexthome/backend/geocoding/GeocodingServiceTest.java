package com.nexthome.backend.geocoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GeocodingServiceTest {
    private final GeocodingCache cache = mock(GeocodingCache.class);
    private final GeocodingBudget budget = mock(GeocodingBudget.class);
    private final GeocodingGateway gateway = mock(GeocodingGateway.class);
    private final GeocodingService service = new GeocodingService(cache, budget, gateway, new AddressNormalizer());

    @Test
    void reusesNormalizedAddressWithoutSpendingTheApiBudget() {
        GeocodingResult cached = result("서울 마포구 월드컵로 1");
        when(cache.find("서울 마포구 월드컵로 1")).thenReturn(Optional.of(cached));

        assertThat(service.geocode("  서울  마포구   월드컵로 1 ")).contains(cached);

        verify(budget, never()).reserve();
        verify(gateway, never()).geocode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void reservesBudgetBeforeCallingNaverAndCachesTheResult() {
        GeocodingResult result = result("서울 마포구 월드컵로 1");
        when(cache.find("서울 마포구 월드컵로 1")).thenReturn(Optional.empty());
        when(gateway.geocode("서울 마포구 월드컵로 1")).thenReturn(Optional.of(result));

        assertThat(service.geocode("서울 마포구 월드컵로 1")).contains(result);

        verify(budget).reserve();
        verify(cache).save(result);
    }

    @Test
    void neverCallsNaverAfterTheBudgetIsExhausted() {
        when(cache.find("서울 마포구 월드컵로 1")).thenReturn(Optional.empty());
        org.mockito.Mockito.doThrow(new ApiBudgetExceededException("limit")).when(budget).reserve();

        assertThatThrownBy(() -> service.geocode("서울 마포구 월드컵로 1"))
                .isInstanceOf(ApiBudgetExceededException.class);
        verify(gateway, never()).geocode(org.mockito.ArgumentMatchers.anyString());
    }

    private GeocodingResult result(String address) {
        return new GeocodingResult(address, address, new BigDecimal("126.90000000"), new BigDecimal("37.50000000"));
    }
}

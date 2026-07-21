package com.nexthome.backend.apartment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApartmentSearchServiceTest {

    @Test
    void searchesApartmentNamesAndAddresses() {
        ApartmentRepository repository = mock(ApartmentRepository.class);
        when(repository.searchByNameOrAddress("마포대로 201")).thenReturn(List.of());

        List<ApartmentSummary> result = new ApartmentSearchService(repository).search("마포대로 201", null);

        assertThat(result).isEmpty();
        verify(repository).searchByNameOrAddress("마포대로 201");
    }

    @Test
    void removesApartmentNoiseWordForOrderIndependentPartialSearch() {
        ApartmentRepository repository = mock(ApartmentRepository.class);
        when(repository.searchByNameOrAddress("동탄 포레나")).thenReturn(List.of());

        new ApartmentSearchService(repository).search("동탄 포레나 아파트", null);

        verify(repository).searchByNameOrAddress("동탄 포레나");
    }
}

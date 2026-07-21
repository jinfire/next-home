package com.nexthome.backend.apartment;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApartmentSearchService {
    private final ApartmentRepository repository;
    ApartmentSearchService(ApartmentRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public List<ApartmentSummary> search(String query, Long regionId) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("검색어가 필요합니다.");
        List<Apartment> result = regionId == null
                ? repository.searchByNameOrAddress(normalized)
                : repository.searchByRegionAndNameOrAddress(regionId, normalized);
        return result.stream().map(ApartmentSummary::from).toList();
    }
}

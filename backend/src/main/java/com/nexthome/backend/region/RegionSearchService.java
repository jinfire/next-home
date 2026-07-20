package com.nexthome.backend.region;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionSearchService {
    private final RegionRepository repository;

    RegionSearchService(RegionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegionSummary> search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("검색어가 필요합니다.");
        return repository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(normalized)
                .stream().map(RegionSummary::from).toList();
    }
}

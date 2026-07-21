package com.nexthome.backend.region;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
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

    @Transactional(readOnly = true)
    public List<RegionOptionGroup> options() {
        Map<Long, RegionOptionGroupBuilder> grouped = new LinkedHashMap<>();
        for (Region region : repository.findAllDistrictsWithProvince()) {
            Region province = region.parent();
            grouped.computeIfAbsent(province.id(), ignored ->
                    new RegionOptionGroupBuilder(province.id(), province.code(), province.name()))
                    .regions.add(new RegionOption(region.id(), region.code(), region.name()));
        }
        return grouped.values().stream()
                .sorted(Comparator.comparingInt(group -> provinceOrder(group.code)))
                .map(RegionOptionGroupBuilder::build)
                .toList();
    }

    private int provinceOrder(String code) {
        return switch (code) {
            case "11" -> 1;
            case "41" -> 2;
            case "28" -> 3;
            default -> 99;
        };
    }

    private static final class RegionOptionGroupBuilder {
        private final Long id;
        private final String code;
        private final String name;
        private final List<RegionOption> regions = new java.util.ArrayList<>();

        private RegionOptionGroupBuilder(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        private RegionOptionGroup build() {
            return new RegionOptionGroup(id, code, name, List.copyOf(regions));
        }
    }
}

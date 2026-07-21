package com.nexthome.backend.region;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    @Query("SELECT region FROM Region region JOIN FETCH region.parent WHERE region.level=2 ORDER BY region.parent.code, region.name")
    List<Region> findAllDistrictsWithProvince();
}

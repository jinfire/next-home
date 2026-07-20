package com.nexthome.backend.region;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
}

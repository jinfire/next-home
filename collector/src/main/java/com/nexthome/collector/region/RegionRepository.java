package com.nexthome.collector.region;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCode(String code);
    List<Region> findByLevelOrderByCodeAsc(short level);
}

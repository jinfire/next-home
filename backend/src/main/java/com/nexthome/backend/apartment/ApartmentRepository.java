package com.nexthome.backend.apartment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    List<Apartment> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
    List<Apartment> findTop20ByRegionIdAndNameContainingIgnoreCaseOrderByNameAsc(Long regionId, String name);
}

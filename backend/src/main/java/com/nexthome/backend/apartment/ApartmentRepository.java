package com.nexthome.backend.apartment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    List<Apartment> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
    List<Apartment> findTop20ByRegionIdAndNameContainingIgnoreCaseOrderByNameAsc(Long regionId, String name);

    @Query(value = """
            SELECT * FROM apartment
            WHERE name ILIKE CONCAT('%', :query, '%') OR address ILIKE CONCAT('%', :query, '%')
            ORDER BY name LIMIT 20
            """, nativeQuery = true)
    List<Apartment> searchByNameOrAddress(@Param("query") String query);

    @Query(value = """
            SELECT * FROM apartment
            WHERE region_id=:regionId
              AND (name ILIKE CONCAT('%', :query, '%') OR address ILIKE CONCAT('%', :query, '%'))
            ORDER BY name LIMIT 20
            """, nativeQuery = true)
    List<Apartment> searchByRegionAndNameOrAddress(@Param("regionId") Long regionId, @Param("query") String query);
}

package com.nexthome.collector.apartment;

import java.util.Optional;
import com.nexthome.collector.region.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    Optional<Apartment> findByRegionAndAddressAndName(Region region, String address, String name);
}

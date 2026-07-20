package com.nexthome.backend.trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findTop100ByApartmentIdOrderByContractDateDescIdDesc(Long apartmentId);
}

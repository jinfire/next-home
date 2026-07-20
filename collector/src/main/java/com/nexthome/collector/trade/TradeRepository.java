package com.nexthome.collector.trade;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    boolean existsBySourceKey(String sourceKey);
}

package com.nexthome.backend.trade;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TradeSummary(Long id, LocalDate contractDate, long priceKrw, BigDecimal exclusiveAreaSqm, Integer floor, boolean cancelled) {
    static TradeSummary from(Trade trade) { return new TradeSummary(trade.id(), trade.contractDate(), trade.priceKrw(), trade.exclusiveAreaSqm(), trade.floor(), trade.cancelled()); }
}

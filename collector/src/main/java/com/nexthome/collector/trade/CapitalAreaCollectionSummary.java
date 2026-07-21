package com.nexthome.collector.trade;

public record CapitalAreaCollectionSummary(
        int regions,
        int months,
        int pages,
        int savedTrades,
        int duplicates) {
}

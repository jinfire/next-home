package com.nexthome.collector.trade;

public record CollectionSummary(
        int pages, int createdRegions, int createdApartments, int savedTrades, int duplicates) {
}

package com.nexthome.collector.trade;

import java.time.YearMonth;

import com.nexthome.collector.molit.MolitTradeClient;
import com.nexthome.collector.molit.MolitTradePage;
import org.springframework.stereotype.Service;

@Service
public class TradeCollectionJob {
    private final MolitTradeClient client;
    private final TradeCollectionService storage;

    public TradeCollectionJob(MolitTradeClient client, TradeCollectionService storage) {
        this.client = client;
        this.storage = storage;
    }

    public CollectionSummary collect(String regionCode, String regionName, YearMonth month, int rows) {
        if (rows < 1 || rows > 1_000) {
            throw new IllegalArgumentException("rows는 1 이상 1000 이하여야 합니다.");
        }
        int page = 1, pages = 0, createdRegions = 0, createdApartments = 0, savedTrades = 0, duplicates = 0;
        while (true) {
            MolitTradePage response = client.fetch(regionCode, month, page, rows);
            pages++;
            if (!response.items().isEmpty()) {
                CollectionResult result = storage.store(regionCode, regionName, response.items());
                createdRegions += result.createdRegions();
                createdApartments += result.createdApartments();
                savedTrades += result.savedTrades();
                duplicates += result.duplicates();
            }
            int totalPages = Math.max(1, (response.totalCount() + rows - 1) / rows);
            if (page >= totalPages || response.items().isEmpty()) {
                break;
            }
            page++;
        }
        return new CollectionSummary(pages, createdRegions, createdApartments, savedTrades, duplicates);
    }
}

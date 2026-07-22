package com.nexthome.collector.trade;

import com.nexthome.collector.region.Region;
import com.nexthome.collector.region.RegionRepository;
import java.time.YearMonth;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CapitalAreaTradeCollectionJob {
    private static final Logger log = LoggerFactory.getLogger(CapitalAreaTradeCollectionJob.class);
    private final RegionRepository regions;
    private final TradeCollectionJob collector;
    private final CollectionCoverageStore coverage;

    public CapitalAreaTradeCollectionJob(RegionRepository regions, TradeCollectionJob collector, CollectionCoverageStore coverage) {
        this.regions = regions;
        this.collector = collector;
        this.coverage = coverage;
    }

    public CapitalAreaCollectionSummary collect(YearMonth start, YearMonth end, int rows) {
        if (start.isAfter(end)) throw new IllegalArgumentException("수집 시작 월은 종료 월보다 늦을 수 없습니다.");
        List<Region> districts = regions.findByLevelOrderByCodeAsc((short) 2);
        int attempts = 0, pages = 0, saved = 0, duplicates = 0;
        for (Region region : districts) {
            for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
                if (coverage.isComplete(region.code(), month)) {
                    log.info("완료된 수도권 실거래 수집 건너뜀: region={}({}), month={}",
                            region.name(), region.code(), month);
                    continue;
                }
                CollectionSummary summary = collector.collect(region.code(), region.name(), month, rows);
                coverage.markComplete(region.code(), month);
                attempts++;
                pages += summary.pages();
                saved += summary.savedTrades();
                duplicates += summary.duplicates();
                log.info("수도권 실거래 수집: region={}({}), month={}, saved={}, duplicates={}",
                        region.name(), region.code(), month, summary.savedTrades(), summary.duplicates());
            }
        }
        return new CapitalAreaCollectionSummary(districts.size(), attempts, pages, saved, duplicates);
    }
}

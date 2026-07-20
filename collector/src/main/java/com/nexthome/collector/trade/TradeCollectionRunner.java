package com.nexthome.collector.trade;

import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "next-home.collector.enabled", havingValue = "true")
public class TradeCollectionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TradeCollectionRunner.class);
    private final TradeCollectionJob job;
    private final String regionCode;
    private final String regionName;
    private final YearMonth month;
    private final int rows;

    public TradeCollectionRunner(
            TradeCollectionJob job,
            @Value("${next-home.collector.region-code}") String regionCode,
            @Value("${next-home.collector.region-name}") String regionName,
            @Value("${next-home.collector.month}") String month,
            @Value("${next-home.collector.rows:100}") int rows) {
        this.job = job;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.month = YearMonth.parse(month);
        this.rows = rows;
    }

    @Override
    public void run(ApplicationArguments args) {
        CollectionSummary summary = job.collect(regionCode, regionName, month, rows);
        log.info("실거래 수집 완료: region={}, month={}, pages={}, saved={}, duplicates={}",
                regionCode, month, summary.pages(), summary.savedTrades(), summary.duplicates());
    }
}

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
@ConditionalOnProperty(name = "next-home.capital-collector.enabled", havingValue = "true")
public class CapitalAreaTradeCollectionRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CapitalAreaTradeCollectionRunner.class);
    private final CapitalAreaTradeCollectionJob job;
    private final YearMonth start;
    private final YearMonth end;
    private final int rows;

    public CapitalAreaTradeCollectionRunner(
            CapitalAreaTradeCollectionJob job,
            @Value("${next-home.capital-collector.start-month}") String start,
            @Value("${next-home.capital-collector.end-month}") String end,
            @Value("${next-home.capital-collector.rows:500}") int rows) {
        this.job = job;
        this.start = YearMonth.parse(start);
        this.end = YearMonth.parse(end);
        this.rows = rows;
    }

    @Override
    public void run(ApplicationArguments args) {
        CapitalAreaCollectionSummary summary = job.collect(start, end, rows);
        log.info("수도권 실거래 전체 수집 완료: regions={}, months={}, pages={}, saved={}, duplicates={}",
                summary.regions(), summary.months(), summary.pages(), summary.savedTrades(), summary.duplicates());
    }
}

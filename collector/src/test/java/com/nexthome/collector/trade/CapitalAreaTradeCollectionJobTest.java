package com.nexthome.collector.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexthome.collector.region.Region;
import com.nexthome.collector.region.RegionRepository;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class CapitalAreaTradeCollectionJobTest {

    @Test
    void collectsEveryDistrictAndMonthInTheRequestedRange() {
        RegionRepository regions = mock(RegionRepository.class);
        TradeCollectionJob collector = mock(TradeCollectionJob.class);
        CollectionCoverageStore coverage = mock(CollectionCoverageStore.class);
        Region seoul = Region.create("11110", "종로구", 2);
        Region gyeonggi = Region.create("41135", "성남시 분당구", 2);
        when(regions.findByLevelOrderByCodeAsc((short) 2)).thenReturn(List.of(seoul, gyeonggi));
        CollectionSummary one = new CollectionSummary(1, 0, 1, 2, 0);
        when(collector.collect(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(500))).thenReturn(one);

        CapitalAreaCollectionSummary summary = new CapitalAreaTradeCollectionJob(regions, collector, coverage)
                .collect(YearMonth.of(2026, 1), YearMonth.of(2026, 2), 500);

        verify(collector).collect("11110", "종로구", YearMonth.of(2026, 1), 500);
        verify(collector).collect("11110", "종로구", YearMonth.of(2026, 2), 500);
        verify(collector).collect("41135", "성남시 분당구", YearMonth.of(2026, 1), 500);
        verify(collector).collect("41135", "성남시 분당구", YearMonth.of(2026, 2), 500);
        verify(coverage).markComplete("11110", YearMonth.of(2026, 1));
        verify(coverage).markComplete("41135", YearMonth.of(2026, 2));
        assertThat(summary.regions()).isEqualTo(2);
        assertThat(summary.months()).isEqualTo(4);
        assertThat(summary.savedTrades()).isEqualTo(8);
    }

    @Test
    void skipsCompletedRegionMonthsAndResumesFromTheFirstIncompleteCheckpoint() {
        RegionRepository regions = mock(RegionRepository.class);
        TradeCollectionJob collector = mock(TradeCollectionJob.class);
        CollectionCoverageStore coverage = mock(CollectionCoverageStore.class);
        Region seoul = Region.create("11110", "종로구", 2);
        when(regions.findByLevelOrderByCodeAsc((short) 2)).thenReturn(List.of(seoul));
        when(coverage.isComplete("11110", YearMonth.of(2026, 1))).thenReturn(true);
        when(collector.collect("11110", "종로구", YearMonth.of(2026, 2), 500))
                .thenReturn(new CollectionSummary(1, 0, 0, 3, 0));

        CapitalAreaCollectionSummary summary = new CapitalAreaTradeCollectionJob(regions, collector, coverage)
                .collect(YearMonth.of(2026, 1), YearMonth.of(2026, 2), 500);

        verify(collector, never()).collect("11110", "종로구", YearMonth.of(2026, 1), 500);
        verify(collector).collect("11110", "종로구", YearMonth.of(2026, 2), 500);
        verify(coverage).markComplete("11110", YearMonth.of(2026, 2));
        assertThat(summary.months()).isEqualTo(1);
    }
}

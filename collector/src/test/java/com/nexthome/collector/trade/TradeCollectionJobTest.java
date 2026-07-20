package com.nexthome.collector.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.List;

import com.nexthome.collector.molit.MolitTradeClient;
import com.nexthome.collector.molit.MolitTradeItem;
import com.nexthome.collector.molit.MolitTradePage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeCollectionJobTest {

    @Mock MolitTradeClient client;
    @Mock TradeCollectionService storage;

    @Test
    void fetchesEveryPageAndAggregatesStorageResults() {
        YearMonth month = YearMonth.of(2026, 1);
        MolitTradeItem first = org.mockito.Mockito.mock(MolitTradeItem.class);
        MolitTradeItem second = org.mockito.Mockito.mock(MolitTradeItem.class);
        when(client.fetch("11110", month, 1, 1)).thenReturn(new MolitTradePage(2, List.of(first)));
        when(client.fetch("11110", month, 2, 1)).thenReturn(new MolitTradePage(2, List.of(second)));
        when(storage.store("11110", "종로구", List.of(first))).thenReturn(new CollectionResult(1, 1, 1, 0));
        when(storage.store("11110", "종로구", List.of(second))).thenReturn(new CollectionResult(0, 1, 0, 1));

        CollectionSummary summary = new TradeCollectionJob(client, storage)
                .collect("11110", "종로구", month, 1);

        assertThat(summary).isEqualTo(new CollectionSummary(2, 1, 2, 1, 1));
        verify(client).fetch("11110", month, 2, 1);
    }

    @Test
    void stopsAfterFirstPageWhenApiHasNoItems() {
        YearMonth month = YearMonth.of(2026, 1);
        when(client.fetch("11110", month, 1, 100)).thenReturn(new MolitTradePage(0, List.of()));

        CollectionSummary summary = new TradeCollectionJob(client, storage)
                .collect("11110", "종로구", month, 100);

        assertThat(summary.pages()).isEqualTo(1);
        assertThat(summary.savedTrades()).isZero();
    }
}

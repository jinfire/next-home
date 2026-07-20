package com.nexthome.collector.molit;

import java.util.List;

public record MolitTradePage(int totalCount, List<MolitTradeItem> items) {
}

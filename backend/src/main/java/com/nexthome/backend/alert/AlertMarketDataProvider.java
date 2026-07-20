package com.nexthome.backend.alert;

import java.util.Optional;

public interface AlertMarketDataProvider {
    Optional<AlertMarketSnapshot> snapshot(AlertCondition condition, int year);
}

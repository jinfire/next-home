package com.nexthome.backend.alert;

import com.nexthome.backend.push.PushNotificationService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.alerts.enabled", havingValue = "true", matchIfMissing = true)
public class AlertDispatchJob {
    private static final Duration COOLDOWN = Duration.ofHours(24);
    private final AlertConditionRepository repository;
    private final AlertMarketDataProvider market;
    private final AlertConditionMatcher matcher;
    private final PushNotificationService push;
    private final Clock clock;

    AlertDispatchJob(
            AlertConditionRepository repository,
            AlertMarketDataProvider market,
            AlertConditionMatcher matcher,
            PushNotificationService push,
            Clock clock) {
        this.repository = repository;
        this.market = market;
        this.matcher = matcher;
        this.push = push;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.alerts.check-interval-ms:3600000}",
            initialDelayString = "${app.alerts.initial-delay-ms:60000}")
    public void evaluate() {
        Instant now = clock.instant();
        int year = Year.now(clock).getValue();
        repository.findAllByEnabledTrue().stream()
                .filter(condition -> condition.canTrigger(now, COOLDOWN))
                .forEach(condition -> market.snapshot(condition, year)
                        .filter(snapshot -> matcher.matches(condition.thresholds(), snapshot))
                        .ifPresent(snapshot -> notify(condition, snapshot, now)));
    }

    private void notify(AlertCondition condition, AlertMarketSnapshot snapshot, Instant now) {
        String payload = """
                {"title":"갈아타기 조건이 좋아졌어요","body":"현재 가격 격차가 설정한 범위에 들어왔습니다.","url":"/#upgrade"}
                """.trim();
        int sent = push.sendToBrowser(condition.browserId(), payload);
        if (sent > 0) {
            condition.markTriggered(now);
            repository.save(condition);
        }
    }
}

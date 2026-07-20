package com.nexthome.backend.alert;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexthome.backend.push.PushNotificationService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AlertDispatchJobTest {
    @Test
    void evaluatesEnabledConditionsSendsPushAndMarksTheCondition() {
        UUID browserId = UUID.randomUUID();
        AlertConditionRepository repository = mock(AlertConditionRepository.class);
        AlertMarketDataProvider market = mock(AlertMarketDataProvider.class);
        PushNotificationService push = mock(PushNotificationService.class);
        AlertCondition condition = new AlertCondition(new AlertConditionRequest(
                browserId, 10, null, 4, new BigDecimal("20"), null));
        when(repository.findAllByEnabledTrue()).thenReturn(List.of(condition));
        when(market.snapshot(condition, 2026)).thenReturn(Optional.of(
                new AlertMarketSnapshot(new BigDecimal("15"), new BigDecimal("25"))));
        when(push.sendToBrowser(eq(browserId), contains("갈아타기 조건"))).thenReturn(1);
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC);
        AlertDispatchJob job = new AlertDispatchJob(repository, market, new AlertConditionMatcher(), push, clock);

        job.evaluate();

        verify(push).sendToBrowser(eq(browserId), contains("갈아타기 조건"));
        verify(repository).save(condition);
    }
}

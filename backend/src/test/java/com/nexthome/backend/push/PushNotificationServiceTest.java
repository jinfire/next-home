package com.nexthome.backend.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PushNotificationServiceTest {
    @Test
    void sendsToEverySubscriptionForTheBrowser() {
        UUID browserId = UUID.randomUUID();
        PushSubscriptionRepository repository = mock(PushSubscriptionRepository.class);
        WebPushGateway gateway = mock(WebPushGateway.class);
        PushSubscription subscription = new PushSubscription(new PushSubscriptionRequest(
                browserId, "https://push.example/1", "p256dh", "auth"));
        when(repository.findAllByBrowserId(browserId)).thenReturn(List.of(subscription));
        PushNotificationService service = new PushNotificationService(repository, gateway);

        int sent = service.sendToBrowser(browserId, "{\"title\":\"Next Home\"}");

        assertThat(sent).isEqualTo(1);
        verify(gateway).send(subscription.target(), "{\"title\":\"Next Home\"}");
    }
}

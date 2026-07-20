package com.nexthome.backend.push;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushNotificationService {
    private final PushSubscriptionRepository repository;
    private final WebPushGateway gateway;

    PushNotificationService(PushSubscriptionRepository repository, WebPushGateway gateway) {
        this.repository = repository;
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public int sendToBrowser(UUID browserId, String payload) {
        var subscriptions = repository.findAllByBrowserId(browserId);
        subscriptions.forEach(subscription -> gateway.send(subscription.target(), payload));
        return subscriptions.size();
    }
}

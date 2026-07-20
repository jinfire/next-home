package com.nexthome.backend.push;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionService {
    private final PushSubscriptionRepository repository;

    PushSubscriptionService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PushSubscriptionResponse register(PushSubscriptionRequest request) {
        PushSubscription subscription = repository.findByEndpoint(request.endpoint())
                .orElseGet(() -> new PushSubscription(request));
        subscription.update(request);
        return repository.save(subscription).response();
    }
}

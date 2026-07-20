package com.nexthome.backend.push;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "push_subscription")
class PushSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "browser_id", nullable = false)
    private UUID browserId;
    @Column(nullable = false)
    private String endpoint;
    @Column(name = "p256dh_key", nullable = false)
    private String p256dh;
    @Column(name = "auth_key", nullable = false)
    private String auth;

    protected PushSubscription() {
    }

    PushSubscription(PushSubscriptionRequest request) {
        update(request);
    }

    void update(PushSubscriptionRequest request) {
        browserId = request.browserId();
        endpoint = request.endpoint();
        p256dh = request.p256dh();
        auth = request.auth();
    }

    PushSubscriptionResponse response() {
        return new PushSubscriptionResponse(id, browserId, endpoint);
    }

    PushTarget target() {
        return new PushTarget(endpoint, p256dh, auth);
    }
}

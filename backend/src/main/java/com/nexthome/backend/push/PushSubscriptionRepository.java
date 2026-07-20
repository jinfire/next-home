package com.nexthome.backend.push;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByEndpoint(String endpoint);
}

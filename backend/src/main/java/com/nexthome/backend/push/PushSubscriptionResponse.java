package com.nexthome.backend.push;

import java.util.UUID;

public record PushSubscriptionResponse(long id, UUID browserId, String endpoint) {
}

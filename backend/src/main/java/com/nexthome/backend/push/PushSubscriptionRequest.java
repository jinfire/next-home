package com.nexthome.backend.push;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PushSubscriptionRequest(
        @NotNull UUID browserId,
        @NotBlank String endpoint,
        @NotBlank String p256dh,
        @NotBlank String auth) {
}

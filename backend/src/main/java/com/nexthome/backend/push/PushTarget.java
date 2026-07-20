package com.nexthome.backend.push;

public record PushTarget(String endpoint, String p256dh, String auth) {
}

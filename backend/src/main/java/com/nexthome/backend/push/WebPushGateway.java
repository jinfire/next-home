package com.nexthome.backend.push;

public interface WebPushGateway {
    void send(PushTarget target, String payload);
}

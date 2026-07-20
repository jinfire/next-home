package com.nexthome.backend.push;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push-subscriptions")
public class PushSubscriptionController {
    private final PushSubscriptionService service;
    private final String publicKey;

    public PushSubscriptionController(
            PushSubscriptionService service,
            @Value("${app.push.vapid-public-key:}") String publicKey) {
        this.service = service;
        this.publicKey = publicKey;
    }

    @GetMapping("/vapid-public-key")
    public Map<String, String> publicKey() {
        return Map.of("publicKey", publicKey);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PushSubscriptionResponse register(@Valid @RequestBody PushSubscriptionRequest request) {
        return service.register(request);
    }
}

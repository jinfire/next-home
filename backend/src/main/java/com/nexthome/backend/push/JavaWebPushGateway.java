package com.nexthome.backend.push;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JavaWebPushGateway implements WebPushGateway {
    private final String publicKey;
    private final String privateKey;
    private final String subject;

    JavaWebPushGateway(
            @Value("${app.push.vapid-public-key:}") String publicKey,
            @Value("${app.push.vapid-private-key:}") String privateKey,
            @Value("${app.push.vapid-subject:mailto:admin@next-home.local}") String subject) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.subject = subject;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public void send(PushTarget target, String payload) {
        if (publicKey.isBlank() || privateKey.isBlank()) {
            throw new IllegalStateException("VAPID keys are not configured");
        }
        try {
            PushService service = new PushService();
            service.setPublicKey(publicKey);
            service.setPrivateKey(privateKey);
            service.setSubject(subject);
            Notification notification = new Notification(
                    target.endpoint(),
                    Utils.loadPublicKey(target.p256dh()),
                    Base64.getUrlDecoder().decode(target.auth()),
                    payload.getBytes(StandardCharsets.UTF_8));
            service.send(notification);
        } catch (Exception exception) {
            throw new WebPushDeliveryException("Failed to deliver browser push", exception);
        }
    }
}

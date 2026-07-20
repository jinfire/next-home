package com.nexthome.backend.push;

public class WebPushDeliveryException extends RuntimeException {
    WebPushDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

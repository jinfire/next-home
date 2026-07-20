package com.nexthome.backend.geocoding;

public class ApiBudgetExceededException extends RuntimeException {
    public ApiBudgetExceededException(String message) {
        super(message);
    }
}

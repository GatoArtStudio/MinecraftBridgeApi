package com.gatoartstudio.api;

public record BridgeResponse(String response, boolean success, String errorMessage) {
    public BridgeResponse {
        if (success && errorMessage != null && !errorMessage.isBlank()) {
            throw new IllegalArgumentException("A successful response cannot contain an error message");
        }

        if (!success && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("A failed response must contain an error message");
        }
    }

    public static BridgeResponse success(String response) {
        return new BridgeResponse(response, true, null);
    }

    public static BridgeResponse failure(String errorMessage) {
        return new BridgeResponse(null, false, errorMessage);
    }
}

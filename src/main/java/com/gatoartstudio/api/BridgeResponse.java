package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BridgeResponse(
        @Nullable String response,
        boolean success,
        @Nullable String errorMessage
) {
    public BridgeResponse {
        if (success && errorMessage != null && !errorMessage.isBlank()) {
            throw new IllegalArgumentException("A successful response cannot contain an error message");
        }

        if (!success && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("A failed response must contain an error message");
        }
    }

    public static @NotNull BridgeResponse success(@Nullable String response) {
        return new BridgeResponse(response, true, null);
    }

    public static @NotNull BridgeResponse failure(@NotNull String errorMessage) {
        return new BridgeResponse(null, false, errorMessage);
    }
}

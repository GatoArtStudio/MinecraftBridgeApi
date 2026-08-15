package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BridgeEvent(@NotNull String type, @NotNull String payload, long timestamp) {
    public BridgeEvent {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be blank");
        }

        Objects.requireNonNull(payload, "payload");
    }

    public static @NotNull BridgeEvent now(@NotNull String type, @NotNull String payload) {
        return new BridgeEvent(type, payload, System.currentTimeMillis());
    }
}

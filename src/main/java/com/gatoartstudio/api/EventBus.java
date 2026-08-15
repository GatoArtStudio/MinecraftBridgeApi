package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

public interface EventBus {
    @NotNull EventSubscription subscribe(@NotNull String eventType, @NotNull EventHandler handler)
            throws IllegalArgumentException, NullPointerException;

    void emit(@NotNull BridgeEvent event) throws NullPointerException;
}

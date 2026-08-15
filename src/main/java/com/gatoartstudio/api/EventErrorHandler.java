package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventErrorHandler {
    void handle(@NotNull BridgeEvent event, @NotNull Throwable error);
}

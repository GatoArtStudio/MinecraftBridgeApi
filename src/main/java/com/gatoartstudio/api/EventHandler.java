package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventHandler {
    void handle(@NotNull BridgeEvent event);
}

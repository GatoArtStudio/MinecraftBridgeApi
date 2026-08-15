package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Bridge {
    @NotNull CompletableFuture<String> ping();

    @NotNull CompletableFuture<String> getPlayerName(@NotNull UUID playerId);

    @NotNull CompletableFuture<BridgeResponse> requestInformation(@NotNull BridgeRequest request);

    @NotNull EventSubscription subscribe(@NotNull String eventType, @NotNull EventHandler handler)
            throws IllegalArgumentException, NullPointerException;

    void emit(@NotNull BridgeEvent event) throws NullPointerException;
}

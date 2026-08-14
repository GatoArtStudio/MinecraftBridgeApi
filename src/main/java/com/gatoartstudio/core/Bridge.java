package com.gatoartstudio.core;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Bridge {
    CompletableFuture<String> ping();

    CompletableFuture<String> getPlayerName(UUID playerId);
}

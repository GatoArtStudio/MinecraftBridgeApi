package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

public record BridgeRequest(@NotNull String type, @NotNull String payload) {
}

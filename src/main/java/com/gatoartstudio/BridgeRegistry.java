package com.gatoartstudio;

import com.gatoartstudio.core.Bridge;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class BridgeRegistry {
    private static final AtomicReference<Bridge> BRIDGE = new AtomicReference<>();

    private BridgeRegistry() {
    }

    public static void register(Bridge bridge) throws IllegalStateException {
        Objects.requireNonNull(bridge, "bridge");

        if (!BRIDGE.compareAndSet(null, bridge)) {
            throw new IllegalStateException("Bridge is already registered!");
        }
    }

    public static Bridge get() throws IllegalStateException {
        Bridge bridge = BRIDGE.get();

        if (bridge == null) {
            throw new IllegalStateException("Bridge is not registered!");
        }

        return bridge;
    }

    public static boolean isAvailable() {
        return BRIDGE.get() != null;
    }

    public static void unregister() {
        BRIDGE.set(null);
    }
}

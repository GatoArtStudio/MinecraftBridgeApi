package com.gatoartstudio.api;

import java.util.concurrent.atomic.AtomicReference;

public final class BridgeRegistry {
    private static final AtomicReference<Bridge> BRIDGE = new AtomicReference<>();

    private BridgeRegistry() {
    }

    public static void register(Bridge instance) throws IllegalStateException {

        if (instance == null) {
            throw new NullPointerException("instance is null");
        }

        if (!BRIDGE.compareAndSet(null, instance)) {
            throw new IllegalStateException("Bridge is already registered!");
        }
    }

    public static Bridge get() throws IllegalStateException {
        Bridge result = BRIDGE.get();

        if (result == null) {
            throw new IllegalStateException("Bridge is not registered!");
        }

        return result;
    }

    public static boolean isAvailable() {
        return BRIDGE.get() != null;
    }

    public static void unregister() {
        BRIDGE.set(null);
    }
}

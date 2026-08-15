package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class EventBusRegistry {
    private static final AtomicReference<EventBus> EVENT_BUS = new AtomicReference<>();

    private EventBusRegistry() {
    }

    public static void register(@NotNull EventBus eventBus) throws IllegalStateException, NullPointerException {
        Objects.requireNonNull(eventBus, "eventBus");

        if (!EVENT_BUS.compareAndSet(null, eventBus)) {
            throw new IllegalStateException("Event bus is already registered");
        }
    }

    public static @NotNull EventBus get() throws IllegalStateException {
        EventBus eventBus = EVENT_BUS.get();

        if (eventBus == null) {
            throw new IllegalStateException("Event bus is not registered");
        }

        return eventBus;
    }

    public static boolean isAvailable() {
        return EVENT_BUS.get() != null;
    }

    public static void unregister() {
        EVENT_BUS.set(null);
    }
}

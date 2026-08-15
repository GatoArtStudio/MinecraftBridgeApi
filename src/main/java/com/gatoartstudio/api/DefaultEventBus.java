package com.gatoartstudio.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultEventBus implements EventBus {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Subscription>> subscriptions =
            new ConcurrentHashMap<>();
    private final EventErrorHandler errorHandler;

    public DefaultEventBus() {
        this((event, error) -> error.printStackTrace());
    }

    public DefaultEventBus(@NotNull EventErrorHandler errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    }

    @Override
    public @NotNull EventSubscription subscribe(@NotNull String eventType, @NotNull EventHandler handler)
            throws IllegalArgumentException, NullPointerException {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be blank");
        }

        Objects.requireNonNull(handler, "handler");

        Subscription subscription = new Subscription(eventType, handler);
        subscriptions.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add(subscription);
        return subscription;
    }

    @Override
    public void emit(@NotNull BridgeEvent event) throws NullPointerException {
        Objects.requireNonNull(event, "event");

        CopyOnWriteArrayList<Subscription> eventSubscriptions = subscriptions.get(event.type());
        if (eventSubscriptions == null) {
            return;
        }

        for (Subscription subscription : eventSubscriptions) {
            if (!subscription.isActive()) {
                continue;
            }

            try {
                subscription.handler.handle(event);
            } catch (Exception error) {
                errorHandler.handle(event, error);
            }
        }
    }

    private final class Subscription implements EventSubscription {
        private final String eventType;
        private final EventHandler handler;
        private volatile boolean active = true;

        private Subscription(String eventType, EventHandler handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        @Override
        public void unsubscribe() {
            if (!active) {
                return;
            }

            active = false;
            CopyOnWriteArrayList<Subscription> eventSubscriptions = subscriptions.get(eventType);
            if (eventSubscriptions != null) {
                eventSubscriptions.remove(this);
                if (eventSubscriptions.isEmpty()) {
                    subscriptions.remove(eventType, eventSubscriptions);
                }
            }
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }
}

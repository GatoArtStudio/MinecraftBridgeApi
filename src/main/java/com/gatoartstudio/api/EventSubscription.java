package com.gatoartstudio.api;

public interface EventSubscription extends AutoCloseable {
    void unsubscribe();

    boolean isActive();

    @Override
    default void close() {
        unsubscribe();
    }
}

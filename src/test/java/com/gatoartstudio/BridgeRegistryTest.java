package com.gatoartstudio;

import com.gatoartstudio.api.Bridge;
import com.gatoartstudio.api.BridgeEvent;
import com.gatoartstudio.api.BridgeRegistry;
import com.gatoartstudio.api.BridgeRequest;
import com.gatoartstudio.api.BridgeResponse;
import com.gatoartstudio.api.EventHandler;
import com.gatoartstudio.api.EventSubscription;
import com.gatoartstudio.core.DefaultEventBus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeRegistryTest {
    private static final UUID PLAYER_ID = UUID.randomUUID();

    @AfterEach
    void cleanRegistry() {
        BridgeRegistry.unregister();
    }

    @Test
    void registerMakesBridgeAvailableAndReturnsTheSameInstance() {
        Bridge bridge = new TestBridge();

        BridgeRegistry.register(bridge);

        assertTrue(BridgeRegistry.isAvailable());
        assertSame(bridge, BridgeRegistry.get());
    }

    @Test
    void registeredBridgeCanBeUsedThroughTheRegistry() {
        BridgeRegistry.register(new TestBridge());

        Bridge bridge = BridgeRegistry.get();

        assertEquals("pong", bridge.ping().join());
        assertEquals("player-" + PLAYER_ID, bridge.getPlayerName(PLAYER_ID).join());
    }

    @Test
    void registeredBridgeCanRequestInformation() {
        BridgeRegistry.register(new TestBridge());

        BridgeRequest request = new BridgeRequest("player", "123");

        BridgeResponse response = BridgeRegistry.get().requestInformation(request).join();

        assertTrue(response.success());
        assertEquals("player:123", response.response());
        assertNull(response.errorMessage());
    }

    @Test
    void registeredBridgeCanSubscribeAndEmitEvents() {
        TestBridge bridge = new TestBridge();
        BridgeRegistry.register(bridge);
        AtomicReference<String> received = new AtomicReference<>();

        bridge.subscribe("player.joined", event -> received.set(event.payload()));
        bridge.emit(BridgeEvent.now("player.joined", "Steve"));

        assertEquals("Steve", received.get());
    }

    @Test
    void cannotRegisterTwoBridges() {
        BridgeRegistry.register(new TestBridge());

        assertThrows(IllegalStateException.class, () -> BridgeRegistry.register(new TestBridge()));
    }

    @Test
    void cannotRegisterNull() {
        assertThrows(NullPointerException.class, () -> BridgeRegistry.register(null));
    }

    @Test
    void getFailsWhenNoBridgeIsRegistered() {
        assertFalse(BridgeRegistry.isAvailable());
        assertThrows(IllegalStateException.class, BridgeRegistry::get);
    }

    @Test
    void unregisterMakesTheRegistryUnavailable() {
        BridgeRegistry.register(new TestBridge());

        BridgeRegistry.unregister();

        assertFalse(BridgeRegistry.isAvailable());
        assertThrows(IllegalStateException.class, BridgeRegistry::get);
    }

    private static final class TestBridge implements Bridge {
        private final DefaultEventBus eventBus = new DefaultEventBus();

        @Override
        public @NotNull CompletableFuture<String> ping() {
            return CompletableFuture.completedFuture("pong");
        }

        @Override
        public @NotNull CompletableFuture<String> getPlayerName(@NotNull UUID playerId) {
            return CompletableFuture.completedFuture("player-" + playerId);
        }

        @Override
        public @NotNull CompletableFuture<BridgeResponse> requestInformation(@NotNull BridgeRequest request) {
            return CompletableFuture.completedFuture(
                    BridgeResponse.success(request.type() + ":" + request.payload())
            );
        }

        @Override
        public @NotNull EventSubscription subscribe(
                @NotNull String eventType,
                @NotNull EventHandler handler
        ) {
            return eventBus.subscribe(eventType, handler);
        }

        @Override
        public void emit(@NotNull BridgeEvent event) {
            eventBus.emit(event);
        }
    }
}

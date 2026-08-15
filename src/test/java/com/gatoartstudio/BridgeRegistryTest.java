package com.gatoartstudio;

import com.gatoartstudio.api.Bridge;
import com.gatoartstudio.api.BridgeRegistry;
import com.gatoartstudio.api.BridgeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        assertEquals("player:123", BridgeRegistry.get().requestInformation(request).join());
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
        @Override
        public CompletableFuture<String> ping() {
            return CompletableFuture.completedFuture("pong");
        }

        @Override
        public CompletableFuture<String> getPlayerName(UUID playerId) {
            return CompletableFuture.completedFuture("player-" + playerId);
        }

        @Override
        public CompletableFuture<String> requestInformation(BridgeRequest request) {
            return CompletableFuture.completedFuture(request.type() + ":" + request.payload());
        }
    }
}

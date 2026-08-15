package com.gatoartstudio;

import com.gatoartstudio.api.BridgeEvent;
import com.gatoartstudio.api.EventBus;
import com.gatoartstudio.api.EventSubscription;
import com.gatoartstudio.core.DefaultEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventBusTest {
    @Test
    void emitsEventOnlyToSubscribersOfTheSameType() {
        EventBus eventBus = new DefaultEventBus();
        List<String> received = new ArrayList<>();

        eventBus.subscribe("player.joined", event -> received.add(event.payload()));
        eventBus.subscribe("player.left", event -> received.add("wrong"));

        eventBus.emit(BridgeEvent.now("player.joined", "Steve"));

        assertEquals(List.of("Steve"), received);
    }

    @Test
    void subscriptionCanBeCancelled() {
        EventBus eventBus = new DefaultEventBus();
        AtomicInteger calls = new AtomicInteger();

        EventSubscription subscription = eventBus.subscribe("test", event -> calls.incrementAndGet());
        subscription.unsubscribe();
        eventBus.emit(BridgeEvent.now("test", "payload"));

        assertFalse(subscription.isActive());
        assertEquals(0, calls.get());
    }

    @Test
    void listenerErrorsDoNotPreventOtherListeners() {
        List<Throwable> errors = new ArrayList<>();
        EventBus eventBus = new DefaultEventBus((event, error) -> errors.add(error));
        AtomicInteger calls = new AtomicInteger();

        eventBus.subscribe("test", event -> {
            throw new IllegalStateException("failure");
        });
        eventBus.subscribe("test", event -> calls.incrementAndGet());

        eventBus.emit(BridgeEvent.now("test", "payload"));

        assertEquals(1, errors.size());
        assertEquals(1, calls.get());
    }

    @Test
    void eventIncludesTimestamp() {
        BridgeEvent event = BridgeEvent.now("test", "payload");

        assertTrue(event.timestamp() > 0);
    }
}

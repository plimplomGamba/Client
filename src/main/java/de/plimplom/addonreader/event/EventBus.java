package de.plimplom.addonreader.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();
    private final ExecutorService executorService;

    public EventBus() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public <T extends ApplicationEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        log.debug("Subscribing listener {} for event type: {}", listener, eventType);
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends ApplicationEvent> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        log.debug("Unsubscribing listener {} for event type: {}", listener, eventType);
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends ApplicationEvent> void publish(T event) {
        log.debug("Publishing event: {}", event);
        Class<?> eventType = event.getClass();
        if (listeners.containsKey(eventType)) {
            List<EventListener<?>> eventListeners = listeners.get(eventType);
            for (EventListener listener : eventListeners) {
                executorService.submit(() -> listener.onEvent(event));
            }
        }
    }

    public interface EventListener<T> {
        void onEvent(T event);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

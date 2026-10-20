package com.feadev.eaids.commentary;

import com.feadev.eaids.commentary.dto.response.CommentaryEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory registry of {@link SseEmitter} instances keyed by fixture ID.
 *
 * <p>Emitters have a 5-minute timeout.  All lifecycle callbacks (timeout, completion,
 * error) call {@link #removeEmitter} so the registry never leaks stale connections.
 */
@Component
@Slf4j
public class SseEmitterRegistry {

    /** 5-minute emitter timeout. */
    private static final long EMITTER_TIMEOUT_MS = 300_000L;

    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    /**
     * Creates a new {@link SseEmitter} for the given fixture, registers it, and returns it.
     * The caller must write the HTTP response immediately after receiving the emitter.
     */
    public SseEmitter subscribe(UUID fixtureId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        emitter.onTimeout(()    -> removeEmitter(fixtureId, emitter));
        emitter.onCompletion(() -> removeEmitter(fixtureId, emitter));
        emitter.onError(t       -> removeEmitter(fixtureId, emitter));

        emitters.computeIfAbsent(fixtureId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        log.debug("SSE subscriber added for fixture {}; total subscribers: {}",
                fixtureId, emitters.getOrDefault(fixtureId, new CopyOnWriteArrayList<>()).size());

        return emitter;
    }

    /**
     * Broadcasts a commentary event to every active subscriber of the given fixture.
     * Subscribers that throw {@link IOException} are removed immediately.
     */
    public void broadcast(UUID fixtureId, CommentaryEventResponse event) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(fixtureId);
        if (list == null || list.isEmpty()) {
            return;
        }

        SseEmitter.SseEventBuilder sseEvent = SseEmitter.event()
                .name("commentary")
                .data(event);

        for (SseEmitter emitter : list) {
            try {
                emitter.send(sseEvent);
            } catch (IOException e) {
                log.warn("Failed to send SSE event to subscriber for fixture {}: {}", fixtureId, e.getMessage());
                removeEmitter(fixtureId, emitter);
            }
        }
    }

    /**
     * Sends a single named event to a specific emitter (used for replay on subscribe).
     */
    public void sendReplay(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.warn("Failed to send replay event: {}", e.getMessage());
        }
    }

    /** Removes the emitter from the registry. Cleans up the key if the list becomes empty. */
    public void removeEmitter(UUID fixtureId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(fixtureId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(fixtureId);
            }
        }
        log.debug("SSE subscriber removed for fixture {}", fixtureId);
    }

    /** Returns the current subscriber count for a fixture (useful for monitoring). */
    public int subscriberCount(UUID fixtureId) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(fixtureId);
        return list == null ? 0 : list.size();
    }
}

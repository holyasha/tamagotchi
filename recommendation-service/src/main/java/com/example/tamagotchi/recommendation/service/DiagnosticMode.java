package com.example.tamagotchi.recommendation.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class DiagnosticMode {
    public enum Mode { NORMAL, SLOW, FAIL }
    private final AtomicReference<Mode> mode = new AtomicReference<>(Mode.NORMAL);
    private final AtomicLong expiresAt = new AtomicLong(0);

    public synchronized void activate(Mode next, int seconds) {
        expiresAt.set(System.nanoTime() + Duration.ofSeconds(seconds).toNanos());
        mode.set(next);
    }

    public synchronized Mode current() {
        if (System.nanoTime() >= expiresAt.get()) mode.set(Mode.NORMAL);
        return mode.get();
    }

    public void beforeRecommendation() {
        Mode current = current();
        if (current == Mode.FAIL) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Recommendation service diagnostic failure");
        }
        if (current == Mode.SLOW) {
            try { Thread.sleep(750); }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Recommendation delay interrupted", exception);
            }
        }
    }
}

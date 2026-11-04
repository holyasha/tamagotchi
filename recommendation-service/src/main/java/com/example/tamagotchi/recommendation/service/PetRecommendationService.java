package com.example.tamagotchi.recommendation.service;

import com.example.tamagotchi.recommendation.api.RecommendationController.RecommendationResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PetRecommendationService {
    private static final Logger log = LoggerFactory.getLogger(PetRecommendationService.class);
    private final DiagnosticMode diagnostics;
    private final MeterRegistry registry;
    private final AtomicInteger active = new AtomicInteger();
    private final Map<String, Counter> requests;
    private final Map<String, Timer> durations;

    public PetRecommendationService(DiagnosticMode diagnostics, MeterRegistry registry) {
        this.diagnostics = diagnostics;
        this.registry = registry;
        requests = Map.of(
                "success", registry.counter("pet.recommendation.requests", "result", "success"),
                "failure", registry.counter("pet.recommendation.requests", "result", "failure"));
        durations = Map.of("success", timer(registry, "success"), "failure", timer(registry, "failure"));
        Gauge.builder("pet.recommendation.active", active, AtomicInteger::get)
                .description("Number of pet recommendation calculations currently running")
                .register(registry);
    }

    private Timer timer(MeterRegistry registry, String result) {
        return Timer.builder("pet.recommendation.processing")
                .description("Time spent calculating care recommendations")
                .tag("result", result)
                .serviceLevelObjectives(Duration.ofMillis(500), Duration.ofSeconds(1))
                .publishPercentileHistogram()
                .register(registry);
    }

    public RecommendationResponse recommend(UUID petId) {
        active.incrementAndGet();
        Timer.Sample sample = Timer.start(registry);
        String result = "failure";
        try {
            diagnostics.beforeRecommendation();
            RecommendationResponse response = new RecommendationResponse(petId, List.of(
                    "Следить за уровнем сытости питомца",
                    "Регулярно играть с питомцем для поддержания настроения",
                    "Контролировать здоровье и своевременно давать отдых"));
            result = "success";
            log.atInfo().addKeyValue("event", "pet.recommendation.completed")
                    .addKeyValue("outcome", result).log("Pet recommendations generated");
            return response;
        } catch (RuntimeException failure) {
            log.atWarn().addKeyValue("event", "pet.recommendation.failed")
                    .addKeyValue("outcome", "failure")
                    .addKeyValue("failureType", failure.getClass().getSimpleName())
                    .log("Pet recommendation failed");
            throw failure;
        } finally {
            sample.stop(durations.get(result));
            requests.get(result).increment();
            active.decrementAndGet();
        }
    }
}

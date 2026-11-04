package com.example.tamagotchi.recommendation.api;

import com.example.tamagotchi.recommendation.service.DiagnosticMode;
import com.example.tamagotchi.recommendation.service.PetRecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class RecommendationController {
    private final PetRecommendationService recommendationService;
    private final DiagnosticMode diagnosticMode;

    public RecommendationController(PetRecommendationService recommendationService, DiagnosticMode diagnosticMode) {
        this.recommendationService = recommendationService;
        this.diagnosticMode = diagnosticMode;
    }

    @GetMapping("/recommendations/{petId}")
    @PreAuthorize("hasRole('SERVICE')")
    public RecommendationResponse recommendations(@PathVariable UUID petId) {
        return recommendationService.recommend(petId);
    }

    @PutMapping("/admin/failure-mode")
    @PreAuthorize("hasRole('OPERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void failureMode(@RequestParam DiagnosticMode.Mode mode,
                            @RequestParam(defaultValue = "90") int seconds) {
        if (seconds < 1 || seconds > 120) throw new IllegalArgumentException("seconds must be between 1 and 120");
        diagnosticMode.activate(mode, seconds);
    }

    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('OPERATOR')")
    public Map<String, String> info() {
        return Map.of("service", "tamagotchi-recommendation-service", "status", "ok",
                "diagnosticMode", diagnosticMode.current().name());
    }

    public record RecommendationResponse(UUID petId, List<String> recommendations) {}
}

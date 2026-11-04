package com.example.tamagotchi.recommendation.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class RecommendationController {
    @GetMapping("/recommendations/{petId}")
    @PreAuthorize("hasRole('SERVICE')")
    public RecommendationResponse recommendations(@PathVariable UUID petId) {
        return new RecommendationResponse(petId, List.of(
                "Следить за уровнем сытости питомца",
                "Регулярно играть с питомцем для поддержания настроения"
        ));
    }

    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('OPERATOR')")
    public Map<String, String> info() {
        return Map.of("service", "tamagotchi-recommendation-service", "status", "ok");
    }

    public record RecommendationResponse(UUID petId, List<String> recommendations) {}
}

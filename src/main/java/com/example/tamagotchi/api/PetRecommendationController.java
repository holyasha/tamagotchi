package com.example.tamagotchi.api;

import com.example.tamagotchi.service.PetService;
import com.example.tamagotchi.service.RecommendationClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Profile("!session-auth")
@RequestMapping("/api/pets")
public class PetRecommendationController {
    private final PetService petService;
    private final RecommendationClient recommendationClient;

    public PetRecommendationController(PetService petService, RecommendationClient recommendationClient) {
        this.petService = petService;
        this.recommendationClient = recommendationClient;
    }

    @GetMapping("/{id}/recommendation")
    @PreAuthorize("hasAnyRole('READER', 'EDITOR')")
    public PetRecommendationResponse recommendation(@PathVariable UUID id) {
        // Проверяем существование питомца до сетевого вызова downstream-сервиса.
        petService.pet(id);
        return recommendationClient.recommendations(id);
    }
}

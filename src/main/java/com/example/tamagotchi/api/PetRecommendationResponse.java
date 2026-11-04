package com.example.tamagotchi.api;

import java.util.List;
import java.util.UUID;

public record PetRecommendationResponse(UUID petId, List<String> recommendations) {
}

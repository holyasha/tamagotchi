package com.example.tamagotchi.api;

import com.example.tamagotchi.domain.PetStatus;
import java.util.UUID;

public record PetResponse(
        UUID id,
        String name,
        String species,
        int hunger,
        int happiness,
        int health,
        PetStatus status
) { }

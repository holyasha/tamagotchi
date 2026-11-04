package com.example.tamagotchi.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePetRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 80) String species
) { }

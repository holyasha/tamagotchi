package com.example.tamagotchi.api;

import com.example.tamagotchi.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(@Valid @RequestBody CreatePetRequest request) {
        return petService.createPet(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OBSERVER', 'PLAYER')")
    public List<PetResponse> all() {
        return petService.pets();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OBSERVER', 'PLAYER')")
    public PetResponse byId(@PathVariable UUID id) {
        return petService.pet(id);
    }
}

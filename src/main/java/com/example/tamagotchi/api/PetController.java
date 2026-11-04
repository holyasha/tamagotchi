package com.example.tamagotchi.api;

import com.example.tamagotchi.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;
    public PetController(PetService petService) { this.petService = petService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(@Valid @RequestBody CreatePetRequest request) {
        return petService.createPet(request);
    }

    @GetMapping
    public List<PetResponse> all() { return petService.pets(); }

    @GetMapping("/{id}")
    public PetResponse byId(@PathVariable UUID id) { return petService.pet(id); }
}

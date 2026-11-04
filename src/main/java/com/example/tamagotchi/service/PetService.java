package com.example.tamagotchi.service;

import com.example.tamagotchi.api.ApiException;
import com.example.tamagotchi.api.CreatePetRequest;
import com.example.tamagotchi.api.PetResponse;
import com.example.tamagotchi.domain.PetEntity;
import com.example.tamagotchi.repository.PetRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class PetService {
    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional
    public PetResponse createPet(CreatePetRequest request) {
        String normalizedName = request.name().trim();
        if (petRepository.existsByName(normalizedName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Питомец с таким именем уже существует");
        }
        try {
            PetEntity pet = petRepository.saveAndFlush(
                    new PetEntity(UUID.randomUUID(), normalizedName, request.species().trim()));
            return toResponse(pet);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Питомец с таким именем уже существует");
        }
    }

    @Transactional(readOnly = true)
    public List<PetResponse> pets() {
        return petRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse pet(UUID id) {
        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Питомец не найден"));
        return toResponse(pet);
    }

    private PetResponse toResponse(PetEntity pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getSpecies(), pet.getHunger(),
                pet.getHappiness(), pet.getHealth(), pet.getStatus());
    }
}

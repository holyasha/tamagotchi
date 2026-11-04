package com.example.tamagotchi.repository;

import com.example.tamagotchi.domain.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PetRepository extends JpaRepository<PetEntity, UUID> {
    boolean existsByName(String name);
}

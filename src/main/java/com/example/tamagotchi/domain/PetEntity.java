package com.example.tamagotchi.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pets")
public class PetEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, length = 80)
    private String species;

    @Column(nullable = false)
    private int hunger;

    @Column(nullable = false)
    private int happiness;

    @Column(nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PetStatus status;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PetEntity() { }

    public PetEntity(UUID id, String name, String species) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.hunger = 50;
        this.happiness = 50;
        this.health = 100;
        this.status = PetStatus.HEALTHY;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getHealth() { return health; }
    public PetStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

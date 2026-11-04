package com.example.tamagotchi.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdsTest {
    @Test
    void preservesValidUuidAndNormalizesCase() {
        assertEquals("11111111-1111-4111-8111-111111111111",
                RequestIds.normalize(List.of("11111111-1111-4111-8111-111111111111")));
        assertEquals("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
                RequestIds.normalize(List.of("AAAAAAAA-AAAA-4AAA-8AAA-AAAAAAAAAAAA")));
    }

    @Test
    void replacesMalformedOrAmbiguousValues() {
        assertNotEquals("1-1-1-1-1", RequestIds.normalize(List.of("1-1-1-1-1")));
        assertNotEquals("11111111-1111-4111-8111-111111111111",
                RequestIds.normalize(List.of("11111111-1111-4111-8111-111111111111",
                        "22222222-2222-4222-8222-222222222222")));
    }
}

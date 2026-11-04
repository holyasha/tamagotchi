package com.example.tamagotchi.service;

import com.example.tamagotchi.api.PetRecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal;

@Component
@Profile("!session-auth")
public class RecommendationClient {
    private static final Logger log = LoggerFactory.getLogger(RecommendationClient.class);
    private final RestClient recommendationRestClient;

    public RecommendationClient(RestClient recommendationRestClient) {
        this.recommendationRestClient = recommendationRestClient;
    }

    public PetRecommendationResponse recommendations(UUID petId) {
        log.info("Calling tamagotchi-recommendation-service petId={}", petId);
        return recommendationRestClient.get()
                .uri("/internal/recommendations/{petId}", petId)
                .attributes(clientRegistrationId("recommendation"))
                .attributes(principal("tamagotchi-pet-service"))
                .retrieve()
                .body(PetRecommendationResponse.class);
    }
}

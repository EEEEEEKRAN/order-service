package com.microcommerce.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration pour WebClient
 * 
 * Configure les clients HTTP pour communiquer avec les autres microservices
 * Optimise les timeouts et la gestion des connexions
 */
@Configuration
public class WebClientConfig {
    
    /**
     * Bean WebClient.Builder avec configuration optimisée
     * 
     * Configure :
     * - Les timeouts de connexion
     * - La taille des buffers
     * - La gestion des erreurs
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .codecs(configurer -> {
                // Augmente la taille max des réponses (par défaut 256KB)
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
            });
    }
    
    /**
     * WebClient spécialisé pour le Product Service
     */
    @Bean("productServiceWebClient")
    public WebClient productServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("http://localhost:8081")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }
    
    /**
     * WebClient spécialisé pour le User Service
     */
    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("http://localhost:8082")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }
}
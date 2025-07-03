package com.microcommerce.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Client pour communiquer avec le User Service
 * 
 * Utilise WebClient pour faire des appels HTTP vers le service utilisateur
 * Principalement pour vérifier l'existence des utilisateurs
 */
@Service
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final WebClient webClient;
    
    @Value("${services.user-service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Vérifie si un utilisateur existe
     * 
     * @param userId l'ID de l'utilisateur
     * @return true si l'utilisateur existe
     */
    public boolean userExists(String userId) {
        logger.info("Vérification de l'existence de l'utilisateur: {}", userId);
        
        try {
            return webClient.get()
                .uri(userServiceUrl + "/api/users/{id}", userId)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(3))
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false)
                .block();
                
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'utilisateur {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère les informations de base d'un utilisateur via l'endpoint interne
     * 
     * @param userId l'ID de l'utilisateur
     * @return les infos de l'utilisateur ou null si introuvable
     */
    public UserInfo getUserInfo(String userId) {
        logger.info("Récupération des infos de l'utilisateur: {}", userId);
        
        try {
            return webClient.get()
                .uri(userServiceUrl + "/api/users/internal/{id}", userId)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .timeout(Duration.ofSeconds(5))
                .block();
                
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'utilisateur {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Classe interne pour représenter les infos d'un utilisateur
     * Correspond au nouveau DTO optimisé du User Service
     */
    public static class UserInfo {
        private String id;
        private String name;
        private String email;
        private String role;
        
        // Constructeurs
        public UserInfo() {}
        
        public UserInfo(String id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
        
        // Getters et Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        @Override
        public String toString() {
            return "UserInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}
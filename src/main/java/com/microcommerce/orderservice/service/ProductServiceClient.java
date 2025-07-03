package com.microcommerce.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Client pour communiquer avec le Product Service
 * 
 * Utilise WebClient pour faire des appels HTTP vers le service produit
 * Gère les timeouts et les erreurs de communication
 */
@Service
public class ProductServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClient.class);
    
    private final WebClient webClient;
    
    @Value("${services.product-service.url:http://localhost:8081}")
    private String productServiceUrl;
    
    public ProductServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Récupère les informations d'un produit via l'endpoint interne optimisé
     * 
     * @param productId l'ID du produit
     * @return les infos du produit ou null si introuvable
     */
    public ProductInfo getProductInfo(String productId) {
        logger.info("Récupération des infos du produit: {}", productId);
        
        try {
            return webClient.get()
                .uri(productServiceUrl + "/api/products/internal/{id}", productId)
                .retrieve()
                .bodyToMono(ProductInfo.class)
                .timeout(Duration.ofSeconds(5)) // Timeout de 5 secondes
                .block(); // Bloquant pour simplifier (en prod, utiliser reactive)
                
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du produit {}: {}", productId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vérifie si un produit existe
     * 
     * @param productId l'ID du produit
     * @return true si le produit existe
     */
    public boolean productExists(String productId) {
        logger.info("Vérification de l'existence du produit: {}", productId);
        
        try {
            return webClient.get()
                .uri(productServiceUrl + "/api/products/{id}", productId)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(3))
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false)
                .block();
                
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du produit {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie la disponibilité d'un produit en stock
     * 
     * @param productId l'ID du produit
     * @param quantity la quantité souhaitée
     * @return true si la quantité est disponible
     */
    public boolean checkProductAvailability(String productId, int quantity) {
        logger.info("Vérification de la disponibilité du produit {} pour {} unités", productId, quantity);
        
        try {
            ProductInfo product = getProductInfo(productId);
            return product != null && product.getStock() >= quantity;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de disponibilité: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Classe interne pour représenter les infos d'un produit
     * Correspond au nouveau DTO optimisé du Product Service
     */
    public static class ProductInfo {
        private String id;
        private String name;
        private BigDecimal price;
        private String category;
        private Integer stock;
        private boolean available;
        
        // Constructeurs
        public ProductInfo() {}
        
        public ProductInfo(String id, String name, BigDecimal price, String category, Integer stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
            this.stock = stock;
            this.available = stock != null && stock > 0;
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
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public Integer getStock() {
            return stock;
        }
        
        public void setStock(Integer stock) {
            this.stock = stock;
            this.available = stock != null && stock > 0;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public void setAvailable(boolean available) {
            this.available = available;
        }
        
        @Override
        public String toString() {
            return "ProductInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", price=" + price +
                    ", category='" + category + '\'' +
                    ", stock=" + stock +
                    ", available=" + available +
                    '}';
        }
    }
}
package com.microcommerce.orderservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * Entité qui représente une commande dans le système
 * 
 * Une commande c'est :
 * - Les infos de base (id, userId, statut, dates)
 * - Une liste d'items commandés
 * - Les totaux calculés automatiquement
 * - Les infos de livraison
 */
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @NotBlank(message = "L'ID utilisateur est obligatoire")
    private String userId;
    
    @NotEmpty(message = "Une commande doit contenir au moins un item")
    private List<OrderItem> items;
    
    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;
    
    @DecimalMin(value = "0.0", message = "Le total ne peut pas être négatif")
    private BigDecimal totalAmount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Infos de livraison (adresse, ville, etc.)
    private String shippingAddress;
    private String shippingCity;
    private String shippingZipCode;
    private String shippingCountry;
    
    // Notes optionnelles du client
    private String notes;
    
    // Constructeurs
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    public Order(String userId, List<OrderItem> items) {
        this();
        this.userId = userId;
        this.items = items;
        calculateTotal();
    }
    
    // Méthode qui calcule le total automatiquement
    public void calculateTotal() {
        if (items != null && !items.isEmpty()) {
            this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal(); // On recalcule automatiquement le total
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getShippingCity() {
        return shippingCity;
    }
    
    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }
    
    public String getShippingZipCode() {
        return shippingZipCode;
    }
    
    public void setShippingZipCode(String shippingZipCode) {
        this.shippingZipCode = shippingZipCode;
    }
    
    public String getShippingCountry() {
        return shippingCountry;
    }
    
    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
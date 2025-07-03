package com.microcommerce.orderservice.event;

import com.microcommerce.orderservice.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Événement commande pour la synchronisation entre services
 * 
 * Quand une commande est créée, modifiée ou supprimée,
 * on envoie cet event pour que les autres services se mettent à jour.
 */
public class OrderEvent {
    
    private String orderId;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private EventType eventType;
    private LocalDateTime timestamp;
    
    // Types d'événements possibles
    public enum EventType {
        CREATED,        // Nouvelle commande
        STATUS_UPDATED, // Statut modifié
        CANCELLED,      // Commande annulée
        DELETED         // Commande supprimée
    }
    
    // Classe interne pour les items de commande
    public static class OrderItemEvent {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        
        public OrderItemEvent() {}
        
        public OrderItemEvent(String productId, String productName, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        // Getters et setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
    
    // Constructeur par défaut pour Jackson
    public OrderEvent() {
    }
    
    public OrderEvent(String orderId, String userId, OrderStatus status, 
                     BigDecimal totalAmount, List<OrderItemEvent> items, EventType eventType) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters et setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public List<OrderItemEvent> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemEvent> items) {
        this.items = items;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                '}';
    }
}
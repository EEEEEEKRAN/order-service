package com.microcommerce.orderservice.event;

import java.time.LocalDateTime;

public class UserEvent {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String eventType; // USER_CREATED, USER_UPDATED, USER_DELETED
    private LocalDateTime timestamp;
    
    // Constructeurs
    public UserEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public UserEvent(Long userId, String name, String email, String role, String eventType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
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
        return "UserEvent{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
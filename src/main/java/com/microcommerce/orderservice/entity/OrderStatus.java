package com.microcommerce.orderservice.entity;

/**
 * Énumération des statuts possibles pour une commande
 * 
 * Le cycle de vie d'une commande :
 * PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
 *         -> CANCELLED (possible à tout moment avant SHIPPED)
 */
public enum OrderStatus {
    
    /**
     * Commande créée mais pas encore confirmée
     * État initial par défaut
     */
    PENDING("En attente"),
    
    /**
     * Commande confirmée par le client
     * Prête à être traitée
     */
    CONFIRMED("Confirmée"),
    
    /**
     * Commande en cours de préparation
     * Les produits sont en cours de préparation
     */
    PROCESSING("En préparation"),
    
    /**
     * Commande expédiée
     * En route vers le client
     */
    SHIPPED("Expédiée"),
    
    /**
     * Commande livrée
     * État final positif
     */
    DELIVERED("Livrée"),
    
    /**
     * Commande annulée
     * État final négatif
     */
    CANCELLED("Annulée");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Vérifie si le statut peut être modifié vers un nouveau statut
     * 
     * @param newStatus le nouveau statut souhaité
     * @return true si la transition est autorisée
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED:
                return newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING:
                return newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // États finaux, pas de transition possible
            default:
                return false;
        }
    }
    
    /**
     * Vérifie si le statut est un état final
     * 
     * @return true si c'est un état final (DELIVERED ou CANCELLED)
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED;
    }
    
    /**
     * Vérifie si la commande peut encore être annulée
     * 
     * @return true si l'annulation est possible
     */
    public boolean canBeCancelled() {
        return this != SHIPPED && this != DELIVERED && this != CANCELLED;
    }
}
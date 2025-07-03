package com.microcommerce.orderservice.repository;

import com.microcommerce.orderservice.entity.Order;
import com.microcommerce.orderservice.entity.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour les opérations CRUD sur les commandes
 * 
 * Fournit les méthodes de base + des requêtes personnalisées
 * pour rechercher les commandes selon différents critères
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    /**
     * Trouve toutes les commandes d'un utilisateur
     * Triées par date de création décroissante (plus récentes en premier)
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Trouve les commandes par statut
     * Utile pour les tableaux de bord admin
     */
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    
    /**
     * Trouve les commandes d'un utilisateur avec un statut spécifique
     */
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);
    
    /**
     * Trouve les commandes créées dans une période donnée
     * Pratique pour les rapports et statistiques
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Trouve les commandes d'un utilisateur dans une période
     */
    List<Order> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Compte le nombre de commandes par statut
     * Utile pour les statistiques rapides
     */
    long countByStatus(OrderStatus status);
    
    /**
     * Compte les commandes d'un utilisateur
     */
    long countByUserId(String userId);
    
    /**
     * Trouve les commandes contenant un produit spécifique
     * Utilise une requête MongoDB pour chercher dans le tableau items
     */
    @Query("{'items.productId': ?0}")
    List<Order> findByProductId(String productId);
    
    /**
     * Trouve les commandes récentes (dernières 24h)
     * Pratique pour le monitoring en temps réel
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<Order> findRecentOrders(LocalDateTime since);
    
    /**
     * Trouve les commandes avec un montant minimum
     * Pour identifier les grosses commandes
     */
    @Query("{'totalAmount': {$gte: ?0}}")
    List<Order> findOrdersWithMinAmount(double minAmount);
    
    /**
     * Trouve les commandes en attente depuis plus de X heures
     * Pour identifier les commandes qui traînent
     */
    @Query("{'status': 'PENDING', 'createdAt': {$lt: ?0}}")
    List<Order> findPendingOrdersOlderThan(LocalDateTime cutoffDate);
}
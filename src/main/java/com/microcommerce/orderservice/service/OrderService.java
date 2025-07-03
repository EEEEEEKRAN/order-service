package com.microcommerce.orderservice.service;

import com.microcommerce.orderservice.entity.Order;
import com.microcommerce.orderservice.entity.OrderItem;
import com.microcommerce.orderservice.entity.OrderStatus;
import com.microcommerce.orderservice.repository.OrderRepository;
import com.microcommerce.orderservice.event.OrderEvent;
import com.microcommerce.orderservice.service.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service principal pour gérer les commandes
 * 
 * Contient toute la logique métier :
 * - Création et validation des commandes
 * - Gestion des transitions de statut
 * - Calculs des totaux
 * - Recherches et filtres
 */
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private OrderEventPublisher orderEventPublisher;
    
    /**
     * Crée une nouvelle commande
     * Valide les produits et calcule le total automatiquement
     */
    public Order createOrder(Order order) {
        logger.info("Création d'une nouvelle commande pour l'utilisateur: {}", order.getUserId());
        
        try {
            // On vérifie que l'utilisateur existe
            if (!userServiceClient.userExists(order.getUserId())) {
                throw new RuntimeException("Utilisateur introuvable: " + order.getUserId());
            }
            
            // On valide et enrichit les items avec les infos produits
            validateAndEnrichOrderItems(order);
            
            // On calcule le total
            order.calculateTotal();
            
            // On sauvegarde
            Order savedOrder = orderRepository.save(order);
            logger.info("Commande créée avec succès: {}", savedOrder.getId());
            
            // Publier l'événement de création de commande
            orderEventPublisher.publishOrderCreated(savedOrder);
            
            return savedOrder;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la commande: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer la commande: " + e.getMessage());
        }
    }
    
    /**
     * Récupère toutes les commandes
     */
    public List<Order> getAllOrders() {
        logger.info("Récupération de toutes les commandes");
        return orderRepository.findAll();
    }
    
    /**
     * Chope une commande par son ID
     */
    public Optional<Order> getOrderById(String id) {
        logger.info("Recherche de la commande: {}", id);
        return orderRepository.findById(id);
    }
    
    /**
     * Récupère toutes les commandes d'un utilisateur
     */
    public List<Order> getOrdersByUserId(String userId) {
        logger.info("Récupération des commandes pour l'utilisateur: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Récupère les commandes par statut
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        logger.info("Récupération des commandes avec le statut: {}", status);
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Met à jour le statut d'une commande
     * Vérifie que la transition est autorisée
     */
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        logger.info("Mise à jour du statut de la commande {} vers {}", orderId, newStatus);
        
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Commande introuvable: " + orderId);
        }
        
        Order order = optionalOrder.get();
        
        // On vérifie que la transition est autorisée
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new RuntimeException(
                String.format("Transition non autorisée de %s vers %s", 
                    order.getStatus(), newStatus)
            );
        }
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Statut mis à jour avec succès pour la commande: {}", orderId);
        
        // Publier l'événement de mise à jour de statut
        orderEventPublisher.publishOrderStatusUpdated(updatedOrder);
        
        return updatedOrder;
    }
    
    /**
     * Annule une commande si c'est possible
     */
    public Order cancelOrder(String orderId) {
        logger.info("Tentative d'annulation de la commande: {}", orderId);
        
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Commande introuvable: " + orderId);
        }
        
        Order order = optionalOrder.get();
        
        if (!order.getStatus().canBeCancelled()) {
            throw new RuntimeException(
                "Impossible d'annuler une commande avec le statut: " + order.getStatus()
            );
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        
        logger.info("Commande annulée avec succès: {}", orderId);
        
        // Publier l'événement d'annulation de commande
        orderEventPublisher.publishOrderCancelled(cancelledOrder);
        
        return cancelledOrder;
    }
    
    /**
     * Supprime une commande (admin seulement)
     */
    public void deleteOrder(String orderId) {
        logger.info("Suppression de la commande: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + orderId));
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Impossible de supprimer une commande déjà livrée");
        }
        
        orderRepository.deleteById(orderId);
        
        logger.info("Commande {} supprimée avec succès", orderId);
        
        // Publier l'événement de suppression de commande
        orderEventPublisher.publishOrderDeleted(order.getId());
    }
    
    /**
     * Recherche les commandes dans une période donnée
     */
    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Recherche des commandes entre {} et {}", startDate, endDate);
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    /**
     * Récupère les stats des commandes
     */
    public OrderStats getOrderStats() {
        logger.info("Calcul des statistiques des commandes");
        
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long processingOrders = orderRepository.countByStatus(OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        return new OrderStats(totalOrders, pendingOrders, confirmedOrders, 
                            processingOrders, shippedOrders, deliveredOrders, cancelledOrders);
    }
    
    /**
     * Trouve les commandes qui contiennent un produit spécifique
     */
    public List<Order> getOrdersByProductId(String productId) {
        logger.info("Recherche des commandes contenant le produit: {}", productId);
        return orderRepository.findByProductId(productId);
    }
    
    /**
     * Valide les articles d'une commande
     */
    public void validateOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins un article");
        }
        
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La quantité doit être positive pour le produit: " + item.getProductId());
            }
            if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif pour le produit: " + item.getProductId());
            }
        }
    }
    
    /**
     * Calcule le montant total d'une commande
     */
    public void calculateTotalAmount(Order order) {
        BigDecimal total = order.getItems().stream()
            .map(OrderItem::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(total);
    }
    
    /**
     * Valide et enrichit les items d'une commande avec les infos produits
     */
    private void validateAndEnrichOrderItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Une commande doit contenir au moins un item");
        }
        
        // Pour chaque item, on vérifie que le produit existe et on récupère ses infos
        order.getItems().forEach(item -> {
            try {
                // Appel au Product Service pour vérifier l'existence et récupérer les infos
                var productInfo = productServiceClient.getProductInfo(item.getProductId());
                
                if (productInfo == null) {
                    throw new RuntimeException("Produit introuvable: " + item.getProductId());
                }
                
                // On enrichit l'item avec les infos du produit
                item.setProductName(productInfo.getName());
                // Note: description supprimée du DTO optimisé pour la performance
                item.setProductCategory(productInfo.getCategory());
                
                // On utilise le prix actuel du produit si pas spécifié
                if (item.getPrice() == null) {
                    item.setPrice(productInfo.getPrice());
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de la validation du produit {}: {}", 
                           item.getProductId(), e.getMessage());
                throw new RuntimeException("Produit invalide: " + item.getProductId());
            }
        });
    }
    
    /**
     * Classe interne pour les statistiques
     */
    public static class OrderStats {
        private final long totalOrders;
        private final long pendingOrders;
        private final long confirmedOrders;
        private final long processingOrders;
        private final long shippedOrders;
        private final long deliveredOrders;
        private final long cancelledOrders;
        
        public OrderStats(long totalOrders, long pendingOrders, long confirmedOrders,
                         long processingOrders, long shippedOrders, long deliveredOrders,
                         long cancelledOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.confirmedOrders = confirmedOrders;
            this.processingOrders = processingOrders;
            this.shippedOrders = shippedOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
        }
        
        // Getters
        public long getTotalOrders() { return totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public long getConfirmedOrders() { return confirmedOrders; }
        public long getProcessingOrders() { return processingOrders; }
        public long getShippedOrders() { return shippedOrders; }
        public long getDeliveredOrders() { return deliveredOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
    }
}
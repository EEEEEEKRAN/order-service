package com.microcommerce.orderservice.controller;

import com.microcommerce.orderservice.entity.Order;
import com.microcommerce.orderservice.entity.OrderStatus;
import com.microcommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les commandes
 * 
 * Expose toutes les APIs qu'il faut pour :
 * - CRUD des commandes
 * - Gestion des statuts
 * - Recherches et filtres
 * - Statistiques
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // En prod, on spécifiera les domaines autorisés
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Crée une nouvelle commande
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
        logger.info("Demande de création de commande pour l'utilisateur: {}", order.getUserId());
        
        try {
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la commande: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de création", "message", e.getMessage()));
        }
    }
    
    /**
     * Récupère toutes les commandes
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        logger.info("Récupération de toutes les commandes");
        
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des commandes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Chope une commande par son ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        logger.info("Récupération de la commande: {}", id);
        
        try {
            Optional<Order> order = orderService.getOrderById(id);
            
            if (order.isPresent()) {
                return ResponseEntity.ok(order.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la commande {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }
    
    /**
     * Récupère les commandes d'un utilisateur
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        logger.info("Récupération des commandes pour l'utilisateur: {}", userId);
        
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des commandes utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère les commandes par statut
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        logger.info("Récupération des commandes avec le statut: {}", status);
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(orders);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Statut invalide", "message", "Statut non reconnu: " + status));
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération par statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Met à jour le statut d'une commande
     * PUT /api/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String id, 
            @RequestBody Map<String, String> statusUpdate) {
        
        logger.info("Mise à jour du statut de la commande: {}", id);
        
        try {
            String newStatusStr = statusUpdate.get("status");
            if (newStatusStr == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Statut manquant", "message", "Le champ 'status' est requis"));
            }
            
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
            
            return ResponseEntity.ok(updatedOrder);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Statut invalide", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de mise à jour", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }
    
    /**
     * Annule une commande
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String id) {
        logger.info("Annulation de la commande: {}", id);
        
        try {
            Order cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(cancelledOrder);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur d'annulation", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }
    
    /**
     * Supprime une commande (admin seulement)
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable String id) {
        logger.info("Suppression de la commande: {}", id);
        
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(Map.of("message", "Commande supprimée avec succès"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de suppression", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }
    
    /**
     * Recherche les commandes dans une période donnée
     * GET /api/orders/search/period?start=...&end=...
     */
    @GetMapping("/search/period")
    public ResponseEntity<?> getOrdersBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        logger.info("Recherche des commandes entre {} et {}", start, end);
        
        try {
            List<Order> orders = orderService.getOrdersBetweenDates(start, end);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par période: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur de recherche", "message", e.getMessage()));
        }
    }
    
    /**
     * Récupère les stats des commandes
     * GET /api/orders/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getOrderStats() {
        logger.info("Récupération des statistiques des commandes");
        
        try {
            OrderService.OrderStats stats = orderService.getOrderStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur de calcul", "message", e.getMessage()));
        }
    }
    
    /**
     * Trouve les commandes qui contiennent un produit spécifique
     * GET /api/orders/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Order>> getOrdersByProductId(@PathVariable String productId) {
        logger.info("Recherche des commandes contenant le produit: {}", productId);
        
        try {
            List<Order> orders = orderService.getOrdersByProductId(productId);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par produit: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint de test pour vérifier que le service tourne bien
     * GET /api/orders/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        logger.info("Test de l'API Order Service");
        
        return ResponseEntity.ok(Map.of(
            "service", "Order Service",
            "status", "OK",
            "timestamp", LocalDateTime.now(),
            "message", "Service de commandes opérationnel ! 🛒"
        ));
    }
}
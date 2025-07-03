package com.microcommerce.orderservice.listener;

import com.microcommerce.orderservice.config.RabbitMQConfig;
import com.microcommerce.orderservice.event.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener pour les événements produits dans le Order Service
 * 
 * Écoute les changements de produits pour :
 * - Valider la disponibilité lors des commandes
 * - Mettre à jour les prix en cache
 * - Gérer les stocks et les réservations
 */
@Component
public class ProductEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);
    
    /**
     * Écoute tous les événements produits sur la queue order-service.product.queue
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_SERVICE_PRODUCT_QUEUE)
    public void handleProductEvent(ProductEvent productEvent) {
        logger.info("Événement produit reçu dans Order Service: {}", productEvent);
        
        try {
            switch (productEvent.getEventType()) {
                case CREATED:
                    handleProductCreated(productEvent);
                    break;
                case UPDATED:
                    handleProductUpdated(productEvent);
                    break;
                case DELETED:
                    handleProductDeleted(productEvent);
                    break;
                default:
                    logger.warn("Type d'événement produit non géré: {}", productEvent.getEventType());
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement produit: {}", productEvent, e);
            // En production, on pourrait implémenter un retry ou envoyer vers une DLQ
        }
    }
    
    /**
     * Gère la création d'un nouveau produit
     */
    private void handleProductCreated(ProductEvent productEvent) {
        logger.info("Nouveau produit disponible pour les commandes: {} (ID: {})", 
                   productEvent.getName(), productEvent.getProductId());
        
        // TODO: Implémenter la logique métier
        // - Ajouter le produit au cache local pour les validations rapides
        // - Mettre à jour les recommandations de produits
        // - Notifier les services de recommandation
    }
    
    /**
     * Gère la mise à jour d'un produit (prix, stock, etc.)
     */
    private void handleProductUpdated(ProductEvent productEvent) {
        logger.info("Produit mis à jour: {} (ID: {}) - Stock: {}, Prix: {}", 
                   productEvent.getName(), productEvent.getProductId(), 
                   productEvent.getStock(), productEvent.getPrice());
        
        // TODO: Implémenter la logique métier
        // - Mettre à jour le cache local avec les nouvelles infos
        // - Vérifier les commandes en cours avec ce produit
        // - Ajuster les prix des paniers en cours si nécessaire
        // - Alerter si le stock devient insuffisant pour les commandes en attente
        
        if (productEvent.getStock() != null && productEvent.getStock() <= 5) {
            logger.warn("⚠️ Stock faible pour le produit {}: {} unités restantes", 
                       productEvent.getName(), productEvent.getStock());
        }
    }
    
    /**
     * Gère la suppression d'un produit
     */
    private void handleProductDeleted(ProductEvent productEvent) {
        logger.info("Produit supprimé: {} (ID: {})", 
                   productEvent.getName(), productEvent.getProductId());
        
        // TODO: Implémenter la logique métier
        // - Supprimer le produit du cache local
        // - Annuler les commandes en cours contenant ce produit
        // - Notifier les clients concernés
        // - Mettre à jour les statistiques
        
        logger.warn("⚠️ Produit {} plus disponible pour de nouvelles commandes", 
                   productEvent.getName());
    }
}
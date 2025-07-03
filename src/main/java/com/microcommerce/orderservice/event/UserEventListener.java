package com.microcommerce.orderservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);
    
    @RabbitListener(queues = "order-service.user.queue")
    public void handleUserEvent(UserEvent userEvent) {
        logger.info("Événement utilisateur reçu dans order-service: {}", userEvent);
        
        try {
            switch (userEvent.getEventType()) {
                case "USER_CREATED":
                    handleUserCreated(userEvent);
                    break;
                case "USER_UPDATED":
                    handleUserUpdated(userEvent);
                    break;
                case "USER_DELETED":
                    handleUserDeleted(userEvent);
                    break;
                default:
                    logger.warn("Type d'événement utilisateur non géré: {}", userEvent.getEventType());
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement utilisateur: {}", userEvent, e);
        }
    }
    
    private void handleUserCreated(UserEvent userEvent) {
        logger.info("Utilisateur créé - ID: {}, Nom: {}, Email: {}", 
                   userEvent.getUserId(), userEvent.getName(), userEvent.getEmail());
        
        // Ici on pourrait mettre à jour un cache local des utilisateurs
        // ou effectuer d'autres actions nécessaires
    }
    
    private void handleUserUpdated(UserEvent userEvent) {
        logger.info("Utilisateur mis à jour - ID: {}, Nom: {}, Email: {}", 
                   userEvent.getUserId(), userEvent.getName(), userEvent.getEmail());
        
        // Ici on pourrait mettre à jour les informations utilisateur dans les commandes
        // ou invalider un cache
    }
    
    private void handleUserDeleted(UserEvent userEvent) {
        logger.info("Utilisateur supprimé - ID: {}", userEvent.getUserId());
        
        // Ici on pourrait marquer les commandes de cet utilisateur comme orphelines
        // ou effectuer d'autres actions de nettoyage
        logger.warn("Attention: L'utilisateur {} a été supprimé. Vérifiez les commandes associées.", 
                   userEvent.getUserId());
    }
}
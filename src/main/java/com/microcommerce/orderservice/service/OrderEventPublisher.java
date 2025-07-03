package com.microcommerce.orderservice.service;

import com.microcommerce.orderservice.entity.Order;
import com.microcommerce.orderservice.entity.OrderItem;
import com.microcommerce.orderservice.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour publier les événements commandes vers RabbitMQ
 * 
 * Chaque fois qu'une commande est créée, modifiée ou supprimée,
 * on envoie un event pour que les autres services se synchronisent.
 */
@Service
public class OrderEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);
    
    // Noms des exchanges et routing keys
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_STATUS_UPDATED_ROUTING_KEY = "order.status.updated";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String ORDER_DELETED_ROUTING_KEY = "order.deleted";
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * Publie un événement de création de commande
     */
    public void publishOrderCreated(Order order) {
        OrderEvent event = createOrderEvent(order, OrderEvent.EventType.CREATED);
        publishEvent(event, ORDER_CREATED_ROUTING_KEY);
        logger.info("Événement ORDER_CREATED publié pour la commande: {}", order.getId());
    }
    
    /**
     * Publie un événement de mise à jour de statut
     */
    public void publishOrderStatusUpdated(Order order) {
        OrderEvent event = createOrderEvent(order, OrderEvent.EventType.STATUS_UPDATED);
        publishEvent(event, ORDER_STATUS_UPDATED_ROUTING_KEY);
        logger.info("Événement ORDER_STATUS_UPDATED publié pour la commande: {} (nouveau statut: {})", 
                   order.getId(), order.getStatus());
    }
    
    /**
     * Publie un événement d'annulation de commande
     */
    public void publishOrderCancelled(Order order) {
        OrderEvent event = createOrderEvent(order, OrderEvent.EventType.CANCELLED);
        publishEvent(event, ORDER_CANCELLED_ROUTING_KEY);
        logger.info("Événement ORDER_CANCELLED publié pour la commande: {}", order.getId());
    }
    
    /**
     * Publie un événement de suppression de commande
     */
    public void publishOrderDeleted(String orderId) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setEventType(OrderEvent.EventType.DELETED);
        publishEvent(event, ORDER_DELETED_ROUTING_KEY);
        logger.info("Événement ORDER_DELETED publié pour la commande: {}", orderId);
    }
    
    /**
     * Crée un événement commande à partir d'un objet Order
     */
    private OrderEvent createOrderEvent(Order order, OrderEvent.EventType eventType) {
        // Conversion des items de commande
        List<OrderEvent.OrderItemEvent> itemEvents = order.getItems().stream()
            .map(this::convertToOrderItemEvent)
            .collect(Collectors.toList());
        
        return new OrderEvent(
            order.getId(),
            order.getUserId(),
            order.getStatus(),
            order.getTotalAmount(),
            itemEvents,
            eventType
        );
    }
    
    /**
     * Convertit un OrderItem en OrderItemEvent
     */
    private OrderEvent.OrderItemEvent convertToOrderItemEvent(OrderItem item) {
        return new OrderEvent.OrderItemEvent(
            item.getProductId(),
            item.getProductName(),
            item.getQuantity(),
            item.getPrice()
        );
    }
    
    /**
     * Envoie l'événement vers RabbitMQ
     */
    private void publishEvent(OrderEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                ORDER_EXCHANGE,
                routingKey,
                event
            );
            logger.debug("Événement commande envoyé avec succès: {}", event);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'événement commande: {}", event, e);
            // En production, on pourrait implémenter un retry ou stocker l'event pour retry plus tard
        }
    }
}
package com.microcommerce.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le Order Service
 * 
 * On écoute les événements produits pour :
 * - Valider la disponibilité des produits
 * - Mettre à jour les prix en temps réel
 * - Gérer les stocks lors des commandes
 */
@Configuration
public class RabbitMQConfig {

    // Noms des exchanges et queues (doivent correspondre au product-service)
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_UPDATED_ROUTING_KEY = "product.updated";
    public static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";
    
    // Queue spécifique au order-service pour les événements produits
    public static final String ORDER_SERVICE_PRODUCT_QUEUE = "order-service.product.queue";
    
    // Configuration pour les événements commandes
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_STATUS_UPDATED_ROUTING_KEY = "order.status.updated";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String ORDER_DELETED_ROUTING_KEY = "order.deleted";
    
    // Routing keys pour écouter tous les événements produits
    public static final String PRODUCT_ALL_ROUTING_KEY = "product.*";

    /**
     * Exchange pour les événements produits (doit exister déjà via product-service)
     */
    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    /**
     * Queue spécifique au order-service pour recevoir les événements produits
     */
    @Bean
    public Queue orderServiceProductQueue() {
        return QueueBuilder.durable(ORDER_SERVICE_PRODUCT_QUEUE).build();
    }

    /**
     * Binding pour écouter tous les événements produits
     */
    @Bean
    public Binding orderServiceProductBinding() {
        return BindingBuilder
            .bind(orderServiceProductQueue())
            .to(productExchange())
            .with(PRODUCT_ALL_ROUTING_KEY);
    }
    
    // Configuration pour les événements commandes
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    // Configuration pour écouter les événements utilisateurs
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_ALL_ROUTING_KEY = "user.*";
    public static final String ORDER_SERVICE_USER_QUEUE = "order-service.user.queue";
    
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }
    
    @Bean
    public Queue orderServiceUserQueue() {
        return new Queue(ORDER_SERVICE_USER_QUEUE, true);
    }
    
    @Bean
    public Binding orderServiceUserBinding() {
        return BindingBuilder
            .bind(orderServiceUserQueue())
            .to(userExchange())
            .with(USER_ALL_ROUTING_KEY);
    }

    /**
     * Convertisseur JSON pour sérialiser/désérialiser les messages
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template RabbitMQ avec convertisseur JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
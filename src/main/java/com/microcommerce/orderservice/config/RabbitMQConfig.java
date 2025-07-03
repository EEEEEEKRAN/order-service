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
    public static final String ORDER_SERVICE_PRODUCT_QUEUE = "order-service.product.queue";
    
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
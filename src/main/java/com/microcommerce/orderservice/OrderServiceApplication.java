package com.microcommerce.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application principale du service de commandes
 * 
 * Ce service gère toute la logique métier liée aux commandes :
 * - Création et gestion des commandes
 * - Liaison entre utilisateurs et produits
 * - Calcul des totaux et gestion des statuts
 * - Communication avec les autres microservices
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
package com.microcommerce.orderservice.entity;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Représente un item dans une commande
 * 
 * Chaque item contient :
 * - L'ID du produit (référence vers le Product Service)
 * - Le nom du produit (copie pour éviter les appels)
 * - La quantité commandée
 * - Le prix unitaire au moment de la commande
 */
public class OrderItem {
    
    @NotBlank(message = "L'ID du produit est obligatoire")
    private String productId;
    
    @NotBlank(message = "Le nom du produit est obligatoire")
    private String productName;
    
    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private int quantity;
    
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    @NotNull(message = "Le prix est obligatoire")
    private BigDecimal price;
    
    // Infos optionnelles du produit (snapshot au moment de la commande)
    private String productDescription;
    private String productCategory;
    
    // Constructeurs
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Méthode utilitaire pour calculer le sous-total de cet item
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters et Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public String getProductCategory() {
        return productCategory;
    }
    
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }
}
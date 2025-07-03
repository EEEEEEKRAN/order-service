# Order Service - Gestion des commandes

Le service qui orchestre tout : commandes, paiements, livraisons... C'est lui qui fait le lien entre les utilisateurs et les produits pour créer des commandes qui tiennent la route !

## C'que ça fait

### Fonctionnalités principales

- **Création de commandes** : Validation, calcul des totaux
- **Gestion du cycle de vie** : Pending → Confirmed → Shipped → Delivered
- **Communication inter-services** : Vérifie les utilisateurs et produits
- **Calculs automatiques** : Prix total, taxes, frais de port
- **Historique** : Suivi complet des commandes
- **Statistiques** : Analyses des ventes

### Endpoints disponibles

#### Publics
- `GET /api/orders/test` - Test que le service tourne

#### Protégés (auth requise)
- `POST /api/orders` - Crée une nouvelle commande
- `GET /api/orders` - Liste toutes les commandes (admin)
- `GET /api/orders/{id}` - Récupère une commande par ID
- `GET /api/orders/user/{userId}` - Commandes d'un utilisateur
- `GET /api/orders/status/{status}` - Commandes par statut
- `PUT /api/orders/{id}/status` - Met à jour le statut
- `PUT /api/orders/{id}/cancel` - Annule une commande
- `DELETE /api/orders/{id}` - Supprime une commande
- `GET /api/orders/search` - Recherche par période
- `GET /api/orders/stats` - Statistiques des commandes
- `GET /api/orders/product/{productId}` - Commandes contenant un produit

#### Internes (pour les autres services)
- `GET /internal/orders/user/{userId}` - Commandes utilisateur allégées
- `GET /internal/orders/stats` - Stats pour le dashboard

## Stack technique

- **Spring Boot 3.2** : Framework principal
- **MongoDB** : Base de données
- **Spring Data MongoDB** : ORM pour MongoDB
- **WebClient** : Communication avec user-service et product-service
- **Validation API** : Validation des données
- **Jackson** : Sérialisation JSON
- **Spring Security** : Authentification

## Comment lancer ?

### Prérequis
- Java 17+
- Maven
- MongoDB qui tourne (port 27017)
- **user-service** et **product-service** qui tournent

### Lancement

```bash
# Depuis le dossier order-service
mvn spring-boot:run
```

Le service démarre sur le port **8081**.

### Avec Docker

```bash
# Build l'image
docker build -t order-service .

# Run le container
docker run -p 8081:8081 order-service
```

## Configuration

### Variables d'environnement

- `MONGODB_URI` : URI de connexion MongoDB (défaut: mongodb://localhost:27017/orderdb)
- `USER_SERVICE_URL` : URL du user-service (défaut: http://localhost:8082)
- `PRODUCT_SERVICE_URL` : URL du product-service (défaut: http://localhost:8080)
- `SERVER_PORT` : Port du service (défaut: 8081)

### Services externes

Le service a besoin de :
- **user-service** : Pour valider les utilisateurs
- **product-service** : Pour récupérer les infos produits et gérer le stock

## Modèle de données

### Order
```json
{
  "id": "string",
  "userId": "string",
  "items": [
    {
      "productId": "string",
      "productName": "string",
      "quantity": "number",
      "unitPrice": "number",
      "totalPrice": "number"
    }
  ],
  "status": "PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED",
  "totalAmount": "number",
  "shippingAddress": "string",
  "shippingCity": "string",
  "shippingZipCode": "string",
  "shippingCountry": "string",
  "notes": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Exemples d'utilisation

### Créer une commande
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ton-jwt-token" \
  -d '{
    "userId": "user-id-123",
    "items": [
      {
        "productId": "product-id-456",
        "quantity": 2
      },
      {
        "productId": "product-id-789",
        "quantity": 1
      }
    ],
    "shippingAddress": "123 Rue de la Paix",
    "shippingCity": "Paris",
    "shippingZipCode": "75001",
    "shippingCountry": "France",
    "notes": "Livraison en point relais SVP"
  }'
```

### Récupérer ses commandes
```bash
curl -H "Authorization: Bearer ton-jwt-token" \
  http://localhost:8081/api/orders/user/ton-user-id
```

### Mettre à jour le statut
```bash
curl -X PUT http://localhost:8081/api/orders/order-id-123/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ton-jwt-token" \
  -d '{"status": "SHIPPED"}'
```

### Annuler une commande
```bash
curl -X PUT http://localhost:8081/api/orders/order-id-123/cancel \
  -H "Authorization: Bearer ton-jwt-token"
```

## Logique métier

### Création d'une commande
1. **Validation utilisateur** : Vérification via user-service
2. **Validation produits** : Vérification existence et stock via product-service
3. **Enrichissement** : Récupération des noms et prix actuels
4. **Calcul total** : Somme automatique des items
5. **Sauvegarde** : Création en base avec statut PENDING

### Gestion des statuts
- **PENDING** : Commande créée, en attente de confirmation
- **CONFIRMED** : Commande validée, prête pour préparation
- **SHIPPED** : Commande expédiée
- **DELIVERED** : Commande livrée
- **CANCELLED** : Commande annulée

### Règles de validation
- Un utilisateur doit exister
- Tous les produits doivent exister
- Le stock doit être suffisant
- Les quantités doivent être positives
- L'adresse de livraison est obligatoire

## Communication inter-services

### Avec user-service
- `GET /internal/users/{id}` : Validation d'existence
- Timeout : 5 secondes
- Retry : 3 tentatives

### Avec product-service
- `GET /internal/products/{id}` : Infos produit
- `POST /internal/products/batch` : Infos multiples produits
- `PUT /internal/products/{id}/reserve-stock` : Réservation stock
- Timeout : 5 secondes
- Retry : 3 tentatives

## Statistiques disponibles

- **Nombre total de commandes**
- **Chiffre d'affaires total**
- **Commandes par statut**
- **Commandes par période**
- **Produits les plus commandés**
- **Utilisateurs les plus actifs**

## Logs et monitoring

- Logs structurés avec Logback
- Métriques Spring Boot Actuator
- Health check sur `/actuator/health`
- Métriques custom : commandes/minute, CA, erreurs inter-services

## Problèmes courants

**Service ne démarre pas ?**
→ Vérifie que MongoDB tourne et que le port 8081 est libre

**Erreur lors de la création de commande ?**
→ Check que user-service et product-service sont accessibles

**Utilisateur non trouvé ?**
→ Vérifie que l'utilisateur existe dans user-service

**Produit non trouvé ?**
→ Vérifie que le produit existe dans product-service

**Stock insuffisant ?**
→ Normal, le service empêche les commandes sans stock

**Timeout inter-services ?**
→ Vérifie la connectivité réseau et que les autres services répondent

---
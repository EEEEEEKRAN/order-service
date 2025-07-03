// Script d'initialisation MongoDB pour Order Service
// Ce script est exécuté automatiquement lors du premier démarrage du conteneur MongoDB

print('Initialisation de la base de données Order Service...');

// Sélectionne la base de données
db = db.getSiblingDB('orderservice_db');

// Crée un utilisateur pour l'application
db.createUser({
  user: 'orderservice_user',
  pwd: 'orderservice_password',
  roles: [
    {
      role: 'readWrite',
      db: 'orderservice_db'
    }
  ]
});

print('Utilisateur orderservice_user créé avec succès.');

// Crée la collection orders avec validation de schéma
db.createCollection('orders', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'items', 'status', 'totalAmount', 'createdAt'],
      properties: {
        userId: {
          bsonType: 'string',
          description: 'ID de l\'utilisateur - requis'
        },
        items: {
          bsonType: 'array',
          minItems: 1,
          description: 'Liste des articles - requis et non vide',
          items: {
            bsonType: 'object',
            required: ['productId', 'productName', 'quantity', 'unitPrice'],
            properties: {
              productId: {
                bsonType: 'string',
                description: 'ID du produit - requis'
              },
              productName: {
                bsonType: 'string',
                description: 'Nom du produit - requis'
              },
              quantity: {
                bsonType: 'int',
                minimum: 1,
                description: 'Quantité - requis et > 0'
              },
              unitPrice: {
                bsonType: 'double',
                minimum: 0,
                description: 'Prix unitaire - requis et >= 0'
              }
            }
          }
        },
        status: {
          enum: ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'],
          description: 'Statut de la commande - requis'
        },
        totalAmount: {
          bsonType: 'double',
          minimum: 0,
          description: 'Montant total - requis et >= 0'
        },
        createdAt: {
          bsonType: 'date',
          description: 'Date de création - requis'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'Date de mise à jour'
        }
      }
    }
  }
});

print('Collection orders créée avec validation de schéma.');

// Crée des index pour optimiser les performances
db.orders.createIndex({ 'userId': 1 });
db.orders.createIndex({ 'status': 1 });
db.orders.createIndex({ 'createdAt': -1 });
db.orders.createIndex({ 'userId': 1, 'status': 1 });
db.orders.createIndex({ 'items.productId': 1 });
db.orders.createIndex({ 'totalAmount': 1 });

print('Index créés pour optimiser les performances.');

// Insère quelques données de test
db.orders.insertMany([
  {
    userId: '507f1f77bcf86cd799439011',
    items: [
      {
        productId: '507f1f77bcf86cd799439021',
        productName: 'Laptop Dell XPS 13',
        quantity: 1,
        unitPrice: 1299.99,
        description: 'Ordinateur portable haute performance',
        category: 'Informatique'
      }
    ],
    status: 'DELIVERED',
    totalAmount: 1299.99,
    taxAmount: 259.99,
    shippingAmount: 0.0,
    shippingAddress: {
      street: '123 Rue de la Paix',
      city: 'Paris',
      postalCode: '75001',
      country: 'France'
    },
    notes: 'Livraison rapide demandée',
    createdAt: new Date('2024-01-15T10:30:00Z'),
    updatedAt: new Date('2024-01-18T14:20:00Z')
  },
  {
    userId: '507f1f77bcf86cd799439012',
    items: [
      {
        productId: '507f1f77bcf86cd799439022',
        productName: 'iPhone 15 Pro',
        quantity: 1,
        unitPrice: 1199.99,
        description: 'Smartphone dernière génération',
        category: 'Téléphonie'
      },
      {
        productId: '507f1f77bcf86cd799439023',
        productName: 'Coque iPhone 15 Pro',
        quantity: 1,
        unitPrice: 29.99,
        description: 'Protection en silicone',
        category: 'Accessoires'
      }
    ],
    status: 'PROCESSING',
    totalAmount: 1229.98,
    taxAmount: 245.99,
    shippingAmount: 9.99,
    shippingAddress: {
      street: '456 Avenue des Champs',
      city: 'Lyon',
      postalCode: '69001',
      country: 'France'
    },
    notes: 'Commande groupée',
    createdAt: new Date('2024-01-20T09:15:00Z'),
    updatedAt: new Date('2024-01-20T16:45:00Z')
  },
  {
    userId: '507f1f77bcf86cd799439013',
    items: [
      {
        productId: '507f1f77bcf86cd799439024',
        productName: 'Casque Sony WH-1000XM5',
        quantity: 2,
        unitPrice: 399.99,
        description: 'Casque à réduction de bruit',
        category: 'Audio'
      }
    ],
    status: 'PENDING',
    totalAmount: 799.98,
    taxAmount: 159.99,
    shippingAmount: 5.99,
    shippingAddress: {
      street: '789 Boulevard Saint-Germain',
      city: 'Marseille',
      postalCode: '13001',
      country: 'France'
    },
    notes: 'Cadeau - emballage spécial',
    createdAt: new Date('2024-01-22T14:30:00Z'),
    updatedAt: new Date('2024-01-22T14:30:00Z')
  }
]);

print('Données de test insérées.');
print('Initialisation de la base de données Order Service terminée avec succès!');

// Affiche les statistiques
print('Statistiques de la base de données :');
print('- Nombre de commandes : ' + db.orders.countDocuments());
print('- Index créés : ' + db.orders.getIndexes().length);
print('- Collections : ' + db.getCollectionNames().join(', '));
# Utilise l'image officielle OpenJDK 17
FROM openjdk:17-jdk-slim

# Métadonnées de l'image
LABEL maintainer="Micro Commerce Team"
LABEL description="Order Service pour l'application micro-commerce"
LABEL version="1.0.0"

# Variables d'environnement
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8083
ENV MONGODB_HOST=mongodb
ENV MONGODB_PORT=27017
ENV MONGODB_DATABASE=orderservice_db
ENV USER_SERVICE_URL=http://user-service:8082
ENV PRODUCT_SERVICE_URL=http://product-service:8081

# Crée un utilisateur non-root pour la sécurité
RUN groupadd -r orderservice && useradd -r -g orderservice orderservice

# Répertoire de travail
WORKDIR /app

# Copie le fichier JAR de l'application
COPY target/order-service-1.0.0.jar app.jar

# Change le propriétaire des fichiers
RUN chown -R orderservice:orderservice /app

# Utilise l'utilisateur non-root
USER orderservice

# Expose le port de l'application
EXPOSE 8083

# Point de santé pour Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

# Commande pour démarrer l'application
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
# Utilise une image Maven avec Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# Copie le fichier pom.xml
COPY pom.xml .

# Télécharge les dépendances (optimisation du cache Docker)
RUN mvn dependency:go-offline -B

# Copie le code source
COPY src src

# Compile l'application
RUN mvn clean package -DskipTests

# Étape finale avec une image Java légère
FROM eclipse-temurin:17-jre-alpine

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

WORKDIR /app

# Copie le JAR compilé depuis l'étape de build
COPY --from=build /app/target/order-service-1.0.0.jar app.jar

# Expose le port 8083 (port du service commande)
EXPOSE 8083

# Commande pour lancer l'application
CMD ["java", "-jar", "app.jar"]
# Etapa 1: construir el jar con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: ejecutar el jar con una imagen más ligera
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/monitoragua-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

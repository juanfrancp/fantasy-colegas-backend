# 1. Etapa de Construcción (Build)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
# Compilamos el proyecto saltando los tests para ir más rápido
RUN mvn clean package -Dmaven.test.skip=true

# 2. Etapa de Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Render nos asignará un puerto, pero informamos del 8080 por defecto
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
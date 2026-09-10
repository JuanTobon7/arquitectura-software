# =============================================================
# MICOS HAKARI — Backend (Spring Boot)
# Build multi-etapa: compila con Maven y ejecuta con JRE 25.
#
# Estructura modular actual:
#   backend (padre, @SpringBootApplication)
#     ├── controller  (REST API)
#     ├── application (servicios, validación, correo)
#     ├── domain      (entidades, puertos)
#     └── persistence (adaptadores JPA, MySQL/H2)
# =============================================================

# --- Etapa 1: Build ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copia la estructura completa de Maven (mantiene la jerarquía de módulos)
COPY backend/pom.xml backend/pom.xml
COPY backend/controller/pom.xml backend/controller/pom.xml
COPY backend/application/pom.xml backend/application/pom.xml
COPY backend/domain/pom.xml backend/domain/pom.xml
COPY backend/persistence/pom.xml backend/persistence/pom.xml

# Descarga dependencias (capa cacheable)
RUN cd backend && mvn -B -pl controller -am dependency:go-offline

# Copia el código fuente
COPY backend/src backend/src
COPY backend/controller/src backend/controller/src
COPY backend/application/src backend/application/src
COPY backend/domain/src backend/domain/src
COPY backend/persistence/src backend/persistence/src

# Compila y empaqueta (omite tests para agilizar)
RUN cd backend && mvn -B -pl controller -am package -DskipTests

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copia el JAR ejecutable generado
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la API REST
EXPOSE 8080

# Variables de entorno por defecto (se sobrescriben con .env / compose)
ENV DB_TIPO=mysql \
    MYSQL_HOST=mysql \
    MYSQL_PORT=3306 \
    MYSQL_DB=micos_hakari \
    MYSQL_USER=root \
    MYSQL_PASSWORD=root \
    MAIL_HOST=smtp.gmail.com \
    MAIL_PORT=587 \
    MAIL_USERNAME=your-email@gmail.com \
    MAIL_PASSWORD=YOUR_GMAIL_APP_PASSWORD \
    MAIL_FROM=your-email@gmail.com \
    FRONTEND_ORIGIN=http://localhost:5173

ENTRYPOINT ["java", "-jar", "app.jar"]

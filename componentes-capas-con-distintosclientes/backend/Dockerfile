# =============================================================
# MICOS HAKARI — Dockerfile multi-stage (Java 25 / Spring Boot 4)
# =============================================================

# ---------- Stage 1: Build ----------
FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /app

# Copiar solo los pom.xml primero para cachear descarga de dependencias
COPY pom.xml ./
COPY starter/pom.xml   starter/
COPY controller/pom.xml controller/
COPY application/pom.xml application/
COPY domain/pom.xml     domain/
COPY persistence/pom.xml persistence/

# Copiar mvnw y .mvn
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn

# Descargar dependencias (si no hay cambios en pom, Docker reutiliza caché)
RUN ./mvnw -B dependency:go-offline -DskipTests || true

# Copiar código fuente
COPY starter/src    starter/src
COPY controller/src controller/src
COPY application/src application/src
COPY domain/src     domain/src
COPY persistence/src persistence/src

# Empaquetar JAR sin tests
RUN ./mvnw -B package -DskipTests -pl starter -am

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/starter/target/starter-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

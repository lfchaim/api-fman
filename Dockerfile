# ── Stage 1: Build ────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache das dependências Maven (layer separado para rebuild mais rápido)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o código-fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuário não-root para segurança corporativa
RUN addgroup -S fmanager && adduser -S fmanager -G fmanager

WORKDIR /app

# Diretórios de dados e logs com permissão correta
RUN mkdir -p /data/filemanager /var/log/fmanager \
    && chown -R fmanager:fmanager /data/filemanager /var/log/fmanager /app

COPY --from=builder /build/target/fmanager-1.0.0.jar app.jar

USER fmanager

EXPOSE 8080

# JVM tuning para containers (Java 21 container-aware)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

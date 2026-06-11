# syntax=docker/dockerfile:1

# --- Build: gera o jar executável com o Gradle Wrapper (Java 21) ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia primeiro o wrapper e os arquivos de build para aproveitar cache de camadas
COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Runtime: imagem enxuta apenas com a JRE ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Usuário sem privilégios
RUN useradd --uid 1001 --create-home spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

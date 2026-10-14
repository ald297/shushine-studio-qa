# ==============================================================================
# Dockerfile Multi-Stage para Shushine Studio API (Java 21 Spring Boot 3.3.3)
# Optimizado para Render, Railway, Azure y contenedores en la nube
# ==============================================================================

# ------------------------------------------------------------------------------
# Etapa 1: Compilación y empaquetado con Maven
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar wrapper y descriptor de dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al Maven Wrapper
RUN chmod +x mvnw

# Descargar dependencias en caché
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y empaquetar el JAR (saltando tests para build rápido en nube)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ------------------------------------------------------------------------------
# Etapa 2: Entorno de ejecución ligero (JRE 21)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copiar el binario JAR generado en la etapa anterior
COPY --from=build /app/target/shushine-studio-api-0.0.1-SNAPSHOT.jar app.jar

# Puerto por defecto (Render inyecta la variable $PORT)
ENV PORT=8080
EXPOSE 8080

# Iniciar la aplicación pasando el puerto dinámico de Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]

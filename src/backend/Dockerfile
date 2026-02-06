# ============================================
# Dockerfile multi-stage para ChefPro Backend
# ============================================
# Etapa 1: Compilación (build)
# Usamos una imagen con Maven y JDK 17 para compilar el proyecto.
# Etapa 2: Ejecución (runtime)
# Usamos una imagen ligera solo con JRE para ejecutar el .jar resultante.
# ============================================

# ---------- ETAPA 1: BUILD ----------
FROM eclipse-temurin:17-jdk AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el wrapper de Maven y el pom.xml primero (para aprovechar la caché de Docker)
# Si el pom.xml no cambia, Docker reutiliza esta capa y no vuelve a descargar dependencias.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Damos permiso de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargamos las dependencias sin compilar el código (esto se cachea)
RUN ./mvnw dependency:go-offline -B

# Ahora sí copiamos todo el código fuente
COPY src src

# Compilamos el proyecto y generamos el .jar (sin ejecutar tests para acelerar el build)
RUN ./mvnw package -DskipTests -B

# ---------- ETAPA 2: RUNTIME ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el .jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Railway inyecta la variable PORT automáticamente.
# Spring Boot la lee gracias a ${PORT:8081} en application.yml
EXPOSE ${PORT:-8081}

# Comando para arrancar la aplicación
# - Usamos la variable PORT de Railway como puerto del servidor
ENTRYPOINT ["java", "-jar", "app.jar"]

# Etapa de compilación
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app
RUN apk add --no-cache maven
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn clean install -DskipTests
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /app/uploads && chmod -R 777 /app/uploads
COPY --from=build /workspace/app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
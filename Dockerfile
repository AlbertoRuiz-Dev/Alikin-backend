FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar POM y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src src
RUN mvn clean install -DskipTests
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
# Documentación del Backend en Spring Boot - Alikin

## Introducción
El backend de "Alikin" es una API RESTful desarrollada con Spring Boot 3.4.4 y Java 17 como parte del proyecto integrado para el Ciclo Formativo de Desarrollo de Aplicaciones Web (DAW) en el I.E.S. Francisco Romero Vargas, Jerez de la Frontera, por Alberto Ruiz Díaz durante el curso 2024/2025. Gestiona la lógica de negocio de una red social musical, incluyendo usuarios, canciones, playlists, comunidades, publicaciones, comentarios y géneros musicales. Utiliza PostgreSQL como base de datos, Spring Security con JWT para autenticación, y springdoc-openapi para documentación de la API. El despliegue se realiza mediante Docker y Docker Compose, integrando el backend con el frontend y la base de datos.

## Arquitectura por Capas
El backend sigue una arquitectura por capas para garantizar separación de responsabilidades y mantenibilidad:

1. **Capa de Controladores** (`com.backendalikin.controller`)
   - Expone endpoints RESTful, valida datos de entrada con `jakarta.validation`, y delega la lógica a los servicios.
   - Ejemplo (`CommunityController`):
     ```java
     import org.springframework.http.ResponseEntity;
     import org.springframework.security.core.Authentication;
     import org.springframework.web.bind.annotation.PostMapping;
     import org.springframework.web.bind.annotation.PathVariable;
     import org.springframework.web.bind.annotation.RequestMapping;
     import org.springframework.web.bind.annotation.RestController;
     import com.backendalikin.service.CommunityService;
     import com.backendalikin.dto.response.MessageResponse;

     @RestController
     @RequestMapping("/api/communities")
     public class CommunityController {
       private final CommunityService communityService;

       public CommunityController(CommunityService communityService) {
         this.communityService = communityService;
       }

       @PostMapping("/{id}/join")
       public ResponseEntity<MessageResponse> joinCommunity(@PathVariable Long id, Authentication authentication) {
         return ResponseEntity.ok(communityService.joinCommunity(id, authentication.getName()));
       }
     }
     ```

2. **Capa de Servicios** (`com.backendalikin.service`)
   - Contiene la lógica de negocio y gestiona transacciones con `@Transactional`.
   - Ejemplo (`PlaylistService`):
     ```java
     import org.springframework.stereotype.Service;
     import org.springframework.transaction.annotation.Transactional;
     import com.backendalikin.dto.request.PlaylistRequest;
     import com.backendalikin.dto.response.PlaylistResponse;
     import com.backendalikin.repository.PlaylistRepository;

     @Service
     public class PlaylistService {
       private final PlaylistRepository playlistRepository;

       public PlaylistService(PlaylistRepository playlistRepository) {
         this.playlistRepository = playlistRepository;
       }

       @Transactional
       public PlaylistResponse createPlaylist(PlaylistRequest request, String username) {
         // Lógica para crear playlist
         return playlistRepository.save(/* ... */);
       }
     }
     ```

3. **Capa de Repositorios** (`com.backendalikin.repository`)
   - Interfaces que extienden `JpaRepository` para operaciones CRUD.
   - Ejemplo (`UserRepository`):
     ```java
     import org.springframework.data.jpa.repository.JpaRepository;
     import java.util.Optional;
     import com.backendalikin.entity.UserEntity;

     public interface UserRepository extends JpaRepository<UserEntity, Long> {
       Optional<UserEntity> findByUsername(String username);
     }
     ```

4. **Capa de Entidades** (`com.backendalikin.entity`)
   - Define el esquema de la base de datos con JPA.
   - Ejemplo (`SongEntity`):
     ```java
     import jakarta.persistence.Entity;
     import jakarta.persistence.Id;
     import jakarta.persistence.GeneratedValue;
     import jakarta.persistence.GenerationType;

     @Entity
     public class SongEntity {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       private String title;
       // Otros campos y relaciones
     }
     ```

5. **DTOs y Mappers** (`com.backendalikin.dto`, `com.backendalikin.mapper`)
   - Usa DTOs (`PlaylistRequest`, `PlaylistResponse`) para aislar entidades.
   - **MapStruct** automatiza conversiones.
   - Ejemplo (`PlaylistMapper`):
     ```java
     import org.mapstruct.Mapper;
     import com.backendalikin.entity.PlaylistEntity;
     import com.backendalikin.dto.response.PlaylistResponse;

     @Mapper(componentModel = "spring")
     public interface PlaylistMapper {
       PlaylistResponse toResponse(PlaylistEntity entity);
     }
     ```

## Estructura de Carpetas
```
Alikin-backend/
├── src/
│   ├── main/
│   │   ├── java/com/backendalikin/
│   │   │   ├── controller/          # Controladores REST
│   │   │   ├── dto/                 # DTOs (request/response)
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── enums/               # Enumeraciones (CommunityRole, Role)
│   │   │   ├── exception/           # Excepciones personalizadas
│   │   │   ├── mapper/              # Mapeadores MapStruct
│   │   │   ├── model/               # Modelos de negocio
│   │   │   ├── poblate/             # Configuración de datos iniciales
│   │   │   ├── repository/          # Repositorios JPA
│   │   │   ├── security/            # Configuración de seguridad
│   │   │   ├── service/             # Servicios de negocio
│   │   │   ├── swagger/             # Configuración de Swagger
│   │   │   ├── BackendAlikinApplication.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   ├── test/                        # Pruebas unitarias
├── Dockerfile                       # Configuración de Docker
├── pom.xml                          # Dependencias Maven
```

## Configuración de Seguridad
- **Spring Security y JWT**:
  - `SecurityConfig` (`com.backendalikin.security.config.SecurityConfig`): Configura autenticación y autorización.
  - `JwtAuthenticationFilter` (`com.backendalikin.security.filter.JwtAuthenticationFilter`): Valida tokens JWT.
  - `JwtTokenProvider` (`com.backendalikin.security.JwtTokenProvider`): Genera y valida tokens.
  - `TokenBlacklistService` (`com.backendalikin.security.TokenBlacklistService`): Gestiona tokens inválidos para logout.
  - Ejemplo (`SecurityConfig`):
    ```java
    import org.springframework.context.annotation.Bean;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.web.SecurityFilterChain;

    public class SecurityConfig {
      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .cors()
          .and()
          .csrf().disable()
          .authorizeHttpRequests()
          .requestMatchers("/api/auth/**").permitAll()
          .anyRequest().authenticated();
        return http.build();
      }
    }
    ```
- **CORS**:
  - Configurado globalmente en `CorsConfig`:
    ```java
    import org.springframework.context.annotation.Bean;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

    public class CorsConfig {
      @Bean
      public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://albertoruiz-dev.tech"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
      }
    }
    ```

## Endpoints Principales
La API está documentada con Swagger UI, accesible en `/swagger-ui/index.html`. Ejemplos de endpoints:

1. **Auth**:
   - `POST /api/auth/login`: Inicia sesión, devuelve un token JWT.
   - `POST /api/auth/signup`: Registra un nuevo usuario.

2. **Users**:
   - `GET /api/users/{id}`: Obtiene los detalles de un usuario.
   - `PUT /api/users/{id}`: Actualiza un usuario.

3. **Playlists**:
   - `POST /api/playlists`: Crea una playlist.
   - `GET /api/playlists/{id}`: Obtiene los detalles de una playlist.

4. **Communities**:
   - `POST /api/communities`: Crea una comunidad.
   - `POST /api/communities/{id}/join`: Une al usuario a una comunidad.

5. **Songs**, **Posts**, **Comments**, **Genres**:
   - Endpoints CRUD similares para cada recurso.

## Base de Datos
- **Tecnología**: PostgreSQL 14.
- **ORM**: Spring Data JPA.
- **Configuración**: Definida en `application.properties`:
  ```properties
  spring.datasource.url=jdbc:postgresql://db:5432/alikin
  spring.datasource.username=postgres
  spring.datasource.password=postgres
  spring.jpa.hibernate.ddl-auto=update
  ```
- **DataSeederConfig** (`com.backendalikin.poblate.DataSeederConfig`): Pobla datos iniciales para pruebas.

## Dependencias Principales
El archivo `pom.xml` define las dependencias de Maven:
```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
  </dependency>
  <dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
  </dependency>
</dependencies>
```

## Despliegue con Docker
El backend se despliega en un contenedor Docker, según el archivo `Dockerfile`:

```dockerfile
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
```

### Integración con Docker Compose
El archivo `docker-compose.yml` (detallado en el prompt del frontend) configura la red `app-network` y los volúmenes `postgres_data` y `media-volume` para persistencia de datos y almacenamiento de archivos.

### Pasos para Desplegar
1. Asegúrese de tener Docker y Docker Compose instalados.
2. Navegue al directorio raíz del proyecto:
   ```bash
   cd alikin
   ```
3. Ejecute:
   ```bash
   docker-compose up --build
   ```
4. Acceda al backend en `http://localhost:8080` o `https://albertoruiz-dev.tech`.

## Instalación y Ejecución en Desarrollo
1. **Requisitos**:
   - Java 17.
   - Maven.
   - PostgreSQL 14.
2. **Instalación**:
   ```bash
   cd Alikin-backend
   mvn install
   ```
3. **Ejecución**:
   ```bash
   mvn spring-boot:run
   ```
   Acceda a `http://localhost:8080`.
- **Documentación de la API**: Acceda a `/swagger-ui/index.html` para explorar y probar los endpoints.
  
## Mejoras
- **Pruebas Unitarias**: Ampliar las pruebas en `src/test` usando JUnit y Spring Security Test.
- **Caché**: Implementar Redis para cachear datos frecuentes (por ejemplo, perfiles de usuario).
- **Monitoreo**: Usar herramientas como Actuator para monitorear el estado del backend en producción.


# Documentación del Proyecto Alikin (sin terminar Abril)
## Tecnologías utilizadas

- **Backend**: Spring Boot 3.4
- **Base de datos**: PostgreSQL
- **Seguridad**: Spring Security + JWT
- **Mapeo de objetos**: JPA/Hibernate
- **Conversión DTO**: MapStruct
- **Contenedores**: Docker y Docker Compose
- **Frontend** (pendiente): Angular

## Arquitectura

La aplicación sigue una arquitectura de capas:

1. **Controladores**: Manejan las peticiones HTTP y realizan la validación básica
2. **Servicios**: Implementan la lógica de negocio
3. **Repositorios**: Proporcionan acceso a la base de datos
4. **Entidades**: Representan las tablas de la base de datos
5. **DTOs**: Transfieren datos entre el cliente y el servidor
6. **Mappers**: Convierten entre entidades y DTOs

## Estructura del proyecto

```
backend-alikin/
├── src/main/java/com/backendalikin/
│   ├── controller/       # Controladores REST
│   ├── dto/              # Objetos de transferencia de datos
│   │   ├── request/      # DTOs para peticiones
│   │   └── response/     # DTOs para respuestas
│   ├── entity/           # Entidades JPA
│   ├── mapper/           # Mappers MapStruct
│   ├── model/            # Modelos de dominio y enumeraciones
│   │   └── enums/        # Enumeraciones
│   ├── repository/       # Repositorios JPA
│   ├── security/         # Configuración de seguridad y JWT
│   └── service/          # Servicios con lógica de negocio
└── src/main/resources/
    └── application.properties  # Configuración de la aplicación
```

## API REST

La API se organiza en los siguientes recursos principales:

### Auth

- `POST /api/auth/signup`: Registrar un nuevo usuario
- `POST /api/auth/login`: Iniciar sesión y obtener token JWT

### Users

- `GET /api/users/{id}`: Obtener usuario por ID
- `GET /api/users/me`: Obtener usuario actual
- `PUT /api/users/{id}`: Actualizar usuario
- `POST /api/users/{id}/follow`: Seguir a un usuario
- `POST /api/users/{id}/unfollow`: Dejar de seguir a un usuario

### Posts

- `POST /api/posts`: Crear una publicación
- `GET /api/posts/{id}`: Obtener publicación por ID
- `PUT /api/posts/{id}`: Actualizar publicación
- `DELETE /api/posts/{id}`: Eliminar publicación
- `POST /api/posts/{id}/vote`: Votar una publicación
- `GET /api/posts/feed`: Obtener feed personalizado

### Comments

- `POST /api/comments/post/{postId}`: Añadir comentario a una publicación
- `GET /api/comments/post/{postId}`: Obtener comentarios de una publicación
- `PUT /api/comments/{id}`: Actualizar comentario
- `DELETE /api/comments/{id}`: Eliminar comentario

### Communities

- `POST /api/communities`: Crear una comunidad
- `GET /api/communities/{id}`: Obtener comunidad por ID
- `PUT /api/communities/{id}`: Actualizar comunidad
- `DELETE /api/communities/{id}`: Eliminar comunidad
- `POST /api/communities/{id}/join`: Unirse a una comunidad
- `POST /api/communities/{id}/leave`: Abandonar una comunidad
- `POST /api/communities/{id}/radio`: Establecer radio de la comunidad

### Songs

- `POST /api/songs`: Subir una canción
- `GET /api/songs/{id}`: Obtener canción por ID
- `GET /api/songs/{id}/stream`: Transmitir una canción
- `PUT /api/songs/{id}`: Actualizar información de una canción
- `DELETE /api/songs/{id}`: Eliminar una canción

### Playlists

- `POST /api/playlists`: Crear una playlist
- `GET /api/playlists/{id}`: Obtener playlist por ID
- `PUT /api/playlists/{id}`: Actualizar playlist
- `DELETE /api/playlists/{id}`: Eliminar playlist
- `POST /api/playlists/{id}/songs/{songId}`: Añadir canción a playlist
- `DELETE /api/playlists/{id}/songs/{songId}`: Eliminar canción de playlist

## Seguridad

La aplicación utiliza JWT (JSON Web Tokens) para la autenticación y autorización:

1. El usuario se registra o inicia sesión para obtener un token
2. El token se incluye en el encabezado `Authorization` de las solicitudes
3. El servidor valida el token y autoriza la operación según los permisos

## Próximos pasos

1. Implementar pruebas unitarias y de integración
2. Completar la documentación de la API con Swagger
3. Implementar características adicionales:
    - Verificación de email
    - Recuperación de contraseña
    - Notificaciones
    - Estadísticas de reproducción
4. Implementar el frontend con Angular

---

Este resumen y documentación reflejan el estado actual del proyecto. Se actualizarán a medida que el desarrollo avance.
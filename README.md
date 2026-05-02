# User API WebFlux + JWT

Author: omartapia <omar.tapia.h@gmail.com>

API REST para crear y administrar usuarios con Spring Boot, WebFlux, R2DBC, H2 en memoria, Flyway, JWT y OpenAPI.

## Requisitos

- Java 17+
- Gradle wrapper incluido

## Ejecutar

```bash
./gradlew bootRun
```

La API queda disponible en:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI:

```text
src/main/resources/openapi.yaml
```

Postman:

```text
src/main/resources/User_API_Enterprise.postman_collection.json
```

## Endpoints

- `POST /users`: crea usuario. Responde `201 Created`.
- `GET /users`: lista usuarios activos. Requiere `Authorization: Bearer <jwt>`.
- `GET /users/{id}`: obtiene usuario por id. Requiere JWT.
- `PUT /users/{id}`: actualización completa. Requiere JWT.
- `PATCH /users/{id}`: actualización parcial. Requiere JWT.
- `DELETE /users/{id}`: desactiva usuario. Responde `204 No Content`. Requiere JWT.

Ejemplo de creación:

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Juan Rodriguez",
    "email": "juan@rodriguez.org",
    "password": "hunter2",
    "phones": [
      {
        "number": "1234567",
        "citycode": "1",
        "contrycode": "57"
      }
    ]
  }'
```

Errores:

```json
{"mensaje": "mensaje de error"}
```

## Validaciones

La validación de contraseña es configurable en `src/main/resources/application.yml`:

```yaml
nisum:
  validation:
    password-regex: "^(?=.*[A-Za-z])(?=.*\\d).{6,}$"
```

El correo también se valida por formato y el correo duplicado responde:

```json
{"mensaje": "El correo ya está registrado"}
```

## Base De Datos

La base de datos es H2 en memoria con R2DBC. Los scripts Flyway están en:

```text
src/main/resources/db/migration
```

## Tests

```bash
./gradlew test
```

El reporte de cobertura Jacoco se genera en:

```text
build/reports/jacoco/test/html/index.html
```

## Diagrama

```text
src/main/resources/architecture-diagram.txt
```

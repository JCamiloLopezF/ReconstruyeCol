# CLAUDE.md — Backend (Spring Boot)

> Colocar este archivo en `backend/CLAUDE.md`. Se lee junto con el `CLAUDE.md` de la raíz del repositorio.

Contexto general del producto: `../CLAUDE.md`. Diseño técnico detallado: `../docs/02-diseno-tecnico.md` (secciones 3 a 8 son las más relevantes para este módulo).

## Stack

- Java (confirmar versión LTS instalada con `java -version` antes de generar el proyecto — no asumir)
- Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security
- PostgreSQL + PostGIS (Hibernate Spatial para mapear `geometry(Point, 4326)`)
- Build: Maven o Gradle — usar lo que el equipo ya conozca y ser consistente una vez elegido, no mezclar

## Comandos

Verificados contra el `build.gradle` real (Gradle, Spring Boot 3.5.3, Java 21).

- Build: `./gradlew clean build`
- Compilar sin tests: `./gradlew clean compileJava`
- Correr local: `./gradlew bootRun` (requiere `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` apuntando a un Postgres con PostGIS; por defecto usa `localhost:5432/ayudaterremoto`)
- Tests: `./gradlew test`

## Estructura esperada de paquetes

```
com.<org>.ayudaterremoto
├── model/          (entidades JPA)
│   └── dto/        (request/response de entrada y salida)
├── controller/     (endpoints REST)
├── service/        (lógica de negocio)
├── repository/     (acceso a datos, Spring Data JPA)
├── mapper/         (conversión entidad <-> DTO)
├── security/       (configuración JWT, filtros)
└── common/         (excepciones, utilidades, configuración PostGIS)
```

## Reglas específicas del backend

- Endpoints públicos (crear/buscar solicitud u oferta): sin `@PreAuthorize`. Endpoints bajo `/api/admin/**`: siempre con `@PreAuthorize("hasRole('ADMIN')")`.
- Las búsquedas por cercanía usan `ST_DWithin` de PostGIS (query nativa o `@Query` de Spring Data JPA) — nunca traer todos los registros a memoria para filtrar distancia en Java.
- Toda entrada de usuario se valida con Bean Validation (`@Valid`, `@NotBlank`, `@Email`, etc.) antes de tocar la base de datos.
- Nunca devolver `password_hash` ni `documento_identidad_hash` en respuestas JSON — usar DTOs de salida, no exponer entidades JPA directamente en los controllers.
- Índice `GIST` obligatorio sobre cualquier columna `geometry` — agregarlo en la migración correspondiente.
- Migraciones versionadas (Flyway o Liquibase) desde el primer commit. Nunca `ddl-auto: update` fuera del entorno local de desarrollo.
- El `token_gestion` de cada solicitud/oferta se genera con suficiente entropía (por ejemplo `UUID.randomUUID()`) para que no sea adivinable.

## Testing mínimo aceptable para el MVP

No se exige cobertura alta. Se exige que los flujos críticos tengan al menos un test que falle si se rompen:
- Crear solicitud / oferta.
- Buscar por cercanía (verificar que el radio funciona correctamente).
- Marcar como atendida con `token_gestion` válido e inválido.
- Aprobar/rechazar un ingeniero (solo accesible como `ROLE_ADMIN`).

Usar `@DataJpaTest` para repositorios y Testcontainers si el tiempo lo permite para probar contra Postgres/PostGIS real en vez de una base en memoria (H2 no soporta PostGIS correctamente).

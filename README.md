# MS-Usuarios — Beca Java IA

Microservicio de gestión de usuarios para el Sistema de Mesa de Ayuda, desarrollado con Java, Spring Boot y PostgreSQL como parte de las prácticas de IA de IDS.

La Práctica 1 consiste en implementar un CRUD REST, utilizando GitHub Copilot como apoyo durante el análisis, desarrollo y pruebas.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.16
- Maven
- PostgreSQL 16
- Spring Data JPA / Hibernate
- Flyway
- Jakarta Validation
- BCrypt
- JUnit 5, Mockito y MockMvc

## Funcionalidades de la Práctica 1

El microservicio permite registrar, consultar, listar, actualizar y eliminar usuarios.

| Método | Endpoint | Función |
|---|---|---|
| POST | `/api/usuarios` | Registrar usuario |
| GET | `/api/usuarios/{id}` | Consultar usuario por ID |
| GET | `/api/usuarios` | Listar usuarios |
| PUT | `/api/usuarios/{id}` | Actualizar nombre y correo |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

Al registrar un usuario, el servidor asigna automáticamente el rol `SOLICITANTE`.

Las contraseñas se almacenan mediante hashes BCrypt y no se incluyen en las respuestas de la API.

La autenticación JWT y la autorización por roles corresponden a prácticas posteriores. Por ello, esta implementación debe utilizarse únicamente en un entorno local controlado.

## Requisitos previos

Para ejecutar el proyecto se necesita:

- JDK 21 instalado.
- PostgreSQL 16 en funcionamiento.
- Puerto `5432` disponible para PostgreSQL.
- Base de datos `ms_usuarios_db`.
- Un usuario de PostgreSQL con permisos para utilizar la base de datos y crear las tablas mediante Flyway.

El repositorio incluye Maven Wrapper (`mvnw.cmd`), por lo que no es necesario instalar Maven por separado.

## Configuración de PostgreSQL

Crear una base de datos llamada:

```sql
CREATE DATABASE ms_usuarios_db;
```

La aplicación utiliza la siguiente conexión, definida en `src/main/resources/application.yaml`:

```text
jdbc:postgresql://localhost:5432/ms_usuarios_db
```

Las credenciales no están escritas directamente en el código. Se obtienen de dos variables de entorno:

- `DB_USERNAME`
- `DB_PASSWORD`

En una terminal CMD de Windows, configurarlas para la sesión actual:

```cmd
set "DB_USERNAME=usuario_postgres"
set "DB_PASSWORD=contrasena_postgres"
```

Sustituir los valores de ejemplo por las credenciales reales.

No subir contraseñas al repositorio.

## Ejecutar el microservicio

Desde la carpeta raíz del proyecto, con PostgreSQL disponible y las variables de entorno configuradas:

```cmd
.\mvnw.cmd spring-boot:run
```

Durante el arranque, Flyway ejecutará automáticamente las migraciones pendientes.

La migración inicial se encuentra en:

```text
src/main/resources/db/migration/V1__create_usuarios.sql
```

Esta crea la tabla `usuarios`, sus restricciones y el índice único para el correo electrónico.

Spring Boot utiliza `ddl-auto: validate` para comprobar la compatibilidad entre las entidades JPA y el esquema de PostgreSQL.

Por defecto, la API estará disponible en:

```text
http://localhost:8080
```

## Ejemplo de uso

### Registrar un usuario

**POST** `/api/usuarios`

```json
{
  "nombre": "Usuario Prueba",
  "email": "usuario@example.com",
  "password": "ClaveSegura123"
}
```

Respuesta esperada: `201 Created`.

La respuesta contiene los datos permitidos del usuario, sin incluir la contraseña ni su hash.

### Consultar usuarios

**GET** `/api/usuarios`

Respuesta esperada: `200 OK` con una lista de usuarios.

### Manejo de errores

La aplicación utiliza `ProblemDetail` para generar respuestas de error consistentes.

| Código | Situación |
|---|---|
| 400 | Solicitud inválida |
| 404 | Usuario inexistente |
| 409 | Correo electrónico duplicado |
| 500 | Error inesperado del servidor |

Las operaciones fueron verificadas manualmente mediante Postman.

## Ejecutar las pruebas

El proyecto incluye 13 pruebas automatizadas:

| Clase | Pruebas |
|---|---|
| `UsuarioServiceTest` | 3 |
| `UsuarioControllerTest` | 8 |
| `CrearUsuarioRequestTest` | 1 |
| `MsUsuariosApplicationTests` | 1 |

Para ejecutar únicamente las 12 pruebas de servicio, controlador y DTO, que no necesitan PostgreSQL:

```cmd
.\mvnw.cmd -B -Dtest=UsuarioServiceTest,UsuarioControllerTest,CrearUsuarioRequestTest test
```

Para ejecutar la verificación completa:

```cmd
.\mvnw.cmd -B clean verify
```

**Importante:** la prueba `MsUsuariosApplicationTests` utiliza `@SpringBootTest`, por lo que la verificación completa requiere PostgreSQL disponible y las variables `DB_USERNAME` y `DB_PASSWORD` configuradas en la terminal que ejecuta Maven.

### Última verificación

Fecha: 04/11/2026.

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Maven también generó correctamente el archivo JAR de la aplicación.

## Documentación

La documentación de la Práctica 1 se encuentra organizada en:

- `docs/requerimientos.md`: historias de usuario, criterios Gherkin, reglas de negocio y contrato CRUD.
- `prompts/historias-usuario.md`: análisis de requerimientos.
- `prompts/modelo-datos-flyway.md`: diseño de base de datos.
- `prompts/implementacion-rest.md`: planificación e implementación REST.
- `prompts/pruebas.md`: planificación e implementación de pruebas.
- `BITACORA-IA.md`: interacciones relevantes con Copilot, resultados y correcciones realizadas.

Los prompts documentan las instrucciones utilizadas durante el desarrollo, incluyendo las revisiones y ajustes posteriores.

## Alcance pendiente

La Práctica 1 no incluye todavía autenticación JWT, autorización por roles, integración con otros microservicios ni despliegue mediante contenedores. Estas funcionalidades se abordarán en las prácticas correspondientes.
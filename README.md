# MS-Usuarios — Beca Java IA

Microservicio de gestión de usuarios para el Sistema de Mesa de Ayuda, desarrollado con Java, Spring Boot y PostgreSQL como parte de las prácticas de IA de IDS.

La Práctica 1 consiste en implementar un CRUD REST, utilizando GitHub Copilot como apoyo durante el análisis, desarrollo y pruebas.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.16
- Spring Security (OAuth2 Resource Server, JWT HS256)
- Maven
- PostgreSQL 16
- Spring Data JPA / Hibernate
- Flyway
- Jakarta Validation
- BCrypt
- JUnit 5, Mockito, MockMvc y Spring Security Test

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

> Nota: desde la Práctica 3, el listado, la consulta por ID, la actualización y la eliminación requieren un JWT de un usuario `ADMIN`. Ver la sección 7.

Las contraseñas se almacenan mediante hashes BCrypt y no se incluyen en las respuestas de la API.

La Práctica 3 incorpora autenticación con JWT y autorización por roles. Las secciones siguientes describen el estado actual del microservicio. El cliente HTTP de `ms-catalogo` está implementado y probado con respuestas simuladas. Queda por validar el flujo con el servicio real. La ausencia de revocación inmediata de JWT es un riesgo abierto, documentado por separado (ver las secciones 10 y 11).

## 1. Descripción y alcance

`ms-usuarios` es el microservicio del Sistema de Mesa de Ayuda responsable de:

- Registrar y administrar usuarios.
- Gestionar los roles `SOLICITANTE`, `AGENTE` y `ADMIN`.
- Persistir los usuarios en PostgreSQL, con migraciones Flyway.
- Autenticar mediante correo y contraseña (`POST /api/auth/login`).
- Emitir tokens JWT firmados con HS256 y validar los tokens recibidos.
- Autorizar las operaciones de administración según el rol.

Implementado y verificado en el repositorio: el registro público, la administración de usuarios (solo `ADMIN`), el login con emisión de JWT, la validación local de JWT y la autorización por roles.

Planificado o pendiente, no implementado en este repositorio: la revocación de tokens, la confirmación del contrato JWT con otros microservicios y la gestión de roles mediante API (no existe ningún endpoint para cambiar roles).

## 2. Requisitos y entorno

- JDK 21.
- PostgreSQL 16 en funcionamiento.
- Maven Wrapper incluido en el proyecto (`mvnw.cmd`); no es necesario instalar Maven por separado.
- Windows con PowerShell o CMD para los comandos de esta guía.

Docker no es necesario para ejecutar el microservicio ni sus pruebas.

## 3. Variables de entorno

`src/main/resources/application.yaml` no contiene credenciales ni secretos. Lee las siguientes variables, que deben estar disponibles en el proceso que ejecuta Spring Boot:

| Variable | Uso | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL, por ejemplo `jdbc:postgresql://localhost:5432/ms_usuarios_db` | Ninguno |
| `DB_USERNAME` | Usuario de PostgreSQL | Ninguno |
| `DB_PASSWORD` | Contraseña de PostgreSQL | Ninguno |
| `JWT_SECRET` | Clave simétrica HS256 codificada en Base64 estándar | Ninguno |

`JWT_SECRET` debe ser una clave criptográficamente aleatoria, codificada en Base64, cuya decodificación proporcione al menos 32 bytes. No utilizar valores de ejemplo, palabras comunes ni cadenas inventadas.

### Modos de uso de JWT_SECRET

| Entorno | Clave | Gestión |
|---|---|---|
| Desarrollo local independiente | Clave propia del desarrollador, solo para pruebas aisladas | Local; no se comparte |
| Integración del Equipo 4 | Única clave compartida por los microservicios autorizados | Generada previamente por el responsable de MS-Usuarios y entregada por un mecanismo privado |
| Producción | Clave distinta de cualquier otro entorno | Mecanismo formal de gestión, acceso y rotación (pendiente de definir) |

Reglas para el entorno de integración del Equipo 4:

- Existe una única clave `JWT_SECRET` para ese entorno, ya generada y resguardada por el responsable de MS-Usuarios.
- Los microservicios autorizados deben utilizar exactamente el mismo valor.
- Los integrantes autorizados reciben la clave por un mecanismo privado. No se publica en el repositorio ni en la documentación.
- Ningún integrante debe generar una clave independiente para el entorno compartido.
- Regenerar la clave requiere coordinación de todo el equipo e invalida los JWT emitidos con la clave anterior.

La clave de integración no debe reutilizarse en producción. Tampoco debe reutilizarse una clave de desarrollo local en otro entorno.

### Creación inicial del secreto (una sola vez)

Este procedimiento crea un secreto nuevo. Se conserva únicamente como referencia para la creación inicial de un entorno nuevo o de un desarrollo local independiente. **No debe ejecutarse en cada arranque ni para el entorno de integración del Equipo 4**, cuya clave ya fue generada y resguardada por el responsable de MS-Usuarios. Regenerar la clave requiere coordinación de todo el equipo e invalida los JWT emitidos con la clave anterior.

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Si el secreto debe conservarse, hacerlo únicamente en el mecanismo privado acordado por el equipo, preferentemente un gestor de secretos. No pegarlo en el repositorio, en archivos versionados, en scripts, en la documentación, en capturas, en logs ni en archivos del proyecto.

### Configurar JWT_SECRET en Windows

La clave debe estar disponible en el proceso que ejecuta Spring Boot. Si el microservicio se inicia desde una terminal, configure la variable en esa misma sesión y ejecute el proyecto con los comandos de la sección 5.

PowerShell (recomendado): la lectura es oculta y el valor no queda en la línea de comandos ni en el historial:

```powershell
$secreto = Read-Host -Prompt "JWT_SECRET" -AsSecureString
$env:JWT_SECRET = [System.Net.NetworkCredential]::new("", $secreto).Password
Remove-Variable secreto
```

Comprobar la configuración sin mostrar el valor. El bloque verifica que la variable existe, que contiene Base64 estándar válido (con relleno `=` y sin espacios internos, como exige el decodificador de Java) y que decodifica a al menos 32 bytes. Solo muestra el resultado y la longitud:

```powershell
if (-not $env:JWT_SECRET) {
    "JWT_SECRET no definida"
} else {
    $candidato = $env:JWT_SECRET.Trim()
    if ($candidato.Length % 4 -ne 0 -or $candidato -notmatch '^[A-Za-z0-9+/]*={0,2}$') {
        "JWT_SECRET inválida: no es Base64 estándar"
    } else {
        $bytes = [Convert]::FromBase64String($candidato)
        if ($bytes.Length -ge 32) { "JWT_SECRET válida: $($bytes.Length) bytes" }
        else { "JWT_SECRET inválida: $($bytes.Length) bytes (mínimo 32)" }
    }
}
```

No ejecute `Get-ChildItem Env:` ni imprima `$env:JWT_SECRET`.

CMD: no ofrece lectura oculta. Si no puede usar PowerShell, escriba la asignación solo en una ventana que cerrará al terminar. El comando queda en el historial de esa ventana, por lo que no debe copiarse en scripts ni en documentación:

```cmd
set "JWT_SECRET=<valor entregado por el responsable>"
```

Para no repetir la configuración en cada sesión, recupere el secreto desde el mecanismo privado acordado por el equipo y vuelva a ejecutar el bloque de PowerShell anterior. No guarde el valor como variable persistente de usuario de Windows: las variables de entorno de usuario no son un almacén cifrado de secretos. Se guardan en texto plano en el perfil del usuario y pueden ser leídas por cualquier proceso que se ejecute con esa cuenta. Tampoco utilice `setx` con el valor en la línea de comandos.

### Variables de base de datos

Configure las variables de conexión en la misma sesión de PowerShell (sustituya los valores de ejemplo por los reales, sin subirlos al repositorio):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/ms_usuarios_db"
$env:DB_USERNAME = "usuario_postgres"
$env:DB_PASSWORD = "contrasena_postgres"
```

En CMD de Windows, la sintaxis es `set "NOMBRE=valor"`. Las credenciales de ejemplo deben sustituirse por las reales y nunca subirse al repositorio.

## 4. Base de datos y Flyway

- Base de datos: PostgreSQL 16. Crear una base vacía, por ejemplo `ms_usuarios_db`:

```sql
CREATE DATABASE ms_usuarios_db;
```

- La conexión se toma de `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
- Flyway está habilitado (`spring.flyway.enabled: true`) y ejecuta las migraciones pendientes durante el arranque. La migración inicial es `src/main/resources/db/migration/V1__create_usuarios.sql`, que crea la tabla `usuarios`, sus restricciones y el índice único sobre `lower(email)`.
- Hibernate usa `ddl-auto: validate`: verifica que las entidades coincidan con el esquema, pero no crea ni modifica tablas. Los cambios de esquema se realizan únicamente mediante migraciones Flyway.

## 5. Ejecución local

Desde la raíz del proyecto, con PostgreSQL disponible y las variables de la sección 3 configuradas en la misma sesión de PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Flyway aplica las migraciones pendientes durante el arranque y Hibernate valida el esquema (`ddl-auto: validate`). La API queda disponible en `http://localhost:8080`, el puerto por defecto de Spring Boot, ya que no se configura `server.port`.

Comprobación de estado, sin autenticación. Los detalles están ocultos por configuración (`show-details: never`):

```powershell
curl.exe -i http://localhost:8080/actuator/health
```

Pruebas y verificación completa:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B clean verify
```

Las pruebas automatizadas utilizan el perfil `test`, con H2 en memoria (`src/test/resources/application-test.yaml`), por lo que no requieren PostgreSQL ni las variables `DB_*`.

## 6. Autenticación JWT

### Obtener un token

**POST** `/api/auth/login`

Ejemplo con credenciales ficticias:

```json
{
  "email": "usuario.ejemplo@example.com",
  "password": "ContrasenaFicticia123"
}
```

El servidor normaliza el correo (elimina espacios y lo convierte a minúsculas). La contraseña no puede superar 72 bytes UTF-8.

Respuesta `200 OK`:

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

- `accessToken`: token JWT emitido por el servicio.
- `tokenType`: siempre `Bearer`.
- `expiresIn`: vigencia en segundos (3600, una hora).

### Uso del token

Enviar el token en el encabezado:

```text
Authorization: Bearer <JWT>
```

### Claims

| Claim | Tipo | Contenido y validación |
|---|---|---|
| `iss` | String | Emisor `ms-usuarios`. Se valida con `JwtIssuerValidator`. |
| `sub` | String | Identificador numérico del usuario, en texto. Debe ser una cadena no vacía (validación en `JwtConfig`). |
| `roles` | Arreglo JSON de String | Emitido con un único nombre de rol: `SOLICITANTE`, `AGENTE` o `ADMIN`. El validador acepta un arreglo no vacío en el que todos los elementos sean roles conocidos (`RolesClaimValidator`). |
| `iat` | NumericDate | Momento de emisión, en segundos enteros desde la época Unix (RFC 7519). |
| `exp` | NumericDate | Momento de expiración: `iat` más 3600 segundos. Es obligatorio y se valida con tolerancia de reloj cero. |

`iat` y `exp` no son cadenas: son NumericDate. Un consumidor debe interpretarlos como segundos enteros desde la época Unix.

El token está firmado con HS256 mediante la clave de `JWT_SECRET`. Para el entorno de integración del Equipo 4, la clave es única y compartida (ver la sección 3). El emisor es `ms-usuarios` y la vigencia es de 3600 segundos. El servicio es stateless: no consulta una base de datos ni una lista de revocación al validar un token. Por ello, un token sigue siendo aceptado hasta su expiración, incluso si la cuenta fue eliminada (ver la sección 10).

La validación criptográfica se prueba con el `JwtDecoder` real, además de las pruebas de autorización que utilizan `jwt()` de Spring Security Test. Se incorporó una comprobación explícita de que `iat` estuviera presente en los claims originales, antes de la conversión de Spring, y pruebas HTTP que rechazan un JWT firmado sin `iat` con **401**; un token equivalente con `iat` válido permite la operación autorizada (**200**).

## 7. Permisos y endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/usuarios` | Público (asigna siempre `SOLICITANTE`) |
| GET | `/actuator/health` | Público |
| GET | `/api/usuarios` | Solo `ADMIN` |
| GET | `/api/usuarios/{id}` | Solo `ADMIN` |
| PUT | `/api/usuarios/{id}` | Solo `ADMIN` |
| DELETE | `/api/usuarios/{id}` | Solo `ADMIN` |
| Cualquier otra ruta | — | Denegado |

El registro público asigna exclusivamente el rol `SOLICITANTE`. Esta es una decisión provisional, pendiente de validación de negocio. Un intento de registro con rol `ADMIN` fue rechazado con `400` en la prueba manual PF-18.

Códigos de error (`ProblemDetail`):

| Código | Situación | Respaldo |
|---|---|---|
| 400 | Validación fallida, cuerpo inválido o correo/contraseña con formato no admitido | Código y pruebas |
| 401 | Falta el token, el token es inválido o expirado, o las credenciales de login son incorrectas (mensaje genérico) | Código, pruebas y evidencia manual `[D]` PF-06, PF-07, PF-08, PF-12 |
| 403 | Token válido sin rol `ADMIN` | Código, pruebas y evidencia manual `[D]` PF-05 |
| 404 | Usuario inexistente | Código y pruebas de controlador; sin evidencia manual con JWT real |
| 409 | Correo electrónico duplicado | Código y pruebas |
| 500 | Error inesperado; no expone detalles | Código |

## 8. Aprovisionamiento del primer administrador

No existe un administrador predeterminado ni un endpoint para cambiar roles. El primer `ADMIN` se crea mediante un procedimiento manual provisional (acordado como B2) que debe ejecutarse con controles operativos apropiados, como una cuenta de base de datos autorizada, registro del cambio y respaldo previo.

1. Registrar una cuenta mediante el flujo público `POST /api/usuarios`. Se crea con rol `SOLICITANTE`.
2. Verificar su persistencia en PostgreSQL, usando un correo en minúsculas (la base de datos exige correos normalizados):

   ```sql
   SELECT id, nombre, email, rol FROM usuarios WHERE email = 'correo.admin@example.com';
   ```

3. Asignar el rol `ADMIN` en una transacción controlada, mediante una cuenta de base de datos autorizada. Ejecute cada bloque por separado y revise el resultado antes de continuar:

   a. Iniciar la transacción:

   ```sql
   BEGIN;
   ```

   b. Asignar el rol. PostgreSQL debe responder exactamente `UPDATE 1`:

   ```sql
   UPDATE usuarios SET rol = 'ADMIN' WHERE email = 'correo.admin@example.com';
   ```

   c. Comprobar la respuesta del paso anterior. Si no es exactamente `UPDATE 1` (por ejemplo `UPDATE 0` o un número distinto), ejecute `ROLLBACK;` y detenga el procedimiento:

   ```sql
   ROLLBACK;
   ```

   d. Si la respuesta fue `UPDATE 1`, verificar los datos dentro de la misma transacción:

   ```sql
   SELECT id, email, rol FROM usuarios WHERE email = 'correo.admin@example.com';
   ```

   e. Confirmar **solo** si el resultado del `SELECT` es correcto (una fila, `rol` igual a `ADMIN`):

   ```sql
   COMMIT;
   ```

   f. Si la verificación del `SELECT` falla por cualquier motivo, ejecute `ROLLBACK;` antes de salir:

   ```sql
   ROLLBACK;
   ```

4. Autenticarse mediante `POST /api/auth/login` con la contraseña de la cuenta. La contraseña se introduce en la petición, nunca en SQL, en archivos de configuración ni en la documentación.

Sustituir `correo.admin@example.com` por el correo real de la cuenta. La consulta no debe mostrar hashes ni contraseñas.

## Ejemplo de uso (Práctica 1)

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

**GET** `/api/usuarios` (requiere JWT de un usuario `ADMIN`, ver la sección 7).

Respuesta esperada: `200 OK` con una lista de usuarios, sin contraseñas ni hashes.

Las operaciones de la Práctica 1 fueron verificadas manualmente mediante Postman.

## 9. Calidad y pruebas

**Última verificación integral reportada** (`.\mvnw.cmd -B clean verify`), después del refuerzo criptográfico de P3:

- **102 pruebas automatizadas aprobadas**, 0 fallos, 0 errores y 0 omitidas; resultado `BUILD SUCCESS`.
- Checkstyle, PMD, CPD y SpotBugs sin hallazgos que impidan superar los controles de calidad.
- OWASP Dependency-Check completado con las supresiones documentadas. Superar el control no significa que las vulnerabilidades estén corregidas; hay riesgos y decisiones de aceptación pendientes (ver la sección 10).
- **Referencia histórica de P3-06 (79 pruebas):** JaCoCo registró 88.40 % de cobertura de líneas (221/250) y 86.99 % de cobertura de instrucciones. Estas cifras pertenecen a esa ejecución anterior y **no deben atribuirse automáticamente a la verificación de 102 pruebas** sin consultar sus reportes correspondientes.

El refuerzo posterior incluye `JwtDecoderCriptografiaTest` y `UsuarioControllerJwtSinIatTest`, además de la corrección en `JwtConfig` para rechazar tokens que no contenían originalmente el claim `iat`.

Los detalles, el historial y las limitaciones están en [docs/QUALITY-REPORT.md](docs/QUALITY-REPORT.md), [docs/SECURITY-REPORT.md](docs/SECURITY-REPORT.md) y `BITACORA-IA.md`.

Las pruebas funcionales manuales PF-01 a PF-23 (Postman, Spring Boot y PostgreSQL) están documentadas en `BITACORA-IA.md`. Son evidencia complementaria de integración y **no forman parte de las 102 pruebas automatizadas**. No constituyen una auditoría de seguridad completa.

### Historial de la Práctica 1

En la Práctica 1 el proyecto incluía 13 pruebas automatizadas, verificadas con la siguiente salida (fecha 04/11/2026). Este resultado corresponde a esa etapa y no al estado actual.

```text
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Esta etapa también generó correctamente el archivo JAR de la aplicación.

## Documentación

La documentación de la Práctica 1 se encuentra organizada en:

- `docs/requerimientos.md`: historias de usuario, criterios Gherkin, reglas de negocio y contrato CRUD.
- `prompts/historias-usuario.md`: análisis de requerimientos.
- `prompts/modelo-datos-flyway.md`: diseño de base de datos.
- `prompts/implementacion-rest.md`: planificación e implementación REST.
- `prompts/pruebas.md`: planificación e implementación de pruebas.
- `BITACORA-IA.md`: interacciones relevantes con Copilot, resultados y correcciones realizadas.

Los prompts documentan las instrucciones utilizadas durante el desarrollo, incluyendo las revisiones y ajustes posteriores. La documentación de seguridad de P3 se encuentra en `docs/planes/PLAN-SEGURIDAD-P3.md` y `docs/SECURITY-REPORT.md`; las interacciones con IA y la revisión humana están en `BITACORA-IA.md`.

## 10. Seguridad y limitaciones

Documentación de referencia:

- [docs/SECURITY-REPORT.md](docs/SECURITY-REPORT.md): riesgos, análisis OWASP y controles JWT.
- [docs/planes/PLAN-SEGURIDAD-P3.md](docs/planes/PLAN-SEGURIDAD-P3.md): plan de la Práctica 3, decisiones y estado de ejecución.
- [`.github/agents/revisor-seguridad.agent.md`](.github/agents/revisor-seguridad.agent.md): agente de revisión de seguridad, limitado a análisis y consulta.

La ejecución del agente revisor identificó **11 hallazgos**: 0 críticos, 2 altos, 4 medios y 5 bajos. Son observaciones que requieren revisión y seguimiento; la existencia del informe no demuestra su remediación. El detalle y las decisiones humanas constan en `docs/SECURITY-REPORT.md` y `BITACORA-IA.md`.

Limitaciones actuales:

- El sistema utiliza JWT stateless y no tiene revocación inmediata de tokens. Un token es válido hasta su expiración (3600 segundos).
- La prueba manual PF-23 demostró que un JWT de un `ADMIN` siguió siendo aceptado (HTTP 200) después de eliminar la cuenta correspondiente. Este riesgo permanece abierto.
- HS256 utiliza una clave simétrica. Cualquier microservicio que conozca `JWT_SECRET` puede emitir tokens válidos; por ello, el acceso al secreto debe limitarse a los servicios y personas autorizadas.
- La firma asimétrica (RS256 u otra) queda fuera del alcance actual.
- Existen vulnerabilidades de dependencias pendientes de tratamiento, según el análisis OWASP Dependency-Check.
- Las supresiones OWASP definidas en `config/owasp-suppressions.xml` no corrigen vulnerabilidades. Tienen fecha de vencimiento y deben reevaluarse.
- La implementación no debe presentarse como lista para producción sin resolver o aceptar formalmente los riesgos aplicables.

## 11. Integración entre microservicios

Flujo esperado:

1. Un cliente inicia sesión mediante `POST /api/auth/login`.
2. MS-Usuarios verifica las credenciales y emite un JWT HS256 (`accessToken`).
3. El cliente envía ese JWT en el encabezado `Authorization: Bearer <JWT>` a cada servicio protegido.
4. Cada microservicio consumidor debe validar la firma, el emisor (`ms-usuarios`), la expiración y los roles (`roles`).
5. En el entorno de integración del Equipo 4, todos los servicios autorizados utilizan la misma clave HS256 (`JWT_SECRET`).

MS-Usuarios no distribuye automáticamente los JWT ni la clave a los demás microservicios.

Estado de la integración, separado en tres partes:

- **Estrategia de clave compartida: definida.** Se mantiene HS256 con una única clave `JWT_SECRET` para el entorno de integración del Equipo 4, generada y resguardada por el responsable de MS-Usuarios. Distribuirla a los consumidores requiere un mecanismo privado y seguro, fuera del repositorio, y aprobación arquitectónica. Ningún secreto debe almacenarse en GitHub, archivos versionados, documentación, ejemplos de código o logs.
- **Integración efectiva de los consumidores: pendiente de verificación.** La emisión y la validación local de JWT en MS-Usuarios están implementadas y probadas. La configuración de validación en los demás microservicios no ha sido verificada como integrada en este repositorio.
- **Contrato JWT y pruebas entre microservicios: pendientes.** El contrato compartido está pendiente de confirmación por los equipos consumidores, y las pruebas de integración entre microservicios no se han realizado.

La integración HTTP saliente con `ms-catalogo` mediante `RestClient` está implementada y cuenta con pruebas automatizadas que utilizan `MockRestServiceServer`. El cliente permite consultar `GET /api/categorias`, propaga el JWT Bearer del contexto autenticado y contempla tiempos de espera y manejo de errores HTTP. La integración extremo a extremo con el microservicio real y su incorporación a un flujo funcional de MS-Usuarios permanecen pendientes de validación.

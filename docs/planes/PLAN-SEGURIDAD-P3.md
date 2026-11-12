# Plan de seguridad — Práctica 3

**Proyecto:** Sistema de Mesa de Ayuda  
**Microservicio:** `ms-usuarios`  
**Rama:** `practica-3`  
**Fecha:** 10/11/2026  
**Versión:** 1.0 — Plan consolidado
**Estado:** Arquitectura revisada y aceptada para continuar con la implementación. El contrato JWT compartido sigue pendiente de confirmación por los consumidores.
**Estado de ejecución:** ver la sección 17 (actualización del 11/11/2026). La versión 1.0 se conserva sin cambios en sus decisiones.

## 1. Objetivo y alcance

Incorporar autenticación JWT, autorización por roles e integración segura entre microservicios en `ms-usuarios`, preservando el CRUD de P1 y los controles de calidad de P2.

`ms-usuarios` debe:

1. Exponer `POST /api/auth/login`.
2. Buscar al usuario por correo y verificar la contraseña con BCrypt.
3. Emitir JWT firmados con HS256 (emisor exclusivo del contrato).
4. Proteger el CRUD mediante Spring Security y aplicar autorización por roles.
5. Validar tokens con verificación criptográfica real.
6. Incorporar un cliente `RestClient` hacia `ms-catalogo` con propagación del Bearer token.

**Fuera de alcance:** cambios en los claims del contrato, endpoint de cambio de contraseña, revocación de tokens, bloqueo de cuentas, modificación de migraciones Flyway, actualización de Spring Boot y desactivación de controles de calidad.

### Convención de etiquetas

| Etiqueta | Significado |
|---|---|
| **[V]** | Hecho verificado en el repositorio |
| **[C]** | Requisito del contrato JWT compartido (pendiente de confirmación) |
| **[H]** | Decisión humana explícita |
| **[P]** | Propuesta técnica sujeta a verificación durante la implementación |

## 2. Diagnóstico inicial del proyecto [V]

- Java 21, Spring Boot 3.5.16, Maven Wrapper, PostgreSQL, Spring Data JPA, Flyway y Lombok.
- Dependencias de seguridad actuales: solo `spring-security-crypto` (explícita). No hay Spring Security ni Actuator.
- Cinco endpoints bajo `/api/usuarios` (`POST`, `GET` lista, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`) en `UsuarioController`.
- `UsuarioService.crear` calcula el hash BCrypt y fija el rol `SOLICITANTE`; `PUT` solo modifica nombre y correo. No existe endpoint de cambio de rol ni de contraseña, ni administrador inicial.
- `Usuario` guarda `passwordHash` pero no tiene `getPasswordHash()`. Las pruebas leen el campo con `ReflectionTestUtils`, por lo que el campo no debe renombrarse.
- `Rol` es un enum con `SOLICITANTE`, `AGENTE` y `ADMIN`, coincidentes con los roles del contrato.
- `UsuarioRepository` es un `JpaRepository` sin consultas propias. El correo se almacena normalizado (`strip().toLowerCase(Locale.ROOT)`), con `CHECK (email = lower(email))` e índice único funcional sobre `lower(email)`.
- `PasswordConfig` expone un `BCryptPasswordEncoder`. `CrearUsuarioRequest` limita la contraseña a 72 bytes UTF-8 y oculta la contraseña en `toString`.
- `ApiExceptionHandler` usa `ProblemDetail` e incluye un `@ExceptionHandler(Exception.class)` que responde 500. Por tanto, una `AccessDeniedException` lanzada dentro de un controlador (por ejemplo, con `@PreAuthorize`) se convertiría en 500; la autorización se resolverá en la cadena de filtros.
- `application.yaml` no define `server.error.*` ni `management.*`. El perfil `test` usa H2 con `create-drop` y Flyway deshabilitado.
- Pruebas: 18 aprobadas (8 de servicio, 8 de controlador con `@WebMvcTest`, 1 de DTO y 1 de contexto). Cobertura de líneas de referencia: 77.78 % (umbral JaCoCo: 70 %). El pom no define Failsafe, por lo que toda clase de prueba debe terminar en `*Test` (o `*Tests`, patrón existente) para ejecutarse con Surefire.
- Controles de calidad de P2: Checkstyle, PMD, CPD, SpotBugs con FindSecBugs, OWASP Dependency-Check (falla con CVSS ≥ 7, con supresiones temporales documentadas) y JaCoCo. Comando de verificación: `.\mvnw.cmd -B verify`.
- `docs/seguridad.md` no existe en este workspace; el contrato se tomó de la especificación recibida en la práctica (repositorio del Equipo 4, rama `docs/p3-seguridad-jwt`).

## 3. Contrato JWT compartido [C]

El contrato fue propuesto en el repositorio del Equipo 4 y **no está confirmado por todos los consumidores**. `ms-usuarios` no propondrá cambios unilaterales en sus claims. Consumidores previstos: `ms-catalogo`, `ms-notificaciones` y `ms-tickets`.

| Elemento | Valor |
|---|---|
| Emisor | Exclusivamente `ms-usuarios` |
| Algoritmo | HS256 |
| Secreto | Variable `JWT_SECRET`, Base64 estándar; mínimo 32 bytes tras decodificar |
| `iss` | `ms-usuarios` |
| `sub` | ID del usuario como String |
| `roles` | Arreglo de String sin prefijo: `SOLICITANTE`, `AGENTE`, `ADMIN` |
| `iat` | NumericDate |
| `exp` | NumericDate; vigencia de 3600 s (`exp = iat + 3600`) |
| Transporte | `Authorization: Bearer <token>` |
| Autoridades Spring | Prefijo `ROLE_` aplicado al convertir `roles` |

El token no incluirá claims adicionales (sin correo, nombre ni hash). El valor real de `JWT_SECRET` nunca se versiona ni aparece en respuestas, mensajes de error o registros.

## 4. Decisiones vigentes

| Id | Decisión | Origen |
|---|---|---|
| D-01 | **Registro público provisional:** `POST /api/usuarios` es público y el rol queda fijado en `SOLICITANTE`. La política definitiva queda pendiente de validación de negocio. | [H] |
| D-02 | **CRUD administrativo:** `GET` lista, `GET /{id}`, `PUT /{id}` y `DELETE /{id}` solo para `ADMIN`. No hay acceso a recursos propios en P3. | [H] |
| D-03 | **Primer ADMIN (opción B2):** aprovisionamiento manual, sin `ApplicationRunner` ni semilla en migraciones. La contraseña administrativa nunca se incorpora a SQL versionado ni a documentación. | [H] |
| D-04 | **Validación estricta del claim `roles`:** obligatorio, arreglo no vacío de String con valores en {`SOLICITANTE`, `AGENTE`, `ADMIN`}. Ausente, nulo, vacío, de tipo incorrecto o con valores desconocidos → 401. Roles válidos pero insuficientes → 403. | [H] |
| D-05 | **Tolerancia de reloj de cero segundos**, configurada explícitamente. El token deja de aceptarse al alcanzar `exp`. Se prueba el comportamiento en el límite. | [H] |
| D-06 | **Catálogo independiente de la autenticación**, pero entregable obligatorio de P3: `RestClient`, propagación del Bearer y pruebas con servidor simulado antes de cerrar la práctica. La operación concreta espera el contrato de Gerardo. | [H] |
| D-07 | `getPasswordHash()` se acepta exclusivamente para uso interno de `AuthService`; nunca se expone en un DTO. | [H] |
| D-08 | No se implementa hash señuelo. El riesgo residual de enumeración por tiempo de respuesta se documenta. | [H] |
| D-09 | Se mantienen las cuatro dependencias aprobadas y se conserva `spring-security-crypto` explícita. | [H] |
| D-10 | Respuesta de login: `accessToken`, `tokenType: "Bearer"` y `expiresIn: 3600`. | [H][C] |
| D-11 | Los errores 401 y 403 usan respuestas genéricas compatibles con `ProblemDetail`. | [H] |
| D-12 | Se conservan las pruebas existentes y los controles de calidad de P2 (sin desactivar reglas ni bajar el umbral JaCoCo). | [H] |
| D-13 | Clave Base64 estándar únicamente; la clave decodificada debe tener al menos 32 bytes y la aplicación falla al arrancar si no se cumple. | [H][C] |
| D-14 | Los secretos de prueba se generan en memoria (ver sección 11.1). | [H] |
| D-15 | Un solo documento de plan vigente. Las diferencias entre las propuestas de Copilot y las correcciones humanas se registran únicamente en `BITACORA-IA.md`. | [H] |

## 5. Dependencias

| Dependencia | Estado | Alcance | Finalidad |
|---|---|---|---|
| `spring-boot-starter-security` | Nueva | compile | Filtros, `SecurityFilterChain`, autorización |
| `spring-boot-starter-oauth2-resource-server` | Nueva | compile | Validación de JWT Bearer (`JwtDecoder`, Nimbus) y emisión con `JwtEncoder` |
| `spring-boot-starter-actuator` | Nueva | compile | Endpoint `health` |
| `spring-security-test` | Nueva | test | `jwt()` y utilidades de MockMvc |
| `spring-security-crypto` | Existente | compile | `BCryptPasswordEncoder` (se conserva explícita) |
| `spring-boot-starter-web`, `-data-jpa`, `-validation`, `flyway-*`, `postgresql`, `lombok`, `spring-boot-starter-test`, `h2` | Existentes | — | Sin cambios |

- Se conserva Spring Boot 3.5.16; no se propone actualización.
- No se añaden bibliotecas JWT alternativas ni dependencias de pruebas HTTP adicionales: `MockRestServiceServer` ya forma parte de `spring-boot-starter-test`/Spring Test.
- Al incorporar las dependencias se revisarán `dependency:tree` y el resultado de OWASP Dependency-Check. Cualquier alerta nueva o cambio en las supresiones se documentará y se someterá a revisión antes de modificar su configuración.

## 6. Arquitectura y componentes

Se mantiene la arquitectura `controller → service → repository`; la lógica de negocio permanece en servicios. DTOs como `record` con Jakarta Validation; inyección por constructor; ninguna entidad JPA se expone en la API.

### 6.1 Archivos a crear

| Clase o archivo | Responsabilidad | Dependencias | Justificación |
|---|---|---|---|
| `config/JwtProperties` | Propiedades `seguridad.jwt.secret` (`${JWT_SECRET}`, sin valor por defecto), `issuer` (`ms-usuarios`) y `expiracion-segundos` (3600). Valida Base64 y ≥ 32 bytes sin imprimir el valor. | Spring Boot | Configuración externa, sin valores en código |
| `config/JwtConfig` | `SecretKeySpec` HmacSHA256, `JwtEncoder` (Nimbus con `ImmutableSecret`), `JwtDecoder` (`NimbusJwtDecoder` fijado a HS256) y validadores de emisor, expiración y claims obligatorios. | `JwtProperties` | Un único punto para firmar y validar con la misma clave |
| `config/SecurityConfig` | `SecurityFilterChain` stateless, CSRF deshabilitado, reglas por URL con `anyRequest().denyAll()`, `oauth2ResourceServer(jwt)`, `JwtAuthenticationConverter` (`roles` → `ROLE_*`). No depende de `JwtProperties`, para poder importarlo en `@WebMvcTest` con un `JwtDecoder` simulado. | `JwtDecoder`, manejador de errores | Autorización centralizada sin `@PreAuthorize` |
| `config/ManejadorErroresSeguridad` | `AuthenticationEntryPoint` y `AccessDeniedHandler` que producen 401/403 genéricos con `ProblemDetail`. | Jackson | Evitar los mensajes por defecto de Spring |
| `controller/AuthController` | `POST /api/auth/login` con `@Valid`. | `AuthService` | Capa web delgada |
| `service/AuthService` | Normaliza el correo, busca el usuario, verifica BCrypt y delega la emisión. Único consumidor de `getPasswordHash()`. | `UsuarioRepository`, `PasswordEncoder`, `TokenService` | Lógica de autenticación en la capa de servicio |
| `service/TokenService` | Emite el JWT con los claims `iss`, `sub`, `roles`, `iat`, `exp` y ningún otro. | `JwtEncoder`, `JwtProperties` | Aísla la emisión y facilita su prueba |
| `dto/LoginRequest` (record) | Correo y contraseña con `@NotBlank`/`@Size`, misma normalización de correo que `CrearUsuarioRequest` y `toString` sin contraseña. | Jakarta Validation | Contrato de entrada coherente |
| `dto/LoginResponse` (record) | `accessToken`, `tokenType` (`"Bearer"`) y `expiresIn` (3600). | — | Contrato de salida (D-10) |
| `exception/CredencialesInvalidasException` | Señala credenciales inválidas (mismo mensaje para usuario inexistente y contraseña incorrecta). | — | Respuesta 401 uniforme |

### 6.2 Archivos a crear en la fase de catálogo

| Clase | Responsabilidad |
|---|---|
| `config/CatalogoProperties` | Lee `SERVICIOS_CATALOGO_URL` y tiempos de espera; opcional. |
| `config/CatalogoClientConfig` | Construye el `RestClient` solo si la URL está configurada. |
| `client/CatalogoClient` | Infraestructura HTTP: URL base, propagación del Bearer y traducción de errores (404, 5xx, servicio no disponible). **No define rutas ni DTOs de catálogo** hasta que Gerardo confirme el contrato. |
| Excepciones de cliente | Recurso inexistente y servicio no disponible, con mapeo a `ProblemDetail` genérico. |

Los nombres exactos del paquete y de las excepciones de esta fase se confirmarán al implementarla. Antes de implementarla se definirá qué componente de `ms-usuarios` consume el cliente, para evitar código sin uso.

### 6.3 Archivos a modificar

| Archivo | Cambio |
|---|---|
| `pom.xml` | Añadir las cuatro dependencias; conservar `spring-security-crypto`. |
| `src/main/resources/application.yaml` | `seguridad.jwt.*`; `management.endpoints.web.exposure.include: health` con detalles ocultos; `server.error.include-message`, `include-stacktrace` y `include-binding-errors` en `never`. |
| `domain/Usuario` | Añadir `getPasswordHash()` (uso interno, sin exposición en DTO). No renombrar el campo. |
| `repository/UsuarioRepository` | Consulta parametrizada `@Query("select u from Usuario u where lower(u.email) = :email")`. Compatible con el índice `uq_usuarios_email_lower`; evita `IgnoreCase` derivado, que generaría `upper(...)`. |
| `exception/ApiExceptionHandler` | Manejador de `CredencialesInvalidasException` → 401 genérico. |
| `dto/CrearUsuarioRequest` o `RestriccionesUsuario` (ya existente) | Constante compartida del límite de 72 bytes, sin cambio de comportamiento. |
| `UsuarioControllerTest` (8 pruebas) y `MsUsuariosApplicationTests` | Adaptación a la seguridad (importar `SecurityConfig`, `JwtDecoder` simulado, `jwt()`, clave de pruebas) conservando las aserciones. |
| `README.md` | `JWT_SECRET`, autenticación, procedimiento B2 sin credenciales y cifra de pruebas actualizada (fase de documentación). |

### 6.4 Sin cambios

`PasswordConfig`, `Rol`, `UsuarioController`, `UsuarioService`, los DTOs de usuario (salvo la constante compartida indicada en 6.3), migraciones Flyway y `application-test.yaml`.

## 7. Flujo de autenticación

1. El cliente envía `POST /api/auth/login` con `email` y `password` (los mismos nombres de campo que `CrearUsuarioRequest` [V]).
2. `@Valid` rechaza campos vacíos y contraseñas de más de 72 bytes UTF-8 con 400.
3. `AuthService` normaliza el correo (`strip().toLowerCase(Locale.ROOT)`) y consulta `lower(email) = :email` mediante consulta parametrizada.
4. Si el usuario no existe o `PasswordEncoder.matches` falla, se lanza `CredencialesInvalidasException` → 401 con el mismo cuerpo en ambos casos.
5. Si coincide, `TokenService` emite el JWT: `iss=ms-usuarios`, `sub=String.valueOf(id)`, `roles=[rol.name()]`, `iat=ahora`, `exp=iat+3600`.
6. Se responde 200 con `{ accessToken, tokenType: "Bearer", expiresIn: 3600 }`.
7. El hash solo se lee dentro de `AuthService`; ni la respuesta, ni los registros, ni los mensajes de error lo contienen. No se registran contraseñas ni tokens.

Riesgo residual aceptado (D-08): el tiempo de respuesta puede diferir entre usuario inexistente y contraseña incorrecta.

## 8. Matriz de autorización aprobada

| Endpoint | Anónimo | SOLICITANTE | AGENTE | ADMIN |
|---|---|---|---|---|
| `POST /api/auth/login` | 200 / 400 / 401 | igual | igual | igual |
| `POST /api/usuarios` | 201 | 201 | 201 | 201 |
| `GET /api/usuarios` | 401 | 403 | 403 | 200 |
| `GET /api/usuarios/{id}` | 401 | 403 | 403 | 200 (o 404 si no existe) |
| `PUT /api/usuarios/{id}` | 401 | 403 | 403 | 200 (o 404 si no existe) |
| `DELETE /api/usuarios/{id}` | 401 | 403 | 403 | 204 (o 404 si no existe) |
| `GET /actuator/health` | 200 sin detalle | 200 | 200 | 200 |
| Cualquier otra ruta o método | 401 | 403 | 403 | 403 (`denyAll`) |

Los códigos de la matriz son los de la autorización; los errores de validación (400), de unicidad (409) y de recurso inexistente (404) son respuestas de negocio de la operación ya autorizada.

### 8.1 Autorización frente a recursos inexistentes

La decisión de autorización se toma en la cadena de filtros, antes de llegar al controlador y sin consultar la base de datos. Por tanto:

- **401** significa "no autenticado": token ausente o inválido. **403** significa "autenticado con roles válidos pero insuficientes". **404** significa "recurso inexistente", y solo se observa si el solicitante ya está autorizado (por ejemplo, `ADMIN` consultando un ID inexistente).
- Un usuario no `ADMIN` recibe 403 sobre `/api/usuarios/{id}` tanto si el ID existe como si no. Así no puede deducir qué identificadores existen.
- Una ruta no declarada o un método no declarado no revela si existe: el anónimo recibe 401 y el autenticado 403 (`denyAll`). No se responde 404 ni 405 para rutas no autorizadas.
- Las respuestas 401/403 no incluyen la ruta solicitada (`instance`), el motivo del rechazo ni detalles del token. La cabecera `WWW-Authenticate` será `Bearer` sin `error_description` ni la causa de la invalidez. **[P]** Se verificará que el despacho interno a `/error` no genere respuestas distintas a las descritas.
- Se mantiene el 404 de `UsuarioNoEncontradoException` existente para `ADMIN`. Antes de la implementación se comprobará cómo responde hoy una ruta inexistente a través de `ApiExceptionHandler` (cuya regla `Exception.class` responde 500) para no introducir regresiones.

## 9. Validación JWT y manejo de errores

### 9.1 Validación del token (`JwtDecoder` real)

| Verificación | Resultado si falla |
|---|---|
| Firma HS256 válida y algoritmo exactamente HS256 (rechazo de `none`, HS384/HS512 y otros) | 401 |
| `iss` igual a `ms-usuarios` | 401 |
| `exp` presente y token no expirado, **tolerancia de reloj 0 s** (D-05) | 401 |
| `iat` presente | 401 |
| `sub` presente y no vacío | 401 |
| `roles` presente, no nulo, arreglo no vacío de String, con valores en {`SOLICITANTE`, `AGENTE`, `ADMIN`} (D-04) | 401 |

- **Expiración:** `JwtTimestampValidator` usa por defecto 60 s de tolerancia, por lo que se configurará explícitamente con `Duration.ZERO`. **[P]** El validador estándar rechaza solo si el instante actual es posterior a `exp`; se añadirá una prueba de límite con reloj fijo (la fábrica del decoder recibirá un `Clock`, sin bean global) y, si el instante exacto `exp` se aceptara, se complementará con un validador propio que rechace cuando `ahora ≥ exp`.
- Un valor como `ROLE_ADMIN` en `roles` se considera valor desconocido → 401.
- La validación de `roles` se ejecuta en el decoder (autenticación fallida → 401) y no en la autorización; así 403 queda reservado para roles válidos insuficientes.
- Un token inválido o expirado devuelve 401 incluso en rutas públicas como `POST /api/usuarios` (comportamiento por defecto del filtro Bearer, aceptado y documentado).

### 9.2 Conversión de roles

`JwtAuthenticationConverter` lee el claim `roles` y asigna autoridades con prefijo `ROLE_` (`ROLE_ADMIN`, etc.). No se utilizan los scopes por defecto.

### 9.3 Errores 401 y 403

- `AuthenticationEntryPoint` y `AccessDeniedHandler` devuelven `ProblemDetail` genérico, sin `error_description`, sin mensajes de excepción y sin información de la ruta.
- El cuerpo es idéntico para token ausente, mal formado, expirado o alterado.
- `server.error.include-message`, `include-stacktrace` y `include-binding-errors` en `never`.
- No se exponen contraseñas, hashes, tokens ni secretos en respuestas ni registros.

## 10. Configuración de seguridad

- `SecurityFilterChain` con sesiones `STATELESS` y CSRF deshabilitado, por tratarse de una API exclusivamente Bearer sin cookies de sesión. **[P]** Si SpotBugs/FindSecBugs reporta este punto como falso positivo, se documentará y se solicitará revisión antes de modificar su configuración.
- Reglas por URL según la sección 8, terminadas en `anyRequest().denyAll()`.
- `JwtEncoder` y `JwtDecoder` compatibles con HS256, con el mismo secreto.
- Actuator: solo `health` expuesto, sin detalles (`show-details: never`). Cualquier otro endpoint de Actuator queda bajo `denyAll`.
- `JWT_SECRET` y `SERVICIOS_CATALOGO_URL` provienen de variables de entorno. La aplicación falla al arrancar si `JWT_SECRET` falta, no es Base64 o decodifica a menos de 32 bytes, sin imprimir el valor.
- `SERVICIOS_CATALOGO_URL` es opcional para el arranque y el login (D-06).
- **[P]** Se verificará que no aparezca la contraseña generada por defecto de Spring Boot en los registros (el bean `JwtDecoder` debería evitar la configuración de usuario por defecto).

## 11. Estrategia de pruebas

Las pruebas deben terminar en `Test`/`Tests` (el pom no define Failsafe) y conservar las 18 pruebas existentes. Se distinguen dos familias:

- **Pruebas de rutas con `jwt()`** (`@WebMvcTest` + `@Import(SecurityConfig)` + `@MockitoBean JwtDecoder`): verifican la matriz de autorización. `jwt()` **no** verifica firma ni expiración, y solo aplica el conversor de la aplicación si se declara `authorities` explícitas.
- **Pruebas criptográficas reales** (`@SpringBootTest` con perfil `test`, H2 y el `JwtDecoder` real): verifican firma, claims y expiración.

### 11.1 Clave JWT de pruebas generada en memoria

- La clave se genera al iniciar la JVM de pruebas con `SecureRandom` (32 bytes) y se codifica en Base64 estándar.
- Un componente de soporte en `src/test` genera la clave una sola vez por JVM, la registra como propiedad (`seguridad.jwt.secret`) mediante un mecanismo compartido y la expone a las pruebas que necesiten firmar tokens propios (por ejemplo, tokens alterados, expirados o con claims incorrectos).
- El mecanismo registra la propiedad en todo test que cargue el contexto Spring completo (incluida `MsUsuariosApplicationTests`). **[P]** Puede ser un `ApplicationContextInitializer` compartido o `@DynamicPropertySource` en una clase base; se decidirá en la fase 1. Debe usar una clave única por JVM para no multiplicar contextos en caché.
- No se versiona ningún secreto real ni fijo: ni en `application.yaml`, ni en `application-test.yaml`, ni en el código de pruebas, ni en el CI. La clave nunca se imprime.
- Un test del propio mecanismo comprobará que la clave decodificada tiene al menos 32 bytes.

### 11.2 Autenticación y emisión

| Caso | Resultado esperado |
|---|---|
| Login correcto | 200, `accessToken`, `tokenType: "Bearer"`, `expiresIn: 3600` |
| Contraseña incorrecta | 401 genérico |
| Usuario inexistente | 401 con cuerpo idéntico al anterior |
| Cuerpo inválido (vacío, `email` o `password` en blanco) | 400 |
| Contraseña > 72 bytes UTF-8 | 400 |
| Correo con mayúsculas o espacios | Coincide con el usuario normalizado |
| Respuesta de login | Sin contraseña ni hash |
| Claims del token emitido | Exactamente `iss`, `sub` (String), `roles` (arreglo de String), `iat`, `exp`; `exp − iat = 3600` |
| Compatibilidad | Un verificador HS256 independiente basado en la clave Base64 valida el token (simula el rol de un consumidor) |
| Flujo de extremo a extremo | Login de `ADMIN` seguido de `GET /api/usuarios` con el token → 200 |

### 11.3 Validación criptográfica con `JwtDecoder` real

| Caso | Resultado esperado |
|---|---|
| Token alterado (payload o firma) | 401 |
| Firma con clave distinta | 401 |
| `alg: none`, HS384 o HS512 | 401 |
| Emisor distinto | 401 |
| Token con `exp` anterior al instante actual | 401 |
| **Reloj fijo, `now = exp − 1 s`** | Aceptado |
| **Reloj fijo, `now = exp`** | Rechazado |
| **Reloj fijo, `now = exp + 1 s`** | Rechazado |
| Sin `exp`, sin `iat` o sin `sub` (o `sub` en blanco) | 401 |
| `roles` ausente, nulo, vacío, de tipo incorrecto (string, número, objeto, arreglo con elementos no String) o con valor desconocido (incluido `ROLE_ADMIN`) | 401 |
| `roles` válido pero insuficiente para la ruta | 403 |
| Token válido con rol permitido | Operación exitosa |

### 11.4 Autorización

- Para cada endpoint protegido de la matriz: sin token → 401; rol insuficiente → 403; rol permitido → éxito.
- `login`, el registro público y `health` son públicos y no requieren casos artificiales de 401/403; se comprueba que cualquier rol obtiene la respuesta pública.
- 404 de recurso frente a 403: `ADMIN` con ID inexistente → 404; `SOLICITANTE`/`AGENTE` con el mismo ID → 403; anónimo → 401.
- Ruta y método no declarados: anónimo → 401; autenticado (incluido `ADMIN`) → 403.
- Las respuestas 401/403 no contienen la ruta solicitada ni detalles del token; `WWW-Authenticate` es `Bearer` sin descripción.
- Actuator: `/actuator/health` → 200 sin detalles; otro endpoint de Actuator → 401/403 según la matriz.
- Prueba unitaria del `JwtAuthenticationConverter` (`roles` → `ROLE_*`).

### 11.5 Regresión

- Las 18 pruebas existentes se conservan; solo se adapta la configuración de seguridad de las pruebas de controlador y de contexto, sin cambiar sus aserciones.
- `.\mvnw.cmd -B verify` debe finalizar correctamente, con Checkstyle, PMD, CPD, SpotBugs, OWASP Dependency-Check y JaCoCo activos y el umbral de 70 % intacto.

## 12. Integración con `ms-catalogo`

- La autenticación no depende de catálogo (D-06): el arranque y el login funcionan sin `SERVICIOS_CATALOGO_URL`.
- Se implementará con `RestClient`; la URL base proviene de `SERVICIOS_CATALOGO_URL`, definida solo mediante configuración externa.
- Propagación: cuando `ms-usuarios` actúa en representación de un usuario, reenvía su Bearer token. El token solo se envía al host configurado y nunca se registra.
- El endpoint de consulta de categorías y el contrato JSON están **pendientes de confirmación por Gerardo**. No se inventan rutas ni DTOs; la fase 4 implementa y prueba solo la infraestructura (cliente, propagación, traducción de errores).
- Pruebas con servidor HTTP simulado (`MockRestServiceServer` o equivalente sin dependencias nuevas):

| Caso | Resultado esperado |
|---|---|
| Respuesta 200 | El cliente devuelve el resultado |
| Respuesta 404 | Excepción de recurso inexistente controlada |
| Respuesta 5xx | Error controlado |
| Servicio no disponible (puerto cerrado o fallo de conexión) | Error controlado, sin interrumpir el servicio |
| `Authorization: Bearer <token>` | Presente cuando se propaga; ausente cuando no hay token |
| Arranque y login sin `SERVICIOS_CATALOGO_URL` | Funcionan |

- La operación concreta de catálogo se completará cuando Gerardo confirme el contrato; mientras tanto, la integración se considera entregada en su infraestructura y pendiente en su contrato.

## 13. Agente revisor de seguridad

Entregable obligatorio, a preparar durante la implementación (fase 6):

`.github/agents/revisor-seguridad.agent.md`

Condiciones:

- Herramientas exclusivamente de **lectura y búsqueda**: sin edición, sin ejecución de comandos y sin escritura de archivos. Los nombres exactos de las herramientas se verificarán al crear el archivo.
- Revisión orientada a **OWASP Top 10** (control de acceso, fallas criptográficas, inyección, configuración insegura, componentes vulnerables, fallas de autenticación, registro y monitoreo, SSRF, entre otros).
- **Informe de hallazgos** con ID, severidad, categoría OWASP, archivo, línea, descripción del hallazgo, evidencia y corrección propuesta. No corrige el código por sí mismo.
- Alcance doble: la rama `practica-3` de este proyecto y **un Pull Request de otro integrante del equipo**, conforme a la guía de la práctica. El PR a revisar lo indicará el responsable; el agente solo lo lee y no lo modifica.
- Revisión humana de todos los hallazgos críticos y altos, y registro de las correcciones aplicadas y de los riesgos justificados.
- Los resultados alimentarán `docs/SECURITY-REPORT.md` y la bitácora.

## 14. Riesgos y decisiones pendientes

### 14.1 Riesgos

| Riesgo | Tratamiento |
|---|---|
| Secreto HS256 compartido entre emisor y tres consumidores: cualquiera de ellos podría emitir tokens válidos | Distribución privada por variable de entorno; rotación y distribución acordadas fuera del repositorio; riesgo inherente al contrato |
| Contrato JWT no confirmado por los consumidores | Prueba de compatibilidad independiente; sin cambios unilaterales; confirmación pendiente |
| Registro público: enumeración por 409, spam, sin verificación de correo ni límite de intentos | Política provisional (D-01); validación de negocio pendiente |
| Login sin limitación de intentos ni bloqueo (no hay campo de estado y las migraciones no se modifican) | Riesgo documentado |
| Diferencia de tiempo entre usuario inexistente y contraseña incorrecta | Riesgo residual aceptado (D-08) |
| Sin revocación: un token es válido hasta 1 h tras eliminar o degradar al usuario | Vigencia corta; riesgo documentado |
| Un `ADMIN` puede eliminarse o eliminar al último `ADMIN` | Decisión de negocio pendiente |
| B2 manual: sin auditoría automática y sin endpoint de cambio de contraseña | Procedimiento documentado sin credenciales; riesgo documentado |
| Token inválido en rutas públicas → 401 | Comportamiento aceptado y documentado |
| Despacho `/error` y rutas inexistentes frente a `ApiExceptionHandler` (`Exception.class` → 500) | Verificación previa y pruebas (sección 8.1) |
| `@PreAuthorize` futuro chocaría con `Exception.class` → 500 | No se usa seguridad por método en P3 |
| OWASP Dependency-Check con dependencias nuevas; supresiones temporales con vencimiento el 06/12/2026 | Revisar el reporte tras cambiar el pom; no desactivar el análisis |
| FindSecBugs (por ejemplo, CSRF deshabilitado) | Documentar posible falso positivo y solicitar revisión; no modificar reglas |
| Márgenes de JaCoCo, CPD y Checkstyle con código nuevo | Pruebas significativas; sin reducir el umbral |
| Transporte entre servicios sin HTTPS | Pendiente de acordar con el equipo |

### 14.2 Decisiones pendientes de aprobación humana

1. Política definitiva de registro público.
2. Consulta del perfil propio y acceso a recursos propios (fuera de P3).
3. Protección del último `ADMIN` y autoeliminación.
4. Confirmación del contrato JWT por `ms-catalogo`, `ms-notificaciones` y `ms-tickets`.
5. Contrato de catálogo (ruta, JSON, códigos de error), pendiente de Gerardo; HTTPS y servicio consumidor interno de `CatalogoClient`.
6. Mecanismo final de la clave de pruebas (inicializador compartido o `@DynamicPropertySource`).
7. Tratamiento de los hallazgos de SpotBugs/FindSecBugs y OWASP que surjan.

## 15. Fases de implementación

Cada fase termina con `.\mvnw.cmd -B verify` y no se inicia hasta que la anterior esté aprobada.

| Fase | Contenido | Pruebas |
|---|---|---|
| 0. Aprobación | Plan v1.0 y bitácora incorporados al repositorio | — |
| 1. Base de seguridad | Dependencias; `application.yaml`; `JwtProperties`; `JwtConfig` (HS256, emisor, expiración con 0 s, claims y `roles` estrictos); `SecurityConfig`; `ManejadorErroresSeguridad`; clave de pruebas en memoria | Matriz con `jwt()`; conversor de roles; 18 pruebas adaptadas; `contextLoads` con la clave en memoria; `dependency:tree` y reporte OWASP |
| 2. Login | Consulta por correo; `getPasswordHash()`; DTOs; `AuthService`; `TokenService`; `AuthController`; excepción y manejo de 401 | Casos de 11.2 |
| 3. Pruebas criptográficas y endurecimiento | Pruebas con `JwtDecoder` real; Actuator solo `health`; `server.error.*`; `denyAll`; 401/403 frente a 404 | Casos de 11.3 y 11.4 |
| 4. Catálogo | `RestClient`, propagación del Bearer, errores y configuración opcional; sin rutas inventadas | Casos de la sección 12 |
| 5. Documentación | README, procedimiento B2 sin credenciales, `docs/SECURITY-REPORT.md` y bitácora | Revisión documental |
| 6. Revisión y entrega | Agente `revisor-seguridad`, revisión de un PR de otro integrante, CI, Pull Request y tag | `.\mvnw.cmd -B verify` y CI en verde |

## 16. Criterios de aceptación

La práctica estará lista para revisión cuando:

- `POST /api/auth/login` emita JWT HS256 con exactamente los claims del contrato y `expiresIn: 3600`.
- Las credenciales incorrectas y el usuario inexistente produzcan el mismo 401 genérico.
- Los endpoints cumplan la matriz de la sección 8, con 401, 403 y 404 diferenciados según la sección 8.1.
- Los tokens alterados, con firma o algoritmo incorrectos, de otro emisor, expirados (incluido el instante exacto `exp`) o con `roles` inválido se rechacen con 401, y los roles válidos insuficientes reciban 403.
- Ninguna respuesta, mensaje de error o registro exponga contraseñas, hashes, tokens, secretos ni rutas no autorizadas.
- `JWT_SECRET` provenga solo del entorno, y no exista ningún secreto real ni fijo en archivos versionados.
- Las pruebas que cargan el contexto usen la clave generada en memoria.
- Actuator exponga únicamente `health`, sin detalles.
- La infraestructura de `RestClient` esté probada con 200, 404, error y servicio no disponible, con propagación del Bearer, y sin afectar el arranque ni el login.
- Las 18 pruebas existentes se conserven y `.\mvnw.cmd -B verify` finalice correctamente con todos los controles de calidad de P2 activos y el umbral de JaCoCo en 70 %.
- Exista `.github/agents/revisor-seguridad.agent.md`, con el informe de hallazgos propio y el de la revisión del PR de otro integrante, con revisión humana documentada.
- Se haya preparado `docs/SECURITY-REPORT.md` y la bitácora contenga las interacciones reales de P3.
- El Pull Request y el tag `p3-entrega` correspondan a la versión final.
- El contrato JWT figure como confirmado únicamente cuando los consumidores lo confirmen; mientras tanto se documenta como pendiente.

## 17. Estado de ejecución (actualización del 11/11/2026)

Esta sección registra el avance real frente a la planificación de las secciones 1 a 16. No modifica las decisiones D-01 a D-15, la matriz de autorización ni el contenido de las fases.

### 17.1 Convención de estado

| Etiqueta | Significado |
|---|---|
| **[V]** | Implementado y comprobado en el código o la configuración del repositorio |
| **[T]** | Cubierto por pruebas automatizadas (resultado reportado de `.\mvnw.cmd -B verify` o reportes de Surefire) |
| **[F]** | Verificado manualmente con Postman, PostgreSQL y Spring Boot (bitácora, PF-01 a PF-23) |
| **[P]** | Planificado; sin evidencia de implementación |
| **[R]** | Riesgo abierto o pendiente de decisión |
| **[C]** | Pendiente de confirmación por un tercero o equipo consumidor |

### 17.2 Estado por fase

| Fase | Estado | Evidencia |
|---|---|---|
| 0. Aprobación | Completada | Plan v1.0 y bitácora incorporados al repositorio (commit `e995a8f`) |
| 1. Base de seguridad | Implementada y verificada | [V] dependencias en `pom.xml`; [V] `seguridad.jwt`, `management` y `server.error.*` en `application.yaml`; [V] `JwtConfig` (HS256, emisor, tolerancia 0 s, claims requeridos), `JwtProperties`, `SecurityConfig`, `ManejadorErroresSeguridad`, `RolesClaimValidator`; [V] clave de pruebas en memoria (`ClaveJwtPruebas`); [T] 79 pruebas aprobadas; [T] cobertura JaCoCo de líneas 88.40 % (221/250) e instrucciones 86.99 % |
| 2. Login | Implementada en el árbol de trabajo, sin commit | [V] `POST /api/auth/login` en `AuthController`; [V] búsqueda por correo normalizado, `getPasswordHash()` y comprobación BCrypt en `AuthService`; [V] emisión HS256 en `TokenService` con los cinco claims y 3600 s; [V] límite de 72 bytes UTF-8; [T] `AuthControllerTest`, `TokenServiceTest`, `AuthServiceTest`, `LoginRequestTest`, `LoginResponseTest`; [F] PF-01 a PF-23 |
| 3. Pruebas criptográficas y endurecimiento | Parcialmente implementada | [V] Actuator expone solo `health`; [V] `server.error.*` en `never`; [V] `anyRequest().denyAll()`; [V] 401 y 403 genéricos; [T] matriz con `jwt()` en `UsuarioControllerSeguridadTest`. [P] Pruebas de límite exacto `exp` (instante `now == exp`) y de firma alterada con `JwtDecoder` real: no se encontraron en las pruebas actuales |
| 4. Catálogo | No implementada | [P] No existen `CatalogoClient`, `RestClient`, propiedades de catálogo ni `SERVICIOS_CATALOGO_URL` en el código |
| 5. Documentación | Parcialmente implementada | [V] `docs/SECURITY-REPORT.md` existe como evidencia de P3-05 y no está completamente actualizado con P3-06; su apartado de PF-23 está presente, pero la revisión global del reporte con P3-06 queda pendiente; [V] bitácora actualizada (sin commit); [P] README sin documentación de `JWT_SECRET` ni de autenticación; [P] procedimiento B2 (creación de administrador) sin documentar en el repositorio |
| 6. Revisión y entrega | Pendiente | [P] `.github/agents/revisor-seguridad.agent.md` no existe; [P] tag `p3-entrega` no existe (solo `p1-entrega` y `p2-entrega`); [P] Pull Request y revisión de un PR de otro integrante |

### 17.3 Pendientes verificados

| Pendiente | Estado | Evidencia y comentario |
|---|---|---|
| Integración con `ms-catalogo` mediante `RestClient` | [P] No implementada | Sin código de cliente ni pruebas con servidor simulado (sección 12) |
| Confirmación del contrato JWT con `ms-catalogo`, `ms-notificaciones` y `ms-tickets` | [C] Pendiente | Sin evidencia de confirmación en el repositorio |
| Contrato de integración con catálogo (ruta, JSON, errores) | [C] Pendiente de Gerardo | No se han inventado rutas ni DTOs |
| Revisión de vulnerabilidades OWASP y supresiones temporales | [R] Abierto | `docs/SECURITY-REPORT.md` documenta tres CVE presentes en 6.5.11 y sin corregir, con 33 reglas de supresión en `config/owasp-suppressions.xml` (las reglas no equivalen a vulnerabilidades): 31 con vencimiento `2026-12-06Z` y 2 con `2027-02-06Z`. Desglose: 18 reglas de Spring Security 6.5.11 incorporadas en P3-05C (3 CVE × 6 artefactos, subconjunto del inventario); 3 reglas previas de `spring-security-crypto` (P2), actualizadas a `2026-12-06Z` en P3-05C (antes `2027-02-06Z`, según `docs/SECURITY-REPORT.md`); 10 reglas de Spring Framework 6.2.19 de P2-22 con `2026-12-06Z`; y 2 reglas de Spring Framework 6.2.19 para CVE-2026-47888 y CVE-2026-47893 (P2) con `2027-02-06Z`. Las reglas de P3-05C son aceptación temporal de riesgo, no falsos positivos demostrados; las demás se asocian por CPE según su nota. La decisión es del responsable del desarrollo y no hay aprobación de un tercero |
| Evaluación de revocación o invalidación de JWT | [R] Riesgo confirmado | No existe revocación inmediata. PF-23: un JWT ADMIN siguió concediendo acceso (HTTP 200 en `GET /api/usuarios`) después de eliminar la cuenta, dentro de la vigencia de 3600 s. Mitigación pendiente de diseño y aprobación |
| Validación de reglas provisionales de negocio | [R] Pendiente de aprobación | D-01 (registro público provisional) y decisiones 1 a 3 de la sección 14.2 siguen abiertas |

### 17.4 Riesgos de la sección 14.1 que se han materializado o cambiado

| Riesgo original | Estado actual |
|---|---|
| Sin revocación: un token es válido hasta 1 h tras eliminar o degradar al usuario | Materializado en PF-23 (ver 17.3). La vigencia corta no basta como mitigación |
| Contrato JWT no confirmado por los consumidores | Sigue abierto |
| OWASP Dependency-Check con dependencias nuevas; supresiones temporales | Abierto. 31 de 33 reglas de supresión vencen el `2026-12-06Z`; 2 reglas de Spring Framework 6.2.19 (CVE-2026-47888 y CVE-2026-47893) vencen el `2027-02-06Z` |
| `Exception.class` → 500 en `ApiExceptionHandler` | No verificado en esta actualización; pendiente de la verificación de la sección 8.1 |

### 17.5 Desviaciones respecto de la planificación

- La sección 16 exige probar el instante exacto `exp`. Hasta esta actualización no hay evidencia de esa prueba; permanece como pendiente.
- La fase 4 y la fase 6 no han comenzado. La fase 3 está incompleta en sus pruebas criptográficas.
- Los cambios de P3-05 (commit `9a5b295`) y de P3-06 (sin commit) no están todavía consolidados en una entrega final.

### 17.6 Criterios de aceptación (sección 16): estado

| Criterio | Estado |
|---|---|
| Login emite HS256 con los claims del contrato y `expiresIn: 3600` | [V][T] |
| Mismo 401 genérico para credenciales incorrectas y usuario inexistente | [V][T][F] |
| 401 y 403 según la matriz de la sección 8 | [V][T] |
| 404 por recurso inexistente (sección 8.1) | [P] Pendiente de comprobación; no verificado |
| Rechazo de tokens alterados, de otro emisor, expirados (incluido `exp` exacto) o con `roles` inválido | [V] parcial; [P] prueba de `exp` exacto y de firma alterada con `JwtDecoder` real |
| Sin exposición de secretos, hashes ni tokens | [V] en código revisado; [P] sin revisión de logs completos |
| `JWT_SECRET` solo desde el entorno | [V] `application.yaml` usa `${JWT_SECRET}`; [P] documentación en README pendiente |
| Pruebas con contexto usan clave en memoria | [V] `ClaveJwtPruebas` |
| Actuator solo `health` | [V] |
| Infraestructura de `RestClient` probada | [P] No implementada |
| `.\mvnw.cmd -B verify` correcto con calidad P2 activa y JaCoCo en 70 % | [T] Reportado por la verificación integral; no se volvió a ejecutar en esta actualización |
| Agente `revisor-seguridad` con revisión humana | [P] Pendiente |
| `docs/SECURITY-REPORT.md` preparado y bitácora con interacciones reales | [V] |
| Pull Request y tag `p3-entrega` | [P] Pendiente |
| Contrato JWT marcado como confirmado solo tras confirmación de consumidores | [C] Sigue pendiente |

### 17.7 Próximos pasos sugeridos

1. Añadir pruebas de límite `exp` (anterior, igual y posterior) y de firma alterada con `JwtDecoder` real.
2. Decidir el tratamiento de PF-23 (validación de existencia del usuario o revocación) antes de cualquier despliegue.
3. Implementar la fase 4 solo tras la confirmación del contrato de catálogo por Gerardo, o dejarla fuera de la entrega con aprobación explícita.
4. Documentar `JWT_SECRET` y el procedimiento B2 en el README, sin credenciales.
5. Crear el agente revisor y ejecutar la revisión humana antes del tag `p3-entrega`.

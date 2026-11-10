# Plan de seguridad — Práctica 3

**Proyecto:** Sistema de Mesa de Ayuda  
**Microservicio:** `ms-usuarios`  
**Rama:** `practica-3`  
**Fecha:** 10/11/2026  
**Versión:** 0.1 — Plan técnico preliminar  
**Estado:** Pendiente de revisión, corrección y aprobación

## 1. Objetivo

Incorporar autenticación JWT, autorización por roles e integración segura entre microservicios, preservando el CRUD desarrollado en P1 y los controles de calidad incorporados durante P2.

`ms-usuarios` será responsable de autenticar credenciales, emitir JWT y proteger sus endpoints. Además, actuará como consumidor de `ms-catalogo`.

## 2. Estado inicial

El proyecto parte de:

- Java 21 y Spring Boot 3.5.16.
- CRUD REST de usuarios con cinco endpoints.
- PostgreSQL, Spring Data JPA y Flyway.
- Contraseñas almacenadas mediante BCrypt.
- `PasswordEncoder` existente en `PasswordConfig`.
- Pruebas automatizadas con JUnit 5, Mockito y MockMvc.
- Perfil de pruebas H2.
- 18 pruebas aprobadas en P2.
- Cobertura local inicial de 77.78 % de líneas.
- CI con controles Checkstyle, PMD, CPD, SpotBugs, OWASP Dependency-Check y JaCoCo.

Actualmente el proyecto no dispone de autenticación JWT ni de una configuración completa de autorización por roles.

## 3. Dependencias previstas

| Dependencia | Finalidad |
|---|---|
| `spring-boot-starter-security` | Configuración de autenticación y autorización |
| `spring-boot-starter-oauth2-resource-server` | Validación de tokens JWT Bearer |
| `spring-boot-starter-actuator` | Endpoint de disponibilidad `health` |
| `spring-security-test` | Pruebas MockMvc de autenticación y roles |

Se conservará Spring Boot 3.5.16. Antes de incorporar dependencias adicionales deberá revisarse su disponibilidad transitiva y su compatibilidad con el `pom.xml` existente.

No se incorporarán bibliotecas JWT alternativas sin una necesidad técnica demostrada.

## 4. Componentes propuestos

| Componente | Acción | Responsabilidad |
|---|---|---|
| `UsuarioRepository` | Modificar | Incorporar una consulta de usuario por correo normalizado |
| `PasswordConfig` | Reutilizar | Verificación BCrypt |
| `AuthController` | Crear | Exponer `POST /api/auth/login` |
| `AuthService` | Crear | Autenticar credenciales y coordinar emisión del JWT |
| DTO de login | Crear | Recibir correo y contraseña |
| DTO de respuesta | Crear | Entregar token, tipo y vigencia |
| `SecurityConfig` | Crear | Configurar filtros, sesiones, CSRF y autorizaciones |
| Configuración JWT | Crear | Encoder, decoder, secreto y validadores |
| Conversor de roles | Configurar | Transformar `roles` en autoridades `ROLE_` |
| Cliente de catálogo | Crear | Consumir un endpoint externo con `RestClient` |
| Pruebas de seguridad | Crear/modificar | Validar autenticación, permisos y token |
| Configuración YAML | Modificar | Incorporar variables de entorno y Actuator |

Las clases definitivas se determinarán durante la revisión del plan para evitar duplicación de responsabilidades.

## 5. Contrato JWT

Se seguirá `docs/seguridad.md` como referencia compartida.

- Algoritmo: HS256.
- Secreto: variable `JWT_SECRET` codificada en Base64.
- Clave: mínimo 32 bytes después de decodificar.
- Emisor (`iss`): `ms-usuarios`.
- Sujeto (`sub`): identificador del usuario como String.
- Roles (`roles`): arreglo de cadenas sin prefijo.
- Emisión (`iat`): fecha de emisión.
- Expiración (`exp`): 60 minutos.
- Autorización HTTP: `Authorization: Bearer <token>`.

Se validará criptográficamente la firma, la expiración y el emisor.

El valor real del secreto no se incluirá en el repositorio.

## 6. Matriz preliminar de autorización

La siguiente matriz representa una política inicial restrictiva para la implementación de seguridad. Requiere validación frente a las reglas de negocio.

| Endpoint | Público | SOLICITANTE | AGENTE | ADMIN |
|---|---|---|---|---|
| `POST /api/auth/login` | Sí | Sí | Sí | Sí |
| `POST /api/usuarios` | No | No | No | Sí |
| `GET /api/usuarios` | No | No | No | Sí |
| `GET /api/usuarios/{id}` | No | No | No | Sí |
| `PUT /api/usuarios/{id}` | No | No | No | Sí |
| `DELETE /api/usuarios/{id}` | No | No | No | Sí |
| `GET /actuator/health` | Sí | Sí | Sí | Sí |

Para P3 se propone una política administrativa inicial sobre el CRUD completo, evitando exponer información de terceros sin una regla de propiedad de recursos confirmada.

Quedan pendientes la política de registro público, la consulta del perfil propio y la creación inicial de un administrador.

## 7. Configuración de seguridad

Se implementará:

- `SecurityFilterChain` con autenticación por JWT.
- Sesiones HTTP `STATELESS`.
- CSRF deshabilitado para la API exclusivamente Bearer.
- `JwtDecoder` HS256 con secreto externo.
- Validación de expiración y emisor.
- Conversión explícita de roles al prefijo `ROLE_`.
- Denegación por defecto de operaciones no autorizadas.
- Actuator con exposición exclusiva de `health`.
- `include-message: never`.
- `include-stacktrace: never`.

## 8. Plan de pruebas

### Autenticación

| Caso | Resultado esperado |
|---|---|
| Login con credenciales correctas | 200 y JWT válido |
| Contraseña incorrecta | 401 |
| Usuario inexistente | 401, sin revelar existencia de la cuenta |
| Token alterado | 401 |
| Token expirado | 401 |
| Token firmado con clave incorrecta | 401 |
| Algoritmo no permitido | 401 |
| Token válido con claims requeridos | Autenticación correcta |

### Autorización

Para cada endpoint protegido:

- Sin token: 401.
- Token válido con rol insuficiente: 403.
- Token válido con rol permitido: respuesta exitosa de la operación.

`login` y `health` son excepciones públicas y no requieren casos artificiales de 401/403.

Las pruebas existentes del CRUD serán adaptadas para incorporar autenticación simulada mediante `jwt()` sin desactivar los filtros de seguridad.

Las pruebas de firmas y expiración utilizarán el decoder real, dado que `jwt()` no verifica la firma criptográfica.

### Integración

Se comprobará:

- Propagación del Bearer token al servicio de catálogo.
- Respuesta exitosa 200.
- Respuesta externa 404.
- Servicio externo no disponible.

Se empleará `MockRestServiceServer` o una herramienta equivalente.

## 9. Integración entre microservicios

`ms-usuarios` emitirá JWT compatibles con:

- `ms-catalogo`.
- `ms-notificaciones`.

Como consumidor, implementará una consulta de categorías mediante `RestClient`.

La URL base se obtendrá de `SERVICIOS_CATALOGO_URL`.

El endpoint específico y el contrato JSON de catálogo están pendientes de confirmación por parte de Gerardo.

## 10. Agente revisor de seguridad

Se preparará:

`.github/agents/revisor-seguridad.agent.md`

Condiciones:

- Herramientas exclusivamente de lectura y búsqueda.
- Sin edición automática.
- Revisión orientada a OWASP Top 10.
- Informe con ID, severidad, categoría OWASP, archivo, línea, hallazgo y corrección propuesta.
- Revisión humana de todos los hallazgos críticos y altos.
- Registro de correcciones aplicadas y riesgos justificados.

El agente deberá revisar también un PR de otro integrante del equipo, conforme a la guía.

## 11. Riesgos

| Riesgo | Tratamiento previsto |
|---|---|
| Incompatibilidad de claims entre servicios | Contrato JWT compartido y prueba de integración |
| Exposición del secreto HS256 | Variables de entorno y distribución privada |
| Token aceptado sin validar firma | Pruebas criptográficas con decoder real |
| Roles sin prefijo correcto | Conversor explícito y pruebas 403/200 |
| Pérdida de acceso al CRUD | Adaptación de pruebas y matriz de permisos |
| Ausencia de administrador inicial | Definir aprovisionamiento seguro antes de operar |
| Caída de catálogo | Manejo controlado de errores |
| Regresiones en P2 | Ejecutar Maven verify y comprobar CI |
| Cobertura insuficiente | Añadir pruebas significativas sin reducir el umbral |
| Vulnerabilidades conocidas de Spring | Mantener trazabilidad de los riesgos documentados en P2 |

## 12. Plan de trabajo

| Fase | Entregable |
|---|---|
| 1. Contrato | `docs/seguridad.md` |
| 2. Planificación | Plan elaborado y corregido en modo Planning |
| 3. Autenticación | Endpoint login y emisión JWT |
| 4. Autorización | Matriz aplicada a los endpoints |
| 5. Pruebas | Casos 401, 403, éxito y validaciones criptográficas |
| 6. Integración | Cliente `RestClient` y pruebas simuladas |
| 7. Revisión | Agente de seguridad y hallazgos |
| 8. Evidencias | `docs/SECURITY-REPORT.md` y bitácora |
| 9. Entrega | Maven verify, CI, PR y tag |

## 13. Revisión y aprobación del plan

**Estado actual:** Plan preliminar preparado antes de implementar código.

Para satisfacer la evidencia de la práctica:

1. Ejecutar un prompt en modo Plan/Planning/Spec.
2. Contrastar la propuesta con el contrato JWT y los requisitos de P3.
3. Identificar y corregir decisiones técnicas de la propuesta.
4. Registrar las correcciones reales en `BITACORA-IA.md`.
5. Aprobar el plan corregido antes de modificar la implementación.
6. Incorporar el plan corregido a la descripción del PR, marcando las decisiones humanas.

No se declararán como realizadas interacciones o correcciones que todavía no hayan ocurrido.

## 14. Criterios de finalización

La práctica estará lista para revisión cuando:

- El login emita JWT válidos mediante HS256.
- Los endpoints estén protegidos según la matriz aprobada.
- Las pruebas de seguridad e integración pasen.
- El CI mantenga activos los controles de calidad.
- El agente revisor y sus hallazgos estén documentados.
- Existan al menos cuatro entradas reales de P3 en la bitácora.
- Se haya preparado `docs/SECURITY-REPORT.md`.
- El PR y el tag `p3-entrega` correspondan a la versión final.
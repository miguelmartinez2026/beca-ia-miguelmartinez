# Requerimientos — MS-Usuarios

**Proyecto:** Sistema de Mesa de Ayuda  
**Microservicio:** `ms-usuarios`  
**Práctica:** P1 — Del requerimiento al código con prompts  
**Versión:** 0.3 — Revisión técnica para implementación  
**Estado:** Base de desarrollo con decisiones provisionales  
**Herramienta de IA utilizada en el análisis inicial:** GitHub Copilot

---

## 1. Necesidad del negocio

Se requiere gestionar los usuarios de un Sistema de Mesa de Ayuda.

El sistema debe permitir registrar usuarios e iniciar sesión de manera segura.

Se contemplan tres roles: SOLICITANTE, AGENTE y ADMIN.

La autenticación utilizará tokens JWT.

Los demás microservicios deberán poder identificar a los usuarios autenticados.

Se necesita consultar y mantener actualizada la información de los usuarios.

Las reglas específicas de administración, permisos y atributos deberán validarse antes de considerarse definitivas.

## 2. Alcance y procedencia de los requisitos

### 2.1 Capacidades establecidas en la guía

El microservicio `ms-usuarios` es responsable del registro de usuarios, inicio de sesión, emisión de JWT y manejo de los roles SOLICITANTE, AGENTE y ADMIN.

La Práctica 1 requiere desarrollar un CRUD REST con persistencia, validaciones, manejo de errores y pruebas.

### 2.2 Propuestas identificadas en el análisis con GitHub Copilot

Las siguientes funcionalidades surgieron durante el análisis y permanecen pendientes de validación:

- Registro público de usuarios.
- Creación administrativa de cuentas AGENTE y ADMIN.
- Modificación administrativa de roles.
- Activación y desactivación de cuentas.
- Consulta del perfil propio.

No se consideran funcionalidades obligatorias de P1 únicamente por haber sido sugeridas por Copilot.

### 2.3 Distribución de responsabilidades

| Práctica | Alcance |
|---|---|
| P1 | Requerimientos, modelo de datos, migración Flyway, CRUD, validaciones, errores y pruebas |
| P2 | Calidad de código y herramientas de análisis |
| P3 | Autenticación JWT, autorización por roles y comunicación segura |
| P4 | Contenedores, integración y CI |

La protección de contraseñas deberá implementarse desde P1.

## 3. Historias de usuario

Se conservan las siete historias identificadas en la primera interacción con Copilot.

| ID | Historia | Prioridad preliminar | Estado |
|---|---|---|---|
| HU-01 | Como usuario, quiero registrar una cuenta para acceder al Sistema de Mesa de Ayuda. | Must | Capacidad identificada |
| HU-02 | Como usuario registrado, quiero iniciar sesión de manera segura para acceder al sistema. | Must | Capacidad identificada |
| HU-03 | Como microservicio consumidor, quiero identificar al usuario autenticado mediante JWT para procesar las operaciones correspondientes. | Must | Integración por definir |
| HU-04 | Como administrador, quiero crear cuentas para agentes y administradores para gestionar al personal del sistema. | Should | Propuesta |
| HU-05 | Como administrador, quiero modificar los roles asignados a los usuarios para gestionar sus responsabilidades. | Should | Propuesta |
| HU-06 | Como administrador, quiero activar o desactivar cuentas para controlar su acceso. | Could | Propuesta |
| HU-07 | Como usuario autenticado, quiero consultar mi perfil para conocer mis datos registrados. | Could | Propuesta |

Las prioridades son preliminares y no constituyen aprobación formal de un cliente.

## 4. Criterios de aceptación de las historias de usuario

Los siguientes criterios se consolidaron a partir de las dos interacciones con Copilot y su revisión posterior. Los escenarios de funcionalidades propuestas no equivalen a requisitos aprobados.

### HU-01 — Registro de usuarios

**Escenario: registro exitoso**

Dado que se proporciona información válida para el registro  
Cuando se solicita crear una cuenta  
Entonces el sistema registra al usuario correctamente  
Y almacena su contraseña protegida

**Escenario: registro con información inválida**

Dado que se proporciona información obligatoria incompleta o inválida  
Cuando se solicita registrar al usuario  
Entonces el sistema rechaza la operación  
Y devuelve los errores de validación

**Pendiente:** confirmar el mecanismo de registro y los datos obligatorios definitivos.

### HU-02 — Inicio de sesión

**Escenario: autenticación exitosa**

Dado que existe un usuario registrado  
Cuando proporciona credenciales correctas  
Entonces el sistema permite su autenticación

**Escenario: credenciales incorrectas**

Dado que se proporcionan credenciales incorrectas  
Cuando se intenta iniciar sesión  
Entonces el sistema rechaza la autenticación

**Implementación principal:** P3.

### HU-03 — Identificación mediante JWT

**Escenario: JWT válido**

Dado que un microservicio recibe un JWT válido  
Cuando verifica el token mediante el mecanismo acordado  
Entonces puede identificar al usuario autenticado

**Escenario: JWT inválido**

Dado que un microservicio recibe un JWT inválido  
Cuando intenta verificarlo  
Entonces la validación falla

**Implementación principal:** P3.

### HU-04 — Creación administrativa de cuentas

**Estado:** propuesta pendiente de validación.

**Escenario: creación autorizada**

Dado que un administrador tiene permisos para crear cuentas  
Cuando solicita crear una cuenta AGENTE o ADMIN con datos válidos  
Entonces el sistema registra la cuenta con el rol correspondiente

**Escenario: creación no autorizada**

Dado que un usuario no tiene permisos administrativos  
Cuando intenta crear una cuenta AGENTE o ADMIN  
Entonces el sistema rechaza la operación

### HU-05 — Modificación de roles

**Estado:** propuesta pendiente de validación.

**Escenario: asignación válida**

Dado que un administrador tiene permisos para modificar roles  
Cuando asigna un rol permitido a un usuario existente  
Entonces el sistema actualiza su rol

**Escenario: rol no reconocido**

Dado que se solicita asignar un rol desconocido  
Cuando el sistema valida la modificación  
Entonces rechaza la operación  
Y devuelve un error de validación

### HU-06 — Activación y desactivación

**Estado:** propuesta pendiente de validación.

**Escenario: desactivación autorizada**

Dado que existe una cuenta activa  
Y un administrador tiene permisos para desactivarla  
Cuando solicita su desactivación  
Entonces el sistema cambia su estado a inactiva

**Escenario: desactivación no autorizada**

Dado que un usuario no tiene permisos administrativos  
Cuando intenta desactivar una cuenta  
Entonces el sistema rechaza la operación

**Observación:** esta funcionalidad requeriría un atributo de estado que no forma parte del modelo inicial de P1.

### HU-07 — Consulta del perfil propio

**Estado:** propuesta pendiente de validación.

**Escenario: consulta autorizada**

Dado que un usuario está autenticado  
Cuando solicita consultar su perfil  
Entonces el sistema devuelve sus datos permitidos

**Escenario: consulta sin autenticación**

Dado que una solicitud no contiene autenticación válida  
Cuando intenta consultar un perfil protegido  
Entonces el sistema rechaza el acceso

**Observación:** la consulta de perfil autenticado es distinta de la operación de consulta por ID utilizada para demostrar el CRUD técnico de P1.

## 5. Contrato técnico provisional del CRUD de P1

Las operaciones siguientes se definen para cumplir la Práctica 1. Constituyen decisiones de implementación y no nuevas historias atribuidas a Copilot.

### 5.1 Endpoints

| Método | Endpoint propuesto | Operación | Respuesta exitosa |
|---|---|---|---|
| POST | `/api/usuarios` | Crear usuario | 201 Created |
| GET | `/api/usuarios/{id}` | Consultar por ID | 200 OK |
| GET | `/api/usuarios` | Listar usuarios | 200 OK |
| PUT | `/api/usuarios/{id}` | Actualizar nombre y correo | 200 OK |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario | 204 No Content |

### 5.2 Datos de entrada

**Create — POST**

Campos recibidos:

- `nombre`: obligatorio.
- `email`: obligatorio.
- `password`: obligatoria.

El servidor asignará provisionalmente el rol `SOLICITANTE` para esta operación de P1.

El cliente no podrá proporcionar `id`, `password_hash` ni `created_at`.

Tampoco podrá asignarse directamente el rol AGENTE o ADMIN.

La creación de cuentas con roles administrativos quedará para la validación de reglas de negocio y la autorización de P3.

**Update — PUT**

Campos recibidos:

- `nombre`: obligatorio.
- `email`: obligatorio.

Esta operación representa la sustitución de los datos editables del usuario. Ambos campos deberán proporcionarse.

No se permitirá modificar mediante este endpoint:

- `id`.
- `rol`.
- `password_hash`.
- `created_at`.

El cambio de contraseña y la administración de roles tendrán contratos propios cuando esas funcionalidades se definan.

### 5.3 Datos de salida

Los endpoints que devuelvan información de usuarios expondrán únicamente:

- `id`.
- `nombre`.
- `email`.
- `rol`.
- `createdAt`.

Nunca devolverán `password` ni `password_hash`.

### 5.4 Reglas de validación

| Atributo | Regla provisional |
|---|---|
| `nombre` | Obligatorio, sin espacios en blanco como único contenido; longitud máxima de 120 caracteres |
| `email` | Obligatorio, formato de correo válido, longitud máxima de 254 caracteres |
| `password` | Obligatoria en creación, mínimo 8 caracteres; las restricciones adicionales deberán ajustarse al algoritmo de hash seleccionado |
| `rol` | Un único valor entre SOLICITANTE, AGENTE y ADMIN |
| `id` | Generado por la base de datos, no editable |
| `created_at` | Generado automáticamente, no editable |

Para esta primera implementación, el correo se normalizará eliminando espacios externos y convirtiéndolo a minúsculas antes de guardarlo.

La unicidad será independiente de mayúsculas y minúsculas.

Estas reglas son decisiones técnicas provisionales.

### 5.5 Manejo de errores

| Condición | Respuesta prevista |
|---|---|
| Información de entrada inválida | 400 Bad Request |
| Usuario inexistente | 404 Not Found |
| Correo electrónico duplicado | 409 Conflict |
| Error inesperado del servidor | 500 Internal Server Error |

Los errores se devolverán mediante `ProblemDetail`, con manejo centralizado mediante `@RestControllerAdvice`.

No se expondrán contraseñas ni información sensible en los mensajes de error.

### 5.6 Restricción de seguridad para P1

La implementación del CRUD de P1 no equivale a una API pública lista para producción.

Hasta implementar la autenticación y autorización de P3:

- El CRUD deberá probarse únicamente en un entorno local aislado.
- No deberá publicarse en un servidor accesible sin controles de acceso.
- No deberán configurarse contraseñas administrativas predeterminadas.
- Las credenciales deberán almacenarse como hashes desde P1.

### 5.7 Eliminación

Para P1 se utilizará eliminación física por identificador.

Esta decisión permite implementar y verificar la operación Delete, pero no constituye una política definitiva de eliminación en el sistema integrado.

En particular, deberá revisarse cuando existan referencias a usuarios desde otros microservicios.

## 6. Modelo conceptual de datos

### 6.1 Entidad Usuario

Se propone una sola tabla `usuarios` para la primera implementación.

| Campo | Descripción | Restricciones propuestas |
|---|---|---|
| `id` | Identificador interno | Clave primaria, generado automáticamente |
| `nombre` | Nombre del usuario | Obligatorio, máximo 120 caracteres |
| `email` | Correo electrónico | Obligatorio, máximo 254 caracteres, único sin distinguir mayúsculas |
| `password_hash` | Hash de contraseña | Obligatorio, nunca expuesto |
| `rol` | Rol del usuario | Obligatorio, uno de los tres valores permitidos |
| `created_at` | Fecha de creación | Obligatoria, generada automáticamente |

La elección exacta de tipos PostgreSQL y la estrategia de índices se revisarán durante el diseño de Flyway.

### 6.2 Roles

Valores conocidos:

- `SOLICITANTE`.
- `AGENTE`.
- `ADMIN`.

Se propone un único rol por usuario para P1.

No se requiere por ahora una tabla adicional de relación entre usuarios y roles.

La base de datos deberá restringir los valores admitidos para evitar registros inconsistentes.

### 6.3 Restricciones y consistencia

- `id` será clave primaria.
- `nombre`, `email`, `password_hash`, `rol` y `created_at` no permitirán valores nulos.
- El correo tendrá una restricción de unicidad que ignore diferencias entre mayúsculas y minúsculas.
- `rol` solo admitirá los tres valores conocidos.
- `created_at` tendrá un valor predeterminado generado por la base de datos.
- Las tablas serán creadas por Flyway, no por generación automática de Hibernate.

La migración deberá justificar cada índice mediante comentarios SQL.

### 6.4 Decisiones técnicas provisionales

| Decisión | Justificación |
|---|---|
| Una tabla de usuarios | Modelo mínimo para el CRUD |
| Correo único | Evitar duplicación de cuentas |
| Un rol por usuario | Simplificar el modelo inicial |
| Rol SOLICITANTE en Create | Evitar asignación directa de privilegios mediante el CRUD local |
| Hash obligatorio | Proteger las credenciales |
| Update limitado a nombre y correo | Evitar mezclar cambios administrativos o de seguridad con actualización general |
| Eliminación física | Satisfacer el CRUD de P1 en un entorno aislado |

Estas decisiones son posteriores al análisis inicial de Copilot y permanecen sujetas a validación.

## 7. Criterios técnicos de aceptación del CRUD de P1

Los siguientes escenarios complementan las siete historias de usuario originales. Su finalidad es permitir la implementación y las pruebas del CRUD exigido por IDS.

### CRUD-01 — Creación

**Escenario: crear usuario válido**

Dado que se proporcionan nombre, correo y contraseña válidos  
Y el correo no está registrado  
Cuando se solicita crear el usuario  
Entonces el sistema devuelve 201  
Y almacena el usuario con rol SOLICITANTE  
Y guarda únicamente el hash de la contraseña

**Escenario: correo duplicado**

Dado que existe un usuario con el mismo correo, sin distinguir mayúsculas  
Cuando se intenta registrar otro usuario  
Entonces el sistema devuelve 409  
Y no crea un registro duplicado

### CRUD-02 — Consulta

**Escenario: consultar usuario existente**

Dado que existe un usuario registrado  
Cuando se consulta su identificador  
Entonces el sistema devuelve 200  
Y presenta sus datos permitidos sin credenciales

**Escenario: usuario inexistente**

Dado que no existe un usuario con el identificador solicitado  
Cuando se consulta dicho identificador  
Entonces el sistema devuelve 404

**Escenario: listar usuarios**

Dado que el sistema contiene cero o más usuarios  
Cuando se solicita el listado  
Entonces el sistema devuelve 200  
Y proporciona una colección sin contraseñas ni hashes

### CRUD-03 — Actualización

**Escenario: actualización válida**

Dado que existe un usuario registrado  
Cuando se solicita actualizar su nombre y correo con valores válidos  
Entonces el sistema devuelve 200  
Y conserva su identificador, rol, contraseña y fecha de creación

**Escenario: actualización de usuario inexistente**

Dado que no existe el usuario solicitado  
Cuando se intenta actualizar sus datos  
Entonces el sistema devuelve 404

**Escenario: correo duplicado**

Dado que el nuevo correo pertenece a otro usuario  
Cuando se intenta actualizar el registro  
Entonces el sistema devuelve 409  
Y conserva los datos anteriores

### CRUD-04 — Eliminación

**Escenario: eliminación exitosa**

Dado que existe un usuario registrado  
Cuando se solicita eliminarlo por identificador  
Entonces el sistema elimina el registro  
Y devuelve 204

**Escenario: eliminación inexistente**

Dado que el identificador solicitado no corresponde a ningún usuario  
Cuando se solicita eliminarlo  
Entonces el sistema devuelve 404

Estos escenarios proporcionan una base para las pruebas de P1 y no representan historias nuevas generadas por Copilot.

## 8. Requerimientos no funcionales

| ID | Requerimiento | Criterio |
|---|---|---|
| RNF-01 | Seguridad de credenciales | No almacenar contraseñas en texto plano |
| RNF-02 | Seguridad de autenticación | Proteger los JWT mediante mecanismos criptográficos adecuados |
| RNF-03 | Validación y errores | Validar entradas y manejar errores con un formato consistente |
| RNF-04 | Mantenibilidad | Incorporar pruebas automatizadas positivas y negativas |
| RNF-05 | Documentación | Documentar el contrato y la ejecución de la API |

Para P1 se aplican los requisitos técnicos de la guía:

- Java 21.
- Spring Boot 3.x.
- Maven.
- PostgreSQL 16.
- Flyway.
- Spring Data JPA.
- Jakarta Validation.
- DTOs mediante Java records.
- `@RestControllerAdvice` y `ProblemDetail`.
- JUnit 5 y Mockito.
- Mínimo ocho pruebas automatizadas con pruebas de servicio y de controlador.
- Verificación mediante `mvn -B verify`.

## 9. Preguntas abiertas

### Negocio y roles

1. ¿El registro de usuarios será público o restringido?
2. ¿Quién podrá crear cuentas AGENTE y ADMIN?
3. ¿Cómo se creará el primer administrador?
4. ¿Un usuario podrá tener varios roles?
5. ¿Quién tendrá permiso para cambiar los roles?
6. ¿Se permitirá activar y desactivar cuentas?
7. ¿La eliminación deberá ser física o lógica en el sistema definitivo?

### Datos y seguridad

8. ¿Qué atributos adicionales necesitarán los usuarios?
9. ¿El correo será definitivamente el identificador de inicio de sesión?
10. ¿Existirá una operación de cambio de contraseña?
11. ¿Qué información será visible en el perfil propio?
12. ¿Cómo verificarán los demás microservicios los JWT?
13. ¿Qué operaciones requerirán permisos administrativos?

Las decisiones provisionales documentadas permiten construir el prototipo de P1 sin presentar estas respuestas como aprobadas.

## 10. Trazabilidad del análisis con GitHub Copilot

### Interacción 1 — Análisis de requerimientos

**Herramienta:** GitHub Copilot  
**Referencia:** `.github/prompts/historias-usuario.prompt.md`

Copilot generó siete historias de usuario, criterios Gherkin, requerimientos no funcionales y preguntas abiertas.

Durante la revisión se detectó que algunas funcionalidades administrativas dependían de reglas no confirmadas.

### Interacción 2 — Revisión crítica de requerimientos

**Herramienta:** GitHub Copilot  
**Referencia:** `.github/prompts/revision-requerimientos.md`

Copilot identificó ambigüedades, supuestos y diferencias entre funcionalidades principales y propuestas.

También sugirió posponer parte del CRUD, recomendación que no se adoptó porque la guía de P1 exige Create, Read, Update y Delete.

### Revisión técnica posterior

Después de las interacciones anteriores, se consolidó el alcance técnico de P1:

- Se conservaron las siete historias originales.
- Se documentó por separado el contrato del CRUD.
- Se limitaron los campos editables en Update.
- Se definió la asignación provisional del rol SOLICITANTE en Create.
- Se establecieron reglas mínimas de validación y errores.
- Se agregó trazabilidad entre operaciones y escenarios de prueba.
- Se identificaron explícitamente las decisiones pendientes.

**Esta revisión no constituye una nueva interacción con GitHub Copilot y no debe registrarse como tal.**

La evidencia original de Copilot corresponde a sus prompts y respuestas reales. Las decisiones técnicas consolidadas en este documento no deben atribuirse a la herramienta como si hubieran sido generadas durante esas interacciones.

## 11. Estado y siguiente actividad

**Historias de usuario:** siete documentadas con escenarios positivos y negativos.

**Requerimientos no funcionales:** cinco documentados.

**Preguntas abiertas:** identificadas.

**Contrato CRUD de P1:** propuesto, con entradas, salidas, validaciones y errores.

**Modelo conceptual:** definido provisionalmente para una tabla de usuarios.

**Migración Flyway:** pendiente de diseñar y generar.

**Código y pruebas:** pendientes de implementación.

**Siguiente paso:** utilizar este documento como contexto para solicitar a GitHub Copilot el modelo físico PostgreSQL y la migración Flyway V1, revisando su propuesta antes de aceptar cambios.

---

*Especificación de trabajo para P1. Las funcionalidades no confirmadas están identificadas como propuestas. La bitácora registra las interacciones efectivas con GitHub Copilot.*
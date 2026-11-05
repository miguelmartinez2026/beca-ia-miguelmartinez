# Cadena de prompts — Paso 3: Implementación REST de `ms-usuarios`

**Proyecto:** Sistema de Mesa de Ayuda (IDS)
**Práctica:** P1 — Paso 3
**Herramienta:** GitHub Copilot Chat en VS Code, modo Agent
**Fuente:** exportación original `chat.json`, facilitada por el desarrollador.
**Fecha de las interacciones:** 4 de noviembre de 2026 (hora de Ciudad de México).

## Criterio de transcripción

Los siete bloques siguientes reproducen **literalmente los mensajes enviados por el usuario a Copilot** en la sesión exportada. Solo se normalizaron los saltos de línea de Windows (CRLF a LF) para mantener Markdown compatible. Los títulos, horas y descripciones colocados fuera de las transcripciones son metadatos editoriales y **no forman parte de los prompts originales**.

La primera interacción incluye explícitamente las **siete piezas** solicitadas por IDS (ROL, CONTEXTO, TAREA, RESTRICCIONES, FORMATO DE SALIDA, EJEMPLO y VERIFICACIÓN). Las interacciones siguientes fueron instrucciones de aprobación, implementación por bloques y depuración: se conservan como fueron redactadas, **sin agregarles piezas artificiales**.

No se reproducen razonamientos internos ni trazas técnicas del asistente; se documentan los mensajes reales del desarrollador.

## Secuencia de trabajo

| Interacción | Instrucción real | Evidencia de desarrollo |
|---|---|---|
| 1 | Plan inicial del CRUD REST | Plan y revisión de decisiones, sin commit. |
| 2 | Aprobación del plan y primer bloque: BCrypt | `a7b06ce` — configuración de BCrypt. |
| 3 | Segundo bloque: entidad y repositorio | `65cd4cb` — entidad y repositorio. |
| 4 | Tercer bloque: DTOs y validaciones | `e6ab617` — DTOs y validaciones. |
| 5 | Cuarto bloque: servicio CRUD | `8f30508` — servicio CRUD y excepciones. |
| 6 | Quinto bloque: controlador y manejo global de errores | `89d0459` — controlador y manejo de errores. |
| 7 | Corrección detectada en Postman: HTTP 500 a HTTP 409 | Corrección incluida en `89d0459` y verificada posteriormente mediante Postman (409). |

---

## Interacción 1 — Plan inicial del CRUD REST

**Fecha y hora:** 04/11/2026 18:10:10 (UTC−06:00)
**Objetivo:** Analizar el alcance, identificar capas, archivos, DTOs, errores y decisiones pendientes; sin implementar.

**Mensaje original enviado a Copilot:**

````markdown
## ROL
Actúa como desarrollador backend senior y arquitecto de software especializado en Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL y diseño de APIs REST.

## CONTEXTO
Estamos desarrollando `ms-usuarios`, el microservicio de gestión de usuarios del Sistema de Mesa de Ayuda de la beca IDS.

Trabajamos en la Práctica 1, paso 3 de la cadena de prompts: implementación del CRUD REST por capas.

Estado actual del proyecto:

- Java 21 y Spring Boot 3.5.16 con Maven.
- PostgreSQL 16.15.
- Migración Flyway `V1__create_usuarios.sql` aplicada y verificada.
- Rama Git `practica-1`.
- Documento funcional `docs/requerimientos.md`, versión 0.3.
Antes de proponer cualquier implementación, revisa los siguientes archivos:

- `docs/requerimientos.md`
- `src/main/resources/db/migration/V1__create_usuarios.sql`
- `pom.xml`
- `src/main/resources/application.yaml`
Respeta las decisiones y los comportamientos establecidos en los requerimientos, distinguiendo las decisiones provisionales de P1 de las funcionalidades previstas para prácticas posteriores.

## TAREA
Analiza el proyecto y presenta un plan de implementación para desarrollar el CRUD REST de usuarios mediante Spring Boot.

Contempla las siguientes operaciones definidas para P1:

1. `POST /api/usuarios`: registrar un usuario con nombre, email y contraseña, asignando el rol SOLICITANTE desde el servidor.
2. `GET /api/usuarios/{id}`: consultar un usuario por identificador.
3. `GET /api/usuarios`: listar usuarios.
4. `PUT /api/usuarios/{id}`: actualizar únicamente nombre y email.
5. `DELETE /api/usuarios/{id}`: eliminar físicamente un usuario.
El plan debe considerar:

- Entidad JPA `Usuario` compatible con la migración V1.
- Repositorio con Spring Data JPA.
- DTOs de entrada y salida implementados con Java records.
- Validaciones mediante Jakarta Validation.
- Capa de servicio con las reglas de negocio.
- Controlador REST.
- Manejo global de excepciones mediante `@RestControllerAdvice` y `ProblemDetail`.
- Normalización del correo electrónico.
- Generación segura del hash de contraseña antes de persistirlo.
- Manejo de errores HTTP 400, 404 y 409 según corresponda.

## RESTRICCIONES

- En esta interacción realiza únicamente un análisis y un plan. No crees ni modifiques archivos.
- No ejecutes comandos ni cambies el repositorio.
- No modifiques la migración Flyway V1.
- No utilices `ddl-auto: create` ni `update`; conserva `validate`.
- No expongas contraseñas ni hashes en las respuestas de la API.
- No permitas que el cliente establezca directamente el ID, el rol o la fecha de creación.
- No implementes JWT, login, recuperación de contraseñas ni administración de roles en esta etapa.
- No agregues dependencias innecesarias.
- Si necesitamos una dependencia para generar hashes BCrypt, identifica cuál y justifica su incorporación. Evita habilitar prematuramente toda la configuración de Spring Security.
- No agregues funcionalidades que no correspondan al alcance documentado de P1.
- Evita clases, interfaces y abstracciones innecesarias.
- No supongas decisiones de negocio que no estén establecidas en los requerimientos.

## FORMATO DE SALIDA
Presenta tu propuesta mediante las siguientes secciones:

1. Resumen de la arquitectura y responsabilidades por capa.
2. Árbol propuesto de paquetes y archivos Java.
3. Tabla con cada archivo propuesto, su responsabilidad y justificación.
4. Diseño de los DTOs y sus validaciones.
5. Contrato REST: endpoints, entradas, salidas y códigos HTTP.
6. Dependencias adicionales necesarias, si las hay.
7. Estrategia de manejo de excepciones y protección de contraseñas.
8. Orden recomendado de implementación, indicando en qué momentos conviene generar commits pequeños.
9. Riesgos, inconsistencias y decisiones pendientes de aprobación.

## EJEMPLO
Para el registro de usuarios:

- El cliente proporciona nombre, email y contraseña.
- El servicio normaliza el correo y genera un hash seguro de la contraseña.
- El rol SOLICITANTE se asigna en el servidor.
- La persistencia respeta las restricciones existentes en PostgreSQL.
- La respuesta HTTP 201 devuelve únicamente información pública del usuario, nunca la contraseña ni su hash.
- Si el correo ya existe, la API responde HTTP 409.
Este ejemplo debe contrastarse con `docs/requerimientos.md`.

## VERIFICACIÓN
Antes de finalizar:

1. Comprueba que las clases propuestas correspondan a los requerimientos de P1.
2. Verifica que el mapeo JPA sea compatible con las seis columnas de la migración V1.
3. Comprueba que los DTOs no expongan información sensible.
4. Identifica cómo se manejarán tanto las validaciones de entrada como los conflictos de integridad de PostgreSQL.
5. Verifica que la propuesta pueda implementarse y probarse con las dependencias disponibles o las adiciones mínimas justificadas.
6. Señala cualquier contradicción entre los requerimientos y el esquema existente.
No implementes todavía el código.

Espera mi aprobación explícita antes de crear o modificar archivos.
````

---

## Interacción 2 — Aprobación del plan y primer bloque: BCrypt

**Fecha y hora:** 04/11/2026 18:17:57 (UTC−06:00)
**Objetivo:** Aprobar o corregir decisiones D1–D8; crear solo la dependencia de BCrypt y `PasswordConfig`.

**Mensaje original enviado a Copilot:**

````markdown
He revisado tu propuesta de implementación del CRUD REST para `ms-usuarios`.

Apruebo la arquitectura general, el contrato REST y la separación por capas, con las siguientes decisiones y ajustes:

**Decisiones D1–D8**

- D1: Aprobado. Normaliza el correo en los constructores compactos de los records, antes de la validación. Mantén esa responsabilidad claramente definida y no la dupliques innecesariamente en el servicio.
- D2: Aprobado. Contraseña obligatoria, mínimo 8 caracteres y máximo 72 bytes UTF-8 para BCrypt. No incluyas contraseñas ni sus valores rechazados en logs o respuestas.
- D3: Aprobado. Utiliza `BCryptPasswordEncoder` mediante una dependencia mínima de `spring-security-crypto`.
- D4: Aprobado. Incluye la cabecera `Location` al crear un usuario.
- D5: Modificación. Rechaza con HTTP 400 los campos JSON desconocidos en lugar de ignorarlos silenciosamente. No permitas que el cliente establezca `id`, `rol`, `passwordHash` ni `createdAt`.
- D6: No restrinjas el correo exclusivamente a ASCII. Considera el manejo adecuado de violaciones de `ck_usuarios_email_normalizado` atribuibles a entradas inválidas, evitando devolver un error 500 cuando corresponda un 400.
- D7: Utiliza Java sin Lombok para la entidad `Usuario`, manteniendo constructores, accesores y métodos explícitos y únicamente los necesarios.
- D8: Aprobado. No recortes ni normalices el nombre; aplica las validaciones documentadas.
**Observaciones adicionales**

1. El árbol de paquetes propuesto contiene 12 archivos Java nuevos, no 11.
2. Mantén el mapeo de `created_at` compatible con el valor generado por PostgreSQL y comprueba su funcionamiento cuando implementemos JPA.
3. Conserva la migración Flyway V1 y `ddl-auto: validate`, sin modificaciones.
4. El problema del test `@SpringBootTest`, que depende de PostgreSQL local, debe resolverse durante el paso de pruebas automatizadas de P1. La entrega deberá permitir verificar el proyecto sin depender exclusivamente de mi configuración local.
5. No implementes JWT ni la configuración completa de Spring Security durante P1.
**AUTORIZACIÓN DE IMPLEMENTACIÓN**

Apruebo el plan general, pero por ahora te autorizo a implementar exclusivamente el primer bloque:

- Agregar `spring-security-crypto` a `pom.xml`, sin versión explícita si está gestionada por Spring Boot.
- Crear `config/PasswordConfig.java` con un bean `PasswordEncoder` que utilice BCrypt.
No crees todavía la entidad, el repositorio, los DTOs, los servicios, el controlador ni las excepciones.

Después de implementar ese primer bloque:

1. Muestra exactamente qué archivos creaste o modificaste.
2. Explica brevemente las decisiones técnicas.
3. Ejecuta `.\mvnw.cmd -B -DskipTests compile` para comprobar la compilación, sin iniciar Spring Boot.
4. Informa el resultado real del comando y cualquier problema encontrado.
5. No hagas commits ni push automáticamente; los revisaremos y realizaremos nosotros.
Cuando termine este bloque, espera mi aprobación para continuar con la siguiente etapa.
````

---

## Interacción 3 — Segundo bloque: entidad y repositorio

**Fecha y hora:** 04/11/2026 18:22:43 (UTC−06:00)
**Objetivo:** Crear `Rol.java`, `Usuario.java` y `UsuarioRepository.java`.

**Mensaje original enviado a Copilot:**

````markdown
El primer bloque de implementación quedó revisado, compilado y registrado en Git mediante un commit.

Ahora autorizo exclusivamente el **segundo bloque: entidad JPA y repositorio**.

Implementa los siguientes archivos respetando el plan que aprobamos:

1. `domain/Rol.java`

- Enum con SOLICITANTE, AGENTE y ADMIN.
2. `domain/Usuario.java`

- Entidad JPA compatible con las seis columnas de `V1__create_usuarios.sql`.
- Identificador `Long` con `GenerationType.IDENTITY`.
- Rol mediante `@Enumerated(EnumType.STRING)`.
- Fecha `created_at` generada por PostgreSQL, recuperada correctamente después de persistir.
- Constructor protegido sin argumentos para JPA.
- Constructor para crear usuarios con los datos necesarios.
- Método `actualizarDatos(nombre, email)` que únicamente modifique esos dos campos.
- Java explícito, sin Lombok.
- Sin setters públicos innecesarios.
- Sin exponer contraseñas o hashes mediante `toString()`.
3. `repository/UsuarioRepository.java`

- Interfaz basada en `JpaRepository<Usuario, Long>`.
- No agregues consultas ni métodos adicionales que todavía no necesitemos.
**Restricciones**

- No modifiques la migración Flyway V1.
- No modifiques `pom.xml` ni `application.yaml`.
- Conserva `ddl-auto: validate`.
- No implementes todavía DTOs, servicios, controladores, excepciones, JWT ni pruebas automatizadas.
- No modifiques documentación ni otros archivos.
- No hagas commits ni push.
**Verificación**

Después de crear los tres archivos:

1. Comprueba el mapeo de cada columna contra la migración V1.
2. Ejecuta `.\mvnw.cmd -B -DskipTests compile`.
3. Informa el resultado real de compilación.
4. Explica cómo recuperará Hibernate el valor de `created_at` generado por PostgreSQL.
5. Indica si existe algún aspecto que debamos verificar durante el arranque de Spring Boot.
No ejecutes operaciones que alteren la base de datos.

Al finalizar, muéstrame los archivos creados y espera mi revisión antes de continuar.
````

---

## Interacción 4 — Tercer bloque: DTOs y validaciones

**Fecha y hora:** 04/11/2026 18:31:13 (UTC−06:00)
**Objetivo:** Crear `CrearUsuarioRequest`, `ActualizarUsuarioRequest` y `UsuarioResponse`.

**Mensaje original enviado a Copilot:**

````markdown
El segundo bloque quedó revisado, validado contra PostgreSQL y registrado en Git con el commit `65cd4cb`.

Ahora autorizo exclusivamente el **tercer bloque de implementación: DTOs y validaciones Jakarta**.

Crea los siguientes archivos en el paquete `com.ids.beca.usuarios.dto`:

1. `CrearUsuarioRequest.java`

- Java record con `nombre`, `email` y `password`.
- `nombre`: obligatorio, no solo espacios, máximo 120 caracteres.
- `email`: obligatorio, formato válido y máximo 254 caracteres.
- Normaliza el correo mediante `strip().toLowerCase(Locale.ROOT)` en el constructor compacto, manejando correctamente valores `null`.
- `password`: obligatoria, mínimo 8 caracteres y máximo 72 bytes UTF-8 por las restricciones de BCrypt.
- Implementa las validaciones correspondientes con Jakarta Validation.
- Evita que `toString()` exponga la contraseña.
2. `ActualizarUsuarioRequest.java`

- Java record con únicamente `nombre` y `email`.
- Aplica las mismas validaciones correspondientes a ambos campos.
- Normaliza el correo antes de la validación.
- No incluyas contraseña, rol, ID ni fecha de creación.
3. `UsuarioResponse.java`

- Java record con `id`, `nombre`, `email`, `rol` y `createdAt`.
- Utiliza los tipos compatibles con nuestra entidad `Usuario`.
- No incluyas `password`, `passwordHash` ni otros datos sensibles.
- Puedes incluir un método estático de conversión desde `Usuario` si resulta útil y no introduce dependencias adicionales.
**RESTRICCIONES**

- Utiliza exclusivamente Java 21, Jakarta Validation y las dependencias existentes.
- No modifiques la entidad `Usuario`, el repositorio, la migración Flyway ni `pom.xml`.
- No implementes todavía servicios, controladores, excepciones ni autenticación JWT.
- No modifiques archivos de configuración ni documentación.
- No agregues dependencias.
- No realices commits ni push.
- Mantén la implementación sencilla y evita abstracciones innecesarias.
**VERIFICACIÓN**

1. Comprueba que los DTOs coincidan con el contrato REST aprobado.
2. Verifica que la contraseña nunca aparezca en `UsuarioResponse` ni en el `toString()` del DTO de registro.
3. Verifica que la normalización del correo sea compatible con las restricciones de PostgreSQL.
4. Ejecuta `.\mvnw.cmd -B -DskipTests compile`.
5. Informa el resultado real y muestra los tres archivos completos.
No avances al servicio ni al controlador. Espera mi revisión y aprobación antes de continuar.
````

---

## Interacción 5 — Cuarto bloque: servicio CRUD

**Fecha y hora:** 04/11/2026 18:35:48 (UTC−06:00)
**Objetivo:** Crear `UsuarioService`, `UsuarioNoEncontradoException` y `EmailDuplicadoException`.

**Mensaje original enviado a Copilot:**

````markdown
El tercer bloque de implementación quedó revisado, compilado y registrado en Git mediante el commit `e6ab617`.

Ahora autorizo exclusivamente el **cuarto bloque: servicio de usuarios y excepciones de negocio**.

## TAREA
Implementa los siguientes archivos:

1. `service/UsuarioService.java`
2. `exception/UsuarioNoEncontradoException.java`
3. `exception/EmailDuplicadoException.java`
El servicio debe implementar las cinco operaciones CRUD definidas en `docs/requerimientos.md`:

**Crear usuario**

- Recibir `CrearUsuarioRequest`.
- Utilizar el correo normalizado del DTO.
- Generar el hash de la contraseña mediante el bean `PasswordEncoder` existente.
- Asignar siempre el rol `SOLICITANTE` desde el servidor.
- Persistir con `UsuarioRepository`.
- Devolver `UsuarioResponse`, sin contraseña ni hash.
**Consultar usuario**

- Buscar mediante su identificador.
- Devolver `UsuarioResponse`.
- Lanzar `UsuarioNoEncontradoException` cuando no exista.
**Listar usuarios**

- Recuperar los usuarios existentes.
- Devolver una lista de `UsuarioResponse` ordenada por identificador ascendente.
- Devolver una lista vacía cuando no existan registros.
**Actualizar usuario**

- Recibir `ActualizarUsuarioRequest`.
- Permitir modificar únicamente nombre y correo electrónico.
- No cambiar ID, contraseña, rol ni fecha de creación.
- Lanzar `UsuarioNoEncontradoException` cuando corresponda.
- Detectar conflictos por correo duplicado.
**Eliminar usuario**

- Eliminar físicamente el registro, según el alcance aprobado para P1.
- Lanzar `UsuarioNoEncontradoException` cuando no exista.
- No implementar borrado lógico.

## MANEJO DE ERRORES

- Implementa las dos excepciones de negocio como excepciones no comprobadas.
- Utiliza transacciones con Spring.
- Para los métodos de consulta, utiliza transacciones de solo lectura cuando corresponda.
- En creación y actualización, utiliza `saveAndFlush` cuando sea necesario para identificar los errores de integridad dentro de la operación.
- Convierte en `EmailDuplicadoException` únicamente las violaciones de unicidad que correspondan al índice `uq_usuarios_email_lower`, identificando correctamente la causa y SQLState `23505`.
- No conviertas indiscriminadamente otras violaciones de integridad en errores de correo duplicado.
- Conserva las demás excepciones para su tratamiento posterior por el manejador global.
- No implementes una comprobación previa `existsByEmail` como sustituto de la restricción única de PostgreSQL.
- Evita incluir contraseñas, hashes o datos sensibles en mensajes de excepción y logs.

## RESTRICCIONES

- Utiliza las clases y dependencias existentes.
- Prefiere inyección por constructor.
- No dupliques innecesariamente la normalización de correo realizada en los DTOs.
- No modifiques `Usuario.java`, `Rol.java`, `UsuarioRepository.java` ni los DTOs existentes.
- No modifiques Flyway V1, `pom.xml` ni `application.yaml`.
- No implementes todavía `UsuarioController` ni `ApiExceptionHandler`.
- No implementes JWT, login ni administración de roles.
- No agregues dependencias.
- No modifiques documentación.
- No hagas commits ni push.

## VERIFICACIÓN

1. Muestra el contenido completo de los tres archivos creados.
2. Explica brevemente cómo funcionan las transacciones y el manejo del correo duplicado.
3. Comprueba que los métodos utilizan correctamente los DTOs y entidades existentes.
4. Ejecuta `.\mvnw.cmd -B -DskipTests compile`.
5. Informa el resultado real de compilación.
6. Identifica cualquier comportamiento que todavía requiera pruebas de integración.
No ejecutes operaciones contra PostgreSQL ni modifiques datos reales.

Al terminar, espera mi revisión y aprobación antes de avanzar al controlador REST.
````

---

## Interacción 6 — Quinto bloque: controlador y manejo global de errores

**Fecha y hora:** 04/11/2026 18:44:36 (UTC−06:00)
**Objetivo:** Crear `UsuarioController` y `ApiExceptionHandler`; configurar el rechazo de propiedades JSON desconocidas.

**Mensaje original enviado a Copilot:**

````markdown
El cuarto bloque de implementación quedó revisado, compilado y registrado mediante el commit `8f30508`.

Ahora autorizo exclusivamente el **quinto bloque: controlador REST y manejo global de errores**.

## TAREA
Implementa los siguientes archivos:

1. `controller/UsuarioController.java`
2. `exception/ApiExceptionHandler.java`
Además, modifica exclusivamente lo necesario en `src/main/resources/application.yaml` para rechazar propiedades JSON desconocidas.

### UsuarioController
Implementa los cinco endpoints definidos en `docs/requerimientos.md`:

- `POST /api/usuarios`: recibe `CrearUsuarioRequest`, utiliza `@Valid`, devuelve HTTP 201 con `UsuarioResponse` y cabecera `Location: /api/usuarios/{id}`.
- `GET /api/usuarios/{id}`: devuelve HTTP 200 con `UsuarioResponse`.
- `GET /api/usuarios`: devuelve HTTP 200 con una lista de `UsuarioResponse`.
- `PUT /api/usuarios/{id}`: recibe `ActualizarUsuarioRequest`, utiliza `@Valid` y devuelve HTTP 200.
- `DELETE /api/usuarios/{id}`: elimina el usuario y devuelve HTTP 204 sin cuerpo.
El controlador debe delegar toda la lógica de negocio en `UsuarioService`.

No expongas directamente entidades JPA.

### ApiExceptionHandler
Utiliza `@RestControllerAdvice` y `ProblemDetail`, preferentemente extendiendo `ResponseEntityExceptionHandler` para aprovechar el manejo de errores de Spring MVC.

Implementa:

1. `UsuarioNoEncontradoException` → HTTP 404.
2. `EmailDuplicadoException` → HTTP 409.
3. Errores de Jakarta Validation → HTTP 400.
4. JSON malformado o propiedades JSON desconocidas → HTTP 400.
5. Identificadores con tipos inválidos → HTTP 400.
6. Violaciones de `ck_usuarios_email_normalizado` atribuibles a entradas inválidas → HTTP 400, identificando la restricción con precisión y sin confundirla con otros errores de integridad.
Para errores de validación, devuelve un `ProblemDetail` con una propiedad adicional `errors`, compuesta por objetos con `campo` y `mensaje`.

No incluyas valores rechazados, contraseñas, hashes, SQL ni detalles internos de excepciones en las respuestas HTTP.

Conserva un tratamiento genérico y seguro para errores inesperados. No conviertas indiscriminadamente todos los errores de base de datos en HTTP 400 o 409.

### application.yaml
Configura Jackson para rechazar campos desconocidos durante la deserialización.

Esto debe impedir que solicitudes que intenten establecer propiedades como `id`, `rol`, `passwordHash` o `createdAt` sean aceptadas silenciosamente.

Conserva intactas las configuraciones existentes de datasource, JPA y Flyway.

## RESTRICCIONES

- Utiliza Java 21 y Spring Boot 3.5.16.
- Respeta los DTOs, excepciones y métodos existentes de `UsuarioService`.
- No modifiques `UsuarioService`, las entidades, el repositorio ni los DTOs.
- No modifiques `pom.xml` ni la migración Flyway V1.
- No agregues dependencias ni abstracciones innecesarias.
- No implementes JWT, login ni autorización por roles.
- No generes todavía pruebas automatizadas; corresponden al siguiente paso de nuestra cadena de prompts.
- No modifiques documentación.
- No hagas commits ni push.

## VERIFICACIÓN

1. Muestra el código completo de los dos archivos Java generados.
2. Muestra el cambio exacto realizado en `application.yaml`.
3. Comprueba que cada endpoint utiliza el método correcto del servicio.
4. Verifica que las respuestas HTTP coincidan con el contrato aprobado.
5. Comprueba que ningún `ProblemDetail` exponga información sensible.
6. Ejecuta `.\mvnw.cmd -B -DskipTests compile`.
7. Informa el resultado real de compilación y cualquier advertencia relevante.
No ejecutes solicitudes HTTP ni operaciones que modifiquen los datos de PostgreSQL.

Al finalizar, espera mi revisión antes de continuar.
````

---

## Interacción 7 — Corrección detectada en Postman: HTTP 500 a HTTP 409

**Fecha y hora:** 04/11/2026 19:02:24 (UTC−06:00)
**Objetivo:** Investigar el error de correo duplicado y ajustar su identificación mediante SQLState sin analizar textos localizados.

**Mensaje original enviado a Copilot:**

````markdown
Detectamos un error durante las pruebas reales del CRUD contra PostgreSQL 16.15.

**Comportamiento observado:**

- El primer POST `/api/usuarios` respondió HTTP 201 correctamente.
- Un segundo POST con el mismo correo, utilizando mayúsculas, respondió HTTP 500 en lugar del 409 esperado.
- PostgreSQL confirmó `SQLState: 23505` y la violación del índice `uq_usuarios_email_lower`.
- `ApiExceptionHandler` recibió una `DataIntegrityViolationException`, no una `EmailDuplicadoException`.
- PostgreSQL emite los mensajes de error en español.
**TAREA**

1. Revisa `UsuarioService.java`, especialmente `guardar()` y `esViolacionDeEmailUnico()`.
2. Identifica por qué la excepción no se convierte en `EmailDuplicadoException`.
3. Comprueba si la identificación del nombre de la restricción puede fallar debido a mensajes localizados o a un valor `null`.
4. Propón y aplica una corrección mínima y justificada para reconocer el conflicto de correo duplicado.
5. Si necesitas contemplar `SQLState 23505` cuando Hibernate no proporciona el nombre de la restricción, limita ese comportamiento al contexto de las operaciones sobre usuarios. No conviertas indiscriminadamente otras violaciones en HTTP 409.
6. Conserva la causa original de la excepción sin exponer datos sensibles en la respuesta HTTP.
**RESTRICCIONES**

- No modifiques la migración Flyway V1 ni las restricciones de PostgreSQL.
- No modifiques entidades, DTOs, repositorios ni el contrato REST.
- Prioriza modificar únicamente `UsuarioService.java`.
- No agregues dependencias ni implementes funcionalidades nuevas.
- No utilices búsquedas por texto dentro de los mensajes SQL, porque dependen del idioma.
- No registres correos, contraseñas, hashes ni filas completas de PostgreSQL en nuevos logs.
- No hagas commits ni push.
**VERIFICACIÓN**

1. Explica la causa identificada, distinguiendo lo confirmado de lo que todavía es una hipótesis.
2. Muestra el cambio exacto realizado.
3. Ejecuta `.\mvnw.cmd -B -DskipTests compile`.
4. Indica qué escenarios debemos volver a probar en Postman.
5. No afirmes que HTTP 409 funciona hasta que lo verifiquemos realmente.
Espera mi revisión antes de realizar otros cambios.
````

---

## Observaciones y ajustes posteriores

- El plan inicial de Copilot fue revisado antes de autorizar código. Se rechazó usar Lombok en la entidad, se exigió responder 400 a propiedades JSON desconocidas y se corrigió el conteo de archivos propuestos.

- La implementación se dividió en cinco bloques pequeños y verificables, con commits separados; Copilot recibió la instrucción de no realizar commits ni push automáticamente.

- El correo duplicado inicialmente produjo HTTP 500 durante pruebas reales. Se detectó la dificultad para obtener el nombre de la restricción en mensajes de PostgreSQL localizados y se solicitó una corrección basada en SQLState `23505`, sin buscar texto de los mensajes SQL.

- **Verificación posterior del desarrollador:** el duplicado se volvió a probar mediante Postman y respondió HTTP 409. La creación, consulta, listado, actualización y eliminación del CRUD también se comprobaron manualmente.

- Esta documentación es evidencia del **paso 3**. Los cuatro mensajes posteriores del mismo JSON corresponden al **paso 4 — pruebas automatizadas**, que debe documentarse por separado.

## Integridad y trazabilidad

- Archivo exportado de origen: `chat.json` (SHA-256: `23f9dd5ca3f7ef4f2b3d10485ef1db5734658c22b3c759aca3f69286d4f1ba07`).

- Interacciones REST extraídas: **7 mensajes originales del usuario** de un total de **11 solicitudes** en la exportación.

- Los encabezados editoriales, el cuadro de secuencia y las observaciones finales son una organización posterior para la documentación de IDS, no mensajes enviados a Copilot.

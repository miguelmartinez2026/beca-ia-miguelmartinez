# Cadena de prompts — Paso 4: Pruebas automatizadas de `ms-usuarios`

**Proyecto:** Sistema de Mesa de Ayuda (IDS)
**Práctica:** P1 — Paso 4
**Herramienta:** GitHub Copilot Chat en VS Code, modo Agent
**Fuente:** exportación original `chat.json`, compartida por el desarrollador.
**Fecha de las interacciones:** 4 de noviembre de 2026 (hora de Ciudad de México).

## Criterio de transcripción

Las cuatro interacciones siguientes contienen **los mensajes originales enviados por el desarrollador a GitHub Copilot**. Únicamente se normalizaron los saltos de línea CRLF a LF. Los títulos, las fechas, los resultados y las observaciones **fuera de los bloques de transcripción** son notas editoriales: no son parte de los prompts enviados.

La **primera interacción** presenta explícitamente las siete piezas requeridas por IDS: **ROL, CONTEXTO, TAREA, RESTRICCIONES, FORMATO DE SALIDA, EJEMPLO y VERIFICACIÓN**. Las posteriores registran la decisión de reducir las pruebas y las instrucciones de implementación por grupos. No se agregaron piezas ficticias a los prompts originales.

Se excluyen de estas transcripciones las trazas de ejecución, los razonamientos del modelo y los datos internos de la exportación. Se documentan las instrucciones reales del desarrollador y se resume por separado el resultado observable.

## Secuencia de trabajo

| Interacción | Instrucción real | Resultado / evidencia |
|---|---|---|
| 1 | Planificar las pruebas de servicio y controlador | Plan inicial de pruebas para revisión; sin crear archivos. |
| 2 | Aprobar una estrategia reducida de 12 pruebas y crear las tres del servicio | `UsuarioServiceTest.java`; commit `d4252b5`. |
| 3 | Crear ocho pruebas HTTP del controlador con MockMvc | `UsuarioControllerTest.java`; commit `b89f10c`. |
| 4 | Crear una prueba de validación del límite BCrypt en bytes UTF-8 | `CrearUsuarioRequestTest.java`; commit `2bbf107`. |

**Verificación independiente posterior del desarrollador:** las 12 pruebas nuevas ejecutadas juntas con **0 fallas, 0 errores y 0 omisiones**; después, `mvnw.cmd -B clean verify` ejecutado con PostgreSQL y `DB_USERNAME`/`DB_PASSWORD` configuradas completó **13 pruebas, 0 fallas, 0 errores, 0 omisiones y `BUILD SUCCESS`**. La prueba adicional corresponde a `MsUsuariosApplicationTests.contextLoads`, que inicia el contexto completo de Spring Boot y depende de la base de datos local. Estos resultados fueron comunicados posteriormente por el desarrollador, **no son parte de los cuatro prompts enviados a Copilot**.

---

## Interacción 1 — Diseño inicial del plan de pruebas

**Fecha y hora:** 04/11/2026 19:19:56 (UTC−06:00)
**Objetivo:** Solicitar una propuesta sin modificaciones sobre casos, niveles de prueba y reproducibilidad del build.

**Mensaje original enviado a Copilot:**

````markdown
## ROL
Actúa como ingeniero de pruebas de software y desarrollador backend senior especializado en Java 21, Spring Boot 3, JUnit 5, Mockito, MockMvc y pruebas automatizadas de APIs REST.

## CONTEXTO
Estamos desarrollando `ms-usuarios`, microservicio del Sistema de Mesa de Ayuda de la beca IDS.

Nos encontramos en la Práctica 1, paso 4 de nuestra cadena de prompts: pruebas automatizadas.

El CRUD REST ya está implementado y fue verificado manualmente contra PostgreSQL 16.15 mediante Postman.

Se comprobaron las operaciones POST, GET por ID, GET general, PUT y DELETE, además de respuestas 400, 404 y 409.

Durante las pruebas detectamos un error HTTP 500 al registrar un correo duplicado. Se corrigió el manejo de SQLState 23505 y se verificó posteriormente que la API respondiera HTTP 409.

El código actual está registrado en la rama `practica-1`, último commit `89d0459`.

Antes de proponer las pruebas, revisa el código Java existente, `pom.xml`, `application.yaml`, `docs/requerimientos.md` y la migración Flyway V1.

## TAREA
Diseña un plan de pruebas automatizadas para verificar el comportamiento del CRUD de usuarios.

Debemos implementar como mínimo ocho pruebas, pero puedes proponer más cuando aporten cobertura útil.

Contempla dos grupos:

1. Pruebas unitarias de `UsuarioService` con JUnit 5 y Mockito.
2. Pruebas del controlador `UsuarioController` con `@WebMvcTest` y MockMvc.
Incluye escenarios positivos y negativos.

Considera especialmente:

- Registro correcto con rol SOLICITANTE.
- Uso de `PasswordEncoder` para generar el hash.
- Consulta de usuario existente e inexistente.
- Listado de usuarios.
- Actualización permitida de nombre y correo.
- Eliminación y usuario inexistente.
- Correo duplicado y respuesta HTTP 409.
- Validaciones Jakarta y respuestas HTTP 400.
- Rechazo de propiedades JSON desconocidas.
- Contraseñas que superen el límite de 72 bytes UTF-8.
- Ausencia de contraseñas y hashes en las respuestas HTTP.
- Recuperación de `createdAt`, cuando corresponda al nivel de prueba.
Revisa también `MsUsuariosApplicationTests.java`, porque actualmente utiliza `@SpringBootTest` y puede requerir PostgreSQL local para arrancar.

Propón cómo lograr que `mvn verify` sea reproducible sin depender exclusivamente de una base de datos instalada en la computadora del desarrollador.

## RESTRICCIONES

- En esta interacción presenta únicamente el análisis y el plan.
- No crees ni modifiques archivos todavía.
- No ejecutes comandos.
- No modifiques el código de producción ni Flyway V1.
- No agregues dependencias sin justificar su necesidad.
- No utilices H2 como sustituto automático de PostgreSQL, porque nuestro esquema contiene características específicas de PostgreSQL.
- No presupongas que Docker está instalado.
- No implementes JWT ni pruebas de autenticación de la Práctica 3.
- No utilices contraseñas reales ni datos sensibles.
- Evita pruebas redundantes o que dependan del orden de ejecución.
- No hagas commits ni push.

## FORMATO DE SALIDA

1. Evaluación del estado actual del proyecto para realizar pruebas.
2. Estrategia de pruebas unitarias y de controlador.
3. Tabla de casos de prueba propuestos, con identificador, objetivo, tipo, resultado esperado y requisito cubierto.
4. Archivos de prueba que sería necesario crear o modificar.
5. Dependencias necesarias, si corresponde.
6. Estrategia para resolver la dependencia de PostgreSQL del test `@SpringBootTest`.
7. Riesgos o defectos del código que las pruebas podrían descubrir.
8. Orden recomendado de implementación y commits pequeños.
9. Decisiones que requieren mi aprobación.

## EJEMPLO
Caso: registrar un correo duplicado.

- En una prueba de servicio, Mockito simula una violación de integridad asociada a SQLState 23505.
- Se comprueba que `UsuarioService` la convierta en `EmailDuplicadoException`.
- En una prueba de controlador, se simula esa excepción y se verifica HTTP 409.
- Se comprueba que la respuesta no incluya información sensible.
Estas pruebas no sustituyen una prueba real de integración con PostgreSQL, que deberá identificarse como una verificación diferente.

## VERIFICACIÓN
Antes de finalizar:

1. Comprueba que el plan contenga al menos ocho pruebas automatizadas.
2. Verifica que existan escenarios positivos y negativos.
3. Distingue correctamente pruebas unitarias, pruebas de controlador y pruebas de integración.
4. Comprueba que las pruebas propuestas correspondan al comportamiento real del código.
5. Identifica cómo verificar el problema 500 → 409 sin depender del idioma de los mensajes SQL.
6. Explica si `mvn verify` funcionaría en un entorno sin PostgreSQL y qué ajustes serían necesarios.
7. Identifica cualquier supuesto o comportamiento pendiente de comprobación.
No generes las pruebas todavía.

Espera mi aprobación explícita antes de crear o modificar archivos.
````

**Resultado documentado por separado:** Copilot presentó un plan amplio de pruebas y señaló la dependencia del `@SpringBootTest` con PostgreSQL. El plan quedó sujeto a revisión.

---

## Interacción 2 — Ajuste del alcance y tres pruebas de servicio

**Fecha y hora:** 04/11/2026 19:29:04 (UTC−06:00)
**Objetivo:** Reducir el alcance a 12 pruebas comprensibles; autorizar exclusivamente tres pruebas unitarias con Mockito.

**Mensaje original enviado a Copilot:**

````markdown
He revisado tu plan de pruebas automatizadas y apruebo una versión reducida para la Práctica 1 de IDS.

La guía exige al menos ocho pruebas automatizadas, utilizando Mockito para el servicio y @WebMvcTest para el controlador, con escenarios positivos y negativos.

Implementaremos exactamente 12 pruebas principales:

- 3 pruebas unitarias de UsuarioService.
- 8 pruebas del controlador mediante MockMvc.
- 1 prueba de validación del límite de contraseña de 72 bytes UTF-8.
El objetivo es obtener cobertura funcional relevante mediante pruebas sencillas, mantenibles y fáciles de explicar durante la revisión del coach.

## TAREA AUTORIZADA
Por ahora, crea exclusivamente:

`src/test/java/com/ids/beca/usuarios/service/UsuarioServiceTest.java`

Implementa exactamente estas tres pruebas:

**Prueba 1: registrar usuario correctamente**

- Utiliza Mockito para simular UsuarioRepository y PasswordEncoder.
- Comprueba que el servicio llama al encoder con la contraseña recibida.
- Comprueba que la entidad que se intenta guardar contiene el hash generado, no la contraseña original.
- Verifica que el rol asignado sea SOLICITANTE.
- Comprueba que el correo corresponda al valor normalizado del DTO.
**Prueba 2: detectar correo duplicado**

- Simula una DataIntegrityViolationException cuya causa sea una ConstraintViolationException de Hibernate con SQLState 23505 y nombre de restricción null.
- Comprueba que el servicio lanza EmailDuplicadoException.
- Este escenario reproduce específicamente la condición que corregimos después del error HTTP 500 → 409.
**Prueba 3: actualizar únicamente los datos permitidos**

- Simula la búsqueda de un usuario existente.
- Actualiza su nombre y correo utilizando ActualizarUsuarioRequest.
- Comprueba que ambos campos cambian.
- Comprueba que el rol, el hash y la fecha de creación se conservan.
- Verifica que PasswordEncoder no se utilice durante la actualización.

## RESTRICCIONES

- Utiliza JUnit 5, Mockito y AssertJ.
- Mantén tres métodos @Test con nombres descriptivos en español.
- Utiliza una estructura Arrange–Act–Assert clara y fácil de entender.
- Evita parametrizaciones, clases auxiliares, fixtures complejos y abstracciones innecesarias.
- No modifiques el código de producción para facilitar las pruebas.
- Si necesitas inspeccionar un atributo privado sin getter, utiliza ReflectionTestUtils únicamente cuando sea necesario.
- Usa exclusivamente datos ficticios.
- No modifiques pom.xml, application.yaml, Flyway, documentación ni otros archivos.
- No generes todavía pruebas del controlador ni de DTOs.
- No utilices PostgreSQL.
- No hagas commits ni push.

## VERIFICACIÓN

1. Muestra el código completo de UsuarioServiceTest.java.
2. Explica brevemente qué valida cada prueba.
3. Ejecuta `.\mvnw.cmd -B -Dtest=UsuarioServiceTest test`.
4. Informa el número de pruebas ejecutadas, aprobadas, fallidas y omitidas.
5. Si alguna falla, explica primero la causa antes de corregirla.
6. No ejecutes todavía mvn verify, porque el test inicial @SpringBootTest sigue dependiendo de PostgreSQL.
Al terminar, espera mi revisión antes de continuar con las ocho pruebas del controlador.
````

**Resultado documentado por separado:** Se creó `UsuarioServiceTest.java` con tres pruebas aprobadas individualmente. Posteriormente se registró el commit `d4252b5`.

---

## Interacción 3 — Ocho pruebas de controlador

**Fecha y hora:** 04/11/2026 19:34:50 (UTC−06:00)
**Objetivo:** Crear exclusivamente ocho casos HTTP usando `@WebMvcTest`, `@MockitoBean` y MockMvc.

**Mensaje original enviado a Copilot:**

````markdown
## ROL
Actúa como desarrollador backend especializado en Java 21, Spring Boot 3, JUnit 5, Mockito y MockMvc, con experiencia en pruebas sencillas, mantenibles y fáciles de explicar.

## CONTEXTO
Estamos desarrollando `ms-usuarios` para el Sistema de Mesa de Ayuda de IDS.

Nos encontramos en la Práctica 1, paso 4 de la cadena de prompts.

Ya implementamos el CRUD REST y realizamos pruebas manuales satisfactorias con Postman.

También tenemos una clase `UsuarioServiceTest.java` con tres pruebas unitarias.

La guía exige al menos ocho pruebas automatizadas, utilizando Mockito y `@WebMvcTest`, con escenarios positivos y negativos.

Nuestra estrategia definitiva contempla 12 pruebas: 3 del servicio, 8 del controlador y 1 de validación de DTOs.

## TAREA
Revisa primero `UsuarioController`, `UsuarioService`, `ApiExceptionHandler`, los DTOs y `application.yaml`.

Después crea exclusivamente:

`src/test/java/com/ids/beca/usuarios/controller/UsuarioControllerTest.java`

Implementa exactamente ocho métodos `@Test`:

1. POST válido devuelve HTTP 201. Verifica los campos públicos, el rol SOLICITANTE, la cabecera Location y que no se expongan contraseñas ni hashes.
2. GET por ID existente devuelve HTTP 200 con los datos esperados.
3. GET general devuelve HTTP 200 y un arreglo JSON de usuarios.
4. PUT válido devuelve HTTP 200 con nombre y correo actualizados.
5. DELETE existente devuelve HTTP 204 sin contenido.
6. POST con nombre en blanco devuelve HTTP 400 y un error de validación.
7. GET de un usuario inexistente devuelve HTTP 404 mediante ProblemDetail.
8. POST de un correo duplicado devuelve HTTP 409 mediante ProblemDetail.

## RESTRICCIONES

- Utiliza `@WebMvcTest(UsuarioController.class)` y `@MockitoBean` para simular UsuarioService.
- Utiliza JUnit 5, Mockito y MockMvc.
- No emplees `@SpringBootTest`.
- No utilices PostgreSQL ni repositorios reales.
- No agregues dependencias.
- Mantén exactamente ocho métodos de prueba.
- No utilices pruebas parametrizadas ni abstracciones complejas.
- Utiliza nombres descriptivos en español.
- Mantén la estructura Arrange–Act–Assert.
- Respeta los contratos HTTP y los nombres reales de los DTOs.
- No modifiques el código de producción.
- No modifiques las tres pruebas existentes.
- No hagas commits ni push.

## FORMATO DE SALIDA
Presenta el código completo de `UsuarioControllerTest.java`.

Después incluye una tabla breve con el nombre de cada prueba, qué verifica y qué respuesta HTTP espera.

## EJEMPLO
Para comprobar un usuario inexistente:

- Arrange: configurar el mock de UsuarioService para lanzar UsuarioNoEncontradoException.
- Act: ejecutar una petición GET con MockMvc.
- Assert: comprobar HTTP 404 y el contenido correspondiente de ProblemDetail.
No necesitamos acceder a una base de datos real para este escenario.

## VERIFICACIÓN

1. Ejecuta `.\mvnw.cmd -B -Dtest=UsuarioControllerTest test`.
2. Comprueba que las ocho pruebas se ejecuten.
3. Informa cuántas pasan, fallan o se omiten.
4. Si una prueba falla, explica primero la causa.
5. No cambies código de producción para hacer pasar artificialmente las pruebas.
6. No ejecutes todavía `mvn verify`, ya que queda pendiente revisar el test inicial dependiente de PostgreSQL.
7. Espera mi revisión antes de modificar cualquier otro archivo.
````

**Resultado documentado por separado:** Se creó `UsuarioControllerTest.java` con ocho pruebas aprobadas individualmente. Posteriormente se registró el commit `b89f10c`.

---

## Interacción 4 — Una prueba de validación de DTO

**Fecha y hora:** 04/11/2026 19:43:11 (UTC−06:00)
**Objetivo:** Verificar con Jakarta Validation el límite BCrypt de 72 bytes UTF-8, incluyendo caracteres multibyte.

**Mensaje original enviado a Copilot:**

````markdown
## ROL
Actúa como desarrollador backend especializado en Java 21, Jakarta Bean Validation y JUnit 5.

## CONTEXTO
Estamos desarrollando `ms-usuarios` en la Práctica 1 de IDS.

Ya implementamos y ejecutamos satisfactoriamente 11 pruebas automatizadas: 3 de UsuarioService y 8 de UsuarioController.

El último commit es `b89f10c`, en la rama `practica-1`.

Nos falta una prueba para completar las 12 que acordamos, priorizando código sencillo y fácil de explicar.

## TAREA
Revisa primero la implementación real de `CrearUsuarioRequest.java`, especialmente las anotaciones Jakarta y la validación del límite BCrypt.

Después crea exclusivamente:

`src/test/java/com/ids/beca/usuarios/dto/CrearUsuarioRequestTest.java`

Implementa exactamente un método `@Test` que utilice un Validator real de Jakarta Bean Validation para comprobar tres escenarios:

1. Una contraseña de 72 bytes UTF-8 debe superar la validación correspondiente al límite BCrypt.
2. Una contraseña de 73 bytes UTF-8 debe producir una violación de dicha restricción.
3. Una contraseña de 37 caracteres `ñ` (74 bytes UTF-8) también debe producir una violación.
Utiliza nombre y correo ficticios válidos para evitar errores adicionales.

Comprueba las violaciones correspondientes a la restricción de 72 bytes, no solamente la existencia genérica de errores.

## RESTRICCIONES

- Utiliza JUnit 5, Jakarta Validation y AssertJ.
- Mantén exactamente un método @Test.
- No utilices Mockito ni Spring Boot para esta prueba.
- No agregues dependencias.
- No modifiques código de producción.
- No modifiques las once pruebas existentes.
- No utilices PostgreSQL.
- Evita métodos auxiliares innecesarios.
- Mantén una estructura sencilla que pueda explicar frente al coach.
- No hagas commits ni push.

## FORMATO DE SALIDA
Muestra el código completo del archivo y explica brevemente cada comprobación.

## EJEMPLO
Para comprobar la contraseña de 73 bytes, crea un CrearUsuarioRequest con una contraseña ficticia de 73 caracteres ASCII y utiliza Validator.validate().

Verifica que exista una violación asociada específicamente a la restricción que impide superar los 72 bytes.

## VERIFICACIÓN

1. Ejecuta `.\mvnw.cmd -B -Dtest=CrearUsuarioRequestTest test`.
2. Confirma que se ejecutó exactamente una prueba.
3. Informa las pruebas aprobadas, fallidas y omitidas.
4. Si falla, explica la causa antes de proponer cualquier corrección.
5. No modifiques el código de producción sin mi autorización.
6. No ejecutes todavía mvn verify.
7. Espera mi revisión antes de continuar.
````

**Resultado documentado por separado:** Se creó `CrearUsuarioRequestTest.java` con un método `@Test` que verificó los tres escenarios: 72 bytes válidos, 73 bytes rechazados y 37 caracteres `ñ` —74 bytes— rechazados. Posteriormente se registró el commit `2bbf107`.

---

## Cierre de la etapa de pruebas

- **Distribución acordada:** 3 pruebas de servicio con Mockito, 8 pruebas del controlador con `@WebMvcTest` y MockMvc, y 1 prueba de DTO con Jakarta Validation.
- **Resultado de las pruebas nuevas:** 12/12 aprobadas en una misma ejecución.
- **Verificación integral:** 13/13 aprobadas con `mvnw.cmd -B clean verify` en el entorno de desarrollo correctamente configurado; empaquetado JAR exitoso.
- **Precaución de reproducibilidad:** el resultado anterior no demuestra que `verify` funcione sin PostgreSQL. `MsUsuariosApplicationTests` exige una base disponible y las variables de entorno de conexión; se debe explicar este requisito en el README y considerar una estrategia independiente de la máquina local en siguientes etapas.
- **Aprendizaje al emplear IA:** se revisó críticamente el plan inicial y se acotó el volumen de pruebas a casos prioritarios, positivos y negativos, que el desarrollador puede explicar y mantener.

*Transcripción preparada a partir de una sesión real de Copilot; no se añadieron prompts retrospectivos ni se modificó el código del proyecto.*

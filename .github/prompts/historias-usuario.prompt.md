## ROL
Actúa como analista de requerimientos de software con experiencia en sistemas de Mesa de Ayuda y arquitectura de microservicios con Java y Spring Boot.

## CONTEXTO
Sistema de Mesa de Ayuda desarrollado mediante microservicios.

Mi responsabilidad es `ms-usuarios`, encargado de la gestión de usuarios y autenticación.

Necesidad del negocio (contexto disponible):

"Necesitamos que el sistema permita registrar usuarios e iniciar sesión de forma segura. Existen tres roles: SOLICITANTE, AGENTE y ADMIN. La autenticación utilizará JWT y otros microservicios necesitarán identificar a los usuarios para realizar sus operaciones."

Tecnologías de referencia: Java 21, Spring Boot 3.x, Maven y PostgreSQL 16.

## TAREA

1. Identifica actores, entidades y reglas de negocio.
2. Redacta al menos cinco historias de usuario con el formato: Como [rol], quiero [acción], para [beneficio].
3. Para cada historia escribe criterios de aceptación en Gherkin (Dado / Cuando / Entonces), incluyendo al menos un escenario positivo y uno negativo.
4. Identifica al menos tres requerimientos no funcionales relacionados con seguridad, rendimiento, mantenibilidad o auditoría.
5. Lista las preguntas abiertas que harías al cliente antes de comenzar el desarrollo.

## RESTRICCIONES

- No inventes reglas de negocio que no estén respaldadas por el contexto.
- Marca explícitamente como SUPUESTO cualquier comportamiento o decisión que requiera confirmación.
- No asumas permisos específicos para los roles sin una definición previa.
- Limita el análisis a `ms-usuarios`.
- No generes código ni diseñes todavía la base de datos.

## FORMATO DE SALIDA
Entrega un documento Markdown con las siguientes secciones:

1. Actores.
2. Entidades.
3. Reglas de negocio.
4. Historias de usuario: tabla con ID, descripción, prioridad MoSCoW y estimación en puntos.
5. Criterios de aceptación Gherkin.
6. Requerimientos no funcionales.
7. Preguntas abiertas.

## EJEMPLO
HU-01: Como SOLICITANTE, quiero registrar una cuenta para acceder al Sistema de Mesa de Ayuda.

Escenario positivo:

- Dado que el registro está permitido para el solicitante.
- Cuando proporciona los datos requeridos.
- Entonces el sistema registra su cuenta.
Escenario negativo:

- Dado que el solicitante omite información obligatoria.
- Cuando intenta registrarse.
- Entonces el sistema rechaza la solicitud e informa los errores.
Nota: este ejemplo es ilustrativo. Las reglas de registro y los campos obligatorios deben validarse.

## VERIFICACIÓN
Antes de finalizar:

1. Comprueba que existan al menos cinco historias de usuario.
2. Verifica que cada historia tenga al menos un escenario negativo.
3. Comprueba que las reglas de negocio provengan del contexto o estén marcadas como SUPUESTO.
4. Verifica que se incluyan al menos tres requerimientos no funcionales.
5. Confirma que las preguntas abiertas sean relevantes para aclarar el alcance del microservicio.
Si falta información, señálala sin inventar una respuesta.
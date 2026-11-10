Requerimientos — MS-Usuarios
Proyecto: Sistema de Mesa de Ayuda
Microservicio: ms-usuarios
Práctica: P1 — Del requerimiento al código con prompts
Versión: 0.5 — Supuestos de negocio identificados durante P2
Estado: Implementación técnica de P1 completada; decisiones de negocio provisionales
Herramienta de IA utilizada en el análisis inicial: GitHub Copilot
1. Necesidad del negocio
Se requiere gestionar los usuarios de un Sistema de Mesa de Ayuda.

El sistema debe permitir registrar usuarios e iniciar sesión de manera segura.

Se contemplan tres roles: SOLICITANTE, AGENTE y ADMIN.

La autenticación utilizará tokens JWT.

Los demás microservicios deberán poder identificar a los usuarios autenticados.

Se necesita consultar y mantener actualizada la información de los usuarios.

Las reglas específicas de administración, permisos y atributos deberán validarse antes de considerarse definitivas.
2. Alcance y procedencia de los requisitos
2.1 Capacidades establecidas en la guía
El microservicio ms-usuarios es responsable del registro de usuarios, inicio de sesión, emisión de JWT y manejo de los roles SOLICITANTE, AGENTE y ADMIN.

La Práctica 1 requiere desarrollar un CRUD REST con persistencia, validaciones, manejo de errores y pruebas.
2.2 Propuestas identificadas en el análisis con GitHub Copilot
SUPUESTO: Las siguientes funcionalidades surgieron durante el análisis, pero sus reglas de negocio no están aprobadas:
- Registro público de usuarios.
- Creación administrativa de cuentas AGENTE y ADMIN.
- Modificación administrativa de roles.
- Activación y desactivación de cuentas.
- Consulta del perfil propio.
No se consideran funcionalidades obligatorias de P1 únicamente por haber sido sugeridas por Copilot. Su incorporación requiere validación antes de implementarlas.
2.3 Distribución de responsabilidades
Práctica	Alcance
P1	Requerimientos, modelo de datos, migración Flyway, CRUD, validaciones, errores y pruebas
P2	Calidad de código y herramientas de análisis
P3	Autenticación JWT, autorización por roles y comunicación segura
P4	Contenedores, integración y CI


La protección de contraseñas deberá implementarse desde P1.
3. Historias de usuario
Se conservan las siete historias identificadas en la primera interacción con Copilot.
ID	Historia	Prioridad preliminar	Estado
HU-01	Como usuario, quiero registrar una cuenta para acceder al Sistema de Mesa de Ayuda.	Must	Capacidad identificada
HU-02	Como usuario registrado, quiero iniciar sesión de manera segura para acceder al sistema.	Must	Capacidad identificada
HU-03	Como microservicio consumidor, quiero identificar al usuario autenticado mediante JWT para procesar las operaciones correspondientes.	Must	Integración por definir
HU-04	Como administrador, quiero crear cuentas para agentes y administradores para gestionar al personal del sistema.	Should	Propuesta
HU-05	Como administrador, quiero modificar los roles asignados a los usuarios para gestionar sus responsabilidades.	Should	Propuesta
HU-06	Como administrador, quiero activar o desactivar cuentas para controlar su acceso.	Could	Propuesta
HU-07	Como usuario autenticado, quiero consultar mi perfil para conocer mis datos registrados.	Could	Propuesta


Las prioridades son preliminares y no constituyen aprobación formal de un cliente.
4. Criterios de aceptación de las historias de usuario
Los siguientes criterios se consolidaron a partir de las dos interacciones con Copilot y su revisión posterior. Los escenarios de funcionalidades propuestas no equivalen a requisitos aprobados.
HU-01 — Registro de usuarios
Escenario: registro exitoso
Dado que se proporciona información válida para el registro
Cuando se solicita crear una cuenta
Entonces el sistema registra al usuario correctamente
Y almacena su contraseña protegida
Escenario: registro con información inválida
Dado que se proporciona información obligatoria incompleta o inválida
Cuando se solicita registrar al usuario
Entonces el sistema rechaza la operación
Y devuelve los errores de validación
Pendiente: confirmar el mecanismo de registro y los datos obligatorios definitivos.
HU-02 — Inicio de sesión
Escenario: autenticación exitosa
Dado que existe un usuario registrado
Cuando proporciona credenciales correctas
Entonces el sistema permite su autenticación
Escenario: credenciales incorrectas
Dado que se proporcionan credenciales incorrectas
Cuando se intenta iniciar sesión
Entonces el sistema rechaza la autenticación
Implementación principal: P3.
HU-03 — Identificación mediante JWT
Escenario: JWT válido
Dado que un microservicio recibe un JWT válido
Cuando verifica el token mediante el mecanismo acordado
Entonces puede identificar al usuario autenticado
Escenario: JWT inválido
Dado que un microservicio recibe un JWT inválido
Cuando intenta verificarlo
Entonces la validación falla
Implementación principal: P3.
HU-04 — Creación administrativa de cuentas
Estado: propuesta pendiente de validación.

Escenario: creación autorizada
Dado que un administrador tiene permisos para crear cuentas
Cuando solicita crear una cuenta AGENTE o ADMIN con datos válidos
Entonces el sistema registra la cuenta con el rol correspondiente
Escenario: creación no autorizada
Dado que un usuario no tiene permisos administrativos
Cuando intenta crear una cuenta AGENTE o ADMIN
Entonces el sistema rechaza la operación
HU-05 — Modificación de roles
Estado: propuesta pendiente de validación.

Escenario: asignación válida
Dado que un administrador tiene permisos para modificar roles
Cuando asigna un rol permitido a un usuario existente
Entonces el sistema actualiza su rol
Escenario: rol no reconocido
Dado que se solicita asignar un rol desconocido
Cuando el sistema valida la modificación
Entonces rechaza la operación
Y devuelve un error de validación
HU-06 — Activación y desactivación
Estado: propuesta pendiente de validación.

Escenario: desactivación autorizada
Dado que existe una cuenta activa
Y un administrador tiene permisos para desactivarla
Cuando solicita su desactivación
Entonces el sistema cambia su estado a inactiva
Escenario: desactivación no autorizada
Dado que un usuario no tiene permisos administrativos
Cuando intenta desactivar una cuenta
Entonces el sistema rechaza la operación
Observación: esta funcionalidad requeriría un atributo de estado que no forma parte del modelo inicial de P1.
HU-07 — Consulta del perfil propio
Estado: propuesta pendiente de validación.

Escenario: consulta autorizada
Dado que un usuario está autenticado
Cuando solicita consultar su perfil
Entonces el sistema devuelve sus datos permitidos
Escenario: consulta sin autenticación
Dado que una solicitud no contiene autenticación válida
Cuando intenta consultar un perfil protegido
Entonces el sistema rechaza el acceso
Observación: la consulta de perfil autenticado es distinta de la operación de consulta por ID utilizada para demostrar el CRUD técnico de P1.
5. Contrato técnico provisional del CRUD de P1
Las operaciones siguientes se definen para cumplir la Práctica 1. Constituyen decisiones de implementación y no nuevas historias atribuidas a Copilot.

SUPUESTO: El contrato REST de P1 es una especificación técnica implementada para la práctica, pero no un contrato de negocio aprobado para el sistema integrado. Las decisiones provisionales se indican en los apartados siguientes.
5.1 Endpoints
Método	Endpoint propuesto	Operación	Respuesta exitosa
POST	/api/usuarios	Crear usuario	201 Created
GET	/api/usuarios/{id}	Consultar por ID	200 OK
GET	/api/usuarios	Listar usuarios	200 OK
PUT	/api/usuarios/{id}	Actualizar nombre y correo	200 OK
DELETE	/api/usuarios/{id}	Eliminar usuario	204 No Content


5.2 Datos de entrada
Create — POST

Campos recibidos:
- nombre: obligatorio.
- email: obligatorio.
- password: obligatoria.
SUPUESTO: El servidor asigna automáticamente el rol SOLICITANTE a las cuentas creadas mediante el CRUD de P1. Esta regla está implementada, pero la política definitiva de registro y asignación de roles no ha sido aprobada.

El cliente no podrá proporcionar id, password_hash ni created_at.

SUPUESTO: El endpoint de creación de P1 no permite que el cliente se asigne los roles AGENTE o ADMIN. La gestión de cuentas privilegiadas requiere una política de autorización validada para P3.

La creación de cuentas con roles administrativos quedará para la validación de reglas de negocio y la autorización de P3.

Update — PUT

Campos recibidos:
- nombre: obligatorio.
- email: obligatorio.
SUPUESTO: Para P1, la operación PUT sustituye los dos campos editables, nombre y email, por lo que ambos deben proporcionarse. El contrato definitivo de actualización (incluida la posible actualización parcial) está pendiente de validación.

No se permitirá modificar mediante este endpoint:
- id.
- rol.
- password_hash.
- created_at.
SUPUESTO: La modificación de roles y el cambio de contraseña se gestionarán mediante contratos separados cuando se definan las reglas de negocio y seguridad correspondientes.
5.3 Datos de salida
SUPUESTO: El DTO público de usuarios de P1 expondrá únicamente estos campos, sujetos a revisión cuando se defina el contrato entre microservicios:
- id.
- nombre.
- email.
- rol.
- createdAt.
Nunca devolverán password ni password_hash. La exclusión de credenciales es un requisito de seguridad, no un supuesto opcional.
5.4 Reglas de validación
SUPUESTO: Los límites y reglas de formato siguientes fueron definidos para construir y probar el CRUD de P1; su aceptación definitiva por negocio está pendiente.
Atributo	Regla provisional
nombre	Obligatorio, sin espacios en blanco como único contenido; longitud máxima de 120 caracteres
email	Obligatorio, formato de correo válido, longitud máxima de 254 caracteres
password	Obligatoria en creación, mínimo 8 caracteres; las restricciones adicionales deberán ajustarse al algoritmo de hash seleccionado
rol	Un único valor entre SOLICITANTE, AGENTE y ADMIN
id	Generado por la base de datos, no editable
created_at	Generado automáticamente, no editable


SUPUESTO: Para P1, el correo se normaliza eliminando espacios externos y convirtiéndolo a minúsculas antes de guardarlo. La unicidad se aplica sin distinguir mayúsculas y minúsculas; su uso definitivo como identificador de acceso debe validarse.

Estas reglas son decisiones técnicas provisionales, aunque ya formen parte de la implementación y de la migración V1.
5.5 Manejo de errores
SUPUESTO: La siguiente correspondencia entre condiciones de fallo y códigos HTTP constituye el contrato técnico de P1, pendiente de validación como contrato definitivo entre servicios.
Condición	Respuesta prevista
Información de entrada inválida	400 Bad Request
Usuario inexistente	404 Not Found
Correo electrónico duplicado	409 Conflict
Error inesperado del servidor	500 Internal Server Error


Los errores se devolverán mediante ProblemDetail, con manejo centralizado mediante @RestControllerAdvice.

No se expondrán contraseñas ni información sensible en los mensajes de error.
5.6 Restricción de seguridad para P1
La implementación del CRUD de P1 no equivale a una API pública lista para producción.

Hasta implementar la autenticación y autorización de P3:
- El CRUD deberá probarse únicamente en un entorno local aislado.
- No deberá publicarse en un servidor accesible sin controles de acceso.
- No deberán configurarse contraseñas administrativas predeterminadas.
- Las credenciales deberán almacenarse como hashes desde P1.
5.7 Eliminación
SUPUESTO: Para la implementación técnica de P1 se utiliza eliminación física por identificador. Es una decisión de alcance del prototipo, no una política de conservación de datos aprobada para el Sistema de Mesa de Ayuda.

Esta decisión permite implementar y verificar la operación Delete, pero no constituye una política definitiva de eliminación en el sistema integrado.

En particular, deberá revisarse cuando existan referencias a usuarios desde otros microservicios.
6. Modelo conceptual de datos
6.1 Entidad Usuario
SUPUESTO: Para P1 se adoptó un modelo mínimo de una sola tabla usuarios. Su suficiencia para el sistema integrado todavía requiere validación.
Campo	Descripción	Restricciones propuestas
id	Identificador interno	Clave primaria, generado automáticamente
nombre	Nombre del usuario	Obligatorio, máximo 120 caracteres
email	Correo electrónico	Obligatorio, máximo 254 caracteres, único sin distinguir mayúsculas
password_hash	Hash de contraseña	Obligatorio, nunca expuesto
rol	Rol del usuario	Obligatorio, uno de los tres valores permitidos
created_at	Fecha de creación	Obligatoria, generada automáticamente


La elección exacta de tipos PostgreSQL y la estrategia de índices se revisarán durante el diseño de Flyway.
6.2 Roles
Valores conocidos:
- SOLICITANTE.
- AGENTE.
- ADMIN.
SUPUESTO: En P1 cada usuario tiene un único rol. El requisito de manejar los tres roles sí está establecido, pero la cardinalidad de roles por usuario no fue confirmada por negocio.

SUPUESTO: Para el modelo inicial no se utiliza una tabla de relación entre usuarios y roles. Se revaluará si se permiten múltiples roles.

La base de datos deberá restringir los valores admitidos para evitar registros inconsistentes.
6.3 Restricciones y consistencia
SUPUESTO: La siguiente configuración del esquema representa las decisiones de persistencia de P1; se deberán revisar sus implicaciones en el sistema integrado.
- id será clave primaria.
- nombre, email, password_hash, rol y created_at no permitirán valores nulos.
- El correo tendrá una restricción de unicidad que ignore diferencias entre mayúsculas y minúsculas.
- rol solo admitirá los tres valores conocidos.
- created_at tendrá un valor predeterminado generado por la base de datos.
- Las tablas serán creadas por Flyway, no por generación automática de Hibernate.
La migración deberá justificar cada índice mediante comentarios SQL.
6.4 Decisiones técnicas provisionales
Las decisiones marcadas SUPUESTO: son decisiones de implementación de P1, no requisitos de negocio aprobados. Se mantienen alineadas al código actual mientras no se valide un contrato definitivo.
Decisión	Justificación
SUPUESTO: Una tabla de usuarios	Modelo mínimo para el CRUD
SUPUESTO: Correo único	Evitar duplicación de cuentas
SUPUESTO: Un rol por usuario	Simplificar el modelo inicial
SUPUESTO: Rol SOLICITANTE en Create	Evitar asignación directa de privilegios mediante el CRUD local
Hash obligatorio	Proteger las credenciales; requisito de seguridad, no supuesto opcional
SUPUESTO: Update limitado a nombre y correo	Evitar mezclar cambios administrativos o de seguridad con actualización general
SUPUESTO: Eliminación física	Satisfacer el CRUD de P1 en un entorno aislado


Estas decisiones son posteriores al análisis inicial de Copilot y permanecen sujetas a validación, excepto la obligación de proteger credenciales, que deriva del requisito de seguridad.
7. Criterios técnicos de aceptación del CRUD de P1
Los siguientes escenarios complementan las siete historias de usuario originales. Su finalidad es permitir la implementación y las pruebas del CRUD exigido por IDS.

SUPUESTO: Las decisiones de negocio representadas en estos escenarios (rol inicial, campos editables, unicidad del correo y eliminación física) corresponden a las reglas técnicas provisionales ya identificadas en las secciones 5 y 6. No equivalen a aprobación de un cliente.
CRUD-01 — Creación
Escenario: crear usuario válido
Dado que se proporcionan nombre, correo y contraseña válidos
Y el correo no está registrado
Cuando se solicita crear el usuario
Entonces el sistema devuelve 201
Y almacena el usuario con rol SOLICITANTE
Y guarda únicamente el hash de la contraseña
Escenario: correo duplicado
Dado que existe un usuario con el mismo correo, sin distinguir mayúsculas
Cuando se intenta registrar otro usuario
Entonces el sistema devuelve 409
Y no crea un registro duplicado
CRUD-02 — Consulta
Escenario: consultar usuario existente
Dado que existe un usuario registrado
Cuando se consulta su identificador
Entonces el sistema devuelve 200
Y presenta sus datos permitidos sin credenciales
Escenario: usuario inexistente
Dado que no existe un usuario con el identificador solicitado
Cuando se consulta dicho identificador
Entonces el sistema devuelve 404
Escenario: listar usuarios
Dado que el sistema contiene cero o más usuarios
Cuando se solicita el listado
Entonces el sistema devuelve 200
Y proporciona una colección sin contraseñas ni hashes
CRUD-03 — Actualización
Escenario: actualización válida
Dado que existe un usuario registrado
Cuando se solicita actualizar su nombre y correo con valores válidos
Entonces el sistema devuelve 200
Y conserva su identificador, rol, contraseña y fecha de creación
Escenario: actualización de usuario inexistente
Dado que no existe el usuario solicitado
Cuando se intenta actualizar sus datos
Entonces el sistema devuelve 404
Escenario: correo duplicado
Dado que el nuevo correo pertenece a otro usuario
Cuando se intenta actualizar el registro
Entonces el sistema devuelve 409
Y conserva los datos anteriores
CRUD-04 — Eliminación
Escenario: eliminación exitosa
Dado que existe un usuario registrado
Cuando se solicita eliminarlo por identificador
Entonces el sistema elimina el registro
Y devuelve 204
Escenario: eliminación inexistente
Dado que el identificador solicitado no corresponde a ningún usuario
Cuando se solicita eliminarlo
Entonces el sistema devuelve 404
Estos escenarios proporcionan una base para las pruebas de P1 y no representan historias nuevas generadas por Copilot.
8. Requerimientos no funcionales
ID	Requerimiento	Criterio
RNF-01	Seguridad de credenciales	No almacenar contraseñas en texto plano
RNF-02	Seguridad de autenticación	Proteger los JWT mediante mecanismos criptográficos adecuados
RNF-03	Validación y errores	Validar entradas y manejar errores con un formato consistente
RNF-04	Mantenibilidad	Incorporar pruebas automatizadas positivas y negativas
RNF-05	Documentación	Documentar el contrato y la ejecución de la API


Para P1 se aplican los requisitos técnicos de la guía:
- Java 21.
- Spring Boot 3.x.
- Maven.
- PostgreSQL 16.
- Flyway.
- Spring Data JPA.
- Jakarta Validation.
- DTOs mediante Java records.
- @RestControllerAdvice y ProblemDetail.
- JUnit 5 y Mockito.
- Mínimo ocho pruebas automatizadas con pruebas de servicio y de controlador.
- Verificación mediante mvn -B verify.
9. Preguntas abiertas
Negocio y roles
1. ¿El registro de usuarios será público o restringido?
2. ¿Quién podrá crear cuentas AGENTE y ADMIN?
3. ¿Cómo se creará el primer administrador?
4. ¿Un usuario podrá tener varios roles?
5. ¿Quién tendrá permiso para cambiar los roles?
6. ¿Se permitirá activar y desactivar cuentas?
7. ¿La eliminación deberá ser física o lógica en el sistema definitivo?
Datos y seguridad
8. ¿Qué atributos adicionales necesitarán los usuarios?
9. ¿El correo será definitivamente el identificador de inicio de sesión?
10. ¿Existirá una operación de cambio de contraseña?
11. ¿Qué información será visible en el perfil propio?
12. ¿Cómo verificarán los demás microservicios los JWT?
13. ¿Qué operaciones requerirán permisos administrativos?
Las decisiones provisionales documentadas permiten construir el prototipo de P1 sin presentar estas respuestas como aprobadas.
10. Trazabilidad del análisis con GitHub Copilot
Interacción 1 — Análisis de requerimientos
Herramienta: GitHub Copilot
Referencia: .github/prompts/historias-usuario.prompt.md

Copilot generó siete historias de usuario, criterios Gherkin, requerimientos no funcionales y preguntas abiertas.

Durante la revisión se detectó que algunas funcionalidades administrativas dependían de reglas no confirmadas.
Interacción 2 — Revisión crítica de requerimientos
Herramienta: GitHub Copilot
Referencia: .github/prompts/revision-requerimientos.md

Copilot identificó ambigüedades, supuestos y diferencias entre funcionalidades principales y propuestas.

También sugirió posponer parte del CRUD, recomendación que no se adoptó porque la guía de P1 exige Create, Read, Update y Delete.
Revisión técnica posterior
Después de las interacciones anteriores, se consolidó el alcance técnico de P1:
- Se conservaron las siete historias originales.
- Se documentó por separado el contrato del CRUD.
- Se limitaron los campos editables en Update.
- Se definió la asignación provisional del rol SOLICITANTE en Create.
- Se establecieron reglas mínimas de validación y errores.
- Se agregó trazabilidad entre operaciones y escenarios de prueba.
- Se identificaron explícitamente las decisiones pendientes.
Esta revisión no constituye una nueva interacción con GitHub Copilot y no debe registrarse como tal.

La evidencia original de Copilot corresponde a sus prompts y respuestas reales. Las decisiones técnicas consolidadas en este documento no deben atribuirse a la herramienta como si hubieran sido generadas durante esas interacciones.
11. Estado de la Práctica 1
Al 04/11/2026 se completó la implementación técnica del CRUD REST de ms-usuarios.
Funcionalidades implementadas
- Migración Flyway V1 aplicada y verificada en PostgreSQL 16.
- Entidad JPA Usuario y repositorio Spring Data JPA.
- DTOs mediante Java records y validaciones Jakarta.
- Cinco endpoints REST para crear, consultar, listar, actualizar y eliminar usuarios.
- Contraseñas protegidas mediante BCrypt.
- Manejo global de excepciones con @RestControllerAdvice y ProblemDetail.
- Normalización del correo y control de duplicados.
Verificaciones realizadas
Las operaciones CRUD fueron probadas manualmente mediante Postman, incluyendo escenarios exitosos y respuestas HTTP 400, 404 y 409.

Se implementaron 12 pruebas automatizadas: 3 de servicio con Mockito, 8 de controlador con MockMvc y 1 de validación del DTO.

La ejecución de .\mvnw.cmd -B clean verify finalizó con BUILD SUCCESS y 13 pruebas aprobadas, incluyendo la prueba inicial contextLoads.

La verificación completa requiere PostgreSQL disponible y las variables de entorno DB_USERNAME y DB_PASSWORD configuradas.
Funcionalidades pendientes para prácticas posteriores
- Autenticación mediante JWT.
- Autorización de operaciones según roles.
- Administración de cuentas y permisos.
- Integración con los demás microservicios.
- Contenedores y automatización CI.
El CRUD desarrollado en P1 está destinado a pruebas en un entorno local controlado. No debe exponerse públicamente sin incorporar los controles de autenticación y autorización correspondientes.
Siguiente actividad
Completar el README con las instrucciones de ejecución y pruebas, revisar las evidencias y preparar el Pull Request y el tag p1-entrega.

Nota de seguimiento P2 (09/11/2026): Los resultados anteriores son históricos de P1. En P2 ya se incorporó CI y se amplió la batería a 16 pruebas verificadas. Se proponen dos pruebas adicionales de servicio (listar usuarios y eliminar usuario existente), pendientes de ejecución y medición de cobertura; no se presentan como aprobadas en este documento.
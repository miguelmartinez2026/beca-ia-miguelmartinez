# Cadena de prompts — Paso 2: Modelo de datos y Flyway

## Primera interacción: propuesta del modelo físico

## ROL
Actúa como arquitecto de bases de datos y desarrollador backend senior especializado en Java 21, Spring Boot 3, PostgreSQL 16 y Flyway.

## CONTEXTO
Estamos desarrollando el microservicio `ms-usuarios` para el Sistema de Mesa de Ayuda de la beca IDS.

Estamos trabajando en la Práctica 1, específicamente en el paso 2 de la cadena de prompts: modelo de datos y migración Flyway.

El documento principal de referencia es `docs/requerimientos.md`, versión 0.3.

Lee ese documento completo antes de comenzar. Respeta la diferencia entre las capacidades establecidas por la guía, las propuestas pendientes de validación y las decisiones técnicas provisionales adoptadas para P1.

Nuestro modelo inicial considera una tabla `usuarios` con seis campos: `id`, `nombre`, `email`, `password_hash`, `rol` y `created_at`.

## TAREA
Diseña el modelo físico PostgreSQL necesario para implementar el CRUD de usuarios de la Práctica 1.

Propón la migración Flyway V1, incluyendo columnas, tipos de datos, claves, restricciones e índices.

En esta primera respuesta presenta únicamente la propuesta para revisión. Todavía no crees ni modifiques archivos.

## RESTRICCIONES

- Utiliza PostgreSQL 16.
- Mantén una sola tabla `usuarios`, salvo que identifiques una necesidad imprescindible y la justifiques.
- Conserva los seis campos acordados.
- Los roles válidos son SOLICITANTE, AGENTE y ADMIN.
- Cada usuario tendrá un único rol durante P1.
- El correo debe ser único sin distinguir mayúsculas de minúsculas, de acuerdo con la normalización prevista en los requerimientos.
- Incluye restricciones coherentes con los campos obligatorios y las longitudes definidas.
- Utiliza Flyway como responsable de crear el esquema.
- No almacenes contraseñas en texto plano. La aplicación será responsable de generar los hashes antes de persistirlos.
- No agregues funcionalidades de JWT, recuperación de contraseñas, sesiones ni administración de roles.
- No generes datos iniciales, usuarios administradores ni contraseñas predeterminadas.
- Justifica cada índice y evita índices redundantes.
- No modifiques archivos existentes ni ejecutes comandos todavía.

## FORMATO DE SALIDA

1. Resumen del modelo propuesto.
2. Tabla con nombre de columna, tipo PostgreSQL, restricciones y justificación.
3. Restricciones e índices, explicando su propósito.
4. Contenido SQL propuesto para `src/main/resources/db/migration/V1__create_usuarios.sql`.
5. Riesgos, supuestos y decisiones pendientes.
6. Lista de archivos que sería necesario crear después de aprobar la propuesta.

## EJEMPLO
Si propones una restricción de unicidad sobre `email`, explica cómo evitará correos duplicados que solo difieran en mayúsculas y minúsculas.

No agregues un índice adicional sobre la clave primaria si PostgreSQL ya proporciona uno automáticamente.

## VERIFICACIÓN
Antes de presentar el resultado:

- Contrasta cada columna con `docs/requerimientos.md`.
- Comprueba que el SQL sea compatible con PostgreSQL 16.
- Comprueba que la migración pueda ejecutarse sobre una base de datos vacía.
- Verifica la coherencia entre las restricciones SQL y las validaciones previstas para Java.
- Identifica cualquier discrepancia con los requerimientos en lugar de corregirla silenciosamente.
No crees archivos ni modifiques el repositorio durante esta respuesta.

Espera mi aprobación explícita antes de implementar la migración.

---

## Segunda interacción: aprobación e implementación

He revisado tu propuesta del modelo físico y la migración Flyway V1 para `ms-usuarios`.

**Apruebo el diseño propuesto**, conservando los seis campos, sus tipos de datos, las restricciones CHECK y el índice único `uq_usuarios_email_lower` sobre `lower(email)`.

Antes de implementar, considera que el entorno del proyecto ha cambiado desde tu análisis inicial:

- Ya existe un proyecto Spring Boot 3.5.16 con Java 21 y Maven.
- El `pom.xml` ya incluye Spring Data JPA, PostgreSQL Driver, Flyway y Validation.
- Existe `src/main/resources/application.yml`, configurado para PostgreSQL mediante variables de entorno.
- PostgreSQL 16.15 ya está instalado y tenemos creada la base de datos `ms_usuarios_db`.
- El archivo `docs/requerimientos.md` versión 0.3 sigue siendo nuestra referencia funcional.
**TAREA**

1. Revisa brevemente el estado actualizado del repositorio para confirmar las rutas y dependencias existentes.
2. Genera exclusivamente el archivo `src/main/resources/db/migration/V1__create_usuarios.sql` con el diseño aprobado.
3. Conserva los comentarios que justifican las restricciones y los índices.
4. Corrige el comentario relacionado con HTTP 409: PostgreSQL debe garantizar la unicidad y generar el error de integridad correspondiente; la aplicación Java será responsable de convertirlo en HTTP 409.
**RESTRICCIONES**

- No agregues tablas, columnas, índices ni funcionalidades adicionales.
- No modifiques `pom.xml`, `application.yml`, `docs/requerimientos.md`, `BITACORA-IA.md` ni otros archivos.
- No generes datos iniciales ni usuarios predeterminados.
- No cambies el índice funcional aprobado.
- No implementes controladores, entidades, repositorios ni JWT.
- No ejecutes todavía la migración ni arranques Spring Boot.
**VERIFICACIÓN**

Al finalizar:

1. Confirma la ruta exacta del archivo creado.
2. Muéstrame su contenido SQL completo.
3. Explica brevemente cualquier diferencia respecto al SQL que propusiste originalmente.
4. Distingue claramente la revisión estática de la verificación pendiente contra PostgreSQL.
No declares la migración como ejecutada o validada en base de datos hasta que realicemos esa comprobación.
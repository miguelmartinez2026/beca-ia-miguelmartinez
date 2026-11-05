# Bitácora de uso de IA — Práctica 1

**Proyecto:** Sistema de Mesa de Ayuda  
**Microservicio:** `ms-usuarios`
**Herramienta:** GitHub Copilot en VS Code
**Práctica:** P1 — Del requerimiento al código con prompts

En esta bitácora registro cómo utilicé Copilot para desarrollar mi microservicio, qué resultados obtuve y qué decisiones o correcciones realicé durante el proceso.

| # | Fecha | Herramienta | Tipo (prompt/skill/agente) | Objetivo | Prompt o archivo | Resultado | Qué ajusté y por qué |
|---|---|---|---|---|---|---|---|
| 1 | 03/11/2026 | Copilot (Chat) | Prompt | Generar historias de usuario a partir de los requerimientos. | `prompts/historias-usuario.md` | Generó 7 historias, escenarios Gherkin, RNF y preguntas abiertas. | Encontré varias funcionalidades que no estaban confirmadas, así que pedí revisar los supuestos. |
| 2 | 03/11/2026 | Copilot (Chat) | Prompt | Revisar las historias y definir qué correspondía a P1. | `prompts/historias-usuario.md` | Identificó ambigüedades y propuso dejar algunas operaciones para después. | No acepté posponer Update y Delete porque la guía exige un CRUD completo. |
| 3 | 04/11/2026 | Copilot (Agent) | Prompt | Diseñar la tabla de usuarios y la migración Flyway. | `prompts/modelo-datos-flyway.md` | Propuso la tabla `usuarios`, sus restricciones y un índice único para el correo. | Le pedí corregir un comentario: PostgreSQL detecta el duplicado, pero es Java quien devuelve HTTP 409. |
| 4 | 04/11/2026 | Copilot (Agent) | Prompt | Crear la migración Flyway V1 aprobada. | `prompts/modelo-datos-flyway.md` | Generó `V1__create_usuarios.sql`. | Revisé el SQL y probé personalmente las restricciones en PostgreSQL antes de continuar. |
| 5 | 04/11/2026 | Copilot (Agent) | Prompt | Planear la implementación del CRUD REST. | `prompts/implementacion-rest.md` | Propuso las capas, los archivos necesarios y varias decisiones técnicas. | Revisé sus propuestas y cambié algunas decisiones, como utilizar Java sin Lombok y rechazar campos JSON desconocidos. |
| 6 | 04/11/2026 | Copilot (Agent) | Prompt | Configurar el cifrado de contraseñas. | `prompts/implementacion-rest.md` | Agregó `spring-security-crypto` y creó `PasswordConfig`. | Decidí utilizar solamente BCrypt, sin activar todavía toda la seguridad de Spring porque JWT corresponde a P3. |
| 7 | 04/11/2026 | Copilot (Agent) | Prompt | Crear la entidad y el repositorio. | `prompts/implementacion-rest.md` | Generó `Usuario`, `Rol` y `UsuarioRepository`. | Revisé que coincidieran con las seis columnas de Flyway y que no se pudieran modificar campos innecesarios. |
| 8 | 04/11/2026 | Copilot (Agent) | Prompt | Implementar los DTOs y sus validaciones. | `prompts/implementacion-rest.md` | Creó los records de creación, actualización y respuesta. | Revisé la normalización del correo y agregamos una validación específica para el límite de 72 bytes de BCrypt. |
| 9 | 04/11/2026 | Copilot (Agent) | Prompt | Implementar el servicio y las excepciones. | `prompts/implementacion-rest.md` | Generó `UsuarioService` y las excepciones para usuarios inexistentes y correos duplicados. | Revisé que el servicio aplicara las reglas de negocio y que las contraseñas se guardaran como hashes. |
| 10 | 04/11/2026 | Copilot (Agent) | Prompt | Crear el controlador y manejar errores HTTP. | `prompts/implementacion-rest.md` | Implementó los cinco endpoints y `ApiExceptionHandler` con `ProblemDetail`. | Probé las operaciones con Postman para comprobar que realmente funcionaran y devolvieran los códigos esperados. |
| 11 | 04/11/2026 | Copilot (Agent) | Prompt de corrección | Corregir un error al registrar correos duplicados. | `prompts/implementacion-rest.md` | Identificó que Hibernate podía devolver `null` como nombre de la restricción y ajustó la detección de SQLState `23505`. | Encontré el problema en Postman: devolvía 500 en vez de 409. Después de la corrección repetí la prueba y comprobé el resultado. |
| 12 | 04/11/2026 | Copilot (Agent) | Prompt | Planear las pruebas automatizadas. | `prompts/pruebas.md` | Propuso una batería bastante amplia de pruebas. | Decidí reducirla a 12 pruebas importantes, porque quería cubrir los requisitos sin generar código innecesario que después no pudiera explicar. |
| 13 | 04/11/2026 | Copilot (Agent) | Prompt | Crear pruebas unitarias del servicio. | `prompts/pruebas.md` | Generó 3 pruebas utilizando Mockito. | Revisé los escenarios y las verificaciones para asegurarme de que probaran las reglas de negocio sin necesitar PostgreSQL. |
| 14 | 04/11/2026 | Copilot (Agent) | Prompt | Crear pruebas del controlador. | `prompts/pruebas.md` | Generó 8 pruebas con `@WebMvcTest` y MockMvc. | Revisé los códigos HTTP, los escenarios negativos y que las respuestas no mostraran información sensible. |
| 15 | 04/11/2026 | Copilot (Agent) | Prompt | Probar el límite de las contraseñas. | `prompts/pruebas.md` | Creó una prueba que comprueba 72, 73 y 74 bytes UTF-8. | Revisé que validara específicamente la restricción de BCrypt y no simplemente cualquier error. |

## Resultado final de la práctica

Al terminar, ejecuté las pruebas automatizadas y comprobé que las 12 pruebas nuevas funcionaran correctamente.

También ejecuté:

```cmd
.\mvnw.cmd -B clean verify
```

El resultado fue **BUILD SUCCESS**, con 13 pruebas aprobadas, ninguna fallida y ninguna omitida.

Durante la primera ejecución tuve un error porque utilicé una terminal sin las variables de entorno de PostgreSQL. Lo identifiqué y repetí el comando desde la terminal configurada.

**Lo más importante que aprendí:** Copilot me ayudó a avanzar con el diseño, la implementación y las pruebas, pero tuve que revisar sus propuestas, decidir qué cambios aceptar y comprobar el funcionamiento real del código.

Los prompts utilizados se encuentran documentados en la carpeta `prompts/`.
# Instrucciones del proyecto — MS-Usuarios

## Contexto tecnológico

- Java 21 y Spring Boot 3.5.
- Maven con Maven Wrapper.
- PostgreSQL 16, Spring Data JPA y Flyway.
- JUnit 5, Mockito y MockMvc.
- Microservicio `ms-usuarios` del Sistema de Mesa de Ayuda.

## Reglas de desarrollo

1. Mantén la arquitectura por capas `controller → service → repository`. La lógica de negocio debe permanecer en la capa de servicio.

2. Utiliza Java records para los DTOs de entrada y salida. Nunca expongas entidades JPA directamente mediante la API REST.

3. Utiliza inyección de dependencias por constructor. Evita `@Autowired` sobre campos.

4. Aplica Jakarta Validation a los DTOs de entrada y `@Valid` en los controladores correspondientes.

5. Centraliza los errores HTTP mediante `@RestControllerAdvice` y utiliza `ProblemDetail` para mantener respuestas consistentes.

6. Mantén los métodos con un máximo de 30 líneas y una complejidad ciclomática máxima de 10. Si un método supera esos límites, propón una refactorización sin alterar su comportamiento.

7. Evita código muerto, imports innecesarios, duplicación de lógica y valores configurables escritos directamente en el código.

8. Conserva Flyway como responsable de las modificaciones del esquema. No utilices Hibernate para crear o actualizar automáticamente las tablas.

9. Mantén las reglas existentes de normalización y unicidad del correo electrónico. No modifiques los campos ni los roles del usuario sin justificación y aprobación.

## Reglas de seguridad

10. Nunca almacenes contraseñas en texto plano. Utiliza BCrypt y respeta el límite de 72 bytes UTF-8 del proceso de hashing.

11. Nunca expongas contraseñas, hashes, tokens, secretos ni credenciales en respuestas HTTP, mensajes de error o registros de la aplicación.

12. No escribas credenciales, claves, IP privadas ni URLs sensibles directamente en el código. Utiliza variables de entorno y configuración externa.

13. Utiliza consultas parametrizadas mediante Spring Data JPA. Nunca construyas consultas SQL concatenando datos proporcionados por el usuario.

## Pruebas y verificación

14. Antes de finalizar una modificación, ejecuta `.\mvnw.cmd -B verify` y comprueba que las pruebas existentes continúen aprobadas. No desactives reglas de calidad ni utilices `@SuppressWarnings` únicamente para conseguir un build exitoso.

## Restricciones de trabajo

- No implementes todavía JWT ni autorización por roles; corresponden a P3.
- No agregues dependencias, modifiques migraciones ni cambies contratos REST sin aprobación.
- Antes de modificar varios archivos, presenta la propuesta y explica su impacto.
- Si una regla de calidad genera un posible falso positivo, documéntalo y solicita revisión antes de modificar su configuración.

## Entrega P1 – Nombre Apellido – ms-usuarios

### Qué entrego

- [x] Código CRUD REST y pruebas automatizadas (`mvn -B verify` en verde).
- [x] Documento de requerimientos en `docs/requerimientos.md`.
- [x] Migración Flyway V1 para PostgreSQL.
- [x] `BITACORA-IA.md` actualizada con las interacciones y correcciones realizadas.
- [x] Prompts utilizados:
  - `prompts/historias-usuario.md`
  - `prompts/modelo-datos-flyway.md`
  - `prompts/implementacion-rest.md`
  - `prompts/pruebas.md`

### Cómo probarlo

Se requiere Java 21, PostgreSQL 16 y la base de datos `ms_usuarios_db`.

Configurar `DB_USERNAME` y `DB_PASSWORD` en las variables de entorno.

Ejecutar las pruebas:

```cmd
.\mvnw.cmd -B clean verify
```

Iniciar el microservicio:

```cmd
.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080/api/usuarios`.

Las instrucciones completas están en el `README.md`.

### Qué hizo la IA y qué hice yo

Utilicé GitHub Copilot para analizar los requerimientos, proponer el modelo de datos, implementar el CRUD y generar pruebas.

Revisé sus propuestas, corregí supuestos y delimité las funcionalidades que correspondían a P1.

Durante las pruebas manuales detecté que un correo duplicado devolvía HTTP 500 en lugar de 409, por lo que solicité investigar y corregir el problema.

También ajusté la cantidad de pruebas propuestas y verifiqué personalmente la migración, los endpoints y la ejecución de Maven.

### Dudas para la revisión

- Confirmar las reglas de administración de usuarios y asignación de roles para la Práctica 3.
- Revisar la estrategia de ejecución de pruebas con PostgreSQL para la futura integración continua.
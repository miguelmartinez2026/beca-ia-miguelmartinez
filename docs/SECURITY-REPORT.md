# Reporte de seguridad — ms-usuarios (P3)

- Fecha de revisión: 2026-11-11
- Próxima reevaluación obligatoria: **2026-12-06** (vencimiento de las supresiones)
- Alcance: aceptación temporal de riesgo para el alcance académico y la configuración actual del microservicio.
- Componente: Spring Security 6.5.11 (gestionado por Spring Boot 3.5.16).
- Fuente de los hallazgos: OWASP Dependency-Check (`failBuildOnCVSS=7`), reporte `target/dependency-check-report.json`.

## 1. Resumen

| Categoría | CVE |
|---|---|
| Corregidas | Ninguna. La corrección 6.5.12 solo está disponible con soporte Enterprise. |
| No aplicables bajo la configuración actual (sin supresión) | CVE-2026-47842, CVE-2026-59276 |
| Riesgo aceptado temporalmente (con supresión hasta 2026-12-06) | CVE-2026-59270, CVE-2026-41707, CVE-2026-47841 |
| Falsos positivos demostrados | Ninguna |
| Riesgo funcional abierto (no es CVE) | JWT vigente tras eliminar una cuenta (PF-23). Mitigación pendiente; ver sección 6. |

Una supresión de OWASP solo oculta el hallazgo; no corrige la vulnerabilidad.

## 2. Inventario de artefactos `org.springframework.security:6.5.11`

Inventario obtenido de `dependency:tree` (2026-11-11) y contrastado con el reporte de OWASP y las reglas de supresión. No se ha vuelto a ejecutar `dependency:tree` en esta revisión; la columna se mantiene tal como fue registrada.
La columna de CVE medias indica si OWASP las reporta (no se suprimen).

| packageUrl | Scope | Alta/crítica (3 CVE) | Medias (47842, 59276) |
|---|---|---|---|
| `pkg:maven/org.springframework.security/spring-security-crypto@6.5.11` | compile | regla previa (P2) | no marcado |
| `pkg:maven/org.springframework.security/spring-security-web@6.5.11` | compile (starter-security) | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-config@6.5.11` | compile (starter-security) | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-core@6.5.11` | compile (starter-oauth2-resource-server) | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-oauth2-resource-server@6.5.11` | compile | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-oauth2-core@6.5.11` | compile | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-oauth2-jose@6.5.11` | compile | suprimidas (P3-05C) | sí |
| `pkg:maven/org.springframework.security/spring-security-test@6.5.11` | test | no analizado: Dependency-Check omite el scope `test` por defecto | no analizado |

Los siete artefactos de scope compile quedan cubiertos. `spring-security-test` es solo de pruebas y no forma parte del artefacto desplegado.

OWASP asocia las CVE por el CPE del producto `spring_security`, que no distingue módulos.
Los módulos realmente afectados son otros (ver sección 3).

## 3. CVE de severidad alta o crítica (aceptación temporal)

### CVE-2026-59270 — LDAP embebido (CVSS 9.1)

- Componente vulnerable: `UnboundIdContainer` (módulo `spring-security-ldap`). Registra una credencial administrativa y escucha en todas las interfaces.
- Evidencia:
  - No hay `spring-security-ldap` ni `unboundid` en `pom.xml`, en el árbol de dependencias ni en el repositorio local de Maven.
  - No hay referencias a LDAP en `src/` ni en `application.yaml`.
- Condición de explotación: instanciar el servidor LDAP embebido. No identificada actualmente.
- Estado: **riesgo aceptado temporalmente**. No corregido.

### CVE-2026-41707 — DPoP (CVSS 7.4)

- Componente vulnerable: `DPoPProofJwtDecoderFactory` (`spring-security-oauth2-jose`). El caché de JWT ID tiene tamaño limitado y puede saturarse para repetir una prueba DPoP.
- Evidencia:
  - **DPoP está implícitamente disponible.** `OAuth2ResourceServerConfigurer` de Spring Security 6.5.11 registra `DPoPAuthenticationConfigurer` cuando `DPoPProofJwtDecoderFactory` está en el classpath, y `oauth2-jose` está presente por el uso de JWT.
  - `DPoPAuthenticationProvider.authenticate` autentica primero el access token con nuestro `JwtDecoder` (HS256) y falla con `invalid_token` antes de procesar la prueba DPoP.
  - La verificación exige un token vinculado a una clave mediante `cnf.jkt`. Desde P3-06, `POST /api/auth/login` (`TokenService`) emite tokens HS256 sin el claim `cnf`; no hay referencias a `cnf` ni `jkt` en `src/main/java`. La precondición no se cumple con el código actual, pero no se demuestra que la vulnerabilidad sea inexplotable: depende de que ese claim no se añada.
  - No hay código DPoP en `src/`.
- Condiciones de explotación: un access token válido firmado con nuestra clave y con `cnf.jkt`, más una prueba DPoP reutilizable tras saturar el caché. No se ha demostrado que estén presentes actualmente.
- Estado: **riesgo aceptado temporalmente**. No corregido. No se afirma que DPoP esté deshabilitado ni que la explotación sea imposible.

### CVE-2026-47841 — WebAuthn (CVSS 7.4)

- Componente vulnerable: soporte WebAuthn de `spring-security-web`. Evasión de la verificación de usuario con un almacén de sesión HTTP distribuido.
- Evidencia:
  - `WebAuthnConfigurer` solo se aplica mediante `http.webAuthn(...)`, que no se invoca en `SecurityConfig`.
  - `webauthn4j` y Spring Session no están en el árbol de dependencias.
  - `SecurityConfig` usa sesiones `STATELESS`, sin `formLogin`, `httpBasic` ni `logout`.
  - No hay referencias a WebAuthn ni passkeys en `src/` ni en la configuración.
- Condición de explotación: habilitar WebAuthn junto con sesiones HTTP distribuidas. No identificada actualmente.
- Estado: **riesgo aceptado temporalmente**. No corregido.

## 4. CVE de severidad media (sin supresión)

Ninguna supera el umbral `failBuildOnCVSS=7`, por lo que no bloquean el build y no se suprimen.

### CVE-2026-47842 (CVSS 6.5)

- `AesBytesEncryptor` con el constructor de dos argumentos, o con generador de IV nulo y modo CBC, cifra con un IV de ceros.
- Evidencia: `src/` solo usa `BCryptPasswordEncoder`; no hay `Encryptors`, `AesBytesEncryptor`, `BytesEncryptor` ni `TextEncryptor`.
- Estado: **no aplicable bajo la configuración actual**. No corregido.

### CVE-2026-59276 (CVSS 5.9)

- Comparaciones sin tiempo constante en `DigestAuthenticationFilter`, `KeyBasedPersistenceTokenService`, `BalloonHashingPassword4jPasswordEncoder`, `Pbkdf2Password4jPasswordEncoder` e `InMemoryOAuth2AuthorizationService` (aviso oficial de Spring).
- Evidencia: ninguno de esos componentes se usa. No hay servidor de autorización ni autenticación Digest, y las contraseñas se verifican con `BCryptPasswordEncoder`, que no figura en la lista.
- Estado: **no aplicable bajo la configuración actual**. No corregido.

## 5. Supresiones en `config/owasp-suppressions.xml`

Las reglas de Spring Security (P3-05C y las tres reglas previas de `spring-security-crypto`) usan `until="2026-12-06Z"` y `packageUrl` exacto, sin expresiones regulares. Las dos reglas de Spring Framework 6.2.19 para CVE-2026-47888 y CVE-2026-47893 (P2) tienen `until="2027-02-06Z"` y no se modifican en este documento.

| CVE | core | oauth2-resource-server | oauth2-core | web | config | oauth2-jose | crypto |
|---|---|---|---|---|---|---|---|
| CVE-2026-59270 | sí | sí | sí | sí | sí | sí | sí (regla previa, vencimiento ajustado) |
| CVE-2026-41707 | sí | sí | sí | sí | sí | sí | sí (regla previa, nota corregida) |
| CVE-2026-47841 | sí | sí | sí | sí | sí | sí | sí (regla previa, vencimiento ajustado) |

- Reglas nuevas en P3-05C: 18 (3 CVE × 6 artefactos: `core`, `oauth2-resource-server`, `oauth2-core`, `web`, `config` y `oauth2-jose`).
- `spring-security-oauth2-jose` contiene directamente `DPoPProofJwtDecoderFactory`; su regla es una aceptación temporal de riesgo bajo la configuración actual, no una ausencia de la vulnerabilidad.
- Las tres reglas previas de `spring-security-crypto` conservan su `packageUrl`; se actualizaron sus notas y su vencimiento (antes `2027-02-06Z`).
- No hay supresiones generales ni para las CVE medias.
- Las reglas de Spring Framework de P2 (`QUALITY-REPORT.md`) no se modificaron.
- La decisión es del responsable del desarrollo; no hay aprobación de un tercero.

## 6. Riesgos residuales

- Las cinco CVE siguen presentes en 6.5.11 y no están corregidas.
- DPoP está implícitamente disponible en el resource server. La explotación requeriría un token válido con `cnf.jkt`. El login de P3-06 no emite ese claim, pero la mitigación depende de mantenerlo así (ver sección 7).
- Las supresiones ocultan las CVE de OWASP: si cambia la configuración, el análisis no avisará automáticamente.
- Las notas de las supresiones deben mantenerse alineadas con el estado real del código.
- La corrección 6.5.12 (y 6.4.19, 5.8.28) solo está disponible con soporte Enterprise.


### JWT vigente después de eliminar una cuenta (P3-06)

- **Estado:** riesgo de seguridad confirmado mediante prueba funcional.
- **Fecha de verificación:** 2026-11-11.
- **Origen:** diseño de autenticación JWT stateless.
- **Mitigación:** pendiente de definición e implementación.

**Descripción**

La validación de JWT comprueba la firma HS256, el emisor,
la vigencia y los claims requeridos, pero no consulta
PostgreSQL para verificar que el usuario continúe existiendo.

Como consecuencia, eliminar una cuenta no revoca
automáticamente los JWT emitidos anteriormente.

**Evidencia funcional (PF-23)**

1. Se registró un usuario de pruebas con ID 10.
2. Se asignó el rol ADMIN directamente en PostgreSQL.
3. El usuario inició sesión y obtuvo un JWT válido.
4. Se eliminó la cuenta mediante DELETE /api/usuarios/10.
5. PostgreSQL confirmó que el usuario ya no existía.
6. Se reutilizó el JWT anterior en GET /api/usuarios.
7. El servidor respondió HTTP 200 OK, devolviendo [].

**Causa arquitectónica**

La validación del JWT es stateless: `JwtConfig` comprueba firma, emisor, tiempos y claims, pero no consulta el usuario en PostgreSQL. Por eso la eliminación de la cuenta no invalida los tokens ya emitidos.

**Impacto**

Un JWT previamente emitido puede conservar los permisos
de una cuenta eliminada mientras permanezca vigente.

En el caso de un JWT con rol ADMIN, esto podría permitir
el acceso a operaciones administrativas aun después
de eliminar la cuenta asociada.

**Condiciones y alcance**

- La vigencia configurada es de 3600 segundos, con tolerancia de reloj de 0 s (`JwtTimestampValidator(Duration.ZERO)`).
- No existe revocación inmediata de tokens.
- El acceso depende de que el JWT conserve una firma
  válida y no haya expirado.
- El comportamiento fue reproducido en el entorno local
  de pruebas funcionales.

**Mitigaciones por evaluar (opciones no aprobadas)**

- Validar la existencia y el estado del usuario durante
  las solicitudes autenticadas.
- Implementar revocación de JWT mediante un mecanismo
  centralizado.
- Evaluar una reducción de la vigencia del token como
  medida complementaria.

**Decisión**

El hallazgo queda documentado como riesgo pendiente.
No se implementará una solución de revocación dentro
de P3-06 sin definir previamente su diseño y alcance.

La ausencia de revocación debe evaluarse y resolverse
o aceptarse formalmente antes de un despliegue
en producción.


## 7. Condiciones que obligan a reevaluar

- Se agrega `spring-security-ldap`, UnboundID o un LDAP embebido.
- Se emiten, aceptan o validan tokens con `cnf` o DPoP, o se configura `.dpop(...)`.
- Se habilita `http.webAuthn(...)`, passkeys, Spring Session o sesiones HTTP.
- Se usa `Encryptors`, `AesBytesEncryptor`, autenticación Digest, un servidor de autorización de Spring o los encoders Password4j.
- Se cambia la emisión, validación o configuración de los tokens JWT (P3-06 en adelante).
- Cambia la versión de Spring Boot o Spring Security.
- Llega el 2026-12-06.

## 8. Recomendación

Actualizar a una versión corregida cuando sea viable: según el aviso de CVE-2026-59276, Spring Security 7.0.7 o 7.1.1 son OSS (confirmar lo mismo para las demás CVE antes de migrar), o 6.5.12 con soporte Enterprise.
Subir de línea a Spring Boot / Spring Security requiere aprobación y su propia evaluación de compatibilidad.
Hasta entonces, mantener las supresiones con su vencimiento y revisarlas en la fecha de reevaluación.

## 9. Controles JWT (P3-05 y P3-06)

Fuentes: `JwtConfig.java`, `JwtProperties.java`, `TokenService.java`, `AuthService.java`, `LoginResponse.java`, `SecurityConfig.java` y `application.yaml`.

**Implementado (verificado en código)**

- Firma HS256: `NimbusJwtDecoder` con HS256 (`JwtConfig`) y `JwsHeader.with(HS256)` (`TokenService`).
- Secreto desde la variable de entorno `JWT_SECRET`, sin valor por defecto. Debe decodificarse en Base64 y tener al menos 32 bytes (`JwtProperties`).
- Emisor `ms-usuarios`. Se validan `iss`, `sub`, `iat`, `exp` y el claim `roles`.
- Vigencia de 3600 s (`seguridad.jwt.expiracion-segundos`) y tolerancia de reloj de 0 s.
- Sesiones `STATELESS` (`SecurityConfig`).
- Login `POST /api/auth/login`. La contraseña se compara con BCrypt (`AuthService`).
- `toString()` enmascara el secreto (`JwtProperties`) y el token (`LoginResponse`).
- `server.error.*` configurado para no exponer detalles (`application.yaml`).

**Probado**

- Automatizado: 79 pruebas aprobadas en la verificación integral. Este documento no desglosa esas pruebas por control. `LoginResponseTest` verifica que `LoginResponse.toString()` no expone el token. `LoginRequestTest` verifica que `LoginRequest.toString()` no expone la contraseña. Este último devuelve solo el correo y un literal `******`, aunque su formato quedó sin cerrar (falta el `]`); es un detalle de calidad, no un riesgo de exposición.
- Manual: escenarios PF-01 a PF-23 documentados en `BITACORA-IA.md`. Los 401/403 están verificados según la sección 17.6 de `PLAN-SEGURIDAD-P3.md`.

**Pendiente de verificación o riesgo abierto**

- Pruebas de frontera de `exp` con el `JwtDecoder` real: no se encontraron en la búsqueda realizada. Las pruebas que usan el helper `jwt()` de Spring no validan firma ni expiración.
- Exposición del hash BCrypt: `Usuario.getPasswordHash()` tiene `@JsonIgnore` (`Usuario.java:71-72`). Las pruebas funcionales manuales PF-02 y PF-16 comprobaron que las respuestas observadas no incluían el hash de contraseña. Pendiente: revisión exhaustiva de los demás endpoints y de posibles rutas de exposición (por ejemplo, logs o errores).
- Contrato JWT con los microservicios consumidores: pendiente de confirmación (`[C]`).
- Revocación de JWT tras eliminar una cuenta (PF-23): pendiente.

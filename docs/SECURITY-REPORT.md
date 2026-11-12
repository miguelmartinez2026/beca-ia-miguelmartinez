# Reporte de seguridad — ms-usuarios (P3)

- Fecha de revisión: 2026-11-11
- Próxima reevaluación obligatoria: **2026-12-06** (vencimiento de las supresiones)
- Alcance: documentación de riesgos y supresiones temporales para el alcance académico y la configuración actual del microservicio. Sin aceptación formal documentada por un tercero.
- Componente: Spring Security 6.5.11 (gestionado por Spring Boot 3.5.16).
- Fuente de los hallazgos: OWASP Dependency-Check (`failBuildOnCVSS=7`), reporte `target/dependency-check-report.json`.

## 1. Resumen

| Categoría | CVE |
|---|---|
| Corregidas | Ninguna verificada en la versión utilizada (6.5.11). La disponibilidad de versiones corregidas requiere confirmación. |
| No aplicables bajo la configuración actual (sin supresión) | CVE-2026-47842, CVE-2026-59276 |
| Hallazgos con supresión temporal hasta 2026-12-06, sin aceptación formal documentada | CVE-2026-59270, CVE-2026-41707, CVE-2026-47841 |
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

## 3. CVE de severidad alta o crítica (supresión temporal)

### CVE-2026-59270 — LDAP embebido (CVSS 9.1)

- Componente vulnerable: `UnboundIdContainer` (módulo `spring-security-ldap`). Registra una credencial administrativa y escucha en todas las interfaces.
- Evidencia:
  - No hay `spring-security-ldap` ni `unboundid` en `pom.xml`, en el árbol de dependencias ni en el repositorio local de Maven.
  - No hay referencias a LDAP en `src/` ni en `application.yaml`.
- Condición de explotación: instanciar el servidor LDAP embebido. No identificada actualmente.
- Estado: **hallazgo suprimido temporalmente, no corregido y pendiente de aceptación formal del riesgo**.

### CVE-2026-41707 — DPoP (CVSS 7.4)

- Componente vulnerable: `DPoPProofJwtDecoderFactory` (`spring-security-oauth2-jose`). El caché de JWT ID tiene tamaño limitado y puede saturarse para repetir una prueba DPoP.
- Evidencia:
  - **DPoP está implícitamente disponible.** `OAuth2ResourceServerConfigurer` de Spring Security 6.5.11 registra `DPoPAuthenticationConfigurer` cuando `DPoPProofJwtDecoderFactory` está en el classpath, y `oauth2-jose` está presente por el uso de JWT.
  - `DPoPAuthenticationProvider.authenticate` autentica primero el access token con nuestro `JwtDecoder` (HS256) y falla con `invalid_token` antes de procesar la prueba DPoP.
  - La verificación exige un token vinculado a una clave mediante `cnf.jkt`. Desde P3-06, `POST /api/auth/login` (`TokenService`) emite tokens HS256 sin el claim `cnf`; no hay referencias a `cnf` ni `jkt` en `src/main/java`. La precondición no se cumple con el código actual, pero no se demuestra que la vulnerabilidad sea inexplotable: depende de que ese claim no se añada.
  - No hay código DPoP en `src/`.
- Condiciones de explotación: un access token válido firmado con nuestra clave y con `cnf.jkt`, más una prueba DPoP reutilizable tras saturar el caché. No se ha demostrado que estén presentes actualmente.
- Estado: **hallazgo suprimido temporalmente, no corregido y pendiente de aceptación formal del riesgo**. No se afirma que DPoP esté deshabilitado ni que la explotación sea imposible.

### CVE-2026-47841 — WebAuthn (CVSS 7.4)

- Componente vulnerable: soporte WebAuthn de `spring-security-web`. Evasión de la verificación de usuario con un almacén de sesión HTTP distribuido.
- Evidencia:
  - `WebAuthnConfigurer` solo se aplica mediante `http.webAuthn(...)`, que no se invoca en `SecurityConfig`.
  - `webauthn4j` y Spring Session no están en el árbol de dependencias.
  - `SecurityConfig` usa sesiones `STATELESS`, sin `formLogin`, `httpBasic` ni `logout`.
  - No hay referencias a WebAuthn ni passkeys en `src/` ni en la configuración.
- Condición de explotación: habilitar WebAuthn junto con sesiones HTTP distribuidas. No identificada actualmente.
- Estado: **hallazgo suprimido temporalmente, no corregido y pendiente de aceptación formal del riesgo**.

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
- `spring-security-oauth2-jose` contiene directamente `DPoPProofJwtDecoderFactory`; su regla es una supresión temporal bajo la configuración actual, no una ausencia de la vulnerabilidad.
- Las tres reglas previas de `spring-security-crypto` conservan su `packageUrl`; se actualizaron sus notas y su vencimiento (antes `2027-02-06Z`).
- No hay supresiones generales ni para las CVE medias.
- Las reglas de Spring Framework de P2 (`QUALITY-REPORT.md`) no se modificaron.
- La decisión de configurar las supresiones fue del responsable del desarrollo; no existe aprobación formal documentada de un tercero para aceptar los riesgos residuales.

## 6. Riesgos residuales

- Las cinco CVE siguen presentes en 6.5.11 y no están corregidas.
- DPoP está implícitamente disponible en el resource server. La explotación requeriría un token válido con `cnf.jkt`. El login de P3-06 no emite ese claim, pero la mitigación depende de mantenerlo así (ver sección 7).
- Las supresiones ocultan las CVE de OWASP: si cambia la configuración, el análisis no avisará automáticamente.
- Las notas de las supresiones deben mantenerse alineadas con el estado real del código.
- La disponibilidad de versiones corregidas, incluidas las que requieren soporte Enterprise, debe confirmarse mediante los avisos oficiales antes de planificar una actualización.

### JWT vigente después de eliminar una cuenta (P3-06)

- **Estado:** riesgo de seguridad confirmado mediante prueba funcional.
- **Fecha de verificación:** 2026-11-11.
- **Origen:** diseño de autenticación JWT stateless.
- **Mitigación:** pendiente de definición e implementación.

**Descripción**

La validación de JWT comprueba la firma HS256, el emisor, la vigencia y los claims requeridos, pero no consulta PostgreSQL para verificar que el usuario continúe existiendo.

Como consecuencia, eliminar una cuenta no revoca automáticamente los JWT emitidos anteriormente.

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

Un JWT previamente emitido puede conservar los permisos de una cuenta eliminada mientras permanezca vigente.

En el caso de un JWT con rol ADMIN, esto podría permitir el acceso a operaciones administrativas aun después de eliminar la cuenta asociada.

**Condiciones y alcance**

- La vigencia configurada es de 3600 segundos, con tolerancia de reloj de 0 s (`JwtTimestampValidator(Duration.ZERO)`).
- No existe revocación inmediata de tokens.
- El acceso depende de que el JWT conserve una firma válida y no haya expirado.
- El comportamiento fue reproducido en el entorno local de pruebas funcionales.

**Mitigaciones por evaluar (opciones no aprobadas)**

- Validar la existencia y el estado del usuario durante las solicitudes autenticadas.
- Implementar revocación de JWT mediante un mecanismo centralizado.
- Evaluar una reducción de la vigencia del token como medida complementaria.

**Decisión**

El hallazgo queda documentado como riesgo pendiente.

No se implementará una solución de revocación dentro de P3-06 sin definir previamente su diseño y alcance.

La ausencia de revocación debe evaluarse y resolverse o aceptarse formalmente antes de un despliegue en producción.

## 7. Condiciones que obligan a reevaluar

- Se agrega `spring-security-ldap`, UnboundID o un LDAP embebido.
- Se emiten, aceptan o validan tokens con `cnf` o DPoP, o se configura `.dpop(...)`.
- Se habilita `http.webAuthn(...)`, passkeys, Spring Session o sesiones HTTP.
- Se usa `Encryptors`, `AesBytesEncryptor`, autenticación Digest, un servidor de autorización de Spring o los encoders Password4j.
- Se cambia la emisión, validación o configuración de los tokens JWT (P3-06 en adelante).
- Cambia la versión de Spring Boot o Spring Security.
- Llega el 2026-12-06.

## 8. Recomendación

Evaluar la actualización a una versión corregida cuando sea viable. En la revisión previa se identificaron Spring Security 7.0.7 y 7.1.1 como versiones OSS candidatas según el aviso de CVE-2026-59276, y 6.5.12 como versión asociada a soporte Enterprise.

Esta información debe confirmarse con los avisos oficiales y con la cobertura específica de las demás CVE antes de seleccionar una versión.

Subir de línea a Spring Boot / Spring Security requiere aprobación y su propia evaluación de compatibilidad.

Hasta entonces, mantener las supresiones con su vencimiento y revisarlas en la fecha de reevaluación.

## 9. Controles JWT (P3-05, P3-06 y corrección posterior)

Fuentes: `JwtConfig.java`, `JwtProperties.java`, `TokenService.java`, `AuthService.java`, `LoginResponse.java`, `SecurityConfig.java`, `application.yaml` y pruebas automatizadas de seguridad.

### 9.1. Controles implementados

- Firma HS256: `NimbusJwtDecoder` con HS256 (`JwtConfig`) y `JwsHeader.with(HS256)` (`TokenService`).
- Secreto desde la variable de entorno `JWT_SECRET`, sin valor por defecto. Debe decodificarse en Base64 y tener al menos 32 bytes (`JwtProperties`).
- Emisor `ms-usuarios`.
- Validación de `iss`, `sub`, `iat`, `exp` y el claim `roles`.
- Vigencia de 3600 segundos (`seguridad.jwt.expiracion-segundos`) y tolerancia de reloj de 0 segundos.
- Sesiones `STATELESS` (`SecurityConfig`).
- Login `POST /api/auth/login`. La contraseña se compara con BCrypt (`AuthService`).
- `toString()` enmascara el secreto (`JwtProperties`) y el token (`LoginResponse`).
- `server.error.*` configurado para no exponer detalles (`application.yaml`).

### 9.2. Corrección de validación del claim `iat`

**Estado: corregido y verificado mediante pruebas automatizadas.**

Durante el fortalecimiento de las pruebas JWT se detectó que `JwtConfig` aceptaba tokens firmados sin el claim original `iat`.

La causa fue el comportamiento de conversión de claims de Spring Security 6.5.11: cuando `iat` estaba ausente y existía `exp`, el convertidor podía completar automáticamente el valor antes de ejecutar los validadores personalizados.

**Corrección aplicada:**

- Se configuró un convertidor personalizado mediante `NimbusJwtDecoder.setClaimSetConverter`.
- Se comprueba la presencia de `iat` en los claims recibidos antes de ejecutar la conversión predeterminada.
- Cuando el claim no existe o es nulo, el token se rechaza.
- Cuando está presente, se conserva la conversión estándar de Spring Security.
- Se mantuvieron HS256, los validadores existentes y el contrato de los tokens emitidos.

**Evidencia de regresión:**

- `JwtDecoderCriptografiaTest.java`: valida firma, algoritmos, expiración y claims mediante el decodificador real.
- `UsuarioControllerJwtSinIatTest.java`: verifica que un JWT firmado sin `iat` recibe HTTP 401, sin revelar el motivo interno ni ejecutar la lógica de negocio.
- El caso de control confirma HTTP 200 cuando el JWT contiene los claims requeridos.

**Commit:** `61b9f37` — `fix(p3): validar iat original y reforzar pruebas JWT`.

### 9.3. Pruebas automatizadas

Última ejecución integral reportada: 2026-11-11.

Comando:

`.\mvnw.cmd -B clean verify`

Resultados:

| Indicador | Resultado |
|---|---|
| Pruebas ejecutadas | 102 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Maven | BUILD SUCCESS |
| Checkstyle | Sin violaciones |
| PMD / CPD | Sin fallos reportados |
| SpotBugs | Sin fallos reportados |
| JaCoCo | Umbral de cobertura cumplido |

La cifra de 102 corresponde a pruebas automatizadas. No incluye las pruebas funcionales manuales PF-01 a PF-23, documentadas en `BITACORA-IA.md`.

Entre las pruebas existentes, `LoginResponseTest` verifica que `LoginResponse.toString()` no exponga el token. `LoginRequestTest` comprueba que `LoginRequest.toString()` no exponga la contraseña.

Las pruebas de frontera de `exp`, anteriormente identificadas como pendientes, quedaron cubiertas mediante `JwtDecoderCriptografiaTest` utilizando el decodificador real.

Las verificaciones 401/403 existentes están documentadas en la sección 17.6 de `PLAN-SEGURIDAD-P3.md`.

El resultado satisfactorio de Maven no significa que todas las vulnerabilidades de dependencias estén corregidas. Los riesgos y supresiones OWASP de las secciones anteriores permanecen sujetos a reevaluación.

### 9.4. Riesgos de autenticación y autorización abiertos

- **PF-23:** los JWT emitidos antes de eliminar una cuenta pueden seguir siendo válidos hasta su expiración. No existe revocación inmediata.
- **HS256 compartido:** un servicio que posea la clave simétrica tiene capacidad criptográfica para firmar tokens. Se requieren controles de distribución, almacenamiento, acceso y rotación.
- **Intentos de autenticación:** no existe limitación de intentos de login.
- **Integración:** aún no está verificado que los demás microservicios validen correctamente el contrato JWT común.
- **Dependencias:** las CVE documentadas en las secciones 3 y 4 continúan sin corrección verificada.

No se implementó revocación ni se migró a criptografía asimétrica. Estas decisiones permanecen fuera del alcance de las correcciones realizadas y no constituyen aceptación automática del riesgo.

### 9.5. Otras verificaciones y mejoras pendientes

- **Exposición del hash BCrypt:** `Usuario.getPasswordHash()` tiene `@JsonIgnore`. Las pruebas funcionales manuales PF-02 y PF-16 comprobaron que las respuestas observadas no incluían el hash de contraseña. Continúa pendiente una revisión exhaustiva de otros endpoints y posibles rutas de exposición, como logs o errores.
- **Formato de `LoginRequest.toString()`:** la prueba verifica que no se expone la contraseña. Permanece un detalle cosmético de formato: falta el corchete de cierre `]`.
- **Contrato JWT entre microservicios:** pendiente de confirmación con los servicios consumidores.
- **Revocación de JWT:** pendiente conforme a PF-23.

---

## 10. Auditoría mediante agente personalizado (P3)

### 10.1. Agente utilizado

Se creó el agente personalizado:

`.github/agents/revisor-seguridad.agent.md`

**Commit:** `ade1a80` — `feat(p3): agregar agente revisor de seguridad`.

El agente está configurado con las herramientas `read` y `search`, siguiendo el principio de mínimo privilegio.

Su responsabilidad consiste en inspeccionar el código, identificar riesgos y entregar recomendaciones sin modificar archivos ni ejecutar comandos.

### 10.2. Alcance de la auditoría

La revisión incluyó:

- Configuración de Spring Security.
- Autenticación y validación de JWT.
- Autorización por roles.
- Controladores y endpoints públicos o protegidos.
- Validación de DTOs y asignación masiva.
- Manejo de excepciones y respuestas de error.
- Posible exposición de contraseñas, hashes y tokens.
- Consultas de base de datos y riesgos de inyección.
- Dependencias, vulnerabilidades y supresiones OWASP.
- Cobertura de pruebas de seguridad.

La auditoría fue estática y de solo lectura. No ejecutó Maven, Dependency-Check ni pruebas funcionales.

### 10.3. Hallazgos del agente

El informe de revisión identificó 11 hallazgos, cuyas clasificaciones se registran como resultado preliminar de la auditoría y están sujetas a validación humana.

| ID | Severidad reportada | Hallazgo | Tratamiento |
|---|---|---|---|
| SEC-001 | ALTA | JWT vigente después de eliminar una cuenta (PF-23) | Riesgo demostrado. Mitigación o aceptación formal pendiente antes de producción. |
| SEC-002 | ALTA | Clave HS256 compartida entre servicios y ausencia del claim aud | Mantener arquitectura de P3; definir distribución segura, control de acceso y rotación. |
| SEC-003 | MEDIA | Ausencia de limitación de intentos de login | Pendiente de evaluación y priorización. |
| SEC-004 | MEDIA | Enumeración de usuarios mediante la respuesta HTTP 409 del registro público. | Revisar requisitos y mitigaciones. |
| SEC-005 | MEDIA | Posible enumeración por diferencias temporales en login | Decisión D-08 documentada; riesgo residual. |
| SEC-006 | MEDIA | Ausencia de auditoría específica de eventos de seguridad | Mejora pendiente. |
| SEC-007 | BAJA | Posible tratamiento incorrecto de excepciones de autorización en el handler genérico | No verificado; requiere prueba específica. |
| SEC-008 | BAJA | Detalles de formato en métodos `toString()` | Mejora cosmética pendiente, sin exposición demostrada. |
| SEC-009 | BAJA | Política de contraseña basada principalmente en longitud mínima | Evaluar conforme a los requisitos de seguridad. |
| SEC-010 | BAJA | Ausencia de prueba explícita sobre `UserDetailsService` predeterminado | Verificación pendiente. |
| SEC-011 | BAJA | Pruebas H2 sin ejecutar migraciones Flyway | Limitación de cobertura de pruebas de persistencia. |

**Resumen de severidades reportadas:**

- Hallazgos críticos: 0.
- Hallazgos altos: 2.
- Hallazgos medios: 4.
- Hallazgos bajos: 5.

Estas severidades son las propuestas por el agente y no constituyen una clasificación formal de vulnerabilidades confirmadas.

Algunos elementos corresponden a riesgos arquitectónicos, hipótesis no verificadas o mejoras de calidad, y no a vulnerabilidades explotables demostradas.

### 10.4. Decisiones de revisión humana

**SEC-001 — Revocación de JWT**

Se confirmó el comportamiento mediante PF-23.

No se aprobó introducir revocación o consultas a PostgreSQL en cada petición durante esta corrección de P3.

El riesgo permanece abierto y debe resolverse o aceptarse formalmente antes de producción.

**SEC-002 — HS256 compartido**

Se mantiene la decisión arquitectónica de utilizar HS256 con una clave compartida en el entorno de integración del equipo.

No se aprobó una migración a RS256 ni una modificación del contrato de claims.

La decisión requiere controles de distribución, almacenamiento y rotación del secreto, además de reconocimiento formal del riesgo.

**Vulnerabilidades de dependencias**

Se revisaron documentalmente las condiciones de afectación de:

- CVE-2026-59270.
- CVE-2026-41707.
- CVE-2026-47841.

No se realizaron nuevos ensayos de explotación ni se confirmó la corrección de estas vulnerabilidades.

Las supresiones existentes no constituyen correcciones y no existe evidencia de aprobación formal por un tercero. Deben reevaluarse conforme a sus fechas de vencimiento.

**Corrección adicional identificada durante la revisión**

Se fortalecieron las pruebas criptográficas del decodificador JWT.

Estas detectaron que la ausencia original del claim `iat` no provocaba el rechazo esperado.

Se aplicó una corrección mínima y se comprobó mediante pruebas del decodificador y de la capa HTTP, como se documenta en la sección 9.2.

### 10.5. Limitaciones y pendientes

- La auditoría inicial no ejecutó comandos ni pruebas; sus conclusiones se basaron en inspección de código y documentación.
- La verificación integral posterior de 102 pruebas corresponde al estado del proyecto después de la corrección JWT, no a una nueva auditoría completa.
- Las vulnerabilidades OWASP previamente documentadas permanecen sin corrección verificada.
- Las decisiones sobre mitigación o aceptación formal de PF-23 y HS256 compartido siguen pendientes.
- La revisión cruzada del PR de otro integrante no se ha realizado.
- El contrato JWT compartido y las pruebas proveedor–consumidor todavía requieren coordinación del equipo.
- La integración saliente mediante `RestClient` sigue pendiente.

Este informe registra la revisión automatizada, las verificaciones técnicas y las decisiones humanas tomadas hasta el momento, sin presentar la P3 completa como finalizada.
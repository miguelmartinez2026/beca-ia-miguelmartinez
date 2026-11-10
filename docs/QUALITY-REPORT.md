Reporte de calidad — ms-usuarios
Práctica: P2 — Calidad como código con instrucciones y skills
Fecha de línea base: 06/11/2026
Última actualización documental: 10/11/2026 (JaCoCo y 18 pruebas Surefire confirmadas localmente)
Rama de trabajo: practica-2
Alcance: Java 21, Spring Boot 3.5.16, Maven y análisis de calidad de ms-usuarios.

Estado ejecutivo: El 09/11/2026 GitHub Actions ejecutó ./mvnw -B verify en la rama practica-2 y terminó en BUILD SUCCESS, con 16 pruebas aprobadas, cero fallos, cero errores y cero omitidas; Checkstyle, PMD, CPD, SpotBugs/FindSecBugs, OWASP y JaCoCo se ejecutaron con la configuración de ese commit. Evidencia de CI, PR #5. Posteriormente, el desarrollador incorporó dos pruebas unitarias (18 casos definidos en total) y compartió una nueva captura del reporte local de JaCoCo del 10/11/2026: 77.78 % de líneas (98/126), superando el umbral de 70 % por 7.78 puntos porcentuales. La captura acredita el porcentaje de JaCoCo y la salida posterior de cuatro reportes Surefire confirma 18 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas. Sigue pendiente comprobar el cierre BUILD SUCCESS de esta misma ejecución de clean verify y el CI posterior al nuevo commit.

Advertencia de seguridad: el resultado de OWASP no implica ausencia de vulnerabilidades. En el análisis local del 09/11/2026 permanecen 23 CVE detectados: 8 activos con CVSS inferior a 7 y 15 suprimidos. De las supresiones, 5 corresponden a asociaciones incorrectas de componentes y 10 a aceptaciones temporales de riesgo que vencen el 06/12/2026; estas diez CVE no están corregidas. Al retirarlas en una prueba controlada, OWASP terminó en BUILD FAILURE por CVSS de 7.5 a 9.8. Se restauró el archivo original después de la prueba. Las decisiones y la evidencia figuran en las secciones 6.4, 6.8 y 6.9.
Lectura de resultados: la ejecución local más reciente de Surefire confirma 18 pruebas aprobadas, sin fallos, errores ni omisiones: controlador (8), servicio (8), DTO (1) y contexto (1). JaCoCo muestra 77.78 % de cobertura de líneas (98/126) el 10/11. El último CI confirmado sigue siendo el del 09/11 con 16 pruebas. Las cifras anteriores (13 pruebas y 71.43 % del 06/11; 16 pruebas y 70.15 % del 07/11) se mantienen como historial, no como métricas vigentes.

Convención de evidencia
Marca	Significado
[V]	Dato contrastado con archivo de código, reporte de analizador, registro de GitHub Actions o salida reproducible que se indica.
[D]	Dato aportado por el desarrollador o conservado del historial, sin registro completo disponible en el repositorio.
[P]	Verificación o corrección pendiente.


El reporte combina la línea base del 06/11, una ejecución local ampliada del 07/11, el CI y las pruebas controladas de OWASP del 09/11, y la captura de JaCoCo aportada el 10/11. No se deben atribuir métricas de una fecha a otra ni interpretar una captura de JaCoCo como evidencia de pruebas aprobadas. Los archivos generados en target/ pueden sobrescribirse al volver a ejecutar Maven; los registros de CI quedan disponibles en GitHub.
1. Objetivo y alcance
Objetivo: establecer la línea base de calidad de ms-usuarios antes de corregir hallazgos, documentar con evidencia los resultados antes y después de cada corrección, y consolidar el estado final de la práctica.

Alcance:
- Código Java de src/main/java y pruebas de src/test/java.
- Dependencias del proyecto (Maven, Spring Boot 3.5.16).
- Reglas definidas en .github/copilot-instructions.md y en la skill .github/skills/quality-gates-java/SKILL.md.
Fuera de alcance:
- JWT y autorización por roles (corresponden a P3).
- Migración a Spring Boot 4 / Spring Framework 7 (descartada en esta práctica; ver §6.9).
- Incorporar versiones comerciales Enterprise de Spring sin disponer de soporte o credenciales; se registran como alternativa no aplicada, no como corrección realizada.
Nota histórica: la versión de la línea base declaraba como fuera de alcance «correcciones de código y actualizaciones de dependencias (no aplicadas)». Esa afirmación era válida el 06/11/2026 en el diagnóstico inicial; los lotes aplicados después se documentan en la sección 7.3.
2. Herramientas utilizadas y versiones
Versiones tomadas de pom.xml y de los encabezados de los reportes [V].
Herramienta	Plugin / versión	Motor o dato adicional	Configuración
Checkstyle	maven-checkstyle-plugin 3.6.0	Checkstyle 10.21.4	config/checkstyle/checkstyle.xml; fase validate; failOnViolation=true; severidad warning
PMD	maven-pmd-plugin 3.26.0	PMD 7.7.0 (reporte)	config/pmd/ruleset.xml
CPD	maven-pmd-plugin 3.26.0 (cpd-check)	PMD 7.7.0 (reporte)	minimumTokens=80
SpotBugs	spotbugs-maven-plugin 4.9.3.0	SpotBugs 4.9.3 (reporte)	effort=Max, threshold=Medium
FindSecBugs	findsecbugs-plugin 1.14.0	Plugin de SpotBugs	Referenciado en el reporte de SpotBugs
OWASP Dependency-Check	dependency-check-maven 13.0.0 (línea base: 12.1.0)	Versión 13.0.0 (reporte JSON actual)	failBuildOnCVSS=7; clave NVD desde NVD_API_KEY; formatos HTML y JSON; sin ossIndexServerId [V]
JaCoCo	jacoco-maven-plugin 0.8.13	—	Límite BUNDLE, contador LINE, mínimo 0.70
JUnit 5 / Mockito / MockMvc	Spring Boot 3.5.16 (BOM)	Java 21	Reportes en target/surefire-reports
SonarQube for IDE	Extensión de VS Code, versión 6.2.0 [V]	Análisis del archivo abierto; no hay evidencia de modo conectado a un servidor [P]	Evidencia limitada a UsuarioService.java (sección 10.1); no hay análisis de todo el proyecto


Reglas de Checkstyle activas [V]: LineLength (120), UnusedImports, MagicNumber, MultipleStringLiterals, CyclomaticComplexity (10), MethodLength (30), ParameterNumber (5), EmptyCatchBlock, NeedBraces.

Verificación del 07/11/2026 [V]: Maven 3.10.0 mediante el Wrapper y Java 21.0.13. Las versiones de las ejecuciones históricas quedan pendientes.
3. Comparación antes y después
La columna «Antes» describe la línea base del 06/11/2026. La columna «Estado más reciente» separa los controles comprobados en CI el 09/11 de la ejecución local del 10/11, con medición visual de JaCoCo y cuatro reportes de Surefire; el mensaje final de Maven y un nuevo CI aún no están documentados.
Herramienta o control	Antes (06/11, línea base)	Estado más reciente y evidencia	Evaluación
Checkstyle	19 violaciones: 8 MagicNumber, 7 LineLength, 4 MultipleStringLiterals	0 violaciones; CI 09/11 confirmó el paso de checkstyle:check [V]	Corregido
PMD	0 violaciones	Sin infracciones bloqueantes; CI verify exitoso [V]	Cumple
CPD	0 duplicaciones sobre el umbral de 80 tokens	Sin duplicaciones bloqueantes; CI verify exitoso [V]	Cumple
SpotBugs / FindSecBugs	0 incidencias	Control superado en CI [V]	Cumple
JaCoCo — líneas (umbral ≥ 70 %)	71.77 % (89/124) [V]	77.78 % (98/126) en captura local del 10/11 [V]; anteriormente 70.15 % (94/134) el 07/11 [D] y gate aprobado en CI 09/11 [V]	Supera el umbral por 7.78 puntos; build nuevo pendiente de confirmar
JaCoCo — instrucciones y ramas	68.3 % de instrucciones; 34.4 % de ramas [V]	Captura 10/11: 75 % de instrucciones (402/530), 46 % de ramas (15/32), como los muestra JaCoCo [V]	Informativo; no son los umbrales exigidos
Pruebas automatizadas	13 aprobadas, 0 fallos [V]	16 aprobadas en CI del 09/11 [V]; 18 aprobadas localmente tras añadir dos pruebas de servicio; 0 fallos, 0 errores y 0 omitidas en los cuatro archivos Surefire [V]	Total local confirmado; CI nuevo pendiente
OWASP — dependencias afectadas	10 dependencias con alertas en Dependency-Check 12.1.0 (71 escaneadas, 49 únicas) [V]	3 con CVE identificadas en el reporte 13.0.0 del 09/11: spring-core, spring-data-jpa, spring-security-crypto [V]	Hay vulnerabilidades pendientes
OWASP — CVE detectados	70 asociaciones dependencia-CVE en la línea base; no son 70 CVE únicos	23 CVE detectados: 8 activos + 15 suprimidos, según JSON del 09/11 [V]	Persisten riesgos
OWASP — supresiones	0 en la línea base	5 asociaciones de módulo incorrectas + 10 aceptaciones temporales. La ejecución sin estas últimas falló por 10 CVE con CVSS ≥ 7 [V]	Observación del instructor pendiente de aprobación
OWASP — gate de Maven	BUILD FAILURE antes de gestionar CVE [D]	BUILD SUCCESS con 15 supresiones; BUILD FAILURE con solo 5, en prueba local del 09/11 [D]	Aprobación condicionada por supresiones
SonarQube for IDE	Sin captura al inicio	Captura en docs/img/sonarqube-usuario-service.jpg, sobre UsuarioService.java [V]	Evidencia del archivo solicitado
Integración continua	Sin workflow	.github/workflows/ci.yml; CI verde en practica-2 el 09/11, ejecutando verify sin opción de omitir OWASP [V]	Incorporado


Nota histórica: el 06/11, tras corregir los hallazgos de Checkstyle y antes de ampliar los tests, se registraron 13 pruebas y 71.43 % (90/126 líneas). Son resultados intermedios, no el estado final. El histórico de OWASP pasó de 47 a 27 y luego a 23 CVE según las ejecuciones descritas en la sección 6.4, con limitaciones de conservación de sus reportes.
4. Resultados iniciales por herramienta
4.1 Checkstyle [V] (target/checkstyle-result.xml)
19 violaciones en 5 archivos Java:
Regla	Cantidad	Archivos
MagicNumber	8	Usuario.java (3), CrearUsuarioRequest.java (3), ActualizarUsuarioRequest.java (2)
LineLength	7	ApiExceptionHandler.java (6), UsuarioController.java (1)
MultipleStringLiterals	4	ApiExceptionHandler.java


Las violaciones son de estilo y mantenimiento, no errores de compilación. Como la regla se ejecuta en la fase validate con failOnViolation=true, bloquea el build antes que el resto de los análisis.
4.2 PMD [V] (target/pmd.xml)
0 violaciones con las reglas de config/pmd/ruleset.xml (código muerto, complejidad, código duro).
4.3 CPD [V] (target/cpd.xml)
0 duplicaciones con minimumTokens=80.
4.4 SpotBugs con FindSecBugs [V] (target/spotbugsXml.xml)
0 incidencias con umbral Medium y esfuerzo Max.
4.5 JaCoCo [V] (target/site/jacoco/jacoco.csv)
Contador	Cubierto / total	Porcentaje
Líneas (umbral)	89 / 124	71.77 %
Instrucciones	362 / 530	68.3 %
Ramas	11 / 32	34.4 %


El umbral de 70 % se aplica a líneas. Clases con menor cobertura de líneas: ApiExceptionHandler (14/34) y UsuarioService (23/35). El margen sobre el umbral es de 1.77 puntos porcentuales.
4.6 Pruebas [V] (target/surefire-reports)
13 pruebas aprobadas, 0 fallos, 0 errores, 0 omitidas: UsuarioControllerTest (8), UsuarioServiceTest (3), CrearUsuarioRequestTest (1) y MsUsuariosApplicationTests (1).
4.7 Procedencia y cronología de los resultados
La línea base del 06/11 se tomó de reportes de target/ producidos durante el diagnóstico inicial. Las correcciones de estilo eliminaron las 19 violaciones de Checkstyle. La ejecución local completa del 06/11 pasó con 13 pruebas y 71.43 % de líneas, pero quedó superada por las tres pruebas negativas incorporadas después.

El 07/11 se ejecutó mvnw.cmd -B verify con el perfil H2 para la prueba de contexto, sin PostgreSQL local, y se registraron 16 pruebas y 70.15 % de cobertura de líneas (94/134). El 09/11 GitHub Actions ejecutó ./mvnw -B verify, confirmó las 16 pruebas y terminó en BUILD SUCCESS, con los controles Maven activos. El 10/11 el desarrollador compartió una captura del reporte local actualizado de JaCoCo con 77.78 % de líneas (98/126) después de añadir dos pruebas al servicio. El resultado de Surefire se verificó por separado con los cuatro archivos .txt: 18 pruebas, 0 fallos, 0 errores y 0 omitidas. Aún falta comprobar el mensaje final de Maven (BUILD SUCCESS) de esa ejecución. La diferencia de denominadores (134 frente a 126 líneas) impide atribuir íntegramente el aumento porcentual de 7.63 puntos a las nuevas pruebas sin comparar los reportes de clases y exclusiones.
4.8 Resultados comprobables (CI 09/11 y JaCoCo local 10/11/2026)
Control	Estado	Fuente
Checkstyle	0 violaciones	Log del CI del PR #5 [V]
PMD y CPD	Gates aprobados	mvn verify en CI [V]
SpotBugs / FindSecBugs	Gate aprobado	mvn verify en CI [V]
Pruebas	18 aprobadas localmente (10/11): controlador (8), servicio (8), DTO (1), contexto (1); fallos 0, errores 0, omitidas 0. El CI 09/11 había ejecutado 16	Cuatro reportes target/surefire-reports/*.txt aportados por el desarrollador [V]; CI 09/11 [V]
JaCoCo	77.78 % de líneas (98/126) en la captura local del 10/11; 75 % de instrucciones (402/530) y 46 % de ramas (15/32). Se conserva como histórico el 70.15 % (94/134) del 07/11	Captura local 10/11, figura en §10.5 [V]; CI 09/11 [V]
OWASP Dependency-Check 13.0.0	BUILD SUCCESS con 15 supresiones en el análisis local del 09/11 a las 17:12:06; BUILD FAILURE con solo 5 supresiones a las 17:34:12	Salidas locales del 09/11, detalle §6.9 [D]
CI completo	BUILD SUCCESS, workflow ci-miguel, commit 2881056	Run #42716270381 [V]


La composición del reporte OWASP con las supresiones originales del 09/11 era de 23 CVE detectados, distribuidos en 15 suprimidos y 8 activos. Los diez CVE temporalmente aceptados están pendientes de corrección de versión. La prueba con cinco supresiones se hizo localmente; después se restauró el XML original. La captura de JaCoCo del 10/11 no es una nueva auditoría OWASP.
4.9 Evidencia histórica del 06/11 y verificación posterior
El 06/11/2026 a las 23:55:49 (UTC−06:00) se registró un clean verify satisfactorio antes de ampliar las pruebas. Se conserva su extracto para trazabilidad; no es la última verificación funcional.
[INFO]
[INFO] --- jacoco:0.8.13:report (report) @ ms-usuarios ---
[INFO] Loading execution data file C:\Users\migue\Documents\Proyectos\MS-Users\beca-ia-miguelmartinez\target\jacoco.exec
[INFO] Analyzed bundle 'ms-usuarios' with 12 classes
[INFO]
[INFO] --- jacoco:0.8.13:check (check) @ ms-usuarios ---
[INFO] Loading execution data file C:\Users\migue\Documents\Proyectos\MS-Users\beca-ia-miguelmartinez\target\jacoco.exec
[INFO] Analyzed bundle 'ms-usuarios' with 12 classes
[INFO] All coverage checks have been met.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  45.008 s
[INFO] Finished at: 2026-11-06T23:55:49-06:00
[INFO] ------------------------------------------------------------------------
El 07/11 se verificó la ejecución con H2 (sección 10.3). El 09/11 el log de GitHub Actions comprobó 16 pruebas, 0 fallos, 0 errores y 0 omitidas, además de All coverage checks have been met y BUILD SUCCESS. Esta última evidencia está disponible en GitHub sin depender de un extracto de consola local.
5. Tres hallazgos de Checkstyle: qué ocurría y cómo se resolvió
5.1 Números mágicos en los límites de usuarios (MagicNumber)
Antes: encontré ocho infracciones en Usuario.java, CrearUsuarioRequest.java y ActualizarUsuarioRequest.java. Los valores 120, 254, 20 y 8 aparecían directamente en anotaciones de persistencia o validación. Aunque los límites eran correctos, no quedaba claro que debían conservar la misma definición que las columnas VARCHAR de Flyway V1, lo cual aumentaba el riesgo de introducir inconsistencias al mantener el código.

Corrección que aprobé: centralicé las longitudes compartidas en domain/RestriccionesUsuario.java: nombre 120, correo 254 y rol 20. El mínimo de ocho caracteres para contraseña quedó como constante privada del DTO correspondiente. Comprobé que los valores no cambiaron y que la migración V1 se mantuvo intacta.

Resultado: ocho infracciones eliminadas sin cambiar las reglas de negocio ni el esquema. Las pruebas posteriores siguieron aprobando.
5.2 Cadenas repetidas en el manejo de errores (MultipleStringLiterals)
Antes: ApiExceptionHandler.java repetía mensajes y claves del cuerpo de error, como errors, campo, mensaje y «La solicitud contiene datos inválidos.». Esto podía producir respuestas ProblemDetail inconsistentes si en una modificación futura se cambiaba solo una aparición.

Corrección que aprobé: extraje las cuatro cadenas a constantes privadas de la misma clase. No cambié los textos públicos, las claves JSON ni los códigos HTTP.

Resultado: cuatro infracciones eliminadas y el contrato de errores preservado.
5.3 Líneas demasiado largas (LineLength)
Antes: Checkstyle señaló siete líneas que excedían 120 columnas, seis en ApiExceptionHandler.java y una en UsuarioController.java. Dificultaban leer las llamadas y expresiones completas y hacían fallar el gate de estilo.

Corrección que aprobé: dividí las expresiones en varias líneas, manteniendo el mismo orden, argumentos y comportamiento.

Resultado: siete infracciones eliminadas. En conjunto, los tres lotes redujeron Checkstyle de 19 a 0 violaciones. No se modificaron rutas REST ni se desactivaron reglas para conseguirlo.
6. Alertas de OWASP y limitaciones del análisis
Las secciones 6.1 a 6.3 conservan la línea base con Dependency-Check 12.1.0. Las secciones 6.4 a 6.8 documentan el análisis con 13.0.0 y las decisiones tomadas el 06/11; la sección 6.9 añade la revalidación del 09/11, posterior a la retroalimentación del instructor.
6.1 Alertas de dependencias (línea base)
Datos del encabezado de target/dependency-check-report.html (Dependency-Check 12.1.0) [V]:
Concepto	Valor
Dependencias escaneadas	71 (49 únicas)
Dependencias vulnerables	10
Vulnerabilidades encontradas (asociaciones dependencia-CVE)	70
Vulnerabilidades suprimidas	0


Métrica reportada por la consola de OWASP:
Métrica	Antes	Después
CVSS máximo reportado	9.8 [D]	9.8 [V] (13.0.0; CVE de spring-core en el JSON)


- Los 70 son asociaciones dependencia-CVE, no CVE únicos. Un mismo CVE puede aparecer asociado a varias dependencias. El número de CVE únicos de la línea base quedó [P]; las cifras del estado final están en la sección 6.4.
- El CVSS máximo de 9.8 lo proporcionó el desarrollador a partir de la consola de OWASP [D]. En el reporte de Dependency-Check 13.0.0 hay 5 CVE de spring-core con CVSS 9.8 [V].
- Una alerta de OWASP indica una coincidencia entre una dependencia y un identificador conocido. No demuestra que el microservicio sea explotable, ni siquiera con un CVSS alto. Cada alerta requiere triage: versión realmente afectada, ruta de ejecución y posible falso positivo.
- En el HTML se observan referencias a avisos de seguridad de Spring (CVE-2026-59314, CVE-2026-47834 y CVE-2026-59276). No se confirmó la dependencia asociada ni el CVSS de cada una.
- El resultado BUILD FAILURE por vulnerabilidades con CVSS ≥ 7.0 es un dato proporcionado por el desarrollador [D]. No hay consola en el repositorio para verificarlo.
6.2 Errores de configuración de la herramienta (distintos de las alertas)
Error	Evidencia	Naturaleza
HTTP 401 Unauthorized de OSS Index (12.1.0)	[V] en la sección «Analysis Exceptions» del HTML de la línea base (leído el 06/11/2026; el archivo se sobrescribió con el reporte de 13.0.0)	Autenticación del analizador OSS Index de Dependency-Check (Sonatype). No es un problema del código. Es independiente del error de metadatos de Flyway descrito abajo.
Advertencia HTTP 401 al obtener metadatos Maven del repositorio flyway-community-db-support	[D]: registro de consola proporcionado por el desarrollador el 06/11/2026, todavía no archivado en el repositorio. Comando: .\mvnw.cmd -B clean verify -Ddependency-check.skip=true. Mensaje: [WARNING] Could not transfer metadata /.meta/prefixes.txt from/to flyway-community-db-support (https://maven.pkg.github.com/flyway/flyway-community-db-support): HTTP Status: 401. No existe evidencia verificable en el repositorio ni en target/ (el HTML actual no contiene ningún 401); es un registro histórico de consola.	Es una advertencia ([WARNING]) de obtención de metadatos Maven; no impidió el BUILD SUCCESS de esa ejecución, y Flyway validó correctamente la migración V1 según el mismo registro [D]. No guarda relación con la autenticación de Sonatype OSS Index ni con Dependency-Check.
URL demasiado largas al actualizar los datos de Dependency-Check 12.1.0	[D]; no figura en el HTML	Problema de la herramienta o de su base interna. No es un problema del código. El desarrollador indicó que 12.2.2 corrigió el problema y se actualizó a 13.0.0 (sección 7.3, lote P2-06); no hay salida de consola que confirme la desaparición del error [P].
OSS Index deshabilitado automáticamente en 13.0.0 por falta de credenciales de Sonatype	[D]; pom.xml no declara ossIndexServerId [V]	Limitación de configuración: no hay token de Sonatype Guide. Es una condición distinta del HTTP 401 observado en la línea base.


Efecto: con OSS Index deshabilitado, ese analizador no aporta resultados al reporte de 13.0.0. No se ha medido cuánta cobertura se pierde [P].

Decisión sobre OSS Index: no se autoriza desactivar ese analizador sin evaluar antes las alternativas y las consecuencias para la cobertura del análisis de seguridad. Cualquier cambio adicional en la configuración de OWASP requiere aprobación previa.
6.3 Limitaciones de lectura del reporte
- El HTML de la línea base (12.1.0) tiene una línea de unos 387 KB y las herramientas de lectura solo muestran su inicio.
- Las 10 dependencias de la línea base y sus CVE únicos no se identificaron [P]. Desde la versión 13.0.0 existe target/dependency-check-report.json, que permite el detalle por dependencia (secciones 6.4 y 6.6).
- Solo se conserva el JSON del último escaneo (23 CVE). Los reportes de los estados de 47 y 27 CVE se sobrescribieron y esas cifras son [D].
6.4 Evolución de las vulnerabilidades detectadas (Dependency-Check 13.0.0)
Estado	CVE	Dependencias con alertas	Cambio aplicado	Evidencia
Primer escaneo con 13.0.0	47	7	—	[D]
Tras postgresql.version=42.7.14 y tomcat.version=10.1.61	27	5	−20	[D]
Tras log4j2.version=2.25.5 y jackson-bom.version=2.21.5	23	3	−4	[V] (target/dependency-check-report.json de la ejecución del 06/11/2026)


Reducción acumulada: 47 → 23 CVE (−24) detectados. Composición del estado de 23 CVE [V] (anterior a las suppressions, que llevó al BUILD FAILURE):
Dependencia	Versión	CVE	CVSS ≥ 7	CVSS por CVE
spring-core	6.2.19	17	12	9.8 (5), 9.1 (1), 7.5 (6), 6.1 (3), 4.3 (1), 3.7 (1)
spring-security-crypto	6.5.11	5	3	9.1, 7.4, 7.4, 6.5, 5.9
spring-data-jpa	3.5.13	1	0	6.5
Total		23	15


- Dependencias que dejaron de tener alertas: tomcat-embed-core, postgresql, log4j-api y jackson-databind (versiones 10.1.61, 42.7.14, 2.25.5 y 2.21.5 en el reporte) [V]. La eliminación de sus CVE se limita a lo que el escáner detecta con esas versiones; no es una garantía de ausencia de otros riesgos.
- Los CVSS corresponden a los valores del reporte (NVD). Pueden diferir de la severidad del proveedor.
- La reducción de 47 a 23 no elimina todos los riesgos de seguridad: se detectaron 23 alertas, 15 de ellas bloqueantes antes de las suppressions, y OSS Index no está activo.
Composición de los 23 CVE confirmada en el reporte local del 09/11/2026 [V] (JSON: 8 entradas vulnerabilities y 15 suppressedVulnerabilities). La misma distribución ya se había registrado el 06/11. Las tres categorías son distintas y no deben mezclarse:
Categoría	CVE	Dependencia / módulo en el JSON	CVSS	Tratamiento	Vencimiento
Detectados y activos (no bloquean)	8: CVE-2026-47883, 47887, 59281, 59280, 59314 (spring-core); CVE-2026-47834 (spring-data-jpa); CVE-2026-47842, 59276 (spring-security-crypto)	spring-core 6.2.19 (5), spring-data-jpa 3.5.13 (1), spring-security-crypto 6.5.11 (2)	6.1, 6.1, 6.1, 4.3, 3.7 / 6.5 / 6.5, 5.9 (máximo 6.5)	Siguen presentes y sin corregir; quedan bajo el umbral failBuildOnCVSS=7. No se validaron individualmente (salvo el caso límite de CVE-2026-59281, sección 6.6)	No aplica
Falsas asociaciones de componente (suprimidas)	5: CVE-2026-47888 y 47893 (Framework, 12 artefactos 6.2.19), CVE-2026-59270, 41707 y 47841 (spring-security-crypto 6.5.11)	Los módulos realmente afectados (spring-messaging, spring-webflux, spring-security-ldap, spring-security-oauth2-jose, spring-security-web) no están en el árbol de dependencias	7.5, 7.5, 9.1, 7.4, 7.4	Suppressions de identificación incorrecta por CPE en config/owasp-suppressions.xml	2027-02-06
Aceptaciones temporales de riesgo (suprimidas, no son falsos positivos)	10: CVE-2026-47884, 47885, 47886, 47890, 47891, 47892, 59282, 59283, 59313, 47889	Módulos de Spring Framework 6.2.19 presentes; funcionalidad vulnerable no observada (sección 6.8)	9.8, 7.5, 7.5, 9.8, 9.8, 9.8, 7.5, 9.1, 9.8, 7.5	Decisión interna del responsable del desarrollo; sin aprobación externa; sin corrección aplicada	2026-12-06


Total: 8 + 5 + 10 = 23 CVE detectados; 15 suprimidos (5 + 10) y 8 activos. Las suppressions de Framework usan expresiones regulares de lista cerrada de 12 artefactos 6.2.19, por lo que también ocultan esos CVE en los módulos donde sí reside la clase vulnerable (sección 6.8). Ninguno de los 15 está corregido.
6.5 Justificación de las actualizaciones de dependencias
Las versiones se gestionan mediante propiedades del BOM de Spring Boot 3.5.16 en <properties> de pom.xml [V], sin declarar dependencias explícitas duplicadas. Spring Boot, Spring Framework, Spring Security y Spring Data no se modificaron.
Propiedad	De → a	Motivo	Evidencia
postgresql.version	42.7.11 → 42.7.14	CVE-2026-54291 en el controlador JDBC; el aviso oficial identifica 42.7.12 como corrección y 42.7.14 es la última de la serie 42.7.x verificada	Versión en pom.xml y en el reporte [V]; aviso oficial consultado en P2-08
tomcat.version	10.1.55 → 10.1.61	19 CVE del Tomcat embebido; se mantiene la serie 10.1.x compatible con Spring Boot 3.5	−19 CVE en el escaneo [D]; versión en el reporte [V]
log4j2.version	2.24.3 → 2.25.5	3 CVE asociados a log4j-api; 2.25.5 es la versión corregida indicada en los avisos de Log4j. Parte de las alertas eran por coincidencia de CPE con artefactos ausentes (log4j-1.2-api, Log4j Core)	−3 CVE [D]; versión en el reporte [V]
jackson-bom.version	2.21.4 → 2.21.5	CVE-2026-54515 (CVSS 5.3)	−1 CVE [D]; versión en el reporte [V]


Aplicabilidad de Log4j y Jackson en este microservicio: la explotación de CVE-2026-49844 y CVE-2026-54515 requiere funciones que el código no usa (JsonTemplateLayout y MapMessage; @JsonIgnoreProperties con ACCEPT_CASE_INSENSITIVE_PROPERTIES). Se actualizaron igualmente por ser un cambio de bajo riesgo dentro de la misma serie. La verificación de esas condiciones se hizo en P2-08 mediante lectura de src; no hay otro respaldo en el repositorio.
6.6 Riesgos pendientes de Spring
Las 23 CVE detectadas corresponden a spring-core 6.2.19, spring-security-crypto 6.5.11 y spring-data-jpa 3.5.13 en el proyecto basado en Spring Boot 3.5.16. Esta sección conserva el diagnóstico y las alternativas; el estado comprobado después de la retroalimentación se encuentra en las secciones 6.8 y 6.9.
Aspecto	Detalle
Versiones corregidas de la serie compatible	Spring Framework 6.2.20, Spring Security 6.5.12 y Spring Data JPA 3.5.14. Según la comprobación de P2-08 en Maven Central y en los avisos de Spring, son solo para clientes con soporte Enterprise y no están en Maven Central. No hay copia de esa comprobación en el repositorio [P].
Alternativa de código abierto	Las versiones corregidas públicas pertenecen a Spring Framework 7 / Spring Security 7, es decir, a Spring Boot 4. La migración se descartó en esta práctica por decisión del desarrollador.
Estado	Limitación vigente, no resuelta. La prueba controlada del 09/11/2026 confirmó que OWASP falla por diez CVE altas al retirar las aceptaciones temporales. Las cinco asociaciones incorrectas están justificadas por ausencia de módulos; las diez restantes son riesgos no corregidos y sin aceptación externa. failBuildOnCVSS=7 se mantiene. La versión pública 3.5.16 es la última de la rama 3.5.x; la corrección 6.2.20 de Framework requiere soporte Enterprise (véase §6.9).
Triage preliminar por código (P2-08)	No se encontraron en src usos de WebFlux, mensajería, WebSocket, SpEL, SSE, LDAP, WebAuthn ni AesBytesEncryptor. Seguridad usa solo BCryptPasswordEncoder (PasswordConfig). El único Sort de Spring Data es constante (Sort.by(ASC, "id")) y no hay consultas nativas.
Caso límite	CVE-2026-59281 (CVSS 6.1, spring-core): ApiExceptionHandler lee getBindingResult().getFieldErrors(), aunque la respuesta es ProblemDetail en JSON y no HTML. No se ha demostrado aplicabilidad ni inaplicabilidad [P].
Severidad del proveedor	En los avisos de Spring consultados en P2-08, varios CVE tienen severidad menor que el CVSS de NVD del reporte (por ejemplo, CVE-2026-47884: 9.8 en NVD y MEDIUM en el aviso). Solo se revisaron 6 CVE de spring-core; el resto [P].


Lo que no se concluye (análisis P2-08, anterior a las suppressions): en ese momento ningún CVE se clasificaba como falso positivo definitivo; que las condiciones de explotación no se hayan observado en el código es una indicación de triage, no una prueba de inaplicabilidad. Posteriormente, P2-16 a P2-21 evaluaron individualmente los 15 CVE bloqueantes y se clasificaron así: 5 como asociación incorrecta de componente (módulos ausentes del árbol de dependencias, suprimidos hasta 2027-02-06) y 10 como componente presente con funcionalidad no observada, que no son falsos positivos y se aceptan temporalmente (sección 6.8). Las evaluaciones P2-16 a P2-21 no tienen entrada en la bitácora [P]. Los 8 CVE activos bajo el umbral no se validaron individualmente. Los CVE asociados por CPE a clases de LDAP, DPoP o WebAuthn en spring-security-crypto no se confirmaron con una inspección del JAR [P]; la ausencia de los módulos correspondientes se basa en el árbol de dependencias.

Opciones evaluadas (vigentes al 09/11/2026):
1. Obtener soporte Enterprise de Spring, con las credenciales en settings.xml o variables de entorno, nunca en el repositorio. No adoptada.
2. Migrar a Spring Boot 4 / Framework 7 en un proyecto futuro. No se realizará en esta práctica, por límite de alcance; no se declara como corrección aplicada.
3. Aceptar el riesgo con suppressions con caducidad. Adoptada de forma temporal por decisión del responsable del desarrollo para los 10 CVE de la sección 6.8; las 5 asociaciones incorrectas se suprimieron por separado. No sustituye las opciones 1 y 2.
P3: al incorporar Spring Security (JWT y roles), los CVE de spring-security-crypto deben reevaluarse.
6.7 Evidencias OWASP y limitaciones aún pendientes
- El análisis OWASP volvió a ejecutarse localmente el 09/11/2026 en dos escenarios (véase §6.9). Las salidas se compartieron para revisión, pero todavía no se incorporaron como archivos versionados al repositorio.
- El JSON del 09/11/2026 confirmó 23 CVE (15 suprimidos y 8 activos). Falta decidir si se versionará una copia depurada o se conservará como artefacto de CI; el contenido actual en target/ se sobrescribe en cada ejecución.
- Validación individual CVE por CVE de los 23 antes de cualquier supresión Realizada para los 15 CVE suprimidos (P2-16 a P2-21, sin entradas de bitácora). Pendiente para los 8 CVE activos bajo el umbral.
- Inspección del despliegue (opciones de JVM, variables de entorno, proxies, exposición de red), que condiciona sobre todo a CVE-2026-59283.
- Token de Sonatype Guide y habilitación de OSS Index (ossIndexServerId, requiere aprobación).
6.8 Aceptación temporal de riesgos de 10 CVE de Spring Framework (P2-22)
Estado histórico al 06/11 (P2-23), revalidado el 09/11 (véase §6.9). En la ejecución del 06/11/2026, 23:55:49 (UTC-06:00), .\mvnw.cmd -B clean verify terminó en BUILD SUCCESS [D] con estas 10 aceptaciones vigentes [V] (suppressedVulnerabilities del JSON). El gate se aprobó por la supresión temporal, no porque los CVE estén corregidos ni porque OWASP esté «limpio»: siguen detectados. La revisión del 09/11 reprodujo el fallo al eliminar esas diez supresiones (sección 6.9).

Alcance y naturaleza de la decisión
- El responsable del desarrollo decidió aceptar temporalmente el riesgo residual de 10 CVE de Spring Framework 6.2.19 para el alcance actual de la práctica.
- No son falsos positivos. Los módulos afectados están presentes en el proyecto; lo que no se ha observado es la funcionalidad vulnerable. Se diferencian de las cinco suppressions de identificación incorrecta de componente (spring-messaging, spring-webflux, spring-security-ldap, spring-security-oauth2-jose y spring-security-web, ausentes del árbol), que no forman parte de esta sección.
- Los CVE no están corregidos. Esta decisión no sustituye una actualización a versiones corregidas: Spring Framework 6.2.20 es solo Enterprise y 7.0.9 (código abierto) pertenece a Spring Framework 7 / Spring Boot 4 (avisos oficiales enlazados abajo; sección 6.6).
- No implica aprobación de un tercero. Es una decisión interna del responsable del desarrollo; no hay aprobación externa, de seguridad ni del proveedor.
- Vencimiento: 2026-12-06 para los 10 CVE. Se implementa con until="2026-12-06Z" en config/owasp-suppressions.xml; según el XSD de Dependency-Check, la regla deja de aplicarse en esa fecha y los CVE vuelven a bloquear el build.
- failBuildOnCVSS=7, OWASP y los demás controles de calidad no se modificaron.
- Alcance técnico de las reglas: cada regla usa un patrón cerrado de 12 artefactos de la versión 6.2.19 (aop, aspects, beans, context, core, expression, jcl, jdbc, orm, tx, web y webmvc), con un solo CVE. El patrón oculta el CVE también en los módulos donde reside la clase vulnerable; por eso se trata como aceptación de riesgo y no como corrección. No incluye spring-messaging, spring-webflux, spring-test ni otras versiones.
Evidencia común
- [V] src (principal y pruebas): un único @RestController (UsuarioController) con @PathVariable Long y @Valid @RequestBody (JSON). No se encontraron Flux, Mono, RouterFunction, ServerResponse, SseEmitter, text/event-stream, XsltView, ViewResolver, SpelExpressionParser, @Value, @Query, DataBinder, @ModelAttribute, Jaxb, Jetty, multipart, cookies ni CORS.
- [V] pom.xml: spring-boot-starter-web (servidor Tomcat, sin exclusiones), data-jpa, validation, flyway, postgresql y spring-security-crypto. Sin WebFlux, motor de vistas, actuator, Jetty ni Aalto como dependencias directas.
- [V] application.yaml: no define spring.expression.compiler.mode. src/main/resources solo contiene application.yaml y la migración Flyway; no existe spring.properties.
- [V] El repositorio incorpora .github/workflows/ci.yml y tiene una ejecución exitosa en GitHub Actions del 09/11/2026. No se identificó en los archivos revisados una configuración completa de despliegue productivo (Dockerfile, Compose, variables reales de ejecución o proxies); el CI no demuestra que se haya validado el entorno de producción.
- [V] El árbol Maven filtrado del 09/11/2026 no mostró spring-webflux, spring-messaging ni los módulos especializados de Spring Security citados. Las ausencias de Reactor, Aalto o Jetty no quedan demostradas por ese filtro; conservan el carácter de observación local previa [D] hasta revisar el árbol completo.
- [D] Revisión de la configuración local por el desarrollador: no hay variables que activen explícitamente el compilador SpEL.
- Riesgo residual común: el despliegue (opciones de JVM, variables de entorno, proxies, exposición de red) no se ha inspeccionado; no se analizó el código de librerías de terceros; los endpoints actuales no tienen autenticación (JWT y roles son P3), por lo que los CVE de denegación de servicio no cuentan con esa barrera si el servicio se expone.
Tabla 1. Identificación, condiciones y decisión
CVE	CVSS original (NVD) / severidad del aviso de Spring	Componente	Condición de explotación	Aviso oficial	Decisión
CVE-2026-47884	9.8 / MEDIUM	spring-webmvc, XsltView	XsltView con mapping /** que renderiza vistas sin nombre de vista explícito: SSRF y RCE	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47885	7.5 / MEDIUM	spring-web, PartEventHttpMessageReader	@RequestBody Flux<PartEvent> con maxInMemorySize en -1: DoS	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47886	7.5 / MEDIUM	spring-expression, operador ^ de SpEL	SpEL controlado por el usuario y un BigDecimal o BigInteger accesible: DoS	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47890	9.8 / LOW	Spring MVC y WebFlux, fragmentos de vista por SSE	Fragmentos de vista por SSE y atacante con control de datos transmitidos a otros usuarios	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47891	9.8 / MEDIUM	Jaxb2Decoder de WebFlux con Aalto	Aplicación WebFlux con el procesador XML Aalto: no se aplica maxInMemorySize	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47892	9.8 / MEDIUM	Endpoints funcionales (WebFlux) con DispatcherServlet	Predicado de cabecera omitido en una solicitud preflight	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-59282	7.5 / MEDIUM	Data binding (DataBinder, BeanWrapper, DirectFieldAccessor)	Rutas de propiedades del usuario sobre un objeto con una List auto-poblada cuyos elementos tienen subpropiedades: DoS	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-59283	9.1 / MEDIUM	spring-expression, SimpleEvaluationContext	SimpleEvaluationContext con el compilador SpEL activo (IMMEDIATE o MIXED): omisión de las protecciones	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-59313	9.8 / LOW	Framework funcional de Spring MVC (ServerResponse.sse())	SSE en texto plano con datos controlados por un atacante	aviso	Aceptación temporal hasta 2026-12-06
CVE-2026-47889	7.5 / MEDIUM	JettyCoreServerHttpResponse (WebFlux sobre Jetty 12 Core)	Cookies sin el atributo sameSite en WebFlux sobre Jetty 12 Core	aviso	Aceptación temporal hasta 2026-12-06


Los CVSS son los del reporte de OWASP (NVD, target/dependency-check-report.json [V]); las severidades del proveedor se tomaron de los avisos oficiales. Spring Framework 6.2.0 a 6.2.19 figura como rango afectado en los diez avisos; todos indican 6.2.20 como versión corregida solo para soporte Enterprise. Solo se verificó en P2-16 el módulo que contiene la clase vulnerable de CVE-2026-47884, CVE-2026-47885, CVE-2026-47886 y CVE-2026-59283; para los demás se usa la descripción del aviso [P].

Tabla 2. Evidencia, mitigaciones, riesgo residual y reevaluación
CVE	Evidencia del código y de la configuración local	Mitigaciones existentes	Riesgo residual y limitaciones	Se reevalúa si
CVE-2026-47884	[V] Sin XsltView ni ViewResolver en src; sin motor de vistas en pom.xml; solo un @RestController.	La API devuelve JSON; no renderiza vistas.	Es el CVE de mayor impacto (SSRF y RCE). Se desconoce el despliegue y no se descartó código de terceros que registre vistas.	Se agregan vistas (XSLT o plantillas) o mappings /** que renderizan vistas.
CVE-2026-47885	[V] Sin Flux, Mono ni multipart en src; pom.xml sin WebFlux. [V] El árbol filtrado del 09/11 no contiene WebFlux; la ausencia de Reactor no queda comprobada por ese filtro.	Pila Spring MVC sobre Tomcat, no reactiva.	Los endpoints no tienen autenticación hasta P3. La ausencia en el árbol no se comprobó en el repositorio.	Se agregan WebFlux, Reactor o endpoints multipart.
CVE-2026-47886	[V] Sin SpelExpressionParser, @Value, @Query ni #{} en src; sin actuator en pom.xml.	Ningún endpoint evalúa expresiones.	No se analizó si librerías de terceros evalúan SpEL con entrada del usuario.	Se evalúan expresiones SpEL con datos del usuario (por ejemplo, en la autorización de P3).
CVE-2026-47890	[V] Sin SseEmitter ni text/event-stream en src; sin motor de plantillas.	Solo respuestas JSON síncronas.	Se desconoce el despliegue.	Se agregan SSE o plantillas de vista.
CVE-2026-47891	[V] Sin XML, Jaxb ni WebFlux en src; Aalto no figura en pom.xml. [V] No aparece WebFlux en el árbol filtrado; Reactor y Aalto requieren revisar el árbol completo.	Los DTO se reciben como JSON con Jackson; no hay procesamiento XML.	La ausencia en el árbol es un dato del desarrollador; no se inspeccionó el despliegue.	Se incorporan dependencias reactivas o procesamiento XML.
CVE-2026-47892	[V] Sin RouterFunction ni ServerResponse en src; sin @CrossOrigin ni configuración CORS; mapeo solo con anotaciones.	No se usan endpoints funcionales.	Se desconoce el comportamiento de proxies o intermediarios del despliegue.	Se agregan endpoints funcionales, CORS o WebFlux.
CVE-2026-59282	[V] El controlador solo usa @PathVariable Long y @Valid @RequestBody; sin @ModelAttribute, DataBinder ni BeanWrapper; sin List como propiedad en DTO ni entidades. spring.jackson.deserialization.fail-on-unknown-properties está en true en application.yaml.	Entrada JSON validada con Jakarta Validation; no se aplican rutas de propiedades del cliente.	No se analizó código de terceros que use el binding; endpoints sin autenticación hasta P3.	Se agrega binding de formularios o parámetros a objetos, o colecciones en los DTO.
CVE-2026-59283	[V] Sin SpEL en src; application.yaml no define spring.expression.compiler.mode; no existe spring.properties. [D] El desarrollador no halló variables que activen el compilador.	El compilador SpEL está desactivado por defecto; no se encontró nada que lo active localmente.	El repositorio incluye .github/workflows/ci.yml, con una ejecución satisfactoria el 09/11/2026, pero no se inspeccionaron las opciones de JVM, las variables de entorno ni la configuración del despliegue productivo. Tampoco se analizó código de terceros que utilice SimpleEvaluationContext. La aprobación de CI no demuestra la ausencia de esta condición de explotación en un entorno de producción.	Se activa el compilador SpEL (propiedad, variable o JVM) o se usa SimpleEvaluationContext.
CVE-2026-59313	[V] Sin RouterFunction, ServerResponse ni SSE en src.	Solo controladores con anotaciones y respuestas JSON.	Se desconoce el despliegue.	Se agregan endpoints funcionales o SSE.
CVE-2026-47889	[V] spring-boot-starter-web (Tomcat) sin exclusiones; sin cookies, WebFlux ni Jetty en src ni en pom.xml. [V] WebFlux no aparece en el árbol filtrado; Jetty y Reactor requieren revisar el árbol completo.	Servidor Tomcat; la aplicación no emite cookies.	La ausencia en el árbol es un dato del desarrollador; no se inspeccionó el despliegue.	Se cambia a Jetty, se agrega WebFlux o la aplicación empieza a emitir cookies.


Condiciones generales que obligan a reevaluar todos los CVE de esta sección: llegar al vencimiento (2026-12-06); cualquier cambio en las dependencias de Spring, Spring Boot o el árbol Maven; la incorporación de JWT y Spring Security en P3; nuevos avisos o cambios de severidad de Spring; y cualquier despliegue real del servicio, cuyas condiciones deben inspeccionarse antes de dar por válida esta aceptación.

Relación con secciones anteriores: las secciones 6.6, 7.2, 7.3, 8 y 9.1, y el encabezado del documento, se actualizaron en P2-23 para reflejar la configuración de config/owasp-suppressions.xml y la ejecución final, conservando como historial el estado sin suppressions. La sección 8 registra una propuesta rechazada en P2-08; los CVE de esta sección se aceptan después, con una evaluación individual (P2-16 y P2-21) y la decisión humana de P2-22.
6.9 Revalidación tras la retroalimentación del instructor (09/11/2026)
6.9.1 Objetivo y alcance de la comprobación
El instructor solicitó actualizar spring-boot-starter-parent al último parche 3.5.x, volver a ejecutar dependency-check:check, retirar supresiones innecesarias y conservar exclusivamente falsos positivos justificables. Se verificó que el proyecto ya declara Spring Boot 3.5.16, última versión de código abierto de la línea 3.5.x. Anuncio oficial de Spring Boot, 25/06/2026. No se encontró un parche público posterior de 3.5.x que actualizar.

Spring Framework 6.2.19 permanece como dependencia administrada. Los avisos de Spring indican que 6.2.20 contiene correcciones, pero está disponible solo mediante soporte Enterprise. Por ejemplo, CVE-2026-47884 identifica el rango afectado 6.2.0–6.2.19 y la corrección 6.2.20 bajo soporte comercial. Por decisión de alcance, no se emprendió una migración a Spring Boot 4.
6.9.2 Árbol de dependencias
Se ejecutó en CMD:
mvnw.cmd -B dependency:tree "-Dincludes=org.springframework:*,org.springframework.security:*"
Resultado: BUILD SUCCESS el 09/11/2026 a las 17:29:35 (UTC−06:00). Maven confirmó:
Dependencia	Versión
Spring Boot (spring-boot-starter-web / data-jpa / test)	3.5.16
Spring Framework (incluido spring-core, spring-web, spring-webmvc, spring-expression)	6.2.19
Spring Data JPA	3.5.13
Spring Security Crypto	6.5.11


No aparecen spring-webflux, spring-messaging, spring-security-ldap, spring-security-oauth2-jose ni spring-security-web en el árbol filtrado por grupo Spring. Esa salida respalda las cinco supresiones por asociaciones incorrectas de componentes, pero no prueba la ausencia de Jetty, Reactor o Aalto, porque son de otros grupos Maven.
6.9.3 Prueba controlada de OWASP
Se mantuvo failBuildOnCVSS=7 y se realizaron dos análisis con OWASP Dependency-Check 13.0.0:
Escenario	Resultado	Fecha y hora (UTC−06:00)
Archivo original con 15 supresiones: 5 asociaciones incorrectas + 10 aceptaciones temporales	BUILD SUCCESS	09/11/2026, 17:12:06
Archivo de prueba, con solo las 5 supresiones por asociación incorrecta	BUILD FAILURE	09/11/2026, 17:34:12


Ambas ejecuciones utilizaron el comando mvnw.cmd -B dependency-check:check. La primera generó un JSON con 23 CVE detectados: 5+3=8 activos (5 en spring-core, 1 en spring-data-jpa, 2 en spring-security-crypto) y 15 suprimidos. La segunda devolvió diez CVE bloqueantes en spring-core 6.2.19:
CVE	CVSS informado por OWASP
CVE-2026-47884	9.8
CVE-2026-47885	7.5
CVE-2026-47886	7.5
CVE-2026-47889	7.5
CVE-2026-47890	9.8
CVE-2026-47891	9.8
CVE-2026-47892	9.8
CVE-2026-59282	7.5
CVE-2026-59283	9.1
CVE-2026-59313	9.8


La prueba demuestra que las diez supresiones temporales hacen pasar el quality gate, no que las vulnerabilidades estén corregidas. No demuestra por sí sola que cada ruta de explotación sea accesible desde los endpoints actuales; las condiciones y limitaciones se examinan individualmente en §6.8.
6.9.4 Estado de las reglas, riesgos y decisión
Se restauró el archivo original config/owasp-suppressions.xml después de la prueba, para no incorporar un cambio experimental que provocara un fallo del CI. El archivo mantiene 15 reglas: cinco supresiones por identificación errónea de módulo (vencen el 06/02/2027) y diez aceptaciones temporales de riesgo (vencen el 06/12/2026). No se eliminaron reglas de forma definitiva y no se cambió la configuración de OWASP ni su umbral de 7.

Estado de la observación: diagnóstico actualizado y documentado; remediación de las diez CVE aún pendiente. Las cinco supresiones por asociación incorrecta tienen justificación técnica por ausencia de módulos en el árbol Spring. Las otras diez no se reclasifican como falsos positivos solamente porque el código del servicio no use determinadas funciones. Obtener BUILD SUCCESS con ellas no satisface por sí solo la solicitud del instructor de corregir las vulnerabilidades y reducir supresiones.

Se comunicará al instructor esta limitación técnica para decidir un criterio de cierre dentro del alcance acordado de Spring Boot 3.5.x. Una solución que requiera soporte Enterprise, o una migración mayor, no se presenta como aplicada. Hasta que exista una corrección o decisión externa expresa, el riesgo sigue abierto.

Fuentes y evidencias: anuncio de Spring Boot 3.5.16; aviso CVE-2026-47884; salidas de CMD de dependency:tree y dependency-check:check del 09/11; JSON local generado el 09/11; pom.xml y config/owasp-suppressions.xml de practica-2. Las salidas locales y el JSON no están archivados todavía como anexos versionados del repositorio.
7. Propuestas de IA, decisiones humanas y correcciones aplicadas
7.1 Propuestas de IA
Origen	Propuesta	Estado
P2-02	Estructura de docs/QUALITY-REPORT.md con comparación antes/después	Aplicada en este documento
P2-02	Plan por lotes: triage OWASP, configuración OWASP, remediación de dependencias, MagicNumber, ApiExceptionHandler y UsuarioController, pruebas de refuerzo opcionales y verificación final	Pendiente de aprobación humana
P2-02	Lista de verificaciones pendientes	Pendiente de completar
P2-03	Borrador de este reporte	Revisado y aprobado por el desarrollador, con ajustes
P2-04	Corrección de 11 hallazgos (LineLength y MultipleStringLiterals) en ApiExceptionHandler.java y UsuarioController.java	Aprobada y aplicada
P2-05	Clase de constantes compartidas RestriccionesUsuario para los 8 MagicNumber (alternativa 3)	Aprobada y aplicada
P2-06	Actualizar Dependency-Check a 13.0.0 con HTML/JSON e incorporar ossIndexServerId	Actualización y reportes aprobados. Rechacé agregar ossIndexServerId sin contar con un token de Sonatype Guide; no está configurado y se conservó el análisis mediante NVD [V]
P2-07	Actualizar postgresql.version y tomcat.version mediante propiedades del BOM	Aprobada y aplicada
P2-08	Actualizar Log4j y Jackson; documentar las correcciones Enterprise de Spring como limitación	Log4j y Jackson: aprobados y aplicados. Spring: no aplicado (sección 6.6)
P2-09	Actualización de este informe final	Aprobada y aplicada en este documento
P2-08	Gestionar CVE mediante suppressions específicas y caducidad	No autorizada inicialmente; posteriormente aplicada en P2-17 a P2-22. Por ese motivo no se presenta como rechazo definitivo a la IA (véase §8)
P2-23	Actualización del informe con la ejecución del 06/11/2026	Aplicada; registro histórico de 13 pruebas, superado después por el CI de 16 pruebas
09/11	Revalidar OWASP después de la retroalimentación y documentar CI	Se agregaron evidencias de dos ejecuciones OWASP, del árbol Maven y del workflow sin afirmar corrección de las diez CVE (§6.9 y §10.4)


7.2 Decisiones humanas
Decisión / observación	Detalle
Línea base contrastada con archivos	Checkstyle, PMD, CPD, SpotBugs, JaCoCo y pruebas coinciden con la línea base; OWASP se verificó parcialmente.
OWASP	70 asociaciones con CVE no son 70 vulnerabilidades únicas, y una alerta no demuestra explotabilidad.
OSS Index	Rechacé configurar ossIndexServerId antes de disponer de credenciales válidas. OWASP utiliza la fuente NVD mediante NVD_API_KEY; OSS Index permanece inactivo por falta de token, como limitación visible en los logs. No se atribuye esa falta de cobertura a una corrección de seguridad.
Correcciones	Línea base (06/11/2026): no se habían autorizado correcciones. Posteriormente se autorizaron y aplicaron los lotes de la sección 7.3. Se mantiene la regla de no suprimir reglas de calidad ni usar @SuppressWarnings. Las suppressions de OWASP no estaban autorizadas en P2-08; después se autorizaron de forma específica (5 asociaciones incorrectas y 10 aceptaciones temporales) con decisión humana.
Spring	Las correcciones Enterprise de Spring no se aplican y no se migra a Spring Boot 4 en esta práctica. Los 10 CVE bloqueantes con funcionalidad no observada se aceptan temporalmente hasta 2026-12-06 (decisión del responsable del desarrollo, sin aprobación externa; sección 6.8).
Aceptación de riesgo (P2-22)	El responsable del desarrollo aceptó temporalmente el riesgo residual de 10 CVE; no son falsos positivos ni están corregidos. Vence el 2026-12-06 y debe reevaluarse o reemplazarse por una actualización a versiones corregidas.
Evidencias de la guía de IDS	Se incorporan SonarQube, los tres hallazgos explicados en primera persona, un rechazo verificable de IA (§8), el build local y el resultado de Actions (§10).


7.3 Correcciones aplicadas
Estado en la línea base (06/11/2026, conservado): desde el diagnóstico inicial (P2-02) no se habían aplicado correcciones al código Java ni actualizaciones de dependencias [D]. La lectura de los archivos Java de ese momento fue consistente con esa afirmación: los hallazgos de Checkstyle de la sección 5 seguían presentes.

La configuración de los analizadores ya estaba realizada: pom.xml declara los plugins de la sección 2 y existen los archivos config/checkstyle/checkstyle.xml y config/pmd/ruleset.xml [V]. Esa configuración no corrige hallazgos; solo permite medirlos.

Lotes aplicados después de la línea base (todos con aprobación previa del desarrollador; las verificaciones las ejecutó el desarrollador):
Lote	Archivos modificados	Resultado de pruebas	Resultado de analizadores	Estado
P2-04 — LineLength y MultipleStringLiterals	ApiExceptionHandler.java, UsuarioController.java	13 aprobadas [D]	Checkstyle 19 → 8 [D]	Aplicado
P2-05 — MagicNumber	RestriccionesUsuario.java (nuevo), Usuario.java, CrearUsuarioRequest.java, ActualizarUsuarioRequest.java	13 aprobadas [D]	Checkstyle 8 → 0; JaCoCo 71.43 % [D] (resultado histórico; último dato en sección 4.8)	Aplicado
P2-06 — Dependency-Check	pom.xml (bloque del plugin): 12.1.0 → 13.0.0, formatos HTML y JSON; se conserva failBuildOnCVSS=7 y NVD_API_KEY	—	Reporte JSON generado: 47 CVE en 7 dependencias [D]	Aplicado
P2-07 — PostgreSQL y Tomcat	pom.xml (<properties>): postgresql.version 42.7.14, tomcat.version 10.1.61	13 aprobadas [D]	OWASP: 47 → 27 CVE [D]	Aplicado
P2-08 — Log4j y Jackson	pom.xml (<properties>): log4j2.version 2.25.5, jackson-bom.version 2.21.5	13 aprobadas (resultado histórico del 06/11) [V]	OWASP: 27 → 23 CVE [V]	Aplicado
P2-17 a P2-20 — Suppressions de identificación incorrecta	config/owasp-suppressions.xml (nuevo; 5 reglas finales), pom.xml (suppressionFiles) y .github/skills/quality-gates-java/references/maven-plugins.xml	—	5 CVE excluidos por asociación CPE incorrecta; vencimiento 2027-02-06 [V] (archivo y JSON)	Aplicado
P2-22 — Aceptación temporal de riesgos	config/owasp-suppressions.xml (10 reglas), docs/QUALITY-REPORT.md (sección 6.8), BITACORA-IA.md; vencimiento 2026-12-06	—	10 CVE bajo aceptación temporal [V] (archivo y JSON)	Aplicado
P2-23 — Informe del 06/11	docs/QUALITY-REPORT.md, BITACORA-IA.md	13 pruebas aprobadas, estado histórico	clean verify exitoso con 15 supresiones y 8 CVE activas	Aplicado
07/11 — Pruebas adicionales	UsuarioServiceTest.java, perfil H2 de contexto	16 aprobadas, 0 fallos, 0 errores, 0 omitidas	Cobertura 70.15 % (94/134) en verificación local	Aplicado
09/11 — CI	.github/workflows/ci.yml	16 pruebas aprobadas en Actions	./mvnw -B verify: BUILD SUCCESS, gates activos	Aplicado
09/11 — Revisión OWASP	Análisis local y actualización documental	No modifica las pruebas	Con 15 supresiones: BUILD SUCCESS; con solo 5: BUILD FAILURE. Se restauró el XML original	Diagnóstico documentado; riesgo pendiente
10/11 — Refuerzo de pruebas	UsuarioServiceTest.java; señalización documental en docs/requerimientos.md	2 casos adicionales, 18 pruebas aprobadas localmente; 0 fallos, 0 errores, 0 omitidas	JaCoCo local: 77.78 % de líneas (98/126); reporte de CI posterior pendiente	Medición local documentada [V]


En los lotes P2-04 a P2-23 no se modificaron pruebas, migraciones Flyway, servicios, repositorios ni endpoints. Posteriormente se añadieron tres pruebas negativas (07/11) y otras dos pruebas de servicio (10/11), sin modificar rutas ni esquema. Ninguna regla de calidad se desactivó y no se usó @SuppressWarnings [V]. Las suppressions de OWASP existen únicamente en config/owasp-suppressions.xml (15 reglas: 5 de asociación incorrecta y 10 de aceptación temporal), referenciado desde suppressionFiles en pom.xml; failBuildOnCVSS=7 se conserva y no se declara ossIndexServerId [V].

Los cambios documentales de la línea base fueron prompts/P2-02-diagnostico-calidad.md, la entrada P2-02 de BITACORA-IA.md y este reporte. Las entradas P2-03 a P2-09 constan en la bitácora. Los prompts originales de esas sesiones y los registros P2-12 a P2-21 quedan pendientes [P].
8. Propuesta de IA rechazada y alternativa adoptada
Durante la interacción P2-06 de calidad de dependencias, GitHub Copilot propuso incorporar ossIndexServerId al bloque OWASP Dependency-Check en pom.xml para utilizar también el servicio Sonatype OSS Index.

Qué rechacé: no autoricé agregar esa configuración porque no disponía de un token válido de Sonatype Guide. Incluir un identificador de servidor sin poder autenticar el servicio no resolvía los hallazgos de seguridad y podía producir un error de autenticación o una falsa impresión de que el analizador adicional estaba operativo. Este rechazo consta en BITACORA-IA.md, entrada P2-06, que distingue expresamente la aprobación de Dependency-Check 13.0.0 y los reportes HTML/JSON de la parte ossIndexServerId no autorizada.

Qué hice en su lugar: limité el cambio a Dependency-Check 13.0.0, habilité sus informes HTML/JSON, mantuve failBuildOnCVSS=7 y utilicé la variable de entorno NVD_API_KEY para actualizar los datos de NVD. El pom.xml no contiene ossIndexServerId; los logs muestran que el analizador Sonatype OSS Index está inactivo por falta de credenciales, una limitación declarada y no oculta. Si posteriormente se obtiene un token válido, su incorporación requerirá una nueva decisión y validación.

Diferencia con las supresiones: en P2-08 no autoricé inicialmente las suppressions por falta de evidencia individual, pero más adelante sí fueron incorporadas. Por tanto, esa propuesta no se utiliza aquí como ejemplo de rechazo definitivo, tal como observó el instructor. Las diez aceptaciones temporales y su riesgo siguen descritos en las secciones 6.8 y 6.9.

Evidencia: BITACORA-IA.md (P2-06 y P2-08), pom.xml, configuración de Dependency-Check y logs de OWASP. No se atribuye a la IA una decisión que no esté respaldada por estas fuentes.
9. Validación final y conclusiones
9.1 Verificación del estado más reciente
Criterio	Resultado	Evidencia / límite
./mvnw -B verify en CI sin omitir analizadores	BUILD SUCCESS	GitHub Actions del 09/11/2026, rama practica-2, commit 2881056; ejecución [V]
Checkstyle, PMD/CPD y SpotBugs/FindSecBugs	Gates aprobados; Checkstyle reportó 0 violaciones	Log de CI [V]
Pruebas automatizadas	18 aprobadas en Surefire local actualizado (0 fallos, 0 errores, 0 omitidas); 16 aprobadas en el último CI verificado	Salida findstr de los cuatro archivos Surefire [V]; CI del 09/11 [V]
JaCoCo — líneas ≥ 70 %	77.78 % (98/126) en la captura local del 10/11; umbral 70 %, margen 7.78 puntos; CI anterior aprobado	§10.5 y captura 10/11 [V]; gate de CI 09/11 [V]
OWASP Dependency-Check 13.0.0	Gate aprobado con 15 supresiones; ocho CVE activas bajo el umbral	JSON local 09/11 y log de CI [D]/[V]
OWASP sin diez aceptaciones temporales	BUILD FAILURE, diez CVE bloqueantes con CVSS 7.5–9.8	Salida de CMD del 09/11 a las 17:34:12 [D]
Corrección efectiva de diez CVE de Spring	Pendiente	La versión instalada 6.2.19 sigue asociada; la corrección 6.2.20 requiere soporte Enterprise (véase §6.9)
SonarQube for IDE en servicio principal	Captura incorporada sobre UsuarioService.java	docs/img/sonarqube-usuario-service.jpg [V]
Evidencia de activación de skill	Dos activaciones automáticas documentadas	§11, docs/img/skill-activacion-1.png y skill-activacion-2.png [V]
Configuración sin bajar umbrales de quality gates	Se conservó failBuildOnCVSS=7 y cobertura de líneas mínima 0.70	pom.xml [V]


9.2 Interpretación de los resultados
El proyecto consiguió automatizar las verificaciones y eliminar los 19 hallazgos de estilo sin desactivar reglas. La ampliación de casos negativos permitió comprobar 16 pruebas en el CI del 09/11 sin depender de PostgreSQL local. Dos nuevas pruebas quedaron definidas y se ejecutaron satisfactoriamente el 10/11 para las rutas listar y eliminar. La nueva captura de JaCoCo muestra 77.78 % de líneas, un margen de 7.78 puntos porcentuales sobre el umbral frente al de 0.15 puntos de la medición histórica del 07/11. La cantidad de líneas consideradas por JaCoCo cambió de 134 a 126; por ello, la mejora porcentual no demuestra por sí sola el impacto exclusivo de las nuevas pruebas. Los cuatro reportes de Surefire confirman 18 pruebas aprobadas y sin omisiones. Falta revisar el BUILD SUCCESS del clean verify posterior.

El CI exitoso demuestra que se ejecutó verify con la configuración actual. No demuestra que las diez vulnerabilidades de Spring estén remediadas, porque aún se aplican las diez reglas temporales. La prueba controlada de OWASP del 09/11 confirmó que retirarlas provoca un fallo del gate.
9.3 Conclusión y pendientes reales
- Calidad de código y CI: comprobados en verde para el commit 2881056 de practica-2 el 09/11/2026.
- Pruebas y cobertura: 18 pruebas aprobadas en los reportes locales Surefire del 10/11 (0 fallos, 0 errores, 0 omitidas); 16 aprobadas en el último CI verificado (09/11); 77.78 % de líneas (98/126) en la captura de JaCoCo. Queda por confirmar el mensaje final de clean verify y un nuevo CI con las dos pruebas.
- Seguridad de dependencias: 23 CVE detectados en el análisis local original; 8 activas por debajo del umbral, 5 asociaciones incorrectas suprimidas y 10 riesgos temporales no corregidos.
- Pendiente de revisión del instructor: definir la vía de cierre de los diez CVE altas dentro de Spring Boot 3.5.x, sin confundir supresión con remediación.
- Pendiente técnico: confirmar el resultado final BUILD SUCCESS de clean verify, revisar la variación de líneas incluidas en JaCoCo, reevaluar CVE al introducir JWT y roles en P3 y comprobar migraciones PostgreSQL reales en un entorno de pruebas apropiado.
- Correcciones locales posteriores al último CI verificado: el desarrollador reportó la incorporación de marcas **SUPUESTO:** en docs/requerimientos.md y la sustitución de la URL fija por ${DB_URL} en application.yaml; la modificación de DB_URL produjo un mvnw.cmd -B verify local satisfactorio con 16 pruebas el 09/11. La inclusión final de ambos cambios en CI y en el PR requiere comprobación posterior.
10. Evidencias de la práctica
Evidencia	Ubicación	Estado
SonarQube for IDE en la clase principal de servicio	docs/img/sonarqube-usuario-service.jpg	Incorporada; analiza solo ese archivo [V]
Dos activaciones automáticas de la skill	docs/img/skill-activacion-1.png, skill-activacion-2.png	Incorporadas (§11) [V]
Resultado mvn verify con controles activos	Actions — CI del PR #5	BUILD SUCCESS 09/11/2026, 16 pruebas [V]
Cobertura porcentual actual	Captura de JaCoCo 10/11: docs/img/jacoco-2026-11-10.png	77.78 % (98/126 líneas), según la fila Total [V]; Surefire local recibido con 18 pruebas correctas [V]; CSV y log final del build no adjuntos [P]
Prueba OWASP con 15 y con 5 supresiones	Salidas de CMD del 09/11 y §6.9	Resultados documentados; no se han versionado los logs locales [D]
JSON de OWASP posterior al 09/11	target/dependency-check-report.json	Analizado; no archivado fuera de target/ [P]
Capturas de ejecuciones históricas con OWASP omitido	No disponibles	Se conservan como referencias históricas, no como evidencia del CI final [P]
Rechazo efectivo de IA	§8 y BITACORA-IA.md, P2-06	Documentado y contrastable con pom.xml [V]
Tres hallazgos explicados	§5	Completos, con problema, decisión y resultado
Prompts originales de algunas sesiones de P2	prompts/	No todos están archivados [P]


10.1 Evidencia de SonarQube for IDE

El 07/11/2026 SonarQube for IDE 6.2.0 analizó UsuarioService.java con Java 21. El registro de la extensión identificó el archivo y concluyó Analysis detected 0 issues and 0 Security Hotspots in 1032ms. La captura es evidencia del análisis de la clase solicitada, no un escaneo integral del proyecto.
10.2 Ejecuciones Maven y resultados
Fecha	Comando / entorno	Resultado	Interpretación
06/11	mvnw.cmd -B clean verify -Ddependency-check.skip=true	BUILD SUCCESS histórico	OWASP omitido; no demuestra el quality gate completo
06/11	mvnw.cmd -B clean verify con supresiones	BUILD SUCCESS, entonces 13 pruebas	Ejecución histórica anterior a la ampliación de tests; extracto en §4.9
07/11	mvnw.cmd -B verify local, perfil H2	16 pruebas, 70.15 % de líneas, BUILD SUCCESS	Verificación ampliada con los gates activos (§10.3)
09/11	./mvnw -B verify en GitHub Actions	16 pruebas, BUILD SUCCESS	Verificación reproducible en runner remoto (§10.4)
09/11, 17:12	mvnw.cmd -B dependency-check:check con 15 supresiones	BUILD SUCCESS	Supresiones originales vigentes; no equivale a CVE corregidos
09/11, 17:34	Mismo comando, solo 5 supresiones	BUILD FAILURE	Diez CVE con CVSS ≥ 7 bloquean; archivo restaurado después (§6.9)
10/11	Captura de target/site/jacoco/index.html tras modificar las pruebas	77.78 % (98/126 líneas)	Evidencia de cobertura; Surefire confirma 18 pruebas correctas. El resultado final BUILD SUCCESS de Maven aún no se adjuntó (§10.5)


10.3 Pruebas sin PostgreSQL local
La prueba de contexto activa el perfil test y usa H2 en memoria. Hibernate genera el esquema de prueba y Flyway queda deshabilitado en ese perfil porque la migración V1 contiene SQL específico de PostgreSQL. Esta prueba confirma el arranque y la integración con JPA bajo H2, no la compatibilidad de Flyway V1 en PostgreSQL. La validación de V1 con PostgreSQL real se realizó anteriormente y se documentó en la bitácora de P1.

El 07/11/2026 se verificó que el puerto 5432 estaba libre y se retiraron las variables DB_USERNAME, DB_PASSWORD y DB_URL del proceso Maven. Pasaron 16 pruebas, sin fallos, errores ni omitidas; se habían añadido tres casos negativos al servicio: actualizar usuario inexistente, eliminar usuario inexistente y actualizar un correo duplicado. Hikari registró la conexión jdbc:h2:mem:ms-usuarios. En ese entorno se configuró JAVA_TOOL_OPTIONS para precargar el agente Mockito y utilizar target/p2-java-tmp como directorio temporal de Java. El comando mvnw.cmd -B verify terminó en BUILD SUCCESS a las 00:36:17 (UTC−06:00). El reporte local de JaCoCo registró 70.15 % de líneas (94/134), frente a 68.66 % (92/134) antes de las tres pruebas; OWASP continuó utilizando las 15 supresiones documentadas.
10.4 Ejecución de GitHub Actions (09/11/2026)
El workflow .github/workflows/ci.yml se ejecutó en la rama practica-2, commit 28810568b89165fb3aaea30111d4a55d63900e2c, mediante un evento pull_request. La ejecución ci-miguel #42716270381 terminó en success el 09/11/2026. El job verify usó Java 21 y ejecutó ./mvnw -B verify con el secreto NVD_API_KEY suministrado a través del entorno, sin registrarlo en texto.

El log contiene:
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
También aparece You have 0 Checkstyle violations. y la ejecución de Dependency-Check 13.0.0. El log no publica el porcentaje de JaCoCo ni demuestra que se hayan corregido las diez CVE temporalmente aceptadas. OSS Index permaneció inactivo por ausencia de credenciales de Sonatype; se mantuvo la consulta a NVD.
10.5 Nuevas pruebas y medición local de JaCoCo (10/11/2026)
El instructor pidió incorporar una o dos pruebas para no quedar cerca del umbral mínimo de cobertura del 70 %. El desarrollador añadió dos casos unitarios a UsuarioServiceTest.java, sobre funcionalidades ya existentes y sin modificar código de producción:
Prueba añadida	Comportamiento comprobado	Estado de evidencia
listarUsuariosDevuelveRespuestasOrdenadasPorId	Comprueba que listar() solicita al repositorio resultados ordenados por id ascendente y transforma las entidades en DTOs UsuarioResponse. Se utiliza Mockito; no demuestra el orden físico de PostgreSQL.	Suite de servicio confirmada: 8 pruebas, 0 fallos y 0 errores; Surefire no desglosa métodos en el .txt mostrado [V]
eliminarUsuarioExistenteLlamaDeleteById	Verifica que eliminar(id) llama a deleteById(id) cuando existsById(id) devuelve true.	Suite de servicio confirmada: 8 pruebas, 0 fallos y 0 errores; Surefire no desglosa métodos en el .txt mostrado [V]


El conjunto actualizado quedó con 18 pruebas aprobadas: ocho de controlador, ocho de servicio, una de validación DTO y una de contexto. El desarrollador aportó la salida de findstr /S /C:"Tests run:" target\surefire-reports\*.txt con cuatro suites y un total de 18 ejecutadas, 0 fallos, 0 errores y 0 omitidas. Estos reportes confirman las suites, pero no prueban por sí solos el resultado global de Maven ni que un CI posterior haya pasado.

Evidencia de ejecución local de Surefire (10/11/2026) [V]:
Clase de prueba	Ejecutadas	Fallos	Errores	Omitidas
UsuarioControllerTest	8	0	0	0
CrearUsuarioRequestTest	1	0	0	0
MsUsuariosApplicationTests	1	0	0	0
UsuarioServiceTest	8	0	0	0
Total	18	0	0	0


La evidencia se obtuvo de target/surefire-reports/*.txt mediante findstr. No se dispone del mensaje final de Maven en esa salida, por lo que no se atribuye un BUILD SUCCESS al ciclo completo de la ejecución.

Medición visual más reciente, proporcionada por el desarrollador el 10/11/2026 [V]:
Métrica	Contadores JaCoCo	Valor mostrado por JaCoCo	Interpretación
Cobertura de líneas (gate ≥ 70 %)	28 no cubiertas de 126; 98 cubiertas	77.78 % (98/126)	Margen de +7.78 puntos porcentuales sobre el mínimo
Cobertura de instrucciones	128 no cubiertas de 530; 402 cubiertas	75 %	Valor informativo; porcentaje tal como aparece en la interfaz
Cobertura de ramas	17 no cubiertas de 32; 15 cubiertas	46 %	Valor informativo; porcentaje tal como aparece en la interfaz
Cobertura de métodos	10 no cubiertos de 50; 40 cubiertos	40/50	Informativo
Cobertura de clases	0 no cubiertas de 12	12/12	Informativo



Comparación con la medición anterior: el 07/11 se informaron 94/134 líneas (70.15 %); el 10/11 la captura muestra 98/126 (77.78 %). La diferencia es +7.63 puntos porcentuales, pero el número total de líneas computadas también cambió (134 frente a 126). Por tanto, no puede afirmarse que la mejora se deba únicamente a las dos pruebas sin analizar el jacoco.csv, las clases instrumentadas y las posibles exclusiones en ambas ejecuciones.

Pendiente para cierre: los cuatro reportes de Surefire ya verifican las 18 pruebas y cero incidencias. Queda por confirmar BUILD SUCCESS en la salida de mvnw.cmd -B clean verify y, después del push, comprobar el nuevo CI del PR #5. El umbral de JaCoCo no se modificó.
11. Activación automática del Skill quality-gates-java
Resultado: 2 de 2 activaciones automáticas demostradas. En dos conversaciones independientes, los prompts describían tareas de calidad sin mencionar el nombre ni la ruta del Skill. En ambas, Copilot seleccionó el Skill quality-gates-java mediante la herramienta Read skill quality-gates-java, sin que el prompt indicara explícitamente el Skill. Activación validada mediante captura de pantalla [V].
#	Objetivo de la conversación	Activación automática observada	Herramienta	Evidencia	Revisión humana
1	Diagnóstico de regresiones de estilo, duplicación y cobertura (sin comandos, sin modificar archivos, sin commits).	Sí. La captura muestra el paso «invoked quality-gates-java skill» y la herramienta Read skill quality-gates-java antes de leer pom.xml y los reportes.	Read skill quality-gates-java	[skill-activacion-1.png](img/skill-activacion-1.png)	Validada: el prompt no nombra el Skill ni su ruta.
2	Diagnóstico de calidad para entrega (estilo, duplicación, errores potenciales, vulnerabilidades de dependencias y cobertura; sin comandos, sin modificar archivos, sin commits).	Sí. La captura muestra que Copilot decide cargar primero el Skill y ejecuta Read skill quality-gates-java antes de leer pom.xml, config/ y este informe.	Read skill quality-gates-java	[skill-activacion-2.png](img/skill-activacion-2.png)	Validada: el prompt no nombra el Skill ni su ruta.



Alcance y límites:
- La evidencia demuestra la activación del Skill, no la ejecución de los analizadores: ambas conversaciones se limitaron a herramientas de lectura.
- La lectura manual de SKILL.md (P2-01) es otra cosa y no se cuenta como activación automática.
- El resultado de Maven se documenta en las secciones 4.9 y 10.3.
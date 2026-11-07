# Prompt P2-02 — Diagnóstico inicial de calidad del microservicio

**Proyecto:** Sistema de Mesa de Ayuda (IDS)
**Microservicio:** `ms-usuarios`
**Práctica:** P2 — Calidad como código con instrucciones y skills
**Herramienta:** GitHub Copilot en modo Agent, Claude Sonnet 5.5 (modelo indicado por el desarrollador)
**Fecha de la interacción:** 6 de noviembre de 2026 (hora de Ciudad de México).

## Criterio de transcripción

El bloque siguiente contiene el mensaje del desarrollador tal como se recibió en la interacción. No se agregaron, reordenaron ni completaron piezas de contenido.

Por indicación del desarrollador, únicamente se restauró el formato Markdown de la tabla de ejemplo de la sección 6, que se recibió aplanada. Se identificaron sus cuatro columnas (Herramienta, Resultado inicial, Evidencia y Observación) y se conservaron el texto y los datos originales sin modificación. El resto del formato Markdown (por ejemplo, los saltos de línea entre algunas listas) no se reconstruyó y puede aparecer tal como se recibió. La comparación con el historial o exportación del chat sigue pendiente.

Los títulos y notas fuera del bloque son editoriales y no forman parte del prompt enviado.

## Prompt original

```text
# P2-02 — Diagnóstico inicial de calidad del microservicio

## 1. ROL
Actúa como un ingeniero de software senior especializado en Java 21, Spring Boot, análisis estático, seguridad de dependencias y aseguramiento de calidad mediante Maven.

Tu responsabilidad es analizar evidencia técnica, identificar hallazgos y proponer un plan de mejora, manteniendo una separación clara entre resultados comprobados, información pendiente y recomendaciones.

## 2. CONTEXTO
Estamos trabajando en la Práctica 2 de la beca IDS, denominada **Calidad como código con instrucciones y skills**, dentro del microservicio `ms-usuarios`.

Tecnologías:

- Java 21.
- Spring Boot 3.5.16.
- Maven.
- PostgreSQL.
- Flyway.
- GitHub Copilot.
Utiliza las siguientes personalizaciones:

- `.github/copilot-instructions.md`
- `.github/skills/quality-gates-java/SKILL.md`
- Las referencias de la skill.
Los reportes generados se encuentran, cuando están disponibles, en:

- `target/pmd.xml`
- `target/cpd.xml`
- `target/spotbugsXml.xml`
- `target/site/jacoco/jacoco.xml`
- `target/dependency-check-report.html`
- Los archivos de resultados de Checkstyle, si existen.
**Resultados iniciales obtenidos durante nuestras ejecuciones:**

- Checkstyle: 19 violaciones (8 MagicNumber, 7 LineLength y 4 MultipleStringLiterals).
- PMD: 0 violaciones reportadas.
- CPD: 0 duplicaciones reportadas.
- SpotBugs con FindSecBugs: 0 incidencias reportadas.
- JaCoCo: 71.8 % de cobertura de líneas; umbral mínimo de 70 %.
- OWASP Dependency-Check: 10 dependencias con alertas y 70 asociaciones con CVE. Esto no equivale necesariamente a 70 vulnerabilidades únicas.
- OWASP finalizó con BUILD FAILURE por vulnerabilidades que alcanzaron el umbral CVSS de 7.0.
- Durante la actualización de datos de OWASP 12.1.0 también ocurrieron errores por URL demasiado largas en la base de datos interna.
Estos resultados son una línea base proporcionada por el desarrollador y deben contrastarse con los archivos disponibles.

## 3. TAREA
Realiza un diagnóstico inicial de la calidad del microservicio.

Necesito que:

1. Revises los reportes disponibles y contrastes los resultados proporcionados.
2. Identifiques y clasifiques los hallazgos de cada herramienta.
3. Distingas problemas de código, alertas de dependencias y errores de configuración de herramientas.
4. Identifiques los archivos Java y las dependencias afectados.
5. Propongas la estructura inicial de `docs/QUALITY-REPORT.md`, con una comparación antes/después.
6. Propongas un plan de corrección por lotes pequeños, priorizando seguridad, mantenimiento y preservación del comportamiento.
7. Identifiques la información pendiente para completar la entrega.

## 4. RESTRICCIONES

- No modifiques archivos todavía.
- No ejecutes comandos de terminal.
- No actualices dependencias.
- No corrijas código Java.
- No desactives ni suprimas reglas de calidad.
- No inventes resultados ni vulnerabilidades.
- No supongas que una alerta de OWASP demuestra que el microservicio es explotable.
- No confundas cobertura de instrucciones con cobertura de líneas.
- No modifiques la documentación de la Práctica 1.
- No generes todavía `QUALITY-REPORT.md`.
Si un reporte no existe o no puedes consultarlo, indícalo explícitamente.

## 5. FORMATO DE SALIDA
Presenta tu respuesta en Markdown, con las siguientes secciones:

**A. Evidencia consultada:** archivos encontrados, archivos faltantes y resultados que pudiste verificar.

**B. Diagnóstico inicial:** tabla con herramienta, resultado reportado, evidencia y observaciones.

**C. Hallazgos relevantes:** problemas detectados, archivos afectados e impacto potencial.

**D. Propuesta de QUALITY-REPORT.md:** estructura de secciones y tabla antes/después, dejando pendiente lo que todavía no se conoce.

**E. Plan de corrección:** lotes pequeños, prioridad técnica y justificación.

**F. Verificaciones pendientes:** información necesaria para completar el diagnóstico y validar posteriormente las correcciones.

Evita explicaciones repetitivas y mantén un nivel técnico claro.

## 6. EJEMPLO
Ejemplo del formato esperado para una fila del diagnóstico:

| Herramienta | Resultado inicial | Evidencia | Observación |
|---|---|---|---|
| Checkstyle | 19 violaciones reportadas | Salida de Maven proporcionada por el desarrollador | Pendiente de corrección; no confundir con un error de compilación Java |

El ejemplo ilustra cómo separar un resultado reportado de otro comprobado directamente en un archivo.

No agregues resultados ficticios para completar tablas.

## 7. VERIFICACIÓN
Antes de entregar tu respuesta, comprueba que:

1. Identificaste las herramientas contempladas en la skill.
2. Cada resultado indica de dónde proviene su evidencia.
3. No presentaste resultados reportados como verificados directamente cuando no pudiste consultar sus archivos.
4. Diferenciaste dependencias con alertas, asociaciones con CVE y vulnerabilidades únicas.
5. Identificaste el cumplimiento inicial del 70 % de cobertura de líneas.
6. El plan de corrección no propone suprimir reglas ni modificar funcionalidades innecesariamente.
7. No realizaste cambios en archivos ni ejecutaste comandos.
Si encuentras información insuficiente para una conclusión, márcala como pendiente en lugar de suponerla.

Presenta primero el diagnóstico y espera mi aprobación antes de realizar cualquier modificación.
```

## Resultado observable (nota editorial)

Diagnóstico entregado sin modificar archivos ni ejecutar comandos. Los resultados y sus limitaciones se registran en la entrada P2-02 de `BITACORA-IA.md`.

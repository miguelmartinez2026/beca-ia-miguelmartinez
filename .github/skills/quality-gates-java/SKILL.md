---
name: quality-gates-java
description: Configura, ejecuta y analiza los controles de calidad del microservicio ms-usuarios con Maven, Checkstyle, PMD, CPD, SpotBugs, FindSecBugs, OWASP Dependency-Check y JaCoCo. Úsala para revisar código muerto, código duro, complejidad, duplicación, vulnerabilidades y cobertura.
---

# Quality Gates — MS-Usuarios

## Cuándo utilizar esta skill

- Antes de entregar una práctica o abrir un Pull Request.
- Al revisar la calidad del código Java.
- Cuando el build falle por un analizador de calidad.
- Después de realizar refactorizaciones.

## Procedimiento

1. Revisa `pom.xml` e identifica los plugins Maven existentes.
2. Consulta `references/maven-plugins.xml`, `references/checkstyle.xml` y `references/pmd-ruleset.xml`.
3. Propón los cambios necesarios para configurar los quality gates sin eliminar plugins existentes.
4. Espera aprobación antes de modificar `pom.xml` o los archivos de configuración.
5. Ejecuta `.\mvnw.cmd -B verify`.
6. Registra los hallazgos en una tabla con herramienta, regla, archivo, línea y severidad.
7. Prioriza las correcciones: vulnerabilidades, errores, código muerto, complejidad y estilo.
8. Propón correcciones en lotes de máximo cinco archivos.
9. Ejecuta nuevamente las pruebas y los analizadores después de cada lote.
10. Actualiza `docs/QUALITY-REPORT.md` con los resultados anteriores y posteriores.

## Reglas obligatorias

- No desactives reglas de análisis para conseguir un build exitoso.
- No agregues `@SuppressWarnings` únicamente para ocultar hallazgos.
- No modifiques el comportamiento de los endpoints existentes.
- No introduzcas dependencias nuevas sin autorización.
- No elimines pruebas existentes.
- Documenta los posibles falsos positivos y solicita aprobación antes de aplicar excepciones.
- Distingue los hallazgos reales de las recomendaciones generales.

## Criterios de terminado

La actividad se considera completada cuando:

1. Todos los quality gates configurados se ejecutan correctamente.
2. `.\mvnw.cmd -B verify` termina con `BUILD SUCCESS`.
3. JaCoCo confirma una cobertura de líneas igual o superior al 70 %.
4. No existen hallazgos pendientes que incumplan las reglas acordadas.
5. `docs/QUALITY-REPORT.md` documenta los resultados y las correcciones.
6. Las pruebas existentes continúan aprobadas.
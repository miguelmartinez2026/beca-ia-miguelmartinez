---
name: docker-springboot
description: Conteneriza ms-usuarios o revisa su Dockerfile, configuración de ejecución y healthcheck con Java 21.
---

1. Lee el POM y confirma el JAR ejecutable y los controles de Maven.
2. Adapta [la plantilla](references/Dockerfile.template) al nombre del artefacto, puerto y endpoint del servicio.
3. Usa Temurin 21 JDK para compilar y Temurin 21 JRE para ejecutar como app. Etiqueta la imagen con una versión explícita. Nunca uses latest.
4. Excluye .git y .env del contexto y evita copiar secretos. Inyecta DB_URL, DB_USERNAME, DB_PASSWORD y JWT_SECRET al arrancar.
5. Ejecuta el workflow de CI y registra los resultados de verify, build, whoami y health. Anota los hallazgos de hadolint, Trivy o revisión de IA.

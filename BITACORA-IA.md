# BITACORA-IA.md

**Proyecto:** Sistema de Mesa de Ayuda  
**Microservicio:** ms-usuarios  
**Herramienta asignada:** GitHub Copilot (Visual Studio Code)  
**Práctica actual:** P1 — Del requerimiento al código con prompts

## Práctica 1 — Registro de interacciones con IA

| # | Fecha | Herramienta | Tipo (prompt/skill/agente) | Objetivo | Prompt o archivo | Resultado | Qué ajusté y por qué |
|---|---|---|---|---|---|---|---|
| 1 | 03/11/2026 | GitHub Copilot (Chat) | Prompt | Analizar los requerimientos de ms-usuarios y generar historias de usuario con criterios de aceptación. | `.github/prompts/historias-usuario.prompt.md` | Se generaron 7 HU, escenarios Gherkin positivos y negativos, 7 RNF y 20 preguntas abiertas. | Identifiqué supuestos sobre registro público, asignación de roles y administración de cuentas que no estaban confirmados. Decidí solicitar una revisión antes de implementar. |
| 2 | 03/11/2026 | GitHub Copilot (Chat) | Prompt | Revisar críticamente las historias generadas y delimitar el alcance de la Práctica 1. | `.github/prompts/revision-requerimientos.md` | Se clasificaron las historias según su respaldo, se identificaron ambigüedades y se propuso un CRUD parcial. | Conservé las observaciones sobre supuestos, pero rechacé posponer Update y Delete porque la guía exige un CRUD completo en P1. |
 
## Estado de la práctica

**Interacciones registradas:** 2 de 4 mínimas.

**Documentación generada:**
- `docs/requerimientos.md` — versión preliminar.
- `.github/prompts/historias-usuario.prompt.md` — prompt inicial.
- `.github/prompts/revision-requerimientos.md` — prompt de revisión.

**Pendiente:** completar los requerimientos, diseñar el modelo de datos, generar Flyway V1, desarrollar el CRUD REST y ejecutar las pruebas automatizadas.

Las siguientes interacciones se incorporarán a la tabla cuando realmente se ejecuten.
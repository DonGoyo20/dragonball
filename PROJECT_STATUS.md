# PROJECT_STATUS

**Fase actual:** 0 — investigación y viabilidad (terminada, esperando aprobación)

## Entorno (sesión en la nube, 2026-10-11)
- Java 21 (OpenJDK), Gradle, Maven, Git, Node 22, Python 3. 4 núcleos, 15 GB RAM.
- Repo vacío, sin commits.
- **Red restringida**: bloqueados maven.neoforged.net, modrinth.com, api.github.com; Maven Central devolvió 429. Sin esto no se puede descargar NeoForge/Minecraft en esta sesión.

## Decisiones aprobadas
- Ninguna todavía.

## Decisiones pendientes de aprobar
1. Versión de Minecraft + plataforma (ver recomendación en el informe).
2. Estrategia: mod independiente vs. ampliar DMZ.
3. Dónde se desarrolla: esta sesión en la nube (compila pero no ejecuta el juego con pantalla) o tu PC.

## Problemas conocidos
- No se pudo leer código ni páginas oficiales de los mods de referencia.

## Próxima tarea
- Tras la aprobación: Fase 1 (mod mínimo que compile y arranque).

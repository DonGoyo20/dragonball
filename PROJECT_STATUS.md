# PROJECT_STATUS

**Fase actual:** 0 completada. Trabajo sobre el papel mientras no hay PC disponible.

## Entorno (sesión en la nube, 2026-10-11)
- Java 21, Gradle, Maven, Git, Node 22, Python 3. 4 núcleos, 15 GB RAM.
- **Red restringida:** bloqueados maven.neoforged.net, modrinth.com, api.github.com; Maven Central dio 429. No se puede descargar NeoForge/Minecraft en esta sesión hasta ampliar la red.

## Decisiones
- **Aprobadas por el usuario:** trabajar sobre el papel hasta tener PC; enfoque de datos JSON para contenido; separar escala de poder (NP) del combate por razón de poder; mundo = Overworld con localizaciones especiales; muerte → Otro Mundo con temporizador y sin pérdida de zeni; sin fatiga, entrenamiento con penalizaciones que dan más XP; niveles bloqueados por evoluciones/formas de cada raza; esferas del dragón con deseos propios y limitados; Otro Mundo con espera mínima antes de poder reaparecer (no te expulsa, para fomentar la exploración).
- **Propuestas, sin confirmar explícitamente:** Minecraft 1.21.1 + NeoForge + Java 21; mod independiente (no ampliar DragonMine Z); no depender de un mod RPG externo para el núcleo; licencia por decidir.

## Documentos
GDD.md (diseño), WORLD_DESIGN.md (mundo, juego libre, niveles), RACES.md (razas en detalle), SYSTEMS.md (reglas base: niveles, ki, combate, comida, entrenamiento), TECHNICAL_DESIGN.md (arquitectura), ROADMAP.md, REFERENCES.md (investigación).

## Problemas conocidos
- Investigación basada solo en búsquedas; no se leyó código de otros mods.
- Fórmulas de escala sin ajustar (solo simulación numérica básica). Corregido: el multiplicador por etapa daba un salto ×207 entre niveles; ahora el poder crece +21,6 % por nivel.
- Nombres de APIs de NeoForge de memoria; a verificar en Fase 1.

## Pruebas pendientes
Todas las del juego (nada implementado todavía).

## Próxima tarea
Revisar SYSTEMS.md (4 preguntas abiertas en §13); luego ajustar RACES.md a esos parámetros; completar formas de Majin y Namekiano; detallar hitos de desbloqueo de formas (Super Saiyajin primero).

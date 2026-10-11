# PROJECT_STATUS

**Fase actual:** 0 completada. Trabajo sobre el papel mientras no hay PC disponible.

## Entorno (sesión en la nube, 2026-10-11)
- Java 21, Gradle, Maven, Git, Node 22, Python 3. 4 núcleos, 15 GB RAM.
- **Red restringida:** bloqueados maven.neoforged.net, modrinth.com, api.github.com; Maven Central dio 429. No se puede descargar NeoForge/Minecraft en esta sesión hasta ampliar la red.

## Decisiones
- **Aprobadas por el usuario:** trabajar sobre el papel hasta tener PC; enfoque de datos JSON para contenido; separar escala de poder (NP) del combate por razón de poder; mundo = Overworld con localizaciones especiales; muerte → Otro Mundo con temporizador y sin pérdida de zeni; sin fatiga, entrenamiento con penalizaciones que dan más XP; niveles bloqueados por evoluciones/formas de cada raza; esferas del dragón con deseos propios y limitados; Otro Mundo con espera mínima antes de poder reaparecer (no te expulsa, para fomentar la exploración).
- **Aprobadas (SYSTEMS.md v2):** cansancio = agotamiento por quedarse sin ki; ki = barra; vida como barra en porcentaje; XP fija por enemigo creciente con la dificultad; ritmo de progreso lento.
- **Referencia de diseño del usuario:** experiencia tipo Elden Ring / Breath of the Wild: historia como columna vertebral + muchos jefes secretos y rutas de personajes con recompensas muy buenas. Vuelo con coste de ki mínimo; XP 50/30/20 (combate/misiones/entrenamiento).
- **Alcance (usuario):** beta = Saga Saiyan hasta Vegeta (niveles 1–35); 1.0 = todo Z. Sin marcadores: diario de ruta (solo palabras del NPC) + mapa del explorador que se dibuja al recorrer. Rutas de personaje = misiones de NPC.
- **Sistema de niveles (usuario, 3.ª ronda):** estilo Elden Ring / Dragon Block C: la XP es una moneda que se gasta en el atributo elegido; nivel total = 1 + mejoras; tope de la beta aún sin decidir; el mapa muestra tu posición.
- **Propuestas, sin confirmar explícitamente:** Minecraft 1.21.1 + NeoForge + Java 21; mod independiente (no ampliar DragonMine Z); no depender de un mod RPG externo para el núcleo; licencia por decidir.

## Documentos
GDD.md (diseño), WORLD_DESIGN.md (mundo, juego libre, niveles), RACES.md (razas en detalle), SYSTEMS.md (reglas base: niveles, ki, combate, comida, entrenamiento), CONTENT_DESIGN.md (exploración, jefes secretos, rutas de personaje), TECHNICAL_DESIGN.md (arquitectura), ROADMAP.md, REFERENCES.md (investigación).

## Problemas conocidos
- Investigación basada solo en búsquedas; no se leyó código de otros mods.
- Fórmulas de escala sin ajustar (solo simulación numérica básica). Corregido: el multiplicador por etapa daba un salto ×207 entre niveles; ahora el poder crece +21,6 % por nivel.
- Nombres de APIs de NeoForge de memoria; a verificar en Fase 1.

## Pruebas pendientes
Todas las del juego (nada implementado todavía).

## Próxima tarea
Resolver las preguntas de SYSTEMS.md §16 y CONTENT_DESIGN.md §10; ajustar RACES.md a esos parámetros (XP de raza, agotamiento); completar formas de Majin y Namekiano; detallar el Kaio-ken (puerta 1, entra en la beta) y después el Super Saiyajin (puerta 2).

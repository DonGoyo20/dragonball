# PROJECT_STATUS

**Fase actual:** 0 completada. Trabajo sobre el papel mientras no hay PC disponible.

## Entorno (sesión en la nube, 2026-10-11)
- Java 21, Gradle, Maven, Git, Node 22, Python 3. 4 núcleos, 15 GB RAM.
- **Red restringida:** bloqueados maven.neoforged.net, modrinth.com, api.github.com; Maven Central dio 429. No se puede descargar NeoForge/Minecraft en esta sesión hasta ampliar la red.

## Decisiones
- **Aprobadas por el usuario:** trabajar sobre el papel hasta tener PC; enfoque de datos JSON para contenido; separar escala de poder (NP) del combate por razón de poder; mundo = Overworld con localizaciones especiales; muerte → Otro Mundo con temporizador y sin pérdida de zeni; sin fatiga, entrenamiento con penalizaciones que dan más XP; niveles bloqueados por evoluciones/formas de cada raza; esferas del dragón con deseos propios y limitados; Otro Mundo con espera mínima antes de poder reaparecer (no te expulsa, para fomentar la exploración).
- **Aprobadas (SYSTEMS.md v2):** cansancio = agotamiento por quedarse sin ki; ki = barra; vida como barra en porcentaje; XP fija por enemigo creciente con la dificultad; ritmo de progreso lento.
- **Referencia de diseño del usuario:** experiencia tipo Elden Ring / Breath of the Wild: historia como columna vertebral + muchos jefes secretos y rutas de personajes con recompensas muy buenas. Vuelo con coste de ki mínimo; XP 50/30/20 (combate/misiones/entrenamiento).
- **Alcance (usuario):** beta = Saga Saiyan hasta Vegeta (la beta termina al derrotarlo; nivel total efectivo ~171); 1.0 = todo Z. Sin marcadores: diario de ruta (solo palabras del NPC) + mapa del explorador que se dibuja al recorrer. Rutas de personaje = misiones de NPC.
- **Sistema de niveles (usuario, 3.ª ronda):** estilo Elden Ring / Dragon Block C: la XP es una moneda que se gasta en el atributo elegido; nivel total = 1 + mejoras; el mapa muestra tu posición.
- **Decisiones del usuario (4.ª ronda):** coste de mejora igual para todos los atributos; tope de 99 por atributo; la beta termina al derrotar a Vegeta; toda habilidad exige atributos mínimos (Kamehameha → control de ki, Kaio-ken → resistencia).
- **Decisiones del usuario (5.ª ronda):** Kaio-ken hasta "×3" con nombres no literales; requisitos siempre visibles; las habilidades se compran con XP (XP = moneda del juego) y los atributos se exigen para usarlas; Vitalidad = resistencia.
- **Decisiones del usuario (6.ª ronda):** aceptadas las cinco propuestas previas a Saiyajin/Humano (origen, combate en tiempo real con teclas, creación limitada, topes de Zenkai y Aprendiz nato, simulador).
- **Decisiones del usuario (7.ª ronda):** el Humano no se compensa; los especialistas pueden superar al equilibrado; garantía de historia (cualquier build debe poder avanzar, medida en el simulador); al Otro Mundo solo se va muriendo; teletransportación a NPC después de la beta.
- **Propuestas, sin confirmar explícitamente:** Minecraft 1.21.1 + NeoForge + Java 21; mod independiente (no ampliar DragonMine Z); no depender de un mod RPG externo para el núcleo; licencia por decidir.

## Documentos
GDD.md (diseño), WORLD_DESIGN.md (mundo, juego libre, niveles), RACES.md (razas en detalle), SYSTEMS.md (reglas base: niveles, ki, combate, comida, entrenamiento), CONTENT_DESIGN.md (exploración, jefes secretos, rutas de personaje), ABILITIES.md (habilidades, formas y requisitos), BETA_RACES.md (Saiyajin y Humano en la beta), tools/simulator (simulador de equilibrio en Python), TECHNICAL_DESIGN.md (arquitectura), ROADMAP.md, REFERENCES.md (investigación).

## Problemas conocidos
- Investigación basada solo en búsquedas; no se leyó código de otros mods.
- Simulador: el equilibrado dominaba sin técnicas de especialista (corregido en el diseño); el Humano rinde peor que el Saiyajin en duelo (pregunta abierta). La primera versión del simulador tenía un sesgo de orden, ya corregido y con pruebas.
- Fórmulas de escala sin ajustar (solo simulación numérica básica). Corregido: el multiplicador por etapa daba un salto ×207 entre niveles; ahora el poder crece +21,6 % por nivel.
- Nombres de APIs de NeoForge de memoria; a verificar en Fase 1.

## Pruebas pendientes
Todas las del juego (nada implementado todavía).

## Próxima tarea
Siguiente decisión pendiente: qué diseñar a continuación (maestros de la beta, bestiario de la beta con niveles y XP, o primera cadena de misiones de Roshi); completar formas de Majin y Namekiano; detallar el Kaio-ken (puerta 1, entra en la beta) y después el Super Saiyajin (puerta 2).

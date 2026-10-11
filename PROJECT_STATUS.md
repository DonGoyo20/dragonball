# PROJECT_STATUS

**Fase actual:** Fase 1 en curso: proyecto NeoForge 1.21.1 compilando, con progresión, bestiario por datos y un enemigo GeckoLib. El resto del diseño sigue **solo diseñado**.

## Estado de implementación (2026-10-11)
| Sistema | Diseñado | Implementado | Compilado | Probado |
|---|---|---|---|---|
| XP, mejoras, nivel total, logros de nivel | sí | sí | sí | pruebas automáticas (JUnit + GameTest) |
| Bestiario como Data Map (34 entradas exportadas de 94) | sí | parcial | sí | GameTest |
| Lobo salvaje (GeckoLib) | sí | sí | sí | verificado en juego por el usuario: se mueve, ataca, suelta objetos, da XP |
| FTB Quests, Patchouli, KubeJS | sí | solo como datos | sí | Patchouli y FTB Quests verificados en juego (libro abre; 3 misiones completadas); KubeJS verificado en juego (araña 10→~50 XP) |
| Atributos con efecto en combate, ki, habilidades, razas, Kaio-ken, maestros, Otro Mundo | sí | **no** | — | — |
Ver `INTEGRATIONS.md`.

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
- **Decisiones del usuario (8.ª ronda):** los mobs de Minecraft con nivel fijo (una vaca o un cerdo mueren casi de un golpe al inicio); jefes vanilla se quedan pero se quitan si complican el código; Saibamen en grupos de 3. Alcance de la beta ampliado: **Dragon Ball clásico hasta la saga Saiyan** (Pilaf, Red Ribbon, luchadores de Baba, hijos de Piccolo Daimaō, saga Saiyan); fuera Freezer, Cell, Buu, GT, Super.
- **Propuestas, sin confirmar explícitamente:** Minecraft 1.21.1 + NeoForge + Java 21; mod independiente (no ampliar DragonMine Z); no depender de un mod RPG externo para el núcleo; licencia por decidir.

## Auditoría de atributos (2026-10-11)
Completada: ver `ATTRIBUTE_AUDIT.md`. Hay 7 atributos (no 5), sin ningún efecto en combate todavía, sin HUD, tope 99 definido pero no aplicado y sin razas en código. **Siguiente paso:** implementación de efectos de los atributos en combate, tras resolver las preguntas abiertas (§G de la auditoría).

## Documentos
GDD.md (diseño), WORLD_DESIGN.md (mundo, juego libre, niveles), RACES.md (razas en detalle), SYSTEMS.md (reglas base: niveles, ki, combate, comida, entrenamiento), CONTENT_DESIGN.md (exploración, jefes secretos, rutas de personaje), ABILITIES.md (habilidades, formas y requisitos), BETA_RACES.md (Saiyajin y Humano en la beta), BESTIARY.md (enemigos y jefes de la beta; datos en design_data/beta/bestiary.json), BESTIARY_PLAN.md (análisis técnico y plan de implementación del bestiario), tools/simulator (simulador de equilibrio en Python), TECHNICAL_DESIGN.md (arquitectura), ROADMAP.md, REFERENCES.md (investigación).

## Problemas conocidos
- Investigación basada solo en búsquedas; no se leyó código de otros mods.
- Simulador: el equilibrado dominaba sin técnicas de especialista (corregido en el diseño); el Humano rinde peor que el Saiyajin en duelo (pregunta abierta). La primera versión del simulador tenía un sesgo de orden, ya corregido y con pruebas.
- Fórmulas de escala sin ajustar (solo simulación numérica básica). Corregido: el multiplicador por etapa daba un salto ×207 entre niveles; ahora el poder crece +21,6 % por nivel.
- Nombres de APIs de NeoForge de memoria; a verificar en Fase 1.

## Pruebas pendientes
y todo lo no implementado. Lo visual se prueba en el PC del usuario.

## Próxima tarea
Aprobaciones de BESTIARY_PLAN.md §5 (plataforma, red/PC, arte, alcance por etapas) para empezar la Fase 1; preguntas de BESTIARY.md §15; completar formas de Majin y Namekiano; detallar el Kaio-ken (puerta 1, entra en la beta) y después el Super Saiyajin (puerta 2).

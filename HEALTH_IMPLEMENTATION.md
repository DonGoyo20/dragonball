# HEALTH_IMPLEMENTATION — Ciclo VIT → salud (2026-10-11; fórmula cúbica 2026-10-12)

## Qué se implementó
Primer atributo funcional: VIT reduce el daño que reciben los jugadores. Diseño y APIs: `HEALTH_FEASIBILITY.md`; fórmula y deuda: `SYSTEMS.md` §3.2.1.
- `core/HealthSystem.java` — fórmulas puras: `vitValue`, `canUpgradeVit`, `vidaRealMaxima`, `vidaRealActual`, `danioAplicado`, `esDanioDeEntorno(boolean)`. Valida VIT en [8, 99].
- `game/health/HealthService.java` — puente con `ServerPlayer`; `esDanioDeEntorno(DamageSource)` (entorno = sin entidad atacante ni directa).
- `game/health/HealthEvents.java` — `LivingDamageEvent.Pre`.
- `PlayerProgress.isAtCap` / `tryUpgrade` — rechaza la mejora de VIT 92 (valor 99). `/dragonball upgrade vit` muestra un mensaje de tope.
- Traducciones `attribute.dragonball.<id>` (7 atributos) y `command.dragonball.attribute_at_cap` en `en_us` y `es_es`, `es_mx`, `es_ar`, `es_cl`, `es_uy`, `es_ve`, `es_ec`; los mensajes de `upgrade` usan ahora el nombre traducido.

## Desviaciones respecto al encargo (y por qué)
1. `esDanioDeEntorno` en el núcleo recibe un `boolean` (hay atacante), no un `DamageSource`: `DamageSource` es de Minecraft y el núcleo debe probarse sin Minecraft. La clasificación real (caída, lava, ahogamiento, zombi) se prueba en GameTest.
2. El tope se aplica en `PlayerProgress.tryUpgrade` (donde vive), no en `ProgressService`. Solo existe para VIT: los demás atributos no tienen valores definidos todavía y no se tocan (fuera de alcance).
3. El test del tope en JUnit prueba la regla pura (`canUpgradeVit`, `vitValue`); `PlayerProgress` no se puede cargar en JUnit (depende de librerías de Minecraft), así que su `tryUpgrade` se verifica en GameTest.
4. Decisión 5 ("al subir VIT… se cura proporcionalmente"): implementado como fracción constante. La barra vanilla no cambia; la vida real sube en proporción. Con la barra a 20/20 sigue a 20/20 y la vida real pasa a ser el nuevo máximo.
5. Se guardan fuera de la fórmula: un guardado con más de 91 mejoras de VIT se trata como el máximo (no lanza excepción dentro del evento de daño).
6. En GameTest hubo que pasar el jugador simulado a supervivencia y avanzar 61 ticks (invulnerabilidad de aparición de 60 ticks de `ServerPlayer`); sin eso ningún daño surte efecto.

## Resultados (ejecutados, 2026-10-11)
- `./gradlew build`: BUILD SUCCESSFUL; JUnit 12/12 (5 anteriores + 7 de `HealthSystemTest`).
- `./gradlew runGameTestServer`: "All 13 required tests passed" (6 anteriores + 7 de `HealthGameTests`).
- `tools/verify_integrations.sh`: núcleo 13, FTB 2, KubeJS 1, todas las comprobaciones de log OK.
GameTests nuevos: zombi con VIT base → 17,0 HP; con VIT 18 → 18,667 HP; caída no escala; zombi no es entorno; `/kill` mata con VIT 99 sin NaN; subir VIT no cambia la barra y escala la vida real; el tope rechaza la mejora 92 y no afecta a otros atributos.

## Estado
| Parte | Estado |
|---|---|
| Fórmulas y regla de tope | implementado, compilado, probado (JUnit) |
| Evento de daño y tope en `tryUpgrade` | implementado, compilado, probado (GameTest con jugador simulado) |
| Traducciones | implementado; nombres de atributo no verificados en pantalla |
| Comportamiento en el juego real (zombi ~1,5 corazones, menos con VIT) | **no verificado en juego** |
| HUD, sincronización al cliente, DEF, daño por nivel del enemigo | no implementado (fuera de alcance) |

## Verificación manual
Ver el final de la respuesta del ciclo / README: survival, `/dragonball progress`, dejarse pegar por un zombi, `/dragonball givexp 500` + `/dragonball upgrade vit` ×10, repetir.

## Cambio 2026-10-12: fórmula cúbica
Tras probarlo en el juego, el usuario vio que la fórmula lineal era demasiado suave (zombi con VIT 18: medio corazón; creeper a bocajarro: 4,5 corazones). Decisión confirmada: `daño_aplicado = daño_vanilla × (8 / VIT)^3`.
- `HealthSystem.danioAplicado` usa `Math.pow(VIT_BASE / vit, DANIO_EXPONENTE)` con `DANIO_EXPONENTE = 3`. JavaDoc con la decisión y la deuda técnica.
- Creeper: **comportamiento esperado, no un fallo.** Su explosión llega a `LivingDamageEvent.Pre` con el creeper como entidad causante, y ya se escalaba con la fórmula lineal (20,25 × 8/18 = 9 HP).
- Clasificación de entorno afinada: hay "atacante" si `source.getEntity()` es una `LivingEntity` y la entidad directa no es TNT (`PrimedTnt`). Zombi, flecha de esqueleto y creeper se escalan; caída, lava, ahogamiento, TNT (aunque la encienda una criatura o un jugador) y `/kill` no.
- Tests: JUnit con 18 → 0,2634, 30 → 0,0569, creeper 20,25 → 1,778 y comprobación de que el cubo es siempre más fuerte que el lineal. GameTest: zombi VIT 8 → 17,0; VIT 18 → 19,74 (menos de 0,3 corazones); creeper VIT 8 muere/borde, VIT 18 → 18,22; TNT sin escalar.
- **Los GameTest de explosión usan `DamageSource` construidos como en vanilla, no explosiones reales**: una explosión real daba solo ~1 HP a 4 bloques en el recinto de pruebas (depende de la exposición medida contra bloques), lo que hacía la prueba frágil. El comportamiento con un creeper real queda para tu verificación manual. Con PvP desactivado el servidor de pruebas ignora el daño entre jugadores, por eso el dueño de la TNT de prueba es un zombi.

## Cambio 2026-10-12 (2): tres categorías de daño
Decisión del usuario: todo el daño escala con VIT salvo el vacío y `/kill`, y el daño de estado continuo tiene un suelo de 1 HP. Sustituye a "el entorno no escala" (la TNT, la caída, etc. ahora escalan).
- `core/TipoDanio` (nuevo): `ESCALA_SIN_SUELO`, `ESCALA_CON_SUELO`, `NO_ESCALA`.
- `HealthSystem.danioAplicado(daño, vit, tipo)`: cúbica para los dos primeros; suelo `SUELO_CONTINUO = 1,0` solo con suelo, limitado al daño original (`min(original, max(escalado, 1))`, para que el suelo nunca aumente un daño menor que 1); `NO_ESCALA` devuelve el original. Se eliminó `esDanioDeEntorno`.
- `HealthService.clasificar(DamageSource)` usa `DamageTypes` de Minecraft (`source.is(...)`): NO_ESCALA = `FELL_OUT_OF_WORLD`, `GENERIC_KILL`; ESCALA_CON_SUELO = `LAVA, IN_FIRE, ON_FIRE, HOT_FLOOR, CAMPFIRE, DROWN, CACTUS, SWEET_BERRY_BUSH, FREEZE, WITHER, MAGIC, DRAGON_BREATH, DRY_OUT, STARVE, IN_WALL, CRAMMING, OUTSIDE_BORDER`; el resto, ESCALA_SIN_SUELO. `HealthEvents` ignora NO_ESCALA y escala el resto. JavaDoc con el porqué del vacío y /kill.
- Matices: el veneno es el tipo `magic`, igual que las pociones de daño instantáneo (reciben el mismo trato, con suelo). "1 HP por tick" se entiende **por aplicación de daño**: vanilla aplica el daño continuo cada pocos ticks por la invulnerabilidad tras un golpe. El vacío de vanilla hace 4 HP por golpe, así que "muerte instantánea" no es literal: sigue matando por golpes repetidos a cualquier VIT.
- Tests nuevos/actualizados — JUnit (14 en total): sin suelo con VIT 8/18/26/30/99 y caída de 20 bloques (17 × (8/26)^3 = 0,495), monotonía, suelo (4 → 1,0 con VIT 26 y 99; sin cambio con VIT 8; nunca sube un daño menor que 1), sin escala (cualquier VIT), VIT fuera de rango. GameTest (17 en total): clasificación de 14 `DamageSource`, caída de 20 bloques real (`causeFallDamage`) con VIT 8 → vida 3,0 y VIT 26 → 19,5; lava con VIT 8/26/99 → 16/19/19; TNT con VIT 8/26; vacío con VIT 8/99 → 16 en ambos; `/kill` mata con VIT 8 y 99; creeper.
- Resultados ejecutados (2026-10-12): `./gradlew build runGameTestServer`: BUILD SUCCESSFUL, JUnit 14/14, "All 17 required tests passed". **No verificado en el juego real.**

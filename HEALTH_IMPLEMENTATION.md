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

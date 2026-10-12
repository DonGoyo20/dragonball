# KI_IMPLEMENTATION — Recurso Ki, liberación, HUD y pantalla de estadísticas (2026-10-12)

## Qué hay
| Parte | Archivos | Estado |
|---|---|---|
| Fórmulas puras | `core/KiSystem`, `core/KiParams`, `core/ReleaseParams`, ampliación de `core/BalanceParams`, `HealthSystem.danioAplicadoEfectivo`, `FuerzaSystem.danioSalienteEfectivo` | implementado, compilado, probado (JUnit) |
| Estado y guardado | `game/ki/KiData`, `PlayerProgress` (+`ki`), `ModAttachments` (`.sync`) | implementado, probado (GameTest) |
| Servidor | `game/ki/KiService`, `KiEvents`, `game/network/*` (2 paquetes), comandos `/dragonball ki` y `releaselimit` | implementado, probado (GameTest) |
| Cliente | `client/KeyBindings`, `ClientEvents`, `client/hud/PlayerHud`, `client/screen/StatsScreen` | implementado; comprobado en un **cliente real con pantalla virtual** (capturas en `docs/img/`); **no en tu PC** |
| Traducciones | 8 idiomas (`en_us`, `es_es`, `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy`, `es_ve`) | añadidas; solo se vio español (es_es) en pantalla |

## Reglas (decisiones del usuario)
- **Reserva** `50 + 10 × KI` (KI 0 → 50; KI 18 → 230). Empieza llena. No cambia con la liberación.
- **Liberación** 0–100 %, límite inicial 50 % (la habilidad "Desbloqueo de Potencial" de Kami-sama lo subirá +5 % por nivel; aún no existe).
- **Gasto** por tick mientras la liberación > 0: `base × (liberación/100) / (1 + CON/60)`.
- **Regeneración** solo con la liberación por debajo del 50 % (máxima a 0 %, lineal entre medias); usa KI, no la liberación.
- **Sin ki** → la liberación baja a 0.
- **Teclas** C (mantener) sube —solo quieto—, X (mantener) baja, H oculta/muestra el HUD, V abre/cierra la pantalla de estadísticas. Se cambian en Opciones → Controles.
- **Estadística efectiva** `base × liberación/100` (se muestra en la pantalla). VIT, FUE y VEL la usan; KI, POD y CON no. Vida máxima y ki máximo son fijos.

## Números que NO eran del diseño (míos, provisionales, en `data/dragonball/balance/combat.json`)
| Qué | Valor | Por qué |
|---|---|---|
| Gasto base a 100 % con CON 0 | 0,2 ki/s | Con el 50 % gastas 0,1 ki/s: la reserva inicial (50) dura ~8 min. A ≤ ~45 % la regeneración compensa el gasto |
| Regeneración a 0 % | 2 % de la reserva por segundo (1 ki/s con KI 0) | Se llena en ~50 s |
| Subida / bajada de la liberación | +10 % / −20 % por segundo | El diseño no daba velocidades |
| "Quieto" | se movió < 0,05 bloques en los últimos 5 ticks y no está dando golpes | |
| Base y crecimiento | KI 0 (+1), CON 0 (+1), VEL 8 (+1) | Con KI 0 la reserva es 50, como pediste; VEL como VIT y FUE |
| VEL → movimiento / salto | `clamp((VEL efectiva/8)^0,5, 0,5, 2,5)` y `clamp(...^0,25, 0,5, 1,8)` | El diseño no daba fórmula; se aplica con modificadores transitorios |
| VEL solo al correr (2026-10-12) | el bonus de velocidad **y de salto** solo existe mientras el jugador corre (doble W / Ctrl); caminando o parado es vanilla | Petición del usuario. Se reaplica cada tick en `KiService.updateMovement` |
| FOV | `client/VelFovFix` deshace el bonus de VEL en el cálculo del FOV (`ComputeFovModifierEvent`, fórmula en `KiSystem.correccionFov`) | El bonus es un modificador de la velocidad y estiraba la cámara como una poción |
| Corazones de vanilla | ocultos (capa `PLAYER_HEALTH`) mientras el HUD del mod esté visible (H los devuelve) | Petición del usuario |
| Liberación inicial | 50 % (= límite) | Para que un personaje nuevo se comporte como en lo ya verificado |
| Efecto de la liberación en VIT/FUE/VEL | función a trozos (ver abajo); **sin castigo** por falta de ki | Decisión del usuario (2026-10-12, 3.ª versión) |

## Cómo se aplica la liberación (función a trozos, 2026-10-12, 3.ª versión)
Sustituye a las dos versiones anteriores (`base × lib/100`, luego `max(0, lib/50)` con castigo ×4 / ×0,25). Modelo del usuario: a 0 % el jugador es vanilla (atributos 8); de 0 a 50 % sube linealmente hasta los atributos del mod; a 50 % tiene los del mod; de 50 a 100 % se multiplican hasta el doble.

```
liberación <= 50:  efectiva = 8 + (base - 8) × (liberación / 50)
liberación >  50:  efectiva = base × (liberación / 50)
```
Es continua en 50 % (ambas ramas dan `base`). El 8 es el valor vanilla de cada atributo (`vit_base`, `fue_base`, `vel_base`) y el 50 es `release.reference_percent`.

| Liberación | base 18 | base 8 (personaje nuevo) |
|---|---|---|
| 0 % | 8 | 8 |
| 25 % | 13 | 8 |
| 50 % | 18 | 8 |
| 75 % | 27 | 12 |
| 100 % | 36 | 16 |

- VIT, FUE y VEL usan esa estadística efectiva en sus fórmulas de siempre: `(8/VIT_ef)^3` de daño recibido, `(FUE_ef/8)^3` de daño hecho, `clamp((VEL_ef/8)^exp)` de movimiento y salto. La pantalla de estadísticas muestra el mismo valor efectivo.
- **0 %**: daño y movimiento de vanilla (VIT 8, FUE 8, VEL 8), para cualquier base. **Sin castigo**: no hay ×4 recibido, ×0,25 hecho ni velocidad ×0,5.
- **Sin ki** la liberación baja a 0 (regla de siempre) y el jugador queda como vanilla, nunca peor.
- Eliminado: el factor `max(0, lib/50)` y los parámetros `max_damage_taken_factor` y `min_damage_dealt_factor` de `combat.json` (un archivo antiguo que aún los tenga los ignora).
- El vacío y `/kill` siguen sin escalar; el daño continuo respeta su suelo de 0,5 HP.

**Avisos en la barra de acción** (mensajes traducidos en los 8 idiomas):
- "¡Sin Ki! Tu liberación vuelve a 0 %" (solo informativo): cuando la liberación llega a 0 **porque se acabó el ki**. Si el jugador la baja a 0 con X teniendo ki, no sale (sería falso decir "sin ki").
- "Debes estar quieto para liberar Ki": cuando mantiene C, se está moviendo y la liberación aún no ha llegado al límite. Como máximo una vez cada 2 segundos (40 ticks) por jugador.

## HUD (esquina superior izquierda)
Capas de `RegisterGuiLayersEvent.registerAboveAll`. Origen (6, 6) en píxeles de GUI.
- **Barra de Vida:** 110 × 11 px, marco negro 1 px, fondo `#101010` con transparencia, relleno verde `#3FD13F`, texto blanco con sombra "HP X / Y" (vida real: `hp vanilla/20 × vida real máxima`).
- **Barra de Ki:** misma medida, 3 px más abajo, relleno cyan `#19D3F3`, texto "Ki X / Y".
- **Línea de liberación** (añadida por mí; el diseño pedía solo dos barras): texto gris "Liberación X % (límite Y %)" debajo, **solo mientras la liberación es mayor que 0 %**; en reposo se oculta.
- Se oculta con H o con F1; no sustituye los corazones de vanilla.
Capturas: `docs/img/ki_hud.png`.

## Pantalla de estadísticas (tecla V)
Ancho 280 px, centrada, fondo difuminado de vanilla. De arriba abajo: título "Estadísticas"; Raza ("sin elegir todavía": las razas no existen aún); Nivel total; XP actual y coste de la siguiente mejora; mejoras que puedes comprar ahora (compras seguidas con el coste creciente); Coste de la siguiente mejora; Liberación actual y límite; tabla de 7 filas: **Atributo | Base | Efectiva | Mejoras**; pie con el comando para comprar y cómo cerrar. DEF y POD muestran "sin efecto aún" (no tienen valores definidos). No permite comprar con el ratón: se compra con `/dragonball upgrade <atributo>`. Se cierra con V o Esc. Captura: `docs/img/ki_stats_screen.png`.

## Sincronización
- `PlayerProgress` (incluido el ki) se sincroniza con `AttachmentType.Builder.sync(StreamCodec)` solo al propio jugador. El códec de red es el mismo `Codec` que se guarda, convertido a NBT.
- **Desviación:** el predicado también exige que la conexión del jugador conozca el paquete de sincronización (`hasChannel`). Los jugadores simulados de las pruebas automáticas no negocian canales y NeoForge lanza una excepción al enviarles datos; sin el predicado fallaban 40 pruebas.
- El ki se escribe cada 5 ticks y solo si cambió (≈ 4 paquetes/s mientras gasta o regenera, 0 si está quieto y lleno).
- `BalanceSyncPayload` (servidor → cliente) envía el balance vigente al entrar y tras `/reload`, para que el HUD muestre máximos correctos aunque un datapack los cambie. `ReleaseInputPayload` (cliente → servidor) envía la tecla mantenida solo cuando cambia; el servidor recorta el valor a −1/0/1.
- Servidor autoritativo: el cliente solo dice qué tecla mantiene; el servidor decide si el jugador está quieto, cuánto sube y cuánto gasta.

## Compatibilidad con partidas existentes
Los guardados anteriores no tienen el campo `ki`: se cargan con el ki inicial (50 de 50, liberación 50 %, límite 50 %). Un guardado dañado se recorta a rangos válidos. Comprar mejoras o ganar XP no reinicia el ki (probado).

## Resultados (ejecutados, 2026-10-12, tras la función a trozos)
- JUnit **43/43** (`./gradlew test`). Nuevos: base 18 (0/25/50/75/100 % → 8/13/18/27/36), base 8 (8/8/8/16), continuidad en 50 % y monotonía, entradas inválidas, sin ki = vanilla sin castigo, 50 % = balance ya verificado, 100 % = doble, interpolación 0–50 %, VEL a 0 % = ×1. Eliminados los tests del castigo y del factor.
- GameTest **52/52** (`./gradlew runGameTestServer`): daño recibido al 0 % = ×1 (antes ×4), daño hecho al 0 % = ×1 (antes ×0,25), sin ki con VIT 18 / FUE 18 = vanilla, VEL 18 al 0 % = sin modificador de velocidad ni salto.
- Cambios posteriores (VEL solo al correr, FOV, corazones): JUnit **44/44** (+`correccionFov`), GameTest **52/52** (`velOnlyAffectsMovementWhileSprinting` sustituye al anterior). **Corazones y FOV solo compilados; no vistos en pantalla.**
- **No verificado en el PC del usuario** ni en pantalla. No se ejecutó `tools/verify_integrations.sh` en este ciclo.

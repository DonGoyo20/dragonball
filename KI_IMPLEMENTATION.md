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
| Liberación inicial | 50 % (= límite) | Para que un personaje nuevo se comporte como en lo ya verificado |
| Castigo sin ki / liberación baja | recibido ×4 (tope), hecho ×0,25 (suelo) | Decisión del usuario (2026-10-12) |

## Cómo se aplica la liberación al combate (ajustado el 2026-10-12, 2.ª versión)
La pantalla de estadísticas muestra la fórmula literal `base × liberación/100`. En combate y movimiento VIT, FUE y VEL usan el **factor de liberación** `max(0, liberación / 50)` (referencia 50 %):
- **0 %** → factor 0 → "indefenso".
- **50 %** (la inicial) → factor 1 → stats completas; todo igual que antes de existir la liberación (balance ya verificado).
- **100 %** → factor 2 → doble de stats (VIT 8 recibe ×1/8 de daño; FUE 8 hace ×8).
Antes el factor tenía un suelo de 0,5; se quitó (decisión del usuario).

**Castigo sin ki / liberación baja.** Con factor 0 las fórmulas cúbicas darían daño recibido infinito y daño hecho cero. Para que el castigo sea el pedido (**×4 recibido, ×0,25 hecho**) el multiplicador del daño recibido tiene un **tope ×4** (`max_damage_taken_factor`) y el del daño hecho un **suelo ×0,25** (`min_damage_dealt_factor`). Es mi interpretación de cómo unir "factor 0" con "×4 / ×0,25":
- Sin ki la liberación es 0, así que ese estado y "0 % con ki" dan el mismo castigo (×4 / ×0,25, para cualquier VIT y FUE: un VIT 26 sin ki recibe ×4, no ×0,03).
- Con liberación baja pero no 0 el tope también actúa: por debajo de ~31 % de liberación (factor < 0,63) el multiplicador teórico supera ×4 y se queda en ×4; por debajo de ~63 % de la liberación de referencia el daño hecho también está en el suelo cuando FUE = 8. A 40 % (factor 0,8): recibes ×1,95 y haces ×0,51.
- El vacío y `/kill` siguen sin escalar; el daño continuo respeta su suelo de 0,5 HP.
- El movimiento (VEL) con factor 0 queda en su suelo de 0,5 (la mitad de velocidad y salto); no se pidió cambiarlo.

**Avisos en la barra de acción** (mensajes traducidos en los 8 idiomas):
- "¡Sin Ki! Estás indefenso": cuando la liberación llega a 0 **porque se acabó el ki**. Si el jugador la baja a 0 con X teniendo ki, no sale (sería falso decir "sin ki").
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

## Resultados (ejecutados, 2026-10-12, tras los 4 ajustes)
- `./gradlew build`: BUILD SUCCESSFUL; **JUnit 41/41** (nuevos: factor 0 = castigo ×4/×0,25 para cualquier VIT/FUE, factor 1 = balance verificado, factor 2 = doble, zona intermedia con tope y suelo, validaciones).
- `./gradlew runGameTestServer`: **"All 52 required tests passed"** (KiGameTests 19). Nuevos o cambiados: daño recibido al 0 % = ×4 (antes ×8), daño hecho al 0 % = ×0,25, castigo sin ki con VIT 18 y FUE 18, aviso "Sin Ki" una sola vez y solo por agotamiento, aviso de estar quieto como máximo una vez cada 40 ticks (instantes 0, 10, 39, 40, 41, 79, 80 → 3 avisos) y solo si se intenta subir moviéndose y aún hay margen.
- **Cliente real con pantalla virtual** (captura `docs/img/ki_hud_adjustments.png`): con liberación 50 % se ve la tercera línea; tras mantener X hasta 0 % la línea desaparece y el ki se regenera a 50/50; mantener C mientras se camina muestra "Debes estar quieto para liberar Ki" en la barra de acción. **No se vio en pantalla** el aviso "¡Sin Ki!" (solo en pruebas automáticas, con un destino de mensajes de prueba).
- Ejecuciones anteriores (primera versión del ciclo): ver el historial del repositorio.

## Limitaciones y pendiente
- No hay técnicas, maestros ni la habilidad que sube el límite: el límite solo se cambia con `/dragonball releaselimit <0-100>` (operadores; para pruebas).
- DEF y POD no se implementan (CON solo abarata el gasto de la liberación).
- Vuelo, Kamehameha y demás técnicas siguen sin existir; ahora tendrán el ki para gastar.
- Los números de la tabla de arriba son provisionales y hay que ajustarlos jugando (datos, sin recompilar).
- El HUD no anima las barras (saltan); la pantalla no compra mejoras.

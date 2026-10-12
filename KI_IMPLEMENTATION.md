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

## Decisión importante que debes revisar: cómo se aplica la liberación al combate
La fórmula pedida `stat_efectiva = base × liberación/100` aplicada tal cual a las fórmulas de combate ya verificadas dejaba a un personaje **nuevo** (límite 50 %) con VIT efectiva 4 (recibe ×8 de daño) y FUE efectiva 4 (hace ×0,125 de daño). Para no cambiar el balance verificado, el combate y el movimiento usan el **factor de liberación** `max(0,5, liberación / 50)`:
- liberación 50 % (la inicial) → factor 1 → todo igual que antes de existir la liberación;
- 100 % → factor 2 (VIT 8 recibe ×1/8 de daño; FUE 8 hace ×8);
- 0 % → factor mínimo 0,5 (VIT recibe ×8; FUE hace ×0,125). El mínimo evita divisiones por cero.
La pantalla de estadísticas **sí** muestra la fórmula literal (`8 → 4,0` con 50 %), así que a un personaje nuevo le saldrá "Efectiva 4,0" aunque en combate cuente como 8. Si prefieres la fórmula literal en combate, es cambiar `reference_percent` y `min_factor` en los datos (con `reference_percent = 100` y `min_factor` bajo), pero el personaje inicial quedaría muy débil.

**Consecuencia de diseño a vigilar:** si el ki llega a 0 la liberación baja a 0 y el jugador se queda con el factor mínimo (recibe ×8 de daño y hace ×0,125) hasta que suba la liberación otra vez (solo quieto, 10 %/s). Con los números por defecto el ki dura ~8 min al 50 %, y bajando a ≤ 45 % con X el gasto queda cubierto por la regeneración.

## HUD (esquina superior izquierda)
Capas de `RegisterGuiLayersEvent.registerAboveAll`. Origen (6, 6) en píxeles de GUI.
- **Barra de Vida:** 110 × 11 px, marco negro 1 px, fondo `#101010` con transparencia, relleno verde `#3FD13F`, texto blanco con sombra "HP X / Y" (vida real: `hp vanilla/20 × vida real máxima`).
- **Barra de Ki:** misma medida, 3 px más abajo, relleno cyan `#19D3F3`, texto "Ki X / Y".
- **Línea de liberación** (añadida por mí; el diseño pedía solo dos barras): texto gris "Liberación X % (límite Y %)" debajo; sin ella no hay forma de ver qué hace la tecla C.
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

## Resultados (ejecutados, 2026-10-12)
- `./gradlew build`: BUILD SUCCESSFUL; JUnit 38/38 (KiSystemTest 11 nuevos, BalanceParamsTest +2).
- `./gradlew runGameTestServer`: "All 49 required tests passed" (KiGameTests 16 nuevas): ciclo de vida del ki con el evento real de tick, reserva, gasto, regeneración, agotamiento, límite y estar quieto, daño recibido y hecho con 0/50/100 %, vida y ki máximos fijos, modificadores de velocidad y salto, topes de VEL/KI/CON, códec de red, datos antiguos y dañados, conservación del ki al comprar mejoras.
- `tools/verify_integrations.sh`: núcleo 49, FTB 2, KubeJS 1 y todas las comprobaciones de log OK (la ejecución de KubeJS también carga el balance con los bloques nuevos).
- **Cliente real con pantalla virtual (software, sin audio; no es tu PC):** el HUD se dibuja (HP 100/100 verde, Ki cyan, línea de liberación) y recibe el límite 100 % sincronizado; V abre y cierra la pantalla de estadísticas; mantener C quieto subió la liberación de 50 % a 90 % en ~4 s; mantener X la bajó a 60 %; H oculta y vuelve a mostrar el HUD. **No probado:** la sensación de la velocidad/salto con VEL, el combate real con liberación distinta, las traducciones distintas de es_es, ni el comportamiento al moverse mientras se mantiene C.

## Limitaciones y pendiente
- No hay técnicas, maestros ni la habilidad que sube el límite: el límite solo se cambia con `/dragonball releaselimit <0-100>` (operadores; para pruebas).
- DEF y POD no se implementan (CON solo abarata el gasto de la liberación).
- Vuelo, Kamehameha y demás técnicas siguen sin existir; ahora tendrán el ki para gastar.
- Los números de la tabla de arriba son provisionales y hay que ajustarlos jugando (datos, sin recompilar).
- El HUD no anima las barras (saltan); la pantalla no compra mejoras.

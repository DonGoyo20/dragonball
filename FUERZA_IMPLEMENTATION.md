# FUERZA_IMPLEMENTATION — Ciclo FUE → daño cuerpo a cuerpo (2026-10-12)

## Qué se implementó
Segundo atributo funcional, con el mismo patrón que VIT. Fórmula y deuda técnica: `SYSTEMS.md` §3.2.2.
- `core/FuerzaSystem.java` — `FUE_BASE = 8`, `FUE_MAX = 99`, `fueValue`, `canUpgradeFue`, `multiplicadorFuerza(fue) = (fue/8)^3`, `danioSaliente`. Valida FUE en [8, 99]. Sin tope de daño.
- `core/DanioCuerpo.java` — enum `PUNO`, `ARMA_DEL_MOD`, `ARMA_VANILLA` y `clasificar(manoVacia, enTagArmasDelMod, tieneDanioDeAtaque)`.
- `game/damage/FuerzaService.java` — valor de FUE del jugador, clasificación de un `ItemStack` y `danioFinal`.
- `game/damage/FuerzaEvents.java` — `LivingDamageEvent.Pre`; solo escala el golpe directo del jugador.
- `game/item/ModTags.java` + `data/dragonball/tags/item/armas_del_mod.json` (vacío).
- `PlayerProgress.isAtCap` ahora cubre también FUE (índice 1): la mejora 92 se rechaza y `/dragonball upgrade fue` muestra el mensaje de tope.

## Cómo se distingue arma vanilla de arma del mod
El mod **no tiene armas todavía** (`ModItems` solo registra el huevo del lobo), así que no había nada que registrar. Mecanismo elegido: un **tag de ítem** `dragonball:armas_del_mod` (datos, no código), hoy vacío. Cada arma futura se añade a ese JSON. Prioridad de la clasificación:
1. Ítem en el tag → `ARMA_DEL_MOD` (escala).
2. Mano vacía, o ítem **sin** modificador de daño de ataque (un bloque, comida) → `PUNO` (escala).
3. Cualquier otro ítem con daño de ataque (espadas, hachas, picos, maza…) → `ARMA_VANILLA` (no escala).
El daño de ataque se lee de los modificadores de atributos del ítem (`ItemStack.getAttributeModifiers()`, atributo `ATTACK_DAMAGE`). Alternativas descartadas: una clase base común (obliga a que toda arma herede de ella, no sirve para armas con distinta clase) y un data map (el tag es el mecanismo estándar y ya lo entienden las recetas y los datapacks).

## Desviaciones respecto al encargo (y por qué)
1. **Valor base de FUE = 8, crecimiento 1,0** (como VIT), según el `FUE_BASE = 8` del encargo. BETA_RACES da al Saiyajin FUE 10 / +1,25; se usará cuando existan las razas.
2. `(99/8)^3 = 1895,1`, no 1896 (el test usa 1895,1 ± 0,1).
3. El detector de ítems está en `game/` (`FuerzaService`), no en `core/`: necesita `ItemStack`. En `core/` queda la clasificación pura con booleanos.
4. La sección de SYSTEMS es **§3.2.2** (la §3.3 ya era la esquiva).
5. El tope 99 también se hace cumplir para FUE (sin eso `fueValue` lanzaría excepción con más de 91 mejoras).
6. **Limitación de las pruebas:** los golpes se aplican con `hurt(playerAttack(...), daño)`, no con `player.attack()`: el jugador simulado de GameTest no recarga la fuerza del ataque. Se prueba el evento y la fórmula, no el daño de la espada de vanilla. Además, **el "arma del mod" solo se prueba a nivel de servicio** (criterio inyectado), porque no existe ninguna arma real que meter en el tag; el camino completo evento → tag → escala quedará por probar cuando exista la primera.
7. El zombi de vanilla trae 2 de armadura y el evento llega después de ella: los tests miden con armadura 0. En el juego, el daño de FUE se calcula **tras** la armadura del defensor.

## Resultados (ejecutados, 2026-10-12)
`./gradlew build runGameTestServer`: BUILD SUCCESSFUL, JUnit 20/20, "All 26 required tests passed".
- JUnit: multiplicador 8 → 1,0; 18 → 11,39; 26 → 34,33; 99 → 1895,1; daño 1,0 con FUE 8 y 34,33 con FUE 26; monotonía y simetría con VIT; fuera de rango lanza excepción; tope de 91 mejoras; clasificación de armas.
- GameTest: puño con FUE 8 → 1,0; FUE 26 → 34,33; el zombi normal muere de un puñetazo; espada de diamante con FUE 26 → 8,0 sin escalar; bloque en la mano = puño; flecha → 5,0 sin escalar; arma del mod (simulada) → escalada; los ataques de mobs no cuentan; el tope rechaza la mejora 92.

## Estado
| Parte | Estado |
|---|---|
| Fórmulas, clasificación, tope | implementado, compilado, probado (JUnit) |
| Evento de daño | implementado, compilado, probado (GameTest con jugador simulado) |
| Con un golpe real en el juego (cooldown, críticos, barrido) | **no verificado** |
| Arma del mod real | no existe todavía |
| DEF, VEL, KI, POD, CON, HUD | no implementados (fuera de alcance) |

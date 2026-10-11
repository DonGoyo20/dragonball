# ATTRIBUTE_AUDIT — Auditoría de atributos (2026-10-11)
Solo investigación; no se escribió código. Leyenda: **[CÓDIGO]** existe en el código · **[DISEÑO]** solo en documentos.

## Corrección previa
El prompt habla de 5 atributos; **el código y el diseño tienen 7**: VIT, FUE, DEF, VEL, KI, POD, CON (`Progression.java:10-11`, `GDD.md` §4, `RACES.md:22`).

## A. Tabla de atributos
| # | ID [CÓDIGO] | Display EN | Display ES | Definido en | Efecto según diseño |
|---|---|---|---|---|---|
| 1 | `vit` | Vitality (solo en docs en español; no hay texto EN en el juego) | Vitalidad | `ProgressService.java:13` (índice 0) | Salud máxima y resistencia; entra en la defensa (^0,3); regeneración |
| 2 | `fue` | Strength | Fuerza | idem (índice 1) | Daño cuerpo a cuerpo (A físico) |
| 3 | `def` | Defense | Defensa | idem (índice 2) | Reducción de daño (D^0,7 físico, ^0,4 ki) |
| 4 | `vel` | Speed | Velocidad | idem (índice 3) | Esquiva (y movimiento según GDD) |
| 5 | `ki` | Max Ki | Ki máximo | idem (índice 4) | Reserva de ki: `50 + 10 × KI` |
| 6 | `pod` | Ki Power | Poder de ki | idem (índice 5) | Daño de técnicas de ki |
| 7 | `con` | Control | Control | idem (índice 6) | Reduce coste de técnicas; resiste el ki; recuperación |

**Display:** el juego **no tiene nombres de atributo traducidos**. `/dragonball progress` imprime los números en crudo (`upgrades [4, 0, 0, ...]`, `DragonBallCommands.java:38`) y `upgrade` muestra el ID (`vit`). En `lang/*.json` no existe ninguna clave de atributo. Los nombres EN de la tabla son los míos (el diseño solo está en español). El orden (`ATTRIBUTES`) es el de `SYSTEMS.md`.

## B. Efectos especificados en el diseño (citas)
- **Vitalidad** — GDD §4: «Salud máxima y resistencia». SYSTEMS §3.2: «`D` contra golpes físicos = `E_DEF^0,7 × E_VIT^0,3`»; «contra técnicas de ki = `E_DEF^0,4 × E_CON^0,3 × E_VIT^0,3`». SYSTEMS §7: «Regeneración natural: 0,2 %/s fuera de combate (escala con VIT: `×(1 + stat_VIT/100)`)». ABILITIES: Kaio-ken exige «VIT ≥ 16» y su drenaje depende de VIT.
- **Fuerza** — GDD §4: «Daño cuerpo a cuerpo». SYSTEMS §3.2: «`A` = `E_FUE` (cuerpo a cuerpo)».
- **Defensa** — GDD §4: «Reducción de daño». Fórmula de `D` arriba.
- **Velocidad** — GDD §4: «Movimiento, esquiva, ataque». SYSTEMS §3.3: «`prob_esquiva = clamp(0,10 + 0,20 · ln(E_VEL_defensor / E_VEL_atacante), 0, 0,6)`». SYSTEMS §2.3 punto 4: «Hacer que la velocidad dé más acciones por segundo rompe el equilibrio… no se adopta». **Conflicto:** GDD dice «ataque», SYSTEMS (que prevalece) lo descarta. «Movimiento» no está cuantificado.
- **Ki máximo** — GDD §4: «Reserva de energía». SYSTEMS §4.1: «`ki_máximo = 50 + 10 × stat_KI` (no escala con el nivel)».
- **Poder de ki** — GDD §4: «Daño de técnicas energéticas». SYSTEMS §4.2: «Daño de la técnica = `E_POD × multiplicador_técnica`».
- **Control** — GDD §4: «Eficiencia de técnicas y recuperación de ki». SYSTEMS §4.2: «`coste = base × 1 / (1 + stat_CON/60)`».
- Requisitos de habilidad por atributo (ABILITIES §3): p. ej. «Kamehameha CON ≥ 20, POD ≥ 20»; «Kaio-ken VIT ≥ 16»; «Vuelo CON ≥ 10». **NO ESPECIFICADO** para ningún atributo: efecto sobre movimiento vanilla (velocidad al caminar), sobre hambre o sobre saltos.

## C. Código relacionado con combate ya existente [CÓDIGO]
**NINGUNO que aplique atributos al jugador.** Búsqueda en todo `src/`:
- `AttributeModifier`, `AttributeInstance`, `MobEffect`/`addEffect`, `PlayerLoggedInEvent`, `PlayerEvent.Clone`, eventos de daño del jugador (`LivingIncomingDamageEvent`, etc.): **0 coincidencias**.
- `WildWolf.java:7,43` — `AttributeSupplier.Builder` con atributos vanilla provisionales del lobo (vida 12, daño 3, velocidad 0,32). No es del jugador y no usa el bestiario.
- `KillXpHandler.java:21-` — único gancho de combate: `LivingDeathEvent`, solo para dar XP.
- `ModAttachments.java:18-19` — el progreso se guarda con `serialize(PlayerProgress.CODEC)` y `copyOnDeath()`.
- `ProgressService.java:30` — mensaje de acción al ganar XP (único texto en pantalla).

## D. Fórmulas y límites documentados
Cita de lo pedido:
- **Cuánto sube por punto:** «El atributo sube en el valor de crecimiento de tu raza (p. ej. Saiyajin: FUE +1,25 por mejora)» (SYSTEMS §2.1) y `valor_atributo = base_raza + crecimiento_raza × mejoras_en_ese_atributo` (§2.2). Tablas base/crecimiento en BETA_RACES §4 (Saiyajin base 8/10/6/8/5/7/4, crec. 1,00/1,25/1,00/1,10/0,90/1,15/0,85; Humano base 6/6/5/6/5/5/7, crec. 1,10/1,00/1,00/1,10/1,00/1,00/1,30). **[DISEÑO]; en el código no existen razas, valores base ni crecimiento: solo se cuentan mejoras por atributo.**
- **Tope:** «Tope por atributo: valor 99 (decisión tuya)» (SYSTEMS §2.1). En código `Progression.ATTRIBUTE_CAP = 99` (`Progression.java:13`) **pero no se usa en ningún sitio**: `PlayerProgress.tryUpgrade` (`:58`) no lo comprueba aunque su comentario dice «o ya está al tope». Además el tope es del *valor* (base + crec.×mejoras), no del número de mejoras.
- **Lineal o escalado:** poder efectivo `E = valor × 1,0399^T × M_forma`: lineal en el valor y exponencial global en el nivel total T (SYSTEMS §2.2); daño `0,05 × clamp((A/D)^0,75, 0,001, 200)` (§3.2). Ki y regeneración son lineales en el valor.
- **¿Subir VIT cura la diferencia?** **NO ESPECIFICADO.** Solo se dice que la vida se guarda como fracción 0–1 (SYSTEMS §3.1), lo que implica que subir VIT no cambiaría la barra, pero no se afirma.
- **¿Se pierden al morir?** Se conservan: «Al morir… No pierdes zeni, nivel ni objetos» (WORLD_DESIGN §9, línea 126). Código: `copyOnDeath()` (`ModAttachments.java:19`) lo cumple. Respec: SYSTEMS §2.7, sin concretar coste.
- **Conflicto entre documentos:** SYSTEMS §3.1 (vida = fracción, barra en proporción) frente a TECHNICAL_DESIGN §6 («no intentaremos meter el NP en atributos de vanilla… Verificar en Fase 2»). Ambos dicen «a verificar».

## E. Estado del HUD
**No existe HUD de progresión.** `client/` solo contiene `WildWolfRenderer.java`. No hay `RegisterGuiLayersEvent` ni pantalla de atributos. La información solo sale:
- por chat al ejecutar `/dragonball progress` (`DragonBallCommands.java:38`): nivel total, XP, coste y la lista cruda de mejoras `[4, 0, ...]`. **No se actualiza solo**: es una foto del momento del comando;
- por barra de acción «+N XP» (`ProgressService.java:30`) al matar.
Traducido: textos de comando sí (en_us, es_es y variantes añadidas); nombres de atributo no. El progreso **no se sincroniza al cliente** (no hay payload ni `syncWith`), así que un HUD requeriría añadirlo.

## F. Tests existentes
- JUnit `ProgressionTest` (5): coste, XP acumulada, factor de nivel, spawner, monotonía. **No tocan efectos de atributos.**
- GameTest (6): data map, XP por matar, mejoras y logros, mejora sin XP, lobo, spawner. `upgradesRaiseLevelAndGrantAdvancement` comprueba el nivel total, no ningún efecto.
- **NINGUNO verifica el efecto de un atributo en el combate** (no hay efectos que verificar).

## G. Preguntas abiertas (necesitan decisión)
1. **Vida:** ¿se implementa la vida en porcentaje (SYSTEMS §3.1) interceptando el daño y mostrando la barra como proporción, o se usa `generic.max_health` de Minecraft con VIT? Cambia toda la arquitectura (a verificar en NeoForge).
2. ¿Subir VIT cura, no cura o mantiene la fracción? (Con vida en fracción, el problema no existe.)
3. **Razas en código:** hoy no hay base/crecimiento. ¿Se implementan Saiyajin/Humano como datos antes de los efectos? Sin ellas, «valor 99» no existe.
4. **Tope 99:** ¿se hace cumplir al comprar (rechazar mejora que lleve el valor sobre 99)? Hoy no se hace.
5. **Enemigos:** el data map solo tiene nivel/arquetipo/XP; las fórmulas de daño necesitan `E` de cada enemigo (existe en `design_data/beta/bestiary.json` pero no se exporta al juego).
6. **Velocidad:** ¿mueve al jugador (atributo de movimiento vanilla) o solo esquiva? GDD y SYSTEMS discrepan.
7. **Ki:** no existe aún barra ni recurso de ki; KI, POD y CON no tienen nada sobre qué actuar hasta que existan técnicas.
8. **Daño de entorno** (lava, caídas) convertido a % con «valor de peligro propio»: ¿cuál?
9. **Pantalla/HUD:** ¿barra de ki y pantalla de atributos ahora o después? Requiere sincronización cliente-servidor.
10. **Nombres EN de atributos** para el juego (propuesta: Vitality, Strength, Defense, Speed, Max Ki, Ki Power, Control).
11. **Respec** (SYSTEMS §2.7): coste sin definir.

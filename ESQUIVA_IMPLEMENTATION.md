# Esquiva con coste de Ki — Fase 2 de la reimplementación de DMZ (2026-10-12)

Estado: **implementada y probada con tests automáticos; no verificada todavía en el juego real** (partículas, sonido y aviso en pantalla incluidos). Sin poise, sin parry y sin stamina (decisiones cerradas).

## Cómo funciona
Al recibir un golpe, el servidor tira un dado: si sale por debajo de la **probabilidad de esquiva**, y hay ki suficiente, el golpe se anula y se gasta ki.

**Probabilidad** (`core/EsquivaSystem`): `clamp(0,10 + 0,20 × ln(VEL efectiva / 8), 0,10; 0,60)`.
La VEL efectiva sigue la función a trozos de la liberación (0 % = vanilla 8, 50 % = la del mod, 100 % = el doble) y nunca baja de 8, así que 10 % es el suelo.

| VEL efectiva | Probabilidad |
|---|---|
| 8 | 10,0 % |
| 18 | 26,2 % |
| 36 | 40,1 % |
| 99 | 60 % (tope; la fórmula daría 60,3 %) |

> **Los ejemplos del encargo no coincidían con su fórmula.** El encargo decía 32 % (VEL 18) y 46 % (VEL 36), pero `0,10 + 0,20·ln(2,25) = 0,262` y `0,10 + 0,20·ln(4,5) = 0,401`. Mantuve la fórmula (es la de SYSTEMS.md §3.3 y la que pide el encargo) y los tests comprueban 26,2 % y 40,1 %. Si querías 32 % y 46 %, hay que cambiar `log_multiplier` (con un solo multiplicador no salen los dos valores: harían falta ≈0,27 y ≈0,24). Ver "Preguntas abiertas".

**Coste en ki:** `round((5 + daño × 0,1) / (1 + CON / 60))`, mínimo 1. Golpe de 3 HP → 5; de 30 HP → 8; de 30 HP con CON 60 → 4. El "daño" es el de vanilla del golpe, antes de armadura y de las reducciones de VIT/DEF.

**Sin ki suficiente:** no se esquiva, se recibe el golpe y aparece "Sin Ki para esquivar" (como mucho una vez cada 2 s).

## Qué se puede esquivar
Solo daño causado por una entidad (mob, jugador, proyectil). **No** se esquivan: lava, fuego, caída, ahogamiento, congelación, rayos, explosiones (decisión mía: son de área), el vacío ni `/kill` (saltan la invulnerabilidad).

## Interacción con el resto
- **VEL y liberación:** la probabilidad usa la VEL efectiva. VEL 18 al 0 % de liberación es 10 %; al 100 %, 40 %.
- **CON:** abarata el coste de cada esquiva (mismo `/60` que el gasto de la liberación).
- **Ki:** se gasta de la misma reserva que la liberación. Si una esquiva deja el ki en 0, el siguiente paso del Ki baja la liberación a 0 (regla de siempre) y sale "¡Sin Ki!".
- **VIT y DEF:** actúan **después** y solo si el golpe no se esquiva: la esquiva se resuelve en `LivingIncomingDamageEvent` y la reducción en `LivingDamageEvent.Pre`.
- **Efectos:** partículas de vanilla (`END_ROD` y `ELECTRIC_SPARK`, blancas y azuladas) enviadas desde el servidor, sonido corto y aviso "¡Esquivado!" en la barra de acción.

## Desviaciones del encargo (técnicas, para que funcione mejor)
1. **`LivingIncomingDamageEvent` en vez de `LivingDamageEvent.Pre`.** `Pre` no es cancelable y para entonces el golpe ya empujó al jugador y activó la animación de herido; con `Incoming` esquivar es de verdad no recibir el golpe.
2. **Partículas de vanilla desde el servidor** en vez de una clase `client/particle/EsquivaParticles`: una partícula propia exigiría registrar el tipo, su textura y su JSON, sin poder verla desde aquí. Se puede cambiar más adelante si quieres un destello propio.
3. **Bloque de datos `dodge`** (no `esquiva`), por coherencia con `release`, `defense`, `pod`.
4. **Claves de traducción** exactamente como en el encargo: `dragonball.combat.dodged`, `dragonball.combat.no_ki_to_dodge` (más `command.dragonball.dodge` para el comando), en los 8 idiomas.
5. `probabilidadEsquiva` recibe un `double` (la VEL efectiva no es entera) y existe una función `sale(probabilidad, tirada)` para poder probar la tirada sin aleatoriedad.

## Datos (`combat.json`)
```json
"dodge": { "base_chance": 0.10, "log_multiplier": 0.20, "max_chance": 0.60,
           "ki_cost_base": 5.0, "ki_cost_per_damage": 0.1, "con_reduction": 60.0 }
```
Mismas reglas que el resto (clave ausente = valor por defecto, valor inválido rechaza el archivo, se sincroniza al cliente, KubeJS puede sobrescribirlo).

## Comando
`/dragonball esquiva` muestra la probabilidad actual, la VEL efectiva y el coste de esquivar un golpe de 1 HP.

## Archivos
- `core/`: `DodgeParams`, `EsquivaSystem` (nuevos); `BalanceParams` (bloque `dodge`).
- `game/`: `combate/EsquivaService`, `combate/EsquivaEvents` (nuevos); `balance/BalanceManager`, `command/DragonBallCommands`.
- `data/`: `combat.json`, 8 archivos de idioma.
- Tests: `EsquivaSystemTest`, `EsquivaGameTests` (nuevos), `GameTestSupport` (nuevo; las pruebas de daño fijan "nunca esquivar"), más cambios menores en `BalanceParamsTest` y en los GameTest existentes.

## Tests (ejecutados 2026-10-12)
- **JUnit 67/67** (11 nuevos en `EsquivaSystemTest`, 1 en `BalanceParamsTest`): probabilidad en VEL 4/8/18/36/99, suelo, monotonía, liberación, coste (3→5, 30→8, 30 con CON 60→4), validaciones, tirada, 600 000 tiradas convergen a la probabilidad, datos validados y configurables.
- **GameTest 67/67** (9 nuevos en `EsquivaGameTests`): VEL 8 esquiva con tirada 0,05 y no con 0,15; VEL 18 con 0,25 sí y con 0,30 no; frecuencia con tirada real (2000 golpes: ~10 % y ~26 %); sin ki no se esquiva y avisa; poco ki no paga un golpe grande; lava, caída, vacío y `/kill` no se esquivan ni gastan ki; la liberación cambia la probabilidad; CON abarata el coste; datos y sincronización.
- `tools/verify_integrations.sh`: núcleo, FTB Quests y KubeJS **todo OK**.

## Deuda técnica
El diseño (SYSTEMS.md §3.3) compara la VEL del defensor con la **del atacante**: `ln(VEL_defensor / VEL_atacante)`. Hoy los enemigos no tienen VEL, así que el atacante se toma como 8 (vanilla). Cuando existan los atributos de los enemigos, el 8 se sustituirá por su VEL.

## Preguntas abiertas
1. ¿Prefieres que VEL 18 dé 32 % y VEL 36 dé 46 % (como en el encargo) en vez de 26 % y 40 % (lo que da la fórmula)? Se arregla en `combat.json` con `log_multiplier`, pero con una sola constante no salen ambos números.
2. ¿Las explosiones (creeper) deben poder esquivarse? Hoy no.

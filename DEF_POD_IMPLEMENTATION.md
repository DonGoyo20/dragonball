# DEF y POD — Fase 1 de la reimplementación de DMZ (2026-10-12)

Estado: **implementado y probado con tests automáticos; no verificado todavía en el juego real.** Los números son provisionales (míos) y están en `data/dragonball/balance/combat.json`.

## Qué se hizo y qué no
| Atributo | Estado |
|---|---|
| **DEF** | Implementado: valor, compra con XP, tope 99 y **reducción del daño instantáneo recibido** |
| **POD** | Solo valor y compra (tope 99). **Sin efecto**: su efecto es el daño de las técnicas de ki y no existen técnicas |
| **CON** | Ya estaba (gasto de la liberación, Ciclo Ki). Su otro efecto (resistir el ki) espera a las técnicas. Sin cambios |

DMZ se usó como referencia (defensa saturante, escalado por raza), pero **no se copió código**: DEF sigue el patrón cúbico de VIT y FUE que ya tenías verificado.

## DEF
- Valor: `8 + 1 × mejoras`, tope 99 (91 mejoras). Se compra con XP como los demás (`/dragonball upgrade def`, pantalla V).
- Daño recibido: `× (8 / DEF efectiva)^3`, **además** de la reducción de VIT. DEF 8 no cambia nada; DEF 18 → ×0,088; VIT 18 + DEF 18 → ×0,0077.
- **DEF efectiva** sigue la función a trozos de la liberación (igual que VIT, FUE y VEL): 0 % = vanilla (8), 50 % = la del mod, 100 % = el doble. A 100 % con VIT 8 y DEF 8 el daño es ×1/64 (`(1/8)·(1/8)`).
- **A qué daño se aplica:** solo al instantáneo (golpes, proyectiles, explosiones, caídas…; categoría `ESCALA_SIN_SUELO`). **No** al continuo (lava, fuego, veneno…, que ya tiene su suelo de 0,5 HP), ni al vacío ni a `/kill`.
- Pantalla V: DEF aparece con su valor base y efectivo (ya no pone "no implementado").

## POD
- Valor inicial **8** (provisional: igual que VIT, FUE, DEF y VEL), `+1` por mejora, tope 99. Se compra y se ve, pero no hace nada hasta que existan las técnicas de ki.

## Datos (`combat.json`)
```json
"defense": { "def_base": 8.0, "def_growth": 1.0, "damage_exponent": 3.0 },
"pod":     { "pod_base": 8.0,  "pod_growth": 1.0 }
```
Mismas reglas que el resto: una clave ausente conserva su valor, un valor inválido rechaza todo el archivo, se sincroniza al cliente y KubeJS puede sobrescribirlo.

## Archivos
`core/DefenseParams`, `core/PodParams`, `core/DefensaSystem` (nuevos); `core/BalanceParams` (bloques `defense` y `pod`), `core/KiSystem.podValue`; `game/balance/BalanceManager` (codecs); `game/health/HealthService` (aplica DEF), `game/progress/{AttributeValues,PlayerProgress}`; `combat.json`.

## Tests (ejecutados 2026-10-12)
- JUnit **53/53** (`DefensaSystemTest` nuevo, 8 tests; más un test en `BalanceParamsTest`): valores y tope de DEF y POD, ×1 con DEF 8, cúbica, función a trozos (0/25/100 %), apilado con VIT, entradas inválidas, validación de los bloques.
- GameTest **57/57** (`DefGameTests` nuevo, 5 tests): reducción real con un zombi, dependencia de la liberación, el daño continuo y el vacío no cambian con DEF, compra y tope, datos y sincronización. Se actualizó `releaseChangesTheDamageTaken` (al 100 % ahora también DEF sube al doble).

## Decisiones mías (provisionales)
1. Exponente 3 para DEF (simétrico a VIT y FUE). Con VIT y DEF altas juntas el daño recibido se vuelve casi nulo; si se siente excesivo, se baja `defense.damage_exponent` sin recompilar.
2. DEF se aplica solo al daño instantáneo.
3. POD base 8.

## Deuda técnica
El diseño (`SYSTEMS.md` §3.2) usa `D = DEF^0,7 × VIT^0,3` contra el poder del atacante y una defensa propia contra el ki (`DEF^0,4 × CON^0,3 × VIT^0,3`). Se hará cuando existan el poder de ataque de los enemigos y las técnicas de ki.

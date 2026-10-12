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
- Daño recibido: `× min(1, defense_multiplier × (8 / DEF efectiva)^3)`, **además** de la reducción de VIT, con `defense_multiplier = 1,25`.
- **Ajuste del 2026-10-12 (decisión del usuario):** con la fórmula pura VIT 18 + DEF 18 daba ×0,0077 (99,2 % de reducción, casi inmune). El multiplicador 1,25 hace que el jugador reciba un 25 % más: DEF 18 sola → ×0,110; VIT 18 + DEF 18 → ×0,0096 (0,0077 × 1,25; el "×0,0097" del encargo es el mismo número redondeado).
- **Tope en 1:** sin el `min(1, …)` el multiplicador 1,25 haría que DEF 8 (o cualquier DEF a 0 % de liberación) recibiera ×1,25, es decir, que *subir* DEF empeorara el daño por debajo de DEF ~8,6. Con el tope, DEF 8 y 0 % siguen siendo ×1 (vanilla) y DEF nunca empeora el daño.
- Configurable: `defense.defense_multiplier` en `combat.json` (1,0 = la fórmula de antes; 1,5 = aún más susceptible).
- **DEF efectiva** sigue la función a trozos de la liberación (igual que VIT, FUE y VEL): 0 % = vanilla (8), 50 % = la del mod, 100 % = el doble. A 100 % con VIT 8 y DEF 8 el daño es ×0,0195 (`1/8 · 1,25/8`); el multiplicador sigue siendo 1,25.
- **A qué daño se aplica:** solo al instantáneo (golpes, proyectiles, explosiones, caídas…; categoría `ESCALA_SIN_SUELO`). **No** al continuo (lava, fuego, veneno…, que ya tiene su suelo de 0,5 HP), ni al vacío ni a `/kill`.
- Pantalla V: DEF aparece con su valor base y efectivo (ya no pone "no implementado").

## POD
- Valor inicial **8** (provisional: igual que VIT, FUE, DEF y VEL), `+1` por mejora, tope 99. Se compra y se ve, pero no hace nada hasta que existan las técnicas de ki.

## Datos (`combat.json`)
```json
"defense": { "def_base": 8.0, "def_growth": 1.0, "damage_exponent": 3.0, "defense_multiplier": 1.25 },
"pod":     { "pod_base": 8.0,  "pod_growth": 1.0 }
```
Mismas reglas que el resto: una clave ausente conserva su valor, un valor inválido rechaza todo el archivo, se sincroniza al cliente y KubeJS puede sobrescribirlo.

## Archivos
`core/DefenseParams`, `core/PodParams`, `core/DefensaSystem` (nuevos); `core/BalanceParams` (bloques `defense` y `pod`), `core/KiSystem.podValue`; `game/balance/BalanceManager` (codecs); `game/health/HealthService` (aplica DEF), `game/progress/{AttributeValues,PlayerProgress}`; `combat.json`.

## Tests (ejecutados 2026-10-12)
- JUnit **55/55** (tras el ajuste del multiplicador; antes 53) (`DefensaSystemTest` nuevo, 8 tests; más un test en `BalanceParamsTest`): valores y tope de DEF y POD, ×1 con DEF 8, cúbica, función a trozos (0/25/100 %), apilado con VIT, entradas inválidas, validación de los bloques.
- GameTest **58/58** (tras el ajuste; el nuevo mide VIT 18 + DEF 18 con 20 HP) (`DefGameTests` nuevo, 5 tests): reducción real con un zombi, dependencia de la liberación, el daño continuo y el vacío no cambian con DEF, compra y tope, datos y sincronización. Se actualizó `releaseChangesTheDamageTaken` (al 100 % ahora también DEF sube al doble).

## Decisiones mías (provisionales)
1. Exponente 3 para DEF (simétrico a VIT y FUE). El multiplicador 1,25 (decisión del usuario) modera la protección combinada; se puede subir `defense.defense_multiplier` sin recompilar.
2. DEF se aplica solo al daño instantáneo.
3. POD base 8.

## Deuda técnica
El diseño (`SYSTEMS.md` §3.2) usa `D = DEF^0,7 × VIT^0,3` contra el poder del atacante y una defensa propia contra el ki (`DEF^0,4 × CON^0,3 × VIT^0,3`). Se hará cuando existan el poder de ataque de los enemigos y las técnicas de ki.

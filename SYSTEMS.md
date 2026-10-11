# SYSTEMS — Reglas base compartidas por todas las razas

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Define cómo funcionan los niveles,
> el poder, el combate, el ki, la comida, el cansancio y el entrenamiento. **Las razas no
> inventan reglas: solo ajustan los parámetros de este documento** (ver §11). Todo valor es
> propuesta (P), configurable en archivos de datos y se ajusta jugando.
>
> **Versión 2 (2026-10-11):** XP por enemigo en tabla fija, cansancio como agotamiento por ki y ritmo lento.
>
> **Sustituye a:** la idea de "multiplicar el poder por etapa" de GDD §2/§3 y WORLD_DESIGN §4
> (producía un salto de ×207 al cambiar de etapa; ver §2.1).

## 1. Mapa de recursos
| Recurso | Qué es | Se gasta con | Se recupera con | Lo toca la raza |
|---|---|---|---|---|
| **Vida** | Barra de salud en porcentaje (0–100 %) | Recibir daño | Regeneración, comida, descanso, senzu | Sí |
| **Ki** | Barra de energía | Ataques de ki y transformaciones (y un poco el vuelo) | Pasivo, cargar, comer, senzu | Sí |
| **Hambre** | Barra de comida de Minecraft ampliada | Esfuerzo, ki, entrenar | Comer | Sí |
| **Recursos especiales** | Agua (Namekiano), Calor (Androide), Tensión (Freezer) | Según raza | Según raza | Solo esa raza |
| **Dominio de forma** | 0–100 % por forma | Usar la forma | — | Sí |

## 2. Niveles, poder y experiencia

### 2.1 Poder efectivo (reemplaza al multiplicador por etapa)
Cada atributo tiene un valor **base** (`stat`) y un **poder efectivo** (`E`):

`E_atributo = stat × g^(nivel−1) × M_forma`

- `g = 1,216` (+21,6 % por nivel). Calculado para que el nivel 100 con la mejor forma ronde 10^12.
- `M_forma`: multiplicador de la forma activa × dominio (§8). Sin forma = 1.
- **Nivel de poder (NP) mostrado** = suma de los `E`.

Por qué: el salto entre niveles es siempre el mismo (+21,6 %) y las etapas dejan de ser un muro. Cada etapa de 20 niveles vale ≈ ×50.

| Nivel | NP sin forma (modelo Saiyajin) |
|---|---|
| 1 | 48 |
| 20 | 6.010 |
| 40 | 512.000 |
| 60 | 36.200.000 |
| 80 | 2.330.000.000 |
| 100 | 143.000.000.000 |

### 2.2 Qué hace cada cosa
- **Nivel:** sube `g^(nivel−1)` para **todos** los atributos a la vez. Es la progresión "automática".
- **Puntos de atributo:** se reparten entre los 7 atributos. Eligen *en qué eres bueno* (la forma de tu poder), no cuánto eres.
- **Puertas de forma (WORLD_DESIGN §4.2):** bloquean el nivel en 15, 35, 55, 75, 95 hasta desbloquear la forma correspondiente.

### 2.3 XP por derrotar enemigos (decisión tuya: puntos fijos por enemigo)
Cada tipo de enemigo tiene un **nivel** y una **XP fija** escritos a mano en el bestiario (archivo de datos). La XP **sube progresivamente con la dificultad** del enemigo. No hay fórmula oculta ni bonus por tipo.

`XP recibida = XP_del_enemigo × factor_nivel × origen`

- `XP_del_enemigo`: valor del bestiario. Se parte de una **tabla guía** y el diseñador puede ajustarla a mano, enemigo por enemigo (jefes incluidos).
- `factor_nivel` (con `dif = nivel_jugador − nivel_enemigo`):
  - `dif < 0` (enemigo más fuerte): `1 + 0,1 × |dif|`, tope 1,5.
  - `dif ≥ 0`: `1 − 0,15 × dif`, mínimo 0. A 7 niveles por encima no das nada.
- `origen`: ×1 normal; **×0,25 si el enemigo viene de un generador de monstruos o granja** (se comprueba el motivo de aparición, a verificar en Fase 1). Evita las granjas de mobs.
- Cada entidad recompensa **una sola vez** (marca de entidad ya recompensada).

**Tabla guía** (XP del enemigo del mismo nivel que el jugador; ritmo lento, 10 + 0,5·L combates parejos por nivel):

| Nivel del enemigo | XP del enemigo | Combates parejos por nivel |
|---|---|---|
| 1 | 4,8 | 10,5 |
| 5 | 52 | 12,5 |
| 10 | 133 | 15 |
| 20 | 302 | 20 |
| 30 | 462 | 25 |
| 50 | 747 | 35 |
| 75 | 1.053 | 47,5 |
| 100 | 1.321 | 60 |

Ritmo: unos 3.465 combates parejos hasta el nivel 100. Con ~90 s por combate, serían ~87 h luchando solo. **El objetivo es que el combate aporte ~50 % de la XP**, el resto sale de misiones, entrenamiento y descubrimientos: unas 40–50 h hasta el nivel 100 como referencia, sin contar contenido narrativo (a validar jugando).

La XP de misiones y entrenamiento se reparte con el mismo criterio: valores fijos en los datos, no fórmulas ocultas.

### 2.4 Qué NO da XP
Enemigos 7 o más niveles por debajo, animales pasivos, muertes que no cause el jugador (o su aliado vinculado) y duplicados de la misma entidad.

## 3. Combate

### 3.1 Vida como porcentaje
La vida real se guarda como **fracción (0–1)** y se muestra en la barra de Minecraft como proporción. Así ninguna cantidad de VIT rompe el límite de vida de Minecraft (ver TECHNICAL_DESIGN §6; a verificar en Fase 2). El daño de entorno de Minecraft (lava, caídas) se convierte a porcentaje con un valor de peligro propio y no escala con el nivel, salvo decisión posterior.

### 3.2 Daño de un golpe (como % de la vida del defensor)
`daño = 0,05 × clamp((A/D)^0,75 , 0,001 , 200)`

- `A` = `E_FUE` (cuerpo a cuerpo) o `E_POD` (técnica de ki) × multiplicador de la técnica.
- `D` = `E_DEF^0,7 × E_VIT^0,3` (la VIT aporta aguante además de la defensa).
- Con `A = D` se necesitan unos 20 golpes. Con 100× más poder, ~10× más daño; con 100× menos, ~10× menos.
- Ningún golpe supera el 100 %.

### 3.3 Esquiva
`prob_esquiva = clamp(0,10 + 0,20 · ln(E_VEL_defensor / E_VEL_atacante), 0, 0,6)`

Misma velocidad = 10 %; el doble de velocidad ≈ 24 %; tope 60 %.

### 3.4 Enemigos
Cada tipo de enemigo se define por **atributos efectivos** propios (`E`) en un archivo de datos (bestiario), con un arquetipo: bruto (FUE/VIT), veloz (VEL), de ki (POD/KI). Los mobs vanilla reciben valores al arrancar (zombi, esqueleto…). Si un mob no está definido, se le asigna un valor por defecto según su vida de vanilla.

## 4. Ki

### 4.1 Reserva
`ki_máximo = 50 + 10 × stat_KI` (no escala con el nivel; los costes sí son porcentajes de la reserva, así que todo escala solo).

### 4.2 Costes y eficiencia
- Toda técnica cuesta un **% de la reserva**.
- El control reduce el coste: `coste = base × 1 / (1 + stat_CON/60)`. CON 4 → ×0,94; CON 60 → ×0,50; CON 100 → ×0,375. (El valor `stat_CON` es el base, sin el factor de nivel.)
- **Daño de la técnica** = `E_POD × multiplicador_técnica`. Gastar más ki en una técnica la hace más fuerte (carga) hasta un tope.

### 4.3 Regeneración y carga
- **Pasiva:** 0,5 %/s de la reserva fuera de combate; 0,1 %/s en combate.
- **Cargar ki:** acción mantenida, 5 %/s. Deja al jugador quieto y con aura visible (se ve desde lejos).
- **Comer** suma recuperación (§6).

### 4.4 Vuelo
Coste muy bajo, 0,1 %/s de la reserva, reducido por CON (mayor velocidad, algo más de coste). Sin ki no se vuela y se cae. *Tu descripción mencionaba solo ataques de ki y transformaciones; mantengo un coste mínimo de vuelo para que el ki importe al explorar. Dime si prefieres vuelo gratis.*

### 4.5 Ki agotado
A 0 %: se desactivan técnicas y formas, y empieza el **agotamiento** (§5).

### 4.6 Ocultar y detectar
- **Ocultar ki:** reduce el rango de detección de los enemigos al jugador; sin ataques de ki.
- **Detectar ki:** se desbloquea con un maestro; muestra criaturas cercanas con su NP relativo (más débil / parejo / más fuerte). Los androides no se detectan.

## 5. Cansancio: agotamiento por quedarse sin ki (decisión tuya)
No existe barra de resistencia ni fatiga diaria. El **cansancio es la penalización por vaciar el ki**.

Cuando el ki llega a 0 (por atacar, transformarte o volar):
- Se apaga la forma activa y las técnicas de ki.
- Estado **Agotado** durante 60 s: −30 % a todos los `E` y el ki no se regenera durante los primeros 5 s.
- Durante Agotado, cargar ki sigue funcionando (recupera, pero estás expuesto).
- El dominio de forma lo reduce: a 100 % de dominio de la forma activa, la duración y la penalización bajan a la mitad.
- Si te quedas sin ki en combate con un enemigo cerca, es una decisión de riesgo real.

Así los ataques de ki y las transformaciones son costosos sin necesidad de barras extra, que era lo que buscabas.

## 6. Comida y hambre

### 6.1 Base
Se mantiene la barra de hambre de Minecraft. Cambios:
- El hambre baja un **50 % más rápido** al usar ki, entrenar o volar.
- A 0 de hambre: sin regeneración natural, `ki_máximo` −20 %. **No mueres de hambre.**

### 6.2 Buff "Bien alimentado"
Comer comida buena (cocinada, guisos) da un buff de 20 min: hasta **+10 % de regeneración de ki y +10 % de XP de entrenamiento**. No se acumula con otros buffs de comida.

### 6.3 Senzu
Restaura el 100 % de vida y ki y quita efectos negativos. Raro, con enfriamiento de 10 min. Los androides no lo usan (kits de reparación).

### 6.4 Raza
Saiyajin: apetito ×1,5 pero buff ×1,5. Namekiano: modo **agua** en vez de comida (§11). Androide: sin hambre. Majin: apetito normal, pero recibe más del buff por comer dulces.

## 7. Salud y regeneración
- **Regeneración natural:** 0,2 %/s fuera de combate (escala con VIT: `×(1 + stat_VIT/100)`).
- **Dormir:** recupera vida y ki por completo y deja avanzar el día.
- **Regeneración racial:** Namekiano, Majin y otros suman por encima de esto (ver §11).

## 8. Formas y dominio
- Cada forma tiene: multiplicador `M`, coste de ki por segundo (% de reserva), efectos secundarios y condiciones de desbloqueo.
- **Dominio:** sube con el tiempo en la forma, en combate. Efectos: baja el coste de ki, sube `M` hasta un +25 % y reduce el agotamiento al quedarte sin ki (§5).
- Al **100 % de dominio** la forma pasa a **estable**: sin coste de ki y el **10 % de su multiplicador pasa a la base** (permanente).
- Multiplicadores propuestos de referencia por puerta: ×1,6 / 2,4 / 3,5 / 5 / 7 (una raza puede variar).

## 9. Entrenamiento
- **XP de entrenamiento:** baja por hora de entrenamiento activo (sin penalización) ≈ `XP_necesaria(L)/1,5`.
- **Entrenamiento activo:** solo cuenta si hay acciones del tipo (golpear, moverse con peso, cargar ki). Quieto no suma.
- **Penalizaciones voluntarias:** suman multiplicador a la XP de entrenamiento; **tope combinado ×5** (WORLD_DESIGN §5).
- **Punto de entrenamiento libre:** cada 30 min de entrenamiento activo de un tipo da un punto libre en el atributo correspondiente. Tope: 2 puntos libres por nivel.
- **Maestros:** entrenar con un maestro suma bonus fijo y puede desbloquear técnicas y formas. Los bonus de raza (Terrícola +30 %) se aplican aquí.

## 10. Datos de configuración
Todos los números de este documento viven en archivos, no en código:
```
data/<modid>/balance/
  leveling.json   # g, XP_necesaria, tabla guía de XP, factor por diferencia de nivel, multiplicador de origen (generador/granja)
  bestiary.json   # nivel, arquetipo, atributos y XP fija de cada enemigo
  combat.json     # 0,05 / 0,75 / 0,7-0,3 / esquiva / topes
  ki.json         # reserva, regeneración, carga, vuelo, coste/CON
  food.json       # hambre, buffs, senzu
  training.json   # XP/hora, tope ×5, punto libre, penalizaciones
  forms_default.json # multiplicadores de referencia
```

## 11. Cómo se conectan las razas (los "enchufes")
Una raza solo rellena estas ranuras. Ninguna toca el código.

| Ranura | Ejemplo |
|---|---|
| `stats.base[7]`, `stats.growth[7]` | Ya definido en RACES.md |
| `xp.combat_mult`, `xp.training_mult`, `xp.master_mult`, `xp.boss_mult` | Terrícola `master_mult 1.3`; Freezer `training_mult 0.85` |
| `ki.regen_mult`, `ki.cost_mult` | Saiyajin `regen_mult < 1` |
| `hp.regen_mult`, `hp.regen_flat` | Namekiano, Majin |
| `hunger.mode` (`food`/`water`/`none`), `hunger.drain_mult`, `hunger.buff_mult` | Namekiano `water`, Androide `none`, Saiyajin `1.5/1.5` |
| `training.type_mult.{pesas, gravedad, meditación…}` | Saiyajin pesas +15 %, meditación −20 % |
| `special_resources[]` | Androide `heat`, Freezer `tension` |
| `traits[]` | Zenkai, Cuerpo elástico… (cada rasgo es un módulo con eventos propios) |
| `forms[5]` | Escalera de formas |

Lo único que sí requiere código nuevo es un **rasgo distinto** (p. ej. Zenkai o Absorción). Por eso los rasgos se programan como módulos reutilizables, con una interfaz común.

## 12. Estado guardado por jugador
`raza, nivel, xp, atributos (7), puntos libres, vida (fracción), ki, estado Agotado (tiempo restante), hambre, buffs, formas (estado, dominio), entidades ya recompensadas, recursos especiales, versión de datos.`

## 13. Decisiones cerradas en esta versión
1. Cansancio = agotamiento por quedarse sin ki (§5). Ki = barra que gastan ataques de ki y transformaciones.
2. Vida como barra en porcentaje (§3.1).
3. XP por enemigo en tabla fija, creciente con la dificultad; sin bonus por primera vez (§2.3).
4. Ritmo lento: 10 + 0,5·L combates parejos por nivel; el combate aporta ~50 % de la XP.

## 14. Preguntas abiertas
1. ¿El vuelo debe tener un pequeño coste de ki (propuesta) o ser gratis?
2. ¿Cuánto de la XP debería venir de misiones (propuesta: ~30 %) y de entrenamiento (~20 %)?

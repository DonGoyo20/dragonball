# SYSTEMS — Reglas base compartidas por todas las razas

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Define cómo funcionan los niveles,
> el poder, el combate, el ki, la comida, el cansancio y el entrenamiento. **Las razas no
> inventan reglas: solo ajustan los parámetros de este documento** (ver §11). Todo valor es
> propuesta (P), configurable en archivos de datos y se ajusta jugando.
>
> **Sustituye a:** la idea de "multiplicar el poder por etapa" de GDD §2/§3 y WORLD_DESIGN §4
> (producía un salto de ×207 al cambiar de etapa; ver §2.1).

## 1. Mapa de recursos
| Recurso | Qué es | Se gasta con | Se recupera con | Lo toca la raza |
|---|---|---|---|---|
| **Vida** | Porcentaje de salud (0–100 %) | Recibir daño | Regeneración, comida, descanso, senzu | Sí |
| **Ki** | Energía para técnicas, vuelo y formas | Técnicas, vuelo, formas | Pasivo, cargar, comer, senzu | Sí |
| **Resistencia** | Aliento de combate, segundos | Correr, esquivar, guardia | Segundos sin esfuerzo | Poco |
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

### 2.3 XP por derrotar enemigos
`XP = (XP_necesaria(nivel) / matanzas_parejas(nivel)) × factor_razón × tipo × decaimiento × bonus_raza`

- `XP_necesaria(L) = 50 · L^1,6`
- `matanzas_parejas(L) = 5 + 0,25 · L` — cuántos enemigos de poder igual al tuyo hacen falta para subir un nivel. **Es el control del ritmo**: se cambia ese número y cambia la velocidad de progreso, sin tocar nada más.
- `factor_razón` (con `r = NP_enemigo / NP_jugador`): 0 si r < 0,1; `r²` si 0,1 ≤ r < 1; `1 + 0,5·(r−1)` con tope 1,5 si r ≥ 1.
- `tipo`: normal ×1, élite ×3, jefe ×10, jefe de historia ×25 (una vez).
- `decaimiento`: ×0,9 por cada muerte reciente del mismo tipo de enemigo; se recupera 1 muerte cada 10 min de juego activo.
- **Primera vez:** ×5 la primera vez que derrotas un tipo de enemigo (solo si r ≥ 0,25). Premia la variedad.
- **Zenkai y otros bonus de raza:** entran como `bonus_raza`.

Resultado (por enemigo parejo):

| Nivel | XP para subir | Matanzas parejas | XP por matanza |
|---|---|---|---|
| 1 | 50 | 5,2 | 9,5 |
| 10 | 1.991 | 7,5 | 265 |
| 20 | 6.034 | 10 | 603 |
| 50 | 26.141 | 17,5 | 1.494 |
| 80 | 55.452 | 25 | 2.218 |
| 99 | 77.981 | 29,8 | 2.621 |

Total hasta el nivel 100: unas 1.730 matanzas parejas. Con ~90 s por combate real son ~43 h **como mínimo teórico** solo luchando; entrenamiento, puertas de forma y misiones lo alargan o lo acortan. Cifra a validar jugando.

### 2.4 Qué NO da XP
Enemigos con `r < 0,1`, muertes de animales pasivos, y cualquier muerte que no cause el jugador (o su pareja/aliado vinculado). Cada entidad solo recompensa una vez (marca de entidad ya recompensada) para evitar duplicados.

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
Coste base 0,3 %/s de la reserva (más rápido, más coste), reducido por CON. Sin ki no se vuela y se cae.

### 4.5 Ki agotado
A 0 %: se desactivan técnicas y formas. La caída de una forma por falta de ki causa **agotamiento** (§5.2).

### 4.6 Ocultar y detectar
- **Ocultar ki:** reduce el rango de detección de los enemigos al jugador; sin ataques de ki.
- **Detectar ki:** se desbloquea con un maestro; muestra criaturas cercanas con su NP relativo (más débil / parejo / más fuerte). Los androides no se detectan.

## 5. Resistencia y cansancio

> **A confirmar contigo:** dijiste "cansancio". Lo he interpretado de dos formas, **ninguna es fatiga diaria** (que ya descartamos). Si querías otra cosa, dímelo.

### 5.1 Resistencia (aliento de combate)
Barra corta de 100 puntos:
- Correr rápido −10/s, esquivar −15, guardia −10/s.
- Se recupera 20/s sin esfuerzo.
- En 0: "sin aliento" 3 s (no esquivas ni corres).
Sirve para dar ritmo al combate y que ser rápido no sea gratis.

### 5.2 Agotamiento por forma
Cuando una forma termina **por quedarte sin ki** (no por decisión propia), sufres:
- −30 % a todos los `E` durante 60 s.
- Se reduce con el dominio de forma (a 100 % de dominio no hay agotamiento).
Así las formas son una decisión de riesgo, no un interruptor gratuito.

### 5.3 Cansancio de entrenamiento
**No existe** (decisión tuya). Las penalizaciones voluntarias (§9) lo sustituyen.

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
- **Dominio:** sube con el tiempo en la forma, en combate. Efectos: baja el coste de ki, sube `M` hasta un +25 % y quita agotamiento.
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
  leveling.json   # g, XP_necesaria, matanzas_parejas, tope de factor_razón, tipos, decaimiento
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
`raza, nivel, xp, atributos (7), puntos libres, vida (fracción), ki, resistencia, hambre, buffs, formas (estado, dominio), contadores anti-farmeo por tipo, recursos especiales, versión de datos.`

## 13. Preguntas abiertas
1. ¿A qué te referías con "cansancio"? (§5)
2. ¿Te parece bien la vida como porcentaje (no se pueden tener "corazones" gigantes, pero escala sin límite)?
3. ¿La recompensa de primera vez ×5 por tipo de enemigo te gusta?
4. ¿Prefieres que el ritmo base sea más lento o más rápido (5 + 0,25·L matanzas por nivel)?

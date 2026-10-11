# SYSTEMS — Reglas base compartidas por todas las razas

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Define cómo funcionan los niveles,
> el poder, el combate, el ki, la comida, el cansancio y el entrenamiento. **Las razas no
> inventan reglas: solo ajustan los parámetros de este documento** (ver §11). Todo valor es
> propuesta (P), configurable en archivos de datos y se ajusta jugando.
>
> **Versión 3 (2026-10-11):** sistema de mejoras por atributo (la XP es una moneda), XP por enemigo en tabla fija, cansancio como agotamiento por ki y ritmo lento.
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

## 2. Atributos, nivel y experiencia (v3: sistema por mejoras, estilo Elden Ring)

> **Cambio de la v3 (2026-10-11):** ya no se "sube de nivel y se reparten 5 puntos". La experiencia es una **moneda**: se gasta para mejorar el atributo que quieras. El nivel es la suma de mejoras. Así el jugador decide su tipo de personaje (fuerza, control de ki extremo, mucho ki…).

### 2.1 Cómo funciona
- **XP** = moneda del juego. Se gana con combates, misiones y entrenamiento (§2.5) y se gasta en **mejoras de atributo** y en **comprar habilidades** (§2.8). Gastarla en habilidades no baja el nivel, pero no se puede gastar dos veces.
- **Mejora:** pagas XP y subes **un** atributo de los 7. El atributo sube en el valor de crecimiento de tu raza (p. ej. Saiyajin: FUE +1,25 por mejora).
- **Nivel total `N` = 1 + número de mejoras.** Es lo que se muestra y lo que usa el juego para escalar el mundo. Referencia de diseño para la versión 1.0 (todo Z): **N ≈ 496**. El máximo real depende de la raza (todos los atributos a 99: entre ~616 y ~742); lo que sobra queda para ampliaciones (Super).
- **Tope por atributo: valor 99** (decisión tuya, como en Elden Ring). Cuántas mejoras hacen falta para llegar depende de la raza (por su crecimiento). Para tener los siete atributos en 99 hacen falta entre ~615 mejoras (Terrícola) y ~741 (Raza de Freezer); en la práctica, los personajes extremos llevan unos atributos al máximo y descuidan otros.
- **Coste de la siguiente mejora** (igual para cualquier atributo, como en Elden Ring; depende solo de tu nivel total): `coste(T) = 10 × (1 + T/5)^1,6`, con `T = N − 1`.

| Nivel total N | Coste de la mejora | XP acumulada |
|---|---|---|
| 1 | 10 | 10 |
| 21 | 131 | 1.315 |
| 46 | 398 | 7.842 |
| 96 | 1.207 | 47.007 |
| 146 | 2.309 | 134.344 |
| 175 | 3.063 | 212.427 |
| 246 | 5.228 | 505.314 |
| 371 | 10.002 | 1.447.623 |
| 496 (referencia 1.0) | ~15.000 | ~3.000.000 |

### 2.2 Poder efectivo
`E_atributo = valor_atributo × 1,0399^T × M_forma`

- `valor_atributo = base_raza + crecimiento_raza × mejoras_en_ese_atributo`.
- `1,0399^T`: +4,0 % por mejora (equivale a +21,6 % cada 5 mejoras, el mismo ritmo de antes). Es **global**: todas las mejoras, en el atributo que sean, hacen más fuerte al personaje entero, y la especialización decide *dónde* es más fuerte.
- `M_forma`: forma activa × dominio (§8). Sin forma = 1.
- **Nivel de poder (NP)** = suma de los `E`.

### 2.3 Especializar: lo que dice el simulador (2026-10-11)
Tres personajes Saiyajin con el mismo nivel total (N = 171, ~16 h de juego), probados con `tools/simulator` (1.000 duelos por pareja, orden aleatorio):

| Build | Mejoras | NP total |
|---|---|---|
| Equilibrado | 24 en cada uno | 173.000 |
| Guerrero | VIT 35, FUE 55, DEF 40, VEL 40 | 182.000 |
| Maestro de ki | VIT 30, DEF 20, KI 35, POD 45, CON 40 | 166.000 |

Hallazgos:
1. **El poder total es parecido (±5 %):** especializar mueve el poder de sitio, no lo crea.
2. **Sin habilidades de especialista, el equilibrado domina** (65–92 % de victorias). El daño es ataque ÷ defensa, así que gana quien tenga más ataque × defensa, y eso se maximiza repartiendo. Solo con números, especializarse no compensa.
3. **La ventaja del especialista debe venir de habilidades que solo él puede usar** (requisitos altos, `ABILITIES.md` §5): con ellas, Equilibrado 35 % / Guerrero 55 % / Maestro de ki 60 % de victorias medias.
4. **Hacer que la velocidad dé más acciones por segundo rompe el equilibrio** (el Maestro de ki cae al 0–5 %): no se adopta.
5. Esto es una medida de equilibrio relativo, sin alcance ni movimiento. Se revalida con combate real en la Fase 2.

*Corrección:* una versión anterior de este apartado se basaba en golpes aislados y daba una imagen demasiado optimista de las especializaciones, y la primera versión del simulador tenía un sesgo de quién ataca primero. Ambos errores están corregidos.

### 2.4 Puertas de forma (en nivel total)
| Puerta | Se alcanza en | Para pasar |
|---|---|---|
| 1 | N = 71 | Desbloquear la forma 1 |
| 2 | N = 171 | Forma 2 |
| 3 | N = 271 | Forma 3 |
| 4 | N = 371 | Forma 4 |
| 5 | N = 471 | Forma 5 (suprema) |

Hasta desbloquear la forma, **la XP se acumula pero no puedes comprar más mejoras** pasada la puerta. (Equivale a los antiguos niveles 15/35/55/75/95.) *Tope de la beta: provisional, ver `CONTENT_DESIGN.md`.*

### 2.5 XP por derrotar enemigos (puntos fijos por enemigo)
Cada tipo de enemigo tiene un **nivel total** y una **XP fija** en el bestiario. La XP sube progresivamente con la dificultad.

`XP recibida = XP_del_enemigo × factor_nivel × origen`

- `factor_nivel` (con `dif = N_jugador − N_enemigo`):
  - `dif < 0` (enemigo más fuerte): `1 + 0,02 × |dif|`, tope 1,5.
  - `dif ≥ 0`: `1 − 0,03 × dif`, mínimo 0. A 33 niveles por encima no das nada.
- `origen`: ×1 normal; **×0,25 si viene de un generador de monstruos o granja** (a verificar en Fase 1).
- Cada entidad recompensa una sola vez.

**Tabla guía** (XP del enemigo del mismo nivel que el jugador; el diseñador la ajusta a mano; la XP de élite y jefes se define en `BESTIARY.md` §3):

| Nivel del enemigo | XP del enemigo | Combates parejos por mejora |
|---|---|---|
| 1 | 4,8 | 2,1 |
| 21 | 52 | 2,5 |
| 46 | 133 | 3 |
| 96 | 302 | 4 |
| 146 | 462 | 5 |
| 246 | 747 | 7 |
| 371 | 1.053 | 9,5 |
| 496 | 1.321 | 12 |

Tiempo acumulado de referencia a ritmo de diseño (90 s por combate, toda la XP al mismo ritmo): N = 71 ≈ 5 h; N = 171 ≈ 16 h; N = 271 ≈ 32 h; N = 496 ≈ 87 h. El reparto 50 % combate / 30 % misiones / 20 % entrenamiento dice *de dónde* viene la XP. Cifras a validar jugando.

### 2.6 Qué NO da XP
Enemigos 33 o más niveles por debajo, animales pasivos, muertes que no cause el jugador (o su aliado) y duplicados de la misma entidad.

### 2.7 Reasignar (respec)
Con un objeto raro o un maestro, se redistribuyen las mejoras ya hechas (el nivel total no cambia). Tiene coste para que no sea trivial cambiar de build cada combate.

### 2.8 Comprar habilidades con XP
Ya no hay "puntos de técnica": las habilidades (técnicas, especiales, formas) se **compran con XP** a un maestro, misión u objeto (decisión tuya, como Elden Ring). Precio como % de la XP acumulada y requisitos de uso por atributo: ver `ABILITIES.md` §3.

## 3. Combate

### 3.1 Vida como porcentaje
La vida real se guarda como **fracción (0–1)** y se muestra en la barra de Minecraft como proporción. Así ninguna cantidad de VIT rompe el límite de vida de Minecraft (ver TECHNICAL_DESIGN §6; a verificar en Fase 2). El daño de entorno de Minecraft (lava, caídas) se convierte a porcentaje con un valor de peligro propio y no escala con el nivel, salvo decisión posterior.

### 3.2 Daño de un golpe (como % de la vida del defensor)
`daño = 0,05 × clamp((A/D)^0,75 , 0,001 , 200)`

- `A` = `E_FUE` (cuerpo a cuerpo) o `E_POD` (técnica de ki) × multiplicador de la técnica.
- `D` contra golpes físicos = `E_DEF^0,7 × E_VIT^0,3`.
- `D` contra técnicas de ki = `E_DEF^0,4 × E_CON^0,3 × E_VIT^0,3` (el control resiste el ki; da una defensa propia al personaje de ki).
- Con `A = D` se necesitan unos 20 golpes. Con 100× más poder, ~10× más daño; con 100× menos, ~10× menos.
- Ningún golpe supera el 100 %.

### 3.2.1 Fórmula provisional de VIT → salud (implementada 2026-10-11) — DEUDA TÉCNICA
Hasta que existan DEF y el poder de ataque `A` de los enemigos, el daño que reciben los jugadores se calcula así (`core/HealthSystem.java`, `game/health/HealthEvents.java`):
- `valor_VIT = 8 + 1,0 × mejoras_VIT` (valores del Saiyajin como base universal hasta que existan razas), tope 99 (91 mejoras).
- El jugador conserva 20 HP de vanilla; `vida_real = (HP_vanilla / 20) × 12,5 × valor_VIT`. No se guarda aparte.
- `daño_aplicado = daño_vanilla × (8 / valor_VIT)`, después de armadura y pociones (`LivingDamageEvent.Pre`). Solo daño causado por entidades; el de entorno (caída, lava, ahogamiento, `/kill`) no se escala, como dice §3.1.
- Al subir VIT la barra vanilla no cambia; la vida real sube en la misma proporción.
**Deuda:** esto es lineal en VIT (`VIT^1`), más fuerte que el diseño de §3.2 (`VIT^0,3` dentro de `D`) y no depende del enemigo. Debe sustituirse por la fórmula de §3.2 cuando existan DEF y los poderes de ataque; mientras tanto un enemigo de nivel 171 pega como uno de nivel 1.

### 3.3 Esquiva
`prob_esquiva = clamp(0,10 + 0,20 · ln(E_VEL_defensor / E_VEL_atacante), 0, 0,6)`

Misma velocidad = 10 %; el doble de velocidad ≈ 24 %; tope 60 %.

### 3.4 Enemigos
Cada tipo de enemigo se define por **atributos efectivos** propios (`E`) en un archivo de datos (bestiario), con un arquetipo: bruto (FUE/VIT), veloz (VEL), de ki (POD/KI). Los mobs vanilla reciben valores al arrancar (zombi, esqueleto…). Si un mob no está definido, se le asigna un valor por defecto según su vida de vanilla.

## 4. Ki

### 4.1 Reserva
`ki_máximo = 50 + 10 × stat_KI` (no escala con el nivel). **Los costes de técnica son absolutos** (unidades de ki), no porcentajes: si fueran un % de la reserva, el atributo KI no cambiaría cuántas técnicas lanzas (fallo que detectó el simulador). Las técnicas más poderosas cuestan más ki, y la reserva grande permite lanzarlas más veces.

### 4.2 Costes y eficiencia
- Toda técnica cuesta una cantidad **absoluta** de ki (p. ej. Ráfaga 4, Kamehameha 120, Makankosappo 200).
- El control reduce el coste: `coste = base × 1 / (1 + stat_CON/60)`. CON 4 → ×0,94; CON 60 → ×0,50; CON 100 → ×0,375. (El valor `stat_CON` es el base, sin el factor de nivel.)
- **Daño de la técnica** = `E_POD × multiplicador_técnica`. Gastar más ki en una técnica la hace más fuerte (carga) hasta un tope.

### 4.3 Regeneración y carga
- **Pasiva:** 0,5 %/s de la reserva fuera de combate; 0,1 %/s en combate.
- **Cargar ki:** acción mantenida, 5 %/s. Deja al jugador quieto y con aura visible (se ve desde lejos).
- **Comer** suma recuperación (§6).

### 4.4 Vuelo
Coste muy bajo, 0,6 de ki por segundo, reducido por CON (mayor velocidad, algo más de coste). Sin ki no se vuela y se cae. *Tu descripción mencionaba solo ataques de ki y transformaciones; mantengo un coste mínimo de vuelo para que el ki importe al explorar. Dime si prefieres vuelo gratis.*

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
- Excepción: algunas formas, como el Kaio-ken, tienen `can_stabilize: false` (no se estabilizan nunca; ver `ABILITIES.md` §7).
- Cada forma tiene: multiplicador `M`, coste de ki por segundo (absoluto), efectos secundarios y condiciones de desbloqueo.
- **Dominio:** sube con el tiempo en la forma, en combate. Efectos: baja el coste de ki, sube `M` hasta un +25 % y reduce el agotamiento al quedarte sin ki (§5).
- Al **100 % de dominio** la forma pasa a **estable**: sin coste de ki y el **10 % de su multiplicador pasa a la base** (permanente).
- Multiplicadores propuestos de referencia por puerta: ×1,6 / 2,4 / 3,5 / 5 / 7 (una raza puede variar).

## 9. Entrenamiento
- **XP de entrenamiento:** baja por hora de entrenamiento activo (sin penalización) ≈ `XP_necesaria(L)/1,5`.
- **Entrenamiento activo:** solo cuenta si hay acciones del tipo (golpear, moverse con peso, cargar ki). Quieto no suma.
- **Penalizaciones voluntarias:** suman multiplicador a la XP de entrenamiento; **tope combinado ×5** (WORLD_DESIGN §5).
- **Mejora libre:** cada 30 min de entrenamiento activo de un tipo da una mejora gratis en el atributo correspondiente (sin gastar XP). Tope: 2 mejoras libres por cada 5 niveles totales.
- **Maestros:** entrenar con un maestro suma bonus fijo y puede desbloquear técnicas y formas. Los bonus de raza (Terrícola +30 %) se aplican aquí.

## 10. Datos de configuración
Todos los números de este documento viven en archivos, no en código:
```
data/<modid>/balance/
  leveling.json   # factor por mejora, coste(T), tope de mejoras por atributo, puertas, tabla guía de XP, factor por diferencia de nivel, multiplicador de origen
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
| `stats.base[7]`, `stats.growth[7]` | Valor inicial y cuánto sube cada atributo por **mejora** (RACES.md) |
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
`raza, XP sin gastar, mejoras por atributo (7), nivel total (derivado), mejoras libres, vida (fracción), ki, estado Agotado (tiempo restante), hambre, buffs, formas (estado, dominio), entidades ya recompensadas, recursos especiales, versión de datos.`

## 13. Decisiones cerradas en esta versión
1. Cansancio = agotamiento por quedarse sin ki (§5). Ki = barra que gastan ataques de ki y transformaciones.
2. Vida como barra en porcentaje (§3.1).
3. XP por enemigo en tabla fija, creciente con la dificultad; sin bonus por primera vez (§2.3).
4. Ritmo lento: 10 + 0,5·L combates parejos por nivel; el combate aporta ~50 % de la XP.

## 14. Decisiones cerradas (2.ª ronda)
1. Vuelo con coste de ki mínimo (0,1 %/s).
2. Reparto de XP: 50 % combate, 30 % misiones, 20 % entrenamiento.
3. Los secretos dan recompensas cualitativas, no inflan niveles (`CONTENT_DESIGN.md` §2).

## 15. Decisiones cerradas (3.ª ronda)
1. **Sistema por mejoras estilo Elden Ring / Dragon Block C:** XP como moneda que se gasta en el atributo elegido; el nivel es la suma de mejoras (§2).
2. El **tope de nivel de la beta queda en el aire** (tú aún no estás seguro). Mientras tanto se trabaja con puertas por nivel total.

## 16. Decisiones cerradas (4.ª ronda)
1. Coste de mejora **igual para todos los atributos** (§2.1).
2. **Tope de 99 por atributo**, sin tope blando (§2.1).
3. **La beta termina al derrotar a Vegeta** (condición de historia, no un número). Como la puerta 2 queda cerrada en la beta, el nivel total efectivo ronda ~171.
4. **Requisitos de atributo para toda habilidad** (técnicas, formas, especiales): ver `ABILITIES.md`.

## 17. Decisiones cerradas (5.ª ronda)
1. La XP es también moneda para comprar habilidades (necesitan una fuente: maestro, misión u objeto). Requisitos de atributo solo para **usar**.
2. Kaio-ken hasta "×3" con nombres no literales.

## 18. Decisiones cerradas (6.ª ronda)
1. Aceptadas las cinco propuestas previas a Saiyajin y Humano (`BETA_RACES.md`).
2. Costes de técnica absolutos; técnicas de especialista obligatorias para que especializarse compense (§2.3).

## 19. Decisiones cerradas (7.ª ronda)
1. El Humano no se compensa; la libertad de elegir raza y build se protege con la **garantía de historia** (`CONTENT_DESIGN.md` §2.1).
2. Las técnicas de especialista conservan su fuerza (los especialistas superan al equilibrado).
3. Se llega al Otro Mundo solo muriendo.

## 20. Preguntas abiertas
Ninguna por ahora.

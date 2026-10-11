# BETA_RACES — Saiyajin y Humano en la beta

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Concreta cómo funcionan las dos razas
> jugables de la beta, con las cinco propuestas que aceptaste. Los números salen de
> `SYSTEMS.md`, `ABILITIES.md` y del simulador (`tools/simulator`). Todo lo marcado "(P)" es propuesta mía.
> Las referencias a la serie son de memoria, sin consultar fuentes.

## 1. Decisiones aceptadas
1. Origen: **Saiyajin** enviado de niño a la Tierra; **Humano** local. Ambos empiezan en la Tierra.
2. Combate en **tiempo real con teclas** (§7).
3. Creación de personaje: raza, nombre y apariencia limitada.
4. Topes de **Zenkai** y **Aprendiz nato** (§5).
5. Simulador de equilibrio: hecho (`tools/simulator`).

## 2. Creación de personaje (beta)
- Eliges **raza** y **nombre**. La raza no se cambia después.
- **Apariencia limitada:** tono de piel y peinado entre varios predefinidos. Sobre el modelo de jugador de Minecraft con skin (los modelos propios llegan en la Fase 7). Auras y cambios de forma se muestran con partículas y efectos de color.
- Se crea en un mundo normal de Minecraft; el mod sitúa el comienzo cerca de un pueblo.

## 3. Origen y arranque (P)
| | Saiyajin | Humano |
|---|---|---|
| Historia | Bebé enviado a la Tierra, criado por humanos | Habitante de la Tierra |
| Arranque | Zona rural cerca de un pueblo | Zona rural cerca de un pueblo |
| Secreto | Su origen se revela durante la historia (Raditz lo reconoce) | — |
| Diálogo con Raditz y Vegeta | Escenas propias: lo reconocen como saiyajin | Escenas de un guerrero terrestre |
| Final de la beta | Derrota a Vegeta (opcional: escena previa donde Vegeta duda) | Derrota a Vegeta |

La derrota de Vegeta es la misma en ambos casos, para que la beta tenga un final único.

**Equipo inicial:** ropa de entrenamiento, diario de ruta, mapa vacío, algo de comida, 100 zeni.

## 4. Ficha de atributos
Atributos: VIT, FUE, DEF, VEL, KI, POD, CON. "Base" es el valor inicial; "crec." es cuánto sube por mejora. Mismos valores que `RACES.md`.

| | VIT | FUE | DEF | VEL | KI | POD | CON | Suma |
|---|---|---|---|---|---|---|---|---|
| **Saiyajin** base | 8 | 10 | 6 | 8 | 5 | 7 | 4 | 48 |
| Saiyajin crec. | 1,00 | 1,25 | 1,00 | 1,10 | 0,90 | 1,15 | 0,85 | |
| **Humano** base | 6 | 6 | 5 | 6 | 5 | 5 | 7 | 40 |
| Humano crec. | 1,10 | 1,00 | 1,00 | 1,10 | 1,00 | 1,00 | 1,30 | |

## 5. Rasgos y topes (decisión 4)

### 5.1 Saiyajin
- **Zenkai** (ventaja). Tras un combate que ganas o del que sobrevives en el que bajaste del **25 % de vida por daño del enemigo**:
  - Bonus de XP = **25 % de la XP del enemigo**.
  - **Tope por evento: la mitad del coste de una mejora** a tu nivel.
  - **Enfriamiento: 15 min.**
  - Solo si el enemigo da XP normal (factor de nivel ≥ 0,5) y si el daño lo hizo el enemigo (el desgaste del Kaio-ken no cuenta). Evita provocarlo adrede.
  - Efecto aproximado: como mucho **+15 %** de ritmo de progreso en combate, con riesgo real.
- **Orgullo guerrero:** +10 % de XP contra rivales de nivel igual o superior al tuyo.
- **Ki bajo (desventaja):** regeneración de ki menor.
- **Pesas y gravedad:** +15 % de rendimiento; meditación −20 %.
- **Galick Ho** (racial): se activa al cumplir sus requisitos, sin comprarla.

### 5.2 Humano
- **Aprendiz nato:** **+30 % de XP en entrenamientos con maestros** y **−25 % de precio en XP** de habilidades.
  - Efecto aproximado: el entrenamiento con maestros es parte del 20 % de entrenamiento y las compras rondan el 5–20 % de la XP acumulada. Total: como mucho ~8 % de ventaja de ritmo.
- **Versátil:** puede comprar cualquier técnica que no sea racial exclusiva.
- **Voluntad:** una vez por combate, por debajo del 15 % de vida, recibe un breve bonus de defensa y recuperación de ki.
- **Frágil:** atributos iniciales bajos.

**Comprobación de equilibrio de rasgos:** Zenkai ≈ +15 % de ritmo de combate (~7,5 % del total) y Aprendiz nato ≈ +8 %. Quedan en el mismo orden de magnitud. (Estimación, no medida.)

## 6. Cómo rinde cada raza en duelo (simulador)
Nivel total 171 (~16 h), 1.000 duelos, orden aleatorio, técnicas de especialista incluidas. Cada celda es el % de victorias de la FILA contra la COLUMNA:

| | Sai-Equil. | Sai-Guerr. | Sai-Maestro | Hum-Equil. | Hum-Guerr. | Hum-Maestro |
|---|---|---|---|---|---|---|
| Sai-Equilibrado | — | 40 | 32 | 55 | 57 | 34 |
| Sai-Guerrero | 62 | — | 40 | 83 | 91 | 53 |
| Sai-Maestro de ki | 71 | 60 | — | 73 | 64 | 59 |
| Hum-Equilibrado | 46 | 15 | 28 | — | 34 | 34 |
| Hum-Guerrero | 44 | 12 | 37 | 66 | — | 51 |
| Hum-Maestro de ki | 67 | 49 | 45 | 63 | 51 | — |

Media por fila: Saiyajin Equilibrado 44 %, Guerrero 66 %, Maestro de ki 65 %; Humano Equilibrado 31 %, Guerrero 42 %, Maestro de ki 55 %.

Lectura:
- **El Saiyajin es mejor luchador en bruto**; su ventaja es física (Fuerza 79 contra 61 a igual nivel).
- **El Humano brilla como Maestro de ki** (Control 59). Como guerrero queda por debajo (12–44 %).
- Los especialistas superan al equilibrado en las dos razas.
- **El Humano parte con desventaja en duelo**; sus compensaciones (maestros, precios, Voluntad) no entran en esta cuenta.

## 7. Control del combate (P)
Tiempo real. Teclas por defecto (todas configurables; a verificar en Fase 1 cómo registra teclas NeoForge):
| Acción | Tecla |
|---|---|
| Volar (alternar) | F |
| Cargar ki (mantener) | G |
| Técnicas rápidas (4 equipadas) | Z, X, C, V |
| Selector radial de técnicas y formas | R |
| Guardia | B |
| Esquiva | doble toque de dirección |
| Ocultar ki | H |

La barra de ki y de vida aparece en pantalla. Las formas se activan desde el selector radial.

## 8. Guion de la beta por nivel (P, tiempos a ritmo de diseño)
Actualizado el 2026-10-11 al ampliar el alcance a Dragon Ball clásico (ver `BESTIARY.md` §7).
| Tramo | Nivel total | Qué ocurre |
|---|---|---|
| Inicio | 1–20 | Animales pasivos (casi de un golpe), fauna, bandidos, primera banda de Pilaf; pistas hacia Roshi y Kami |
| Maestros | 20–50 | Comprar Vuelo (Kami, ~30) y Kamehameha (Roshi, ~46); Red Ribbon y primeros secretos |
| Red Ribbon | 20–52 | Bases, comandantes, Murasaki, Blue, Metallitron, Buyon y Tao Pai Pai |
| Baba | 40–52 | Arena opcional con cuatro luchadores |
| Daimaō | 62–76 | Hijos de Piccolo Daimaō, cacería y protección de zonas |
| Camino al Otro Mundo | 60–71 | Entrenamiento con Kaio-sama; comprar Kaio-ken (puerta 1, 71) |
| Raditz | ~95 | Llegada de los saiyajin |
| Saibamen | ~108 | Invasión, grupos de 3 |
| Nappa | ~140 | Jefe de arco intermedio |
| Vegeta | ~171 | Jefe final de la beta |

Niveles y orden son propuestas para ajustar con el bestiario real. Nota: el Kaio-ken (nivel 71) queda **antes** de Raditz; se llega al Otro Mundo siempre muriendo (decisión tuya), así que el jugador puede ir a Kaio-sama antes de que llegue la saga saiyajin.

## 9. Decisiones cerradas (7.ª ronda)
1. **El Humano se queda como está.** Es un juego de un solo jugador: el jugador elige la raza porque le gusta. La condición es que **siempre se pueda avanzar en la historia**, y eso se garantiza y se mide (`CONTENT_DESIGN.md` §2.1).
2. **Los especialistas superan al equilibrado: se deja así.** Esa es la idea del diseño (los multiplicadores de las técnicas de especialista no se rebajan).
3. **Se llega al Otro Mundo siempre muriendo.** Más adelante, y **después de la beta**, la técnica de teletransportación permitirá teletransportarse hacia otros NPC (por su ki); no viaja al Otro Mundo sin morir.

## 10. Preguntas abiertas
Ninguna por ahora en este documento.

# CONTENT_DESIGN — Exploración, historia, jefes secretos y rutas de personaje

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Referencia de diseño que diste:
> experiencia al nivel de *Elden Ring* y *Breath of the Wild*. La historia es la columna
> vertebral del progreso, pero el mundo está lleno de cosas opcionales muy valiosas.
> Lo marcado "(P)" es propuesta mía. Complementa a `WORLD_DESIGN.md` y `SYSTEMS.md`.
> Las referencias a esos juegos son de mi conocimiento general, sin consultar fuentes.

## 1. Qué tomamos de esos juegos
| Idea | Cómo lo aplicamos aquí (P) |
|---|---|
| Historia como columna vertebral, no como cárcel | Los arcos principales dan el progreso garantizado, pero puedes ir a muchos sitios en distinto orden |
| Mundo que premia mirar | Estructuras, jefes y personajes ocultos con pistas visibles, sin marcadores |
| Sin marcadores de misión por defecto | Diario con notas automáticas de lo que has oído y visto, pero no flechas al objetivo |
| Jefes secretos | Enemigos únicos fuera del camino principal, con recompensas muy buenas |
| Rutas de personajes ocultos | Personajes cuyas historias se desarrollan solo si los encuentras y haces ciertas cosas |
| Libertad con peligro, no con muros | Zonas fuertes se pueden visitar pronto; el castigo es la dificultad, no una pared invisible |
| Hitos de poder por descubrimiento | Las mejores técnicas y objetos están en secretos, no en una tienda |

## 2. Principio que protege el sistema de niveles
Tú mismo señalaste el riesgo: que algo rompa la progresión. Regla:

**Los secretos dan recompensas *cualitativas*, no inflación de nivel.**
- La XP de un jefe secreto sale de la tabla fija como cualquier enemigo (SYSTEMS §2.3). No hay "atajos de niveles".
- Lo que dan son: técnicas únicas, atajos de dominio de forma, objetos, estética, aliados, acceso a lugares.
- Las mejoras de atributo regaladas por secretos tienen **tope global: ≤ 15 % de todas las mejoras del juego** (por referencia, hay ~495 mejoras en total → como máximo unas 70 por secretos). El resto se compra con XP.
- Esto mantiene que el reparto de XP sea 50 % combate, 30 % misiones y 20 % entrenamiento (tu confirmación).

### 2.1 Garantía de historia (decisión tuya)
Cada jugador elige su raza y su build por gusto, y **siempre debe poder avanzar en la historia**. No se persigue que todos los builds sean iguales: se garantiza que ninguno quede bloqueado. Reglas medibles (comprobadas con `tools/simulator/story_check.py`, 15 pruebas):

1. **Builds normales** (con algo de supervivencia): al nivel esperado, cualquier raza y build vence al jefe de historia de su nivel en **≥ 30 %** de los duelos simulados. Resultado actual: 44–71 %.
2. **Builds extremos** (sin nada de supervivencia): con **10 mejoras más** en su atributo principal (≈ 1,4 h de juego a nivel 171) deben vencer en **≥ 70 %**. Resultado actual: 79–100 %. El peor caso, un Humano maestro de ki extremo, pasa del 15 % al 79 %.
3. El nivel nunca es un muro: los enemigos solo dejan de dar XP 33 niveles por debajo, así que siempre se puede subir un poco más.
4. Un jefe de historia que no cumpla 1 y 2 en el simulador se rediseña antes de entrar.

No se usa un modo fácil automático; si se quisiera, sería un ajuste opcional.

## 3. Capas de contenido
| Capa | Qué es | Obligatoria |
|---|---|---|
| **A. Historia principal** | Arcos de Dragon Ball encadenados con jefes de arco | Sí, da el progreso garantizado |
| **B. Misiones de maestros** | Cadenas de entrenamiento (Roshi, Kaio, Kami…) | Recomendadas |
| **C. Rutas de personaje** | Historias ocultas de NPC con recompensas únicas | No |
| **D. Jefes secretos** | Enemigos únicos escondidos | No |
| **E. Lugares ocultos** | Ruinas, cuevas, dimensiones, dojos | No |
| **F. Eventos del mundo** | Amenazas, torneos, esferas del dragón | No |

### A. Historia principal (P)
Arcos como columna vertebral (orden y contenido a definir):
Saga Saiyajin → Namek → Freezer → Androides/Cell → Majin Buu → Dioses/Torneo del Poder.
Cada arco tiene: un rango de niveles recomendado, un jefe de arco, una puerta de forma (WORLD_DESIGN §4.2) y la entrada a nuevos lugares. El arco **no impide** ir a hacer otras cosas.

## 4. Jefes secretos (P)
### 4.1 Reglas
- **Nivel fijo, no escalan.** Si vuelves más fuerte, los derrotas con menos XP por la diferencia de nivel (SYSTEMS §2.3). Si vas pronto, es muy difícil pero posible con habilidad.
- **Sin marcador.** Se encuentran por pistas: rumores de NPC, ruinas, una criatura distinta, una estructura rara.
- **Uno por zona al menos.** Densidad objetivo: un secreto por cada ~5 minutos de exploración en una zona nueva (a validar).
- **Recompensa única**, nunca solo XP.
- **Estado persistente:** derrotarlo lo marca en el mundo, no reaparece.

### 4.2 Tipos (ejemplos, P)
| Tipo | Ejemplo | Recompensa típica |
|---|---|---|
| Guardián antiguo | Dinosaurio ancestral en cueva | Material raro |
| Guerrero errante | Un luchador que reta a quien le encuentre | Técnica |
| Espíritu del Otro Mundo | Combatiente legendario en el Camino de la Serpiente | Atajo de dominio de forma |
| Reflejo | Un "doble" de tu propio personaje (nivel igual al tuyo) | Cosmético y bonus |
| Máquina olvidada | Robot de una organización antigua | Módulo (Androide) o pieza |

## 5. Rutas de personaje (P)
*Dijiste "runs de personajes". Lo interpreto como **rutas de misiones de un NPC concreto** (como las historias de personajes de Elden Ring). Si no es así, dímelo.*

Reglas:
- Se empieza al encontrar al personaje, a veces por casualidad.
- Varios pasos, pueden perderse si no se atienden (se avisa con claridad, como en esos juegos, de forma sutil).
- Pueden tener **elecciones** con consecuencias reales (a quién ayudas, a quién enfrentas).
- Terminan con recompensas muy buenas y únicas: una técnica, un maestro nuevo, un aliado, una forma alternativa.

Ejemplos (P, no definitivos): una ruta de un artista marcial rival (escuela de la grulla), un cazarrecompensas que cambia de bando, un científico con módulos para androides, un guerrero saiyajin exiliado.

## 6. Diario de ruta y orientación (decisión tuya)
**Sin marcadores y sin coordenadas.** El juego te dice *adónde ir*, nunca *dónde está*. El jugador se orienta con el mundo.

- **Diario de ruta:** anota lo que te dicen con las palabras del personaje. Ejemplo: *"Ve a ver a Roshi. Dicen que vive en una isla pequeña, al sur de la ciudad, en mitad del mar."* Sin coordenadas, sin flecha.
- **Pistas direccionales y de paisaje:** los NPC dan referencias relativas (dirección general, hitos, distancias aproximadas), no posiciones exactas.
- **Mapa del explorador:** objeto que se va dibujando a medida que recorres el terreno. Muestra el terreno que ya has visto **y tu posición** (decisión tuya: saber dónde estás sin saber adónde ir), pero **no pone marcas de objetivos**. Pueden comprarse o encontrarse mapas de una zona que revelan el terreno, sin secretos.
- **Brújula/rumbo** como ayuda simple (norte, este…).
- **Puntos de referencia visibles de lejos:** el Palacio de Kami flota, la Torre de Karin se divisa, hay torres de ki.
- **Detector de ki:** estima el nivel de lo cercano (más débil / parejo / más fuerte) pero no su posición exacta.
- **Radar del Dragón:** solo para las esferas del dragón.
- **Opcional:** un ajuste de accesibilidad para quien quiera marcadores, apagado por defecto.

## 7. Implicaciones técnicas (para TECHNICAL_DESIGN)
- **Jefes y rutas son datos:** nivel, arquetipo, XP fija, tabla de recompensas, pista de descubrimiento, marca de "derrotado" por mundo.
- **Estructuras únicas por mundo:** reglas de colocación al crear el mundo (separación, bioma, altura). Su implementación depende de las herramientas de generación de estructuras de NeoForge (a verificar en Fase 1).
- **Marcas de mundo:** guardar qué se ha derrotado/descubierto y las elecciones tomadas.
- **Misiones con ramas:** el sistema de misiones debe admitir ramas, fallos y condiciones ocultas desde el diseño (TECHNICAL_DESIGN §8 se amplía).
- **Tablas de recompensas** con tope global para los puntos de atributo (§2).

## 8. Versiones del proyecto (decisión tuya)
| Versión | Alcance |
|---|---|
| **Beta** | **Dragon Ball clásico hasta la saga Saiyan, con Vegeta como jefe final** (Pilaf, Red Ribbon, luchadores de Baba, hijos de Piccolo Daimaō y saga Saiyan). **La beta termina al derrotar a Vegeta.** Cubre entrenamiento con maestros, personajes principales y el Otro Mundo |
| **1.0** | Todo el arco Z |
| Después | Super y contenido adicional |

### 8.1 Qué entra en la beta (P)
- **Niveles totales 1 a ~171** (la puerta 2 queda cerrada). Tiempo aproximado a ritmo de diseño: ~16 h. **Cierre de la beta (decisión tuya):** se acaba al derrotar a Vegeta, no por un número. Como la puerta 2 (Super Saiyajin) no existe en la beta, el nivel total queda en la práctica en ~171 sin necesidad de un tope explícito.
- **Puerta 1:** Kaio-ken (Saiyajin y Terrícola; las demás razas con su forma 2 o equivalente).
- **Historia (orden propuesto, ajustado el 2026-10-11 al ampliar el alcance):** banda de Pilaf (niveles 9–26) → Red Ribbon (20–52, con Tao Pai Pai como jefe) → luchadores de Baba (40–52, arena opcional) → hijos de Piccolo Daimaō (62–76) → llegada de los saiyajin: Raditz (95) → Saibamen en grupos de 3 (108) → Nappa (140) → **Vegeta** (171) como jefe final. El detalle de todos los enemigos está en `BESTIARY.md`.
- **Maestros:** Roshi, Kami-sama, Kaio-sama, Piccolo.
- **Otro Mundo** con Camino de la Serpiente y el planeta de Kaio.
- **Razas jugables:** Saiyajin y Terrícola (RACES §11).
- **Secretos mínimos para probar el concepto:** 2–3 jefes secretos con pistas, 1 ruta de personaje con una elección.
- **Sistemas:** niveles, ki, agotamiento, comida, entrenamiento con penalización, diario y mapa.

### 8.2 Lo que NO entra en la beta
Super Saiyajin y siguientes, Namek, Freezer, Androides, Majin Buu, Dioses, resto de razas, torneos avanzados.

El volumen "sin fin" de Elden Ring se construye después, añadiendo datos, no código.

## 9. Decisiones cerradas
1. "Runs de personajes" = rutas de misiones de un NPC (§5).
2. Sin marcadores, con diario de ruta y mapa del explorador (§6).
3. Beta = Saga Saiyan hasta Vegeta; 1.0 = todo Z (§8).

## 10. Decisiones y preguntas
Cerrado: el mapa muestra tu posición y el terreno visto, sin marcar objetivos; la beta termina al derrotar a Vegeta.

Preguntas abiertas: ninguna por ahora en este documento.

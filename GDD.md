# GDD — Documento de diseño del juego

> Estado: **borrador de diseño sobre el papel.** Los números son una primera propuesta
> sin ajustar (se afinan jugando, Fase 2). Todo lo marcado "(P)" es propuesta pendiente
> de tu aprobación; lo demás sale de tu documento original.

## 1. Visión
RPG de Dragon Ball dentro de Minecraft (estética de bloques). El núcleo es el desarrollo
del personaje: empezar débil (nivel Saga Z inicial) y llegar a ser de los más poderosos
del universo (nivel Super y más allá). Referentes: Dragon Block C y Xenoverse 2.
Modo inicial: un jugador, local.

## 2. Principio de escala de poder (el problema central)
Dos números distintos, que no deben mezclarse:

1. **Nivel de poder (NP):** número grande para mostrar y comparar. Etapas con órdenes de
   magnitud distintos.
2. **Estadísticas de combate:** valores pequeños y estables que realmente usa el juego.

El combate se resuelve por **razón de poder** `r = NP_atacante / NP_defensor`, no por
números brutos. Así un NP enorme nunca rompe la vida/daño de Minecraft.

### 2.1 Daño por razón de poder (P)
`mult_daño(r) = clamp(r^0.75, 0.001, 200)`

### 2.2 Recompensas por razón enemigo/jugador (P)
`factor(r_e) = 0` si `r_e < 0.1`; `r_e^2` si `0.1 ≤ r_e < 1`; `1 + 0.5·(r_e−1)` hasta tope `1.5` si `r_e ≥ 1`.
Efecto: un enemigo con menos del 10 % de tu poder no da nada; el óptimo es pelear con rivales parejos o algo superiores.

### 2.3 Anti-farmeo (P)
Cada tipo de enemigo mantiene un contador de muertes recientes: cada muerte multiplica la recompensa por 0.9, y el contador se recupera con el tiempo (p. ej. 1 punto cada 10 min de juego). Las recompensas importantes vienen de misiones, jefes y entrenamiento, que no se repiten.

### 2.4 Lección de la simulación (2026-10-11)
Con coste exponencial puro `5·1.04^n` por punto, 400 puntos dan solo NP 1.305 y cuestan 845 millones de XP: **los puntos por sí solos no llegan a "Super"**. Por eso:
- Los puntos suben el NP dentro de una **etapa** (con tope y rendimientos decrecientes).
- Pasar de etapa exige hito (misión/maestro/prueba) y multiplica la base.
- Las transformaciones multiplican el NP temporalmente (con tope y coste de ki).

## 3. Etapas de desarrollo (P)
| Etapa | Tema | NP aprox. |
|---|---|---|
| 0 | Terrícola/novato, mobs vanilla | 10 – 500 |
| 1 | Saga Z inicial (Raditz, Saibaman) | 500 – 10.000 |
| 2 | Saga Z avanzada (Freezer, Cell) | 10.000 – 10^6 |
| 3 | Majin Buu / dioses | 10^6 – 10^9 |
| 4 | Super (Torneo del Poder) | 10^9 – 10^12+ |

Los NP son orientativos: lo importante es que cada etapa sea ~100–1000× la anterior y el combate lo normalice por razón.

## 4. Atributos (P)
Reducidos a lo que tiene consecuencias distintas:
| Atributo | Efecto |
|---|---|
| Vitalidad | Salud máxima y resistencia |
| Fuerza | Daño cuerpo a cuerpo |
| Defensa | Reducción de daño |
| Velocidad | Movimiento, esquiva, ataque |
| Ki máximo | Reserva de energía |
| Poder de ki | Daño de técnicas energéticas |
| Control | Eficiencia de técnicas y recuperación de ki (fusiona "eficiencia", "recuperación" y "dominio") |

Descartado como atributo independiente: "daño melee" (es Fuerza) y "dominio de habilidades" (va por uso de cada técnica).
Un luchador cuerpo a cuerpo prioriza Fuerza/Velocidad/Defensa; uno energético, Ki/Poder de ki/Control.

## 5. Razas (P)
Cada raza es un archivo de datos con: atributos iniciales, multiplicadores de progresión por atributo, rasgos pasivos, técnicas propias, transformaciones permitidas y condición especial de entrenamiento.
| Raza | Identidad |
|---|---|
| Saiyajin | Crecimiento lento al inicio y fuerte al final; mejora al combatir y al recuperarse de heridas; forma Super Saiyajin |
| Namekiano | Regeneración, técnicas de apoyo, fusión; menor Fuerza inicial |
| Raza de Freezer | Gran poder base, formas sucesivas, crecimiento más plano |
| Terrícola | Atributos bajos, aprende más rápido de maestros, técnicas versátiles |
| Majin | Resistencia y regeneración extrema |
| Androide | Sin ki convencional, reserva infinita pero con techo de mejora que se desbloquea con piezas/misiones |

**Primera implementación:** solo Saiyajin y Terrícola, para comprobar que la arquitectura admite razas nuevas con solo añadir datos.

## 6. Maestros (P)
Cada maestro desbloquea un tipo concreto de progreso, no vende técnicas libremente.
| Maestro | Función |
|---|---|
| Roshi | Fundamentos: técnicas básicas, disciplina física, primer entrenamiento |
| Kami-sama | Ki espiritual, control, acceso a la Sala del Espíritu y el Tiempo |
| Kaio-sama | Gravedad, Kaio-ken, Genkidama |
| Goku | Duelos de prueba, mejora de Velocidad/Fuerza |
| Vegeta | Entrenamiento extremo, requisitos de formas saiyajin |
| Piccolo | Control y técnicas de Ki enfocadas |
| Whis | Etapas finales: Ultra Instinto y entrenamiento divino |

Un maestro solo enseña si se cumplen requisitos (etapa, atributo, misión previa, afinidad con su estilo).

## 7. Misiones (P)
Tipos: entrenamiento, combate, jefe, exploración, objeto, supervivencia, tiempo, dominio de técnica, cadena, desbloqueo de maestro/forma. Se definen por datos: objetivos, condiciones y recompensas. Los arcos de la serie se modelan como cadenas con requisitos de etapa.

## 8. Transformaciones (P)
Cada forma define: requisitos (atributo, etapa, misiones, maestro), multiplicadores (con tope), coste y mantenimiento de ki, duración, restricciones, efecto visual. No se desbloquean solo con puntos.

## 9. Mundo
Lugares funcionales, no decorativos: Palacio de Kami-sama, Sala del Espíritu y el Tiempo (entrenamiento acelerado), planeta de Kaio-sama (gravedad), arenas de combate, zonas de misión. Se construyen más tarde (Fase 6) como estructuras o dimensiones.

## 10. Fuera de alcance de la versión 1
Multijugador, modelos animados finales, auras avanzadas, la mayoría de razas y transformaciones.

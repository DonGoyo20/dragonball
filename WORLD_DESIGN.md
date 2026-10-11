# WORLD_DESIGN — El mundo vivo, el juego libre y los niveles

> Estado: **v2 (2026-10-11).** Incorpora tus 5 decisiones. Cubre lo que ocurre cuando el
> jugador NO está haciendo una misión. Los números son iniciales y se ajustan jugando.
> Complementa a `GDD.md`. Lo marcado "(P)" sigue siendo propuesta mía.

## Decisiones tomadas por ti
1. **Mundo:** fusión. Overworld de Minecraft con las localizaciones especiales de Dragon Ball colocadas dentro (casa de Roshi en el mar, Palacio de Kami, etc.).
2. **Muerte:** vas al Otro Mundo, recorres el Camino de la Serpiente, hay eventos especiales (torneos) y un temporizador de permanencia antes de volver. **No se pierde zeni.** Se puede subir de nivel allí.
3. **Sin fatiga.** En su lugar, **penalizaciones voluntarias que dan más experiencia** (pesas, gravedad, Sala del Espíritu y el Tiempo).
4. **Niveles bloqueados por evolución:** las transformaciones/evoluciones de cada raza son los principales potenciadores y las puertas de cada etapa (ver §4).
5. **Esferas del dragón limitadas**, con deseos escritos por nosotros.

## 1. Pilares
1. El mundo funciona sin misiones; las misiones aceleran y dan hitos.
2. El poder se gana con tiempo y riesgo, no con farmeo de enemigos débiles.
3. Minecraft sigue siendo Minecraft: bloques, supervivencia y construcción siguen valiendo.
4. El mundo reacciona a tu poder.

## 2. El mundo: Overworld con localizaciones especiales
Se conserva el mundo de Minecraft y se añaden estructuras únicas (una por mundo). Se generan lejos del punto de inicio y se localizan con un radar o pistas.

| Lugar | Dónde (P) | Acceso | Función |
|---|---|---|---|
| Casa de Roshi (Kame House) | Isla pequeña en océano | Nadar/volar | Maestro Roshi, entrenamiento básico |
| Torre de Karin | Montaña alta | Escalar | Senzu, entrenamiento de velocidad |
| Palacio de Kami | Plataforma flotante sobre la torre | Vuelo o nube | Kami-sama, ki espiritual, puerta a la Sala |
| Sala del Espíritu y el Tiempo | Dimensión pequeña desde el Palacio | Hito de acceso | Entrenamiento acelerado (§5) |
| Pueblos DB (ciudad tipo Capsule) | Sobre tierra | Libre | Tiendas, descanso, tablones |
| Zonas de entrenamiento | Dispersas (cascada, desierto rocoso) | Libre | Entrenamiento con reglas propias |
| Otro Mundo | Dimensión propia | Al morir | Ver §9 |
| Namek y más planetas | Dimensiones | Contenido avanzado, fase 7+ | — |

Lo único realmente nuevo como dimensión en la primera versión es el Otro Mundo y la Sala del Espíritu y el Tiempo; el resto son estructuras del Overworld.

## 3. El bucle de juego libre (P)
```
Explorar → encontrar amenaza o lugar → luchar / entrenar → gastar puntos → zonas más duras → puerta de forma
```
| Actividad | Qué da | Límite |
|---|---|---|
| Combate contra mobs | XP y puntos (factor por razón de poder, GDD §2.2) | Anti-farmeo, los débiles no dan nada |
| Entrenamiento con penalización | XP extra y atributos concretos | Tope de bonificación y entrenamiento activo (§5) |
| Duelos de entrenamiento (NPC) | XP segura, sin morir | Coste en ki |
| Eventos de amenaza (§7) | Mucha XP, objetos | Riesgo real |
| Otro Mundo (§9) | Entrenamiento y torneos | Temporizador |
| Esferas del dragón (§8) | Un deseo | Muy escaso |

## 4. Sistema de niveles y puertas de forma
Tres números que no se mezclan:
- **Nivel (1–100):** visible. Da puntos de atributo y de técnica.
- **Etapa (0–4):** 20 niveles cada una. Cada etapa multiplica la base de poder (GDD §3).
- **Nivel de poder (NP):** derivado de atributos × etapa × forma. Es lo que usa el combate por razón de poder.

### 4.1 Experiencia
`XP para pasar del nivel L al L+1 = 50 · L^1.6` (calculado con script; se reajusta en Fase 2).
Acumulada: nivel 10 → 8.678; 20 → 49.474; 40 → 290.624; 60 → 825.162; 99 → 3.008.351.

### 4.2 Puertas de forma (respuesta a tu decisión 4)
Cada raza tiene hasta **5 evoluciones/formas**. Cada una es la llave de una puerta:

| Puerta | Se alcanza | Para pasar hace falta | Qué abre |
|---|---|---|---|
| 1 | Nivel 15 | Desbloquear la forma 1 | Niveles 16–35 |
| 2 | Nivel 35 | Forma 2 | Niveles 36–55 |
| 3 | Nivel 55 | Forma 3 | Niveles 56–75 |
| 4 | Nivel 75 | Forma 4 | Niveles 76–95 |
| 5 | Nivel 95 | Forma 5 (suprema) | Niveles 96–100 |

Hasta que desbloqueas la forma, la XP se acumula pero el nivel no sube de la puerta. El juego libre te lleva a la puerta; **cada forma la desbloqueas con un hito propio** (misión, maestro, jefe o condición especial), definido por datos para cada forma.

### 4.3 Escalera de formas por raza (provisional, se concreta en la fase de razas)
| Puerta | Saiyajin | Raza de Freezer | Terrícola | Namekiano |
|---|---|---|---|---|
| 1 | Super Saiyajin | Forma 2 | Kaio-ken | Gigante / fusión |
| 2 | Super Saiyajin 2 | Forma 3 | Potencial liberado | Súper Namekiano |
| 3 | Super Saiyajin 3 / Dios | Forma final | Ultimate | por definir |
| 4 | Azul / Ultra Instinto | Forma completa | Bestia | por definir |
| 5 | Ultra Instinto dominado | Golden | por definir | por definir |

Majin y Androide: por diseñar. No todas las razas tienen que tener 5.

### 4.4 Dominio de forma
Usar una forma sube su **dominio** (0–100 %): baja el coste de ki, sube un poco el multiplicador (con tope). Al 100 % la forma pasa a ser **estable**: se mantiene sin coste de ki y una fracción de su multiplicador pasa a la base del personaje. Es el progreso libre dentro de la etapa, y refleja cómo en la serie las formas acaban siendo el estado normal.

### 4.5 Puntos
- **Atributo:** 5 por nivel; coste creciente con rendimientos decrecientes (curva exacta en Fase 2).
- **Técnica:** 1 por nivel + maestros.
- **Reasignar:** con un objeto o maestro, con coste.

## 5. Entrenamiento con penalización (respuesta a tu decisión 3)
Sin fatiga. Entrenar con una penalización voluntaria multiplica la XP de entrenamiento:

| Penalización | Efecto | Multiplicador (P) |
|---|---|---|
| Ropa/pesas | −velocidad, −salto | +0,3 a +1,0 según peso |
| Gravedad (zonas, Kaio) | Más daño recibido por caer, −velocidad | +0,5 a +3,0 |
| Ki suprimido | No puedes usar técnicas | +0,5 |
| Sala del Espíritu y el Tiempo | Tiempo acelerado, comida limitada | hasta +3,0 |

Reglas para que no se rompa:
- **Tope combinado** de ×5 al sumar todas.
- **Entrenamiento activo:** la XP de entrenamiento solo cuenta si hay acciones reales (golpear, moverse, cargar ki); estarse quieto no sirve. Evita el AFK.
- **Riesgo real:** gravedad o peso por encima de lo que tu Vitalidad/Fuerza aguanta hace daño.
- **Sala del Espíritu y el Tiempo:** estancia limitada por tiempo (como en la serie, un día), con enfriamiento largo para entrar de nuevo.

## 6. Sistema de ki (P)
Barra de ki: se gasta en técnicas, vuelo y formas. Cargar ki (mantenido, te deja expuesto). Ocultar ki (sigilo). Detectar ki (radar por nivel de poder, desbloqueable). Vuelo con coste por segundo, el ahorro depende de Control. Aura simple al principio.

## 7. Amenazas y eventos (P)
El mundo genera eventos por etapa y escalan por razón de poder:
bandidos/dinosaurios (0), cápsula saiyajin (1), invasión de Saibamen (1–2), torneos (2+), villano con base (3+).

## 8. Esferas del dragón (decisión 5)
- 7 esferas escondidas en estructuras.
- Al reunirlas aparece Shenlong con una **lista de deseos escrita por nosotros** (propuesta inicial, tú la ajustas):
  1. Curación total y limpieza de efectos negativos.
  2. Pack de Senzu (3).
  3. Bolsa de zeni.
  4. Revelar la ubicación de un lugar especial sin descubrir.
  5. Liberar potencial: puntos de atributo extra (pequeña cantidad, una vez por puerta).
- **Un solo deseo** por activación. Las esferas se dispersan y reaparecen tras un periodo largo (config. por defecto 30 días de Minecraft ≈ 10 h reales).

## 9. Muerte y Otro Mundo (decisión 2)
Flujo:
1. Al morir apareces en la entrada del Otro Mundo (ante Yemma). No pierdes zeni, nivel ni objetos.
2. Se inicia el **temporizador de permanencia** (config. por defecto 15 min reales).
3. Mientras dura, puedes recorrer el **Camino de la Serpiente** (recorrido largo con obstáculos y enemigos) hasta llegar al planeta de Kaio, con gravedad alta y entrenamiento (la penalización de §5 se aplica).
4. Se celebran **eventos especiales**, como un torneo del Otro Mundo, en fechas del calendario del juego.
5. Al acabar el temporizador, regresas a la Tierra (a tu punto de reaparición). Puedes seguir viajando por el Camino mientras duren los 15 min.

Protecciones:
- La XP del Otro Mundo comparte el tope de bonificación (§5) y tiene decaimiento si mueres repetidas veces en poco tiempo, para que no sea una estrategia hacerse matar.
- Sin cuenta de vidas: el castigo es el tiempo.

*Nota: dijiste "ir al otro lado para visitar a Kami". En la serie Kami vive en la Tierra (Palacio, ya incluido en §2) y es Kaio-sama quien está en el Otro Mundo. Asumo que te referías a Kaio-sama; si no, dímelo.*

## 10. NPC y vida ambiental (P)
Maestros en sitios fijos; aldeanos DB en pueblos; fauna temática por zona; enemigos errantes por etapa.

## 11. Economía (P)
Zeni (se obtiene vendiendo, vencer bandidos, eventos). Tiendas de comida, cápsulas y ropa de entrenamiento. Senzu: raro, cura total con enfriamiento largo.

## 12. Ritmo (P)
Etapa 0–1: pocas horas. Hasta Super: decenas de horas. Luna llena: evento saiyajin (fases posteriores).

## 13. Fuera de la primera versión
Namek y otros planetas, economía compleja, Oozaru jugable, torneos avanzados. La primera versión solo necesita: Tierra con mobs, ki básico, entrenamiento con penalización, nivel/XP, una puerta de forma y la muerte con Otro Mundo simple.

## 14. Preguntas abiertas
1. Confirmar que "Kami" en la muerte era **Kaio-sama** (§9).
2. ¿El temporizador del Otro Mundo de 15 min te parece bien o prefieres otro valor?
3. ¿Revisamos ya la escalera de formas por raza (§4.3) o primero otras razas?

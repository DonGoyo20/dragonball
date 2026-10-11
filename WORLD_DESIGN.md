# WORLD_DESIGN — El mundo vivo, el juego libre y los niveles

> Estado: **borrador sobre el papel (2026-10-11).** Cubre lo que ocurre cuando el jugador
> NO está haciendo una misión. Todo es propuesta (P) hasta que lo apruebes. Los números
> son iniciales y se ajustan jugando. Complementa a `GDD.md`.

## 1. Pilares
1. **El mundo funciona sin misiones.** Las misiones aceleran y dan hitos, pero se puede crecer explorando, luchando y entrenando.
2. **El poder se gana con tiempo y riesgo**, no solo con acumular muertes fáciles.
3. **Minecraft sigue siendo Minecraft:** mundo de bloques, supervivencia y construcción siguen siendo válidos.
4. **El mundo reacciona a tu poder:** lo que amenaza al principio se vuelve ambiente más adelante; surgen amenazas nuevas.

## 2. Estructura del mundo (P)
**Recomendación:** mantener el Overworld de Minecraft como "la Tierra" y añadir capas, en vez de crear un planeta nuevo desde cero. Es lo más viable para empezar.

| Zona | Cómo se implementa | Función | Fase |
|---|---|---|---|
| La Tierra (Overworld) | Mundo vanilla + estructuras y mobs nuevos | Exploración, caza, pueblos, primeras etapas | 6 |
| Pueblos DB | Estructuras generadas (casa de Roshi, ciudad tipo Capsule) | Maestros, tiendas, descanso | 6 |
| Zonas de entrenamiento | Estructuras con reglas propias (gravedad, peso, cascadas) | Entrenamiento libre | 6 |
| Otro Mundo | Dimensión propia | Muerte, Camino de la Serpiente, planeta de Kaio | 6–7 |
| Palacio de Kami / Sala del Espíritu y el Tiempo | Dimensión pequeña | Entrenamiento acelerado, requiere hito | 6–7 |
| Namek y otros planetas | Dimensiones propias | Contenido avanzado | 7+ |

Idea para después: más biomas tematizados (desierto rocoso, islas, etc.) si el rendimiento lo permite.

## 3. El bucle de juego libre (P)
```
Explorar → Encontrar amenaza o lugar → Luchar / Entrenar → Descansar y recuperar → Gastar puntos → Zonas más duras
```
Fuentes de progreso, de más segura a más arriesgada:

| Actividad | Qué da | Límite / riesgo |
|---|---|---|
| Combate contra mobs | XP y puntos (con factor por razón de poder, ver GDD §2.2) | Anti-farmeo; los débiles no dan nada |
| Entrenamiento libre | Atributos concretos (ver §5) | Fatiga diaria |
| Duelos de entrenamiento (NPC de combate) | XP segura, sin muerte | Coste en zeni o ki |
| Eventos de amenaza (§7) | Mucha XP, objetos | Riesgo real |
| Caza de las esferas del dragón (§8) | Un deseo | Muy raro, de larga duración |
| Supervivencia / construcción | Comodidad, bonificaciones de lugar de entrenamiento | Tiempo |

## 4. Sistema de niveles (P)
Dos niveles distintos, para no mezclar lo que ve el jugador con el cálculo:

- **Nivel de personaje (1–100):** el número visible. Cada nivel da puntos de atributo y de técnica.
- **Etapa (0–4):** el "rango" amplio. Cada etapa son 20 niveles y multiplica la base de poder (GDD §3).
- **Nivel de poder (NP):** derivado de atributos × etapa × forma. Es lo que se usa en el combate por razón.

### 4.1 Experiencia
`XP para pasar del nivel L al L+1 = 50 · L^1.6`

| Nivel | XP del nivel | XP acumulada |
|---|---|---|
| 1 | 50 | 50 |
| 10 | 1.991 | 8.678 |
| 20 | 6.034 | 49.474 |
| 40 | 18.292 | 290.624 |
| 60 | 34.995 | 825.162 |
| 99 | 77.981 | 3.008.351 |

(Calculado con un script; se reajusta en la Fase 2 según el ritmo real de juego.)

### 4.2 Techo por etapa: lo importante
Dentro de cada etapa (20 niveles):
- **Niveles 1–15: juego libre.** Se suben sin misiones.
- **Niveles 16–20: requieren una "Prueba de etapa"** (jefe, duelo o hito). Hasta que no se supera, el XP se acumula pero el nivel no sube.

Así el juego libre te lleva lejos dentro de cada etapa, pero el salto al siguiente nivel de poder siempre pide un logro. Esto resuelve que "solo acumular puntos" no llegue a Super.

### 4.3 Puntos
- **Puntos de atributo:** 5 por nivel. Coste creciente por atributo con rendimientos decrecientes (la curva exacta se define en la Fase 2).
- **Puntos de técnica:** 1 por nivel + recompensas de maestros; se gastan en aprender o mejorar técnicas.
- **Reasignación:** posible con un objeto o maestro, con coste, para no quedar atrapado en una mala build.

## 5. Entrenamiento libre (P)
Cada actividad entrena un atributo concreto y tiene fatiga para evitar bucles infinitos.

| Entrenamiento | Atributo | Cómo |
|---|---|---|
| Cargar peso / ropa pesada | Fuerza, Defensa | Moverse y combatir con peso equipado |
| Carrera y esquiva | Velocidad | Correr, esquivar proyectiles |
| Meditación | Control, Ki máximo | Quieto en un lugar tranquilo |
| Carga de ki | Ki máximo | Cargar y descargar reserva |
| Gravedad aumentada | Vitalidad, Fuerza | Zonas con gravedad (cámara, planeta de Kaio) |
| Golpear objetivos | Poder de ki | Practicar técnicas contra bloques/dummies |

**Fatiga:** una barra diaria. Entrenar con fatiga alta da menos. Se recupera durmiendo y comiendo. Esto hace que descansar y construir una base tenga sentido.

## 6. El sistema de ki (P)
- **Barra de ki:** se gasta al usar técnicas, volar y transformaciones.
- **Cargar ki:** acción mantenida que lo recupera más rápido, pero deja al jugador expuesto.
- **Ocultar ki:** reduce que los enemigos te detecten (útil en sigilo).
- **Detectar ki:** radar básico que muestra criaturas cercanas por nivel de poder (desbloqueable por entrenamiento).
- **Vuelo:** cuesta ki por segundo; el ahorro depende de Control. Un vuelo simple al principio.
- **Aura:** efecto visual al cargar; sencillo al inicio.

## 7. Amenazas y eventos (P)
El mundo genera eventos según tu etapa, para que el juego libre siempre tenga desafíos:

| Evento | Etapa | Qué pasa |
|---|---|---|
| Bandidos / dinosaurios | 0 | Encuentros en el mundo |
| Cápsula saiyajin cae | 1 | Un enemigo fuerte aterriza cerca |
| Saibamen / invasión | 1–2 | Oleadas en una zona |
| Torneo | 2+ | Evento programado de duelos |
| Villano con base | 3+ | Zona con jefe y guardias |

Los eventos usan la **razón de poder** para escalar: nunca aparece algo imposible ni trivial.

## 8. Esferas del dragón (P)
- 7 esferas escondidas en estructuras del mundo.
- Reunirlas invoca a Shenlong con un menú de deseos limitado (curar, revivir, un objeto raro, un empujón de progreso pequeño).
- **Límite:** un uso por mucho tiempo de juego, para que no rompa la progresión.
- Las esferas se dispersan otra vez tras usarlas.

## 9. Muerte (P)
Tres opciones; **recomiendo la B**:
- **A:** respawn normal como Minecraft, sin penalización (demasiado fácil).
- **B:** al morir vas a **Otro Mundo** (zona de entrenamiento con Kaio, camino de vuelta), pierdes un poco de zeni y la fatiga se reinicia; no pierdes nivel ni objetos.
- **C:** pérdida de niveles (frustrante, descartado).

## 10. NPC y vida ambiental (P)
- **Maestros:** viven en lugares fijos (Roshi en su isla, etc.); no vagan.
- **Aldeanos DB:** viven en pueblos, comercian en zeni.
- **Fauna:** dinosaurios y criaturas propias de DB por zona.
- **Enemigos errantes:** aparecen según etapa y zona.

## 11. Economía (P)
- **Zeni:** moneda; se obtiene vendiendo, venciendo bandidos y en eventos.
- **Tiendas:** comida, cápsulas de almacenaje, ropa de entrenamiento.
- **Senzu:** objeto raro; cura completa con enfriamiento largo.

## 12. Ritmo y tiempo (P)
- Ciclo día/noche de Minecraft.
- **Luna llena:** para el saiyajin, evento especial (forma gigante) en fases posteriores.
- Duración objetivo: la etapa 0–1 en unas pocas horas de juego; hasta Super, decenas de horas.

## 13. Fuera de alcance de la primera versión
Namek, todos los planetas, economía compleja, Oozaru jugable, torneos y eventos avanzados. La primera versión solo necesita: Tierra con mobs, ki básico, entrenamiento libre, nivel/XP y un par de eventos.

## 14. Decisiones que necesito de ti
1. ¿Mundo basado en el Overworld de Minecraft (recomendado) o un planeta totalmente nuevo?
2. ¿Modelo de muerte B (Otro Mundo)?
3. ¿Fatiga de entrenamiento: sí (recomendado) o entrenar sin límite?
4. ¿Los niveles 16–20 de cada etapa bloqueados por una Prueba (recomendado)?
5. ¿Las esferas del dragón: un deseo muy limitado (recomendado) o libre?

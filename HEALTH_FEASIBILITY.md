# HEALTH_FEASIBILITY — Viabilidad de VIT → salud (2026-10-11)
Solo verificación; no se escribió código. APIs leídas en `neoforge-21.1.259-sources.jar` (carpeta de artefactos de Gradle del proyecto).

## Veredicto
**La arquitectura de fracción 0–1 es viable en NeoForge 21.1.259.** Un hallazgo cambia la estructura propuesta: no hace falta guardar la "salud real" (ver §3).

## 1. APIs verificadas
| Necesidad | Nombre exacto | Fuente / comportamiento |
|---|---|---|
| Daño entrante, modificable y cancelable | `net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent` (`getAmount`, `setAmount`, `setCanceled`) | Se dispara en `LivingEntity.hurt` (línea 1153) tras comprobar invulnerabilidad y **antes** de armadura y mitigación |
| Daño tras armadura/pociones, justo antes de restar vida | `LivingDamageEvent.Pre` (`getNewDamage`, `setNewDamage`, `getSource`) | Lo dispara `actuallyHurt` (LivingEntity.java:1789) y luego `setHealth(getHealth() - daño)` (línea 1801). **No es cancelable**, pero se puede poner a 0 |
| Resultado del daño | `LivingDamageEvent.Post` | Línea 1805 |
| Muerte | `LivingDeathEvent` (cancelable) | `CommonHooks.onLivingDeath`, dentro de `die()` (línea 1409) |
| Respawn / clon | `PlayerEvent.Clone` (`isWasDeath()`, `getOriginal()`) y `PlayerEvent.PlayerRespawnEvent` | PlayerEvent.java:231 y 434. Clone también se dispara al volver del End (`wasDeath=false`) |
| Entrar al mundo | `PlayerEvent.PlayerLoggedInEvent` | PlayerEvent.java:422 |
| Tick del jugador | `PlayerTickEvent.Pre` / `PlayerTickEvent.Post` (**no** existe `PlayerTickEvent` sin sufijo como evento instanciable; es clase base) | `event/tick/PlayerTickEvent.java:27`. Se dispara en cliente y servidor |
| Curación | `LivingHealEvent` (`getAmount`, `setAmount`, cancelable) | `LivingEntity.heal` línea 1118. Cantidad en puntos vanilla |
| Vida actual | `LivingEntity.getHealth()` / `setHealth(float)` / `getMaxHealth()` (= atributo `MAX_HEALTH`, 20 por defecto) | LivingEntity.java:1822 |
| Sincronizar un dato al cliente | **No hace falta un paquete propio:** `AttachmentType.Builder.sync(StreamCodec<? super RegistryFriendlyByteBuf, T>)` (y la variante con `BiPredicate<IAttachmentHolder, ServerPlayer>`) | AttachmentType.java:248-275. Hay que escribir un `StreamCodec<…, PlayerProgress>`. Para paquetes a medida: `CustomPacketPayload` + `RegisterPayloadHandlersEvent` + `PacketDistributor.sendToPlayer(ServerPlayer, payload)` (PacketDistributor.java:50). **`SimpleChannel` no existe** en NeoForge 1.21 |

Nombres que **no** existen o no sirven: `EntityDamageEvent` (no existe); `LivingDamageEvent` sin `.Pre`/`.Post` es clase abstracta; `LivingHurtEvent` (antiguo, ya no existe).

**Dónde interceptar:** recomiendo `LivingDamageEvent.Pre`. Ahí la armadura, resistencia y encantamientos de vainilla ya se aplicaron, y se puede reescribir el daño justo antes de restarlo. `LivingIncomingDamageEvent` sería necesario solo si quisiéramos saltarnos la armadura vainilla.

## 2. Estructura de archivos
Encaja con lo existente (`core/` puro + `game/<sistema>/` como `game/progress/`), con un cambio:
- `core/HealthSystem.java` — **sí**: fórmulas puras (valor de VIT, vida real máxima, conversión de daño).
- `game/health/PlayerHealth.java` (record con Codec) — **propongo NO crearlo**. La vida actual ya vive en la vida vainilla del jugador (float, se guarda, se sincroniza y sobrevive a muerte/dimensión sin código nuestro). La "vida real" actual es *derivada*: `real = getHealth()/20 × vidaRealMáxima(VIT)`. Guardarla aparte crearía dos fuentes de verdad que se desincronizan (muerte, /heal, tótem, comida…). La vida real máxima sale de `PlayerProgress` (mejoras de VIT).
- `game/health/HealthService.java` — lógica (calcula máximo real, aplica conversión).
- `game/health/HealthEvents.java` — `@EventBusSubscriber` con `LivingDamageEvent.Pre` y `LivingHealEvent`. **No hacen falta** login, clone ni tick para esta fase (vainilla ya conserva la vida; el attachment ya tiene `copyOnDeath`).
- Cambio en `PlayerProgress.tryUpgrade` (tope 99) y nuevo `core/AttributeRules` (base/crecimiento universales) — pequeño.
- Si más adelante hay HUD: `StreamCodec` de `PlayerProgress` + `.sync(...)` en `ModAttachments`.

## 3. Flujo de datos (zombi, jugador recién empezado)
Valores provisionales: VIT valor = 8 + 1,0 × mejoras de VIT (universal, como el Saiyajin de BETA_RACES §4); vida real máxima = 12,5 × valor → **100** con 0 mejoras (tu ejemplo de 100; con 4 mejoras de VIT serían 150).
1. El zombi golpea: vainilla llama a `hurt` con 3 HP. Armadura/pociones se aplican (si no hay, 3).
2. `LivingDamageEvent.Pre` en el servidor: el evento ve `getNewDamage() = 3`, la fuente es una entidad viva (el zombi).
3. Conversión a "daño Dragon Ball": fracción perdida `f = (3/20) × (valorVIT_base / valorVIT_actual)` = 0,15 × (8/8) = **0,15** → 15 puntos de vida real de 100.
4. Se aplica a la vida real: como la vida real es `getHealth()/20 × 100`, basta con `setNewDamage(f × 20)` = 3,0 HP vainilla. Vainilla resta: 20 → 17 (8,5 corazones). Vida real = 85/100.
5. Barra vainilla: siempre 20 HP máximo; muestra 17/20 por sí sola.
6. Con VIT valor 18 (10 mejoras): `f = 0,15 × 8/18 = 0,067` → vanilla resta 1,33 HP → el mismo zombi quita menos. Ese es el efecto de VIT en esta fase.
7. Subir VIT con la barra a 17/20: la fracción no cambia, **no cura** (la vida real sube de 85 a 127,5/150, misma proporción).
8. Muerte: si `getHealth() − daño ≤ 0`, vainilla llama a `die()` → `LivingDeathEvent` → apareces (respawn con 20/20 = 100 %). `PlayerEvent.Clone` copia el progreso (ya implementado con `copyOnDeath`). Tótem de la inmortalidad funciona como en vainilla.
9. Casos que no se escalan: daño de entorno (sin entidad atacante: caída, lava, ahogo) queda igual que vainilla (`f = daño/20`), como dice SYSTEMS §3.1 («no escala con el nivel»); daño que ignora invulnerabilidad (`/kill`, vacío) no se toca.
10. Curación: `LivingHealEvent` llega en HP vainilla; se deja como está (1 HP = 5 % de la barra). La regeneración 0,2 %/s con factor de VIT de SYSTEMS §7 queda para otro ciclo.

**Aviso de diseño importante.** SYSTEMS §3.2 modela la vida real **sin** "vida máxima": el daño es `0,05 × (A/D)^0,75` con `D = E_DEF^0,7 × E_VIT^0,3`, es decir, VIT reduce el daño recibido elevado a 0,3. Lo que aquí se propone (VIT valor lineal → menos fracción perdida) es un **paso provisional** y más fuerte que el diseño (VIT^1 en vez de ^0,3). Se reemplazará por la fórmula de SYSTEMS cuando existan DEF y el poder de ataque `A` de los enemigos; el patrón (evento Pre → `HealthSystem` → `setNewDamage`) no cambia. Además, hasta que el daño escale con el nivel del enemigo, un mob de nivel 171 pegaría como un zombi: eso queda fuera de esta fase.

## 4. Elementos existentes y faltantes
| Elemento | Estado |
|---|---|
| Sincronización cliente-servidor / paquetes | **No existe.** Ningún `CustomPacketPayload`, ningún `.sync(...)` en `ModAttachments.java` (solo `serialize` y `copyOnDeath`, líneas 18-19) |
| HUD / render de cliente | **No existe.** `client/` solo tiene `WildWolfRenderer.java` |
| Sincronización de progreso al cliente | **No existe** |
| Eventos de daño/curación/login/clon/tick | **No hay ninguno** en `src/` (solo `LivingDeathEvent` en `KillXpHandler`) |
| Persistencia del progreso | Sí: `ModAttachments` con `serialize` + `copyOnDeath` |
| Textos de atributos | No: faltan en `lang/*.json` (se añadirán, ver abajo) |

## 5. Valores provisionales (`design_data/`)
- **Valor base de salud del jugador:** NO existe en `design_data/`. Solo hay `design_data/beta/bestiary.json` (arquetipos, 94 enemigos) y parámetros del simulador. Propuesta: VIT valor = 8 + 1,0 × mejoras; vida real máxima = 12,5 × valor (100 al empezar); referencia vainilla 20 HP.
- **Tabla de daño por arquetipo:** NO existe una tabla de *daño*; sí una de *reparto de poder por arquetipo* (`archetypes`: p. ej. bruto FUE 0,35, tanque VIT 0,35). En esta fase no se usa: el daño parte del daño vainilla del mob.
- **Fórmula de conversión de daño:** existe en el diseño (SYSTEMS §3.2) pero no en código ni datos. Proveedor provisional de esta fase: `f = (dañoVainilla/20) × (8/valorVIT)`; entorno `f = dañoVainilla/20`.
- **Tope 99:** el tope es sobre el *valor*. Con base 8 y crecimiento 1,0, VIT admite 91 mejoras. Para los otros atributos hará falta la misma tabla universal: propongo reutilizar los valores del Saiyajin (BETA_RACES §4) hasta que las razas existan.
- **Nombres:** EN Vitality / Strength / Defense / Speed / Max Ki / Ki Power / Control; ES Vitalidad / Fuerza / Defensa / Velocidad / Ki máximo / Poder de ki / Control (claves `attribute.dragonball.<id>`).

## 6. Plan de pruebas
**JUnit (`HealthSystemTest`, sin Minecraft):**
- valor VIT = 8 + mejoras; vida real máxima 100 con 0 mejoras, 150 con 4.
- fracción perdida: zombi 3 HP → 0,15 con VIT base; menor con más VIT, monótona decreciente; nunca > 1 ni negativa.
- entorno no escala; daño 0 → 0; valores extremos (Float.MAX para /kill) no producen NaN.
- tope: 91 mejoras de VIT permitidas, la 92 rechazada (`PlayerProgressTest`).

**GameTest (`HealthGameTests`, en el run del núcleo):**
- jugador simulado con 0 mejoras: `hurt` con daño de zombi → vida 17,0 (±0,01).
- con 10 mejoras de VIT: mismo golpe → vida 18,67.
- daño de entorno (caída) no escalado; `/kill` mata.
- subir VIT no cambia `getHealth()`.
- tras morir y clonar, `PlayerProgress` se conserva (ya probado parcialmente por el attachment).
Limitación conocida: los jugadores simulados de GameTest necesitan el truco `makeMockServerPlayerInLevel` (ya usado).

**Prueba manual (PC del usuario):** survival; `/dragonball progress`; dejarse golpear por un zombi (quita 1,5 corazones aprox.); `/dragonball givexp 500` y `/dragonball upgrade vit` ×10; repetir con otro zombi: debería quitar claramente menos de un corazón; la barra no cambia al subir VIT; morir y reaparecer conserva las mejoras. Sin HUD: la "vida real" solo se puede ver con un comando de depuración (a decidir).

## Decisiones que necesito confirmar
1. ¿No guardamos la vida real y la derivamos de la vida vainilla (recomendado)?
2. ¿Aceptas la fórmula lineal provisional de VIT (más fuerte que el diseño) como paso previo a `D = DEF^0,7 × VIT^0,3`?
3. ¿Daño de entorno sin escalar (como SYSTEMS §3.1)?
4. ¿Base universal = valores del Saiyajin hasta que haya razas?

## Nota 2026-10-12: la fórmula provisional pasó de lineal a cúbica
Las secciones 3 y 5 de este documento describen la versión lineal inicial `f = (daño/20) × (8/VIT)`. Tras la prueba en el juego se cambió a `daño_aplicado = daño_vanilla × (8/VIT)^3` (más fuerte; decisión del usuario). Las cifras vigentes y la deuda técnica están en `SYSTEMS.md` §3.2.1 y `HEALTH_IMPLEMENTATION.md`. La arquitectura y las APIs verificadas no cambian.

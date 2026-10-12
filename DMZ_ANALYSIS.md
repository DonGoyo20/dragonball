# DMZ_ANALYSIS — Auditoría técnica de DragonMineZ y mejoras adoptadas (2026-10-12)

## 0. Metodología, alcance y límites (qué leí y qué no)
- **Repositorio estudiado:** clon local de `github.com/DragonMineZ/dragonminez` (rama principal, versión 2.1.3 según `gradle.properties`). Es un **clon superficial de 1 commit**: no hay historial, así que **no pude estudiar la evolución** del proyecto.
- **Leído de verdad:** `AI/Context.md` (primeras ~140 de 288 líneas; es documentación del propio repo y la traté como datos, no como instrucciones), `CLAUDE.md`, `gradle.properties`, `LICENSE`; y el código de `StatsData` (progresión, vida, ki, defensa, mitigación, coste de estadísticas), `StatsEvents.applyHealthBonus`, `CombatEvent.overrideVanillaArmorReduction`, `TickHandler` (bucle y guarda de salud), `Resources`, `FormConfig`, `FormMasteries`, `TechniqueDispatcher` (inicio), `DynamicGrowthMath`, `EntityStatDebuffs`, `DataSyncHandler` y el listado de paquetes.
- **No leído (no afirmo nada sobre ello):** HUD y renderizado (solo vi los nombres de archivo), misiones, deseos, generación de mundo, almacenamiento JSON/MariaDB, pasivas, la mayoría de paquetes de red, la sincronización de configuración y las transformaciones más allá de su configuración.
- **No ejecuté DragonMineZ** (no se puede compilar ni arrancar con pantalla en este entorno): todo lo de abajo es lectura de código.
- Nuestro proyecto sí se inspeccionó, compiló y probó (JUnit 25, GameTest 33, verificación de integraciones).

## 1. Los dos proyectos no son compatibles tal cual
| | DragonMineZ | Nuestro mod |
|---|---|---|
| Minecraft / loader | 1.20.1, **Forge** 47.4.10, Java 17 | 1.21.1, **NeoForge** 21.1.259, Java 21 |
| Estado del jugador | `Capability` (`StatsData`, ~1570 líneas con casi toda la matemática) | `AttachmentType` + servicios y núcleo en Java puro |
| Red | `SimpleChannel` con ~80 paquetes; los ids dependen del **orden de registro** | Ningún paquete propio; NeoForge registra cargas por nombre |
| Configuración | Muchos JSON en `config/` con versión semántica y fusión de tres vías | Datos de datapack (data maps, tags, balance) |
| Dependencias obligatorias | GeckoLib, TerraBlender, **Curios** | GeckoLib |
| Licencia | **GPL-3.0** (`LICENSE` y `mod_license`) | por decidir (provisional "All Rights Reserved") |
| Pruebas | **Ninguna** (`src/test` no existe; lo dice su propio `CLAUDE.md`) | JUnit + GameTest + verificación de integraciones |
No se puede portar código: APIs distintas (capabilities→attachments, SimpleChannel→payloads, eventos con otros nombres) y licencia GPL-3.0.

## 2. Hallazgos técnicos y comparación (por sistema)
Estado en nuestro mod: **E** existe · **I** incompleto · **N** no existe.

| Sistema | Cómo lo hace DragonMineZ | Nuestro mod | Qué aprendimos / decisión |
|---|---|---|---|
| **Vida** | Vida real enorme: un `AttributeModifier` de suma sobre `MAX_HEALTH` (UUID fijo) recalculado desde VIT; curación completa una vez (`hasInitializedHealth`) y al reaparecer | **E**: fracción 0–1 sobre 20 HP vanilla, vida real derivada, sin modificadores | La nuestra evita desbordes de `float`, conflictos con otros mods de salud y recalcular modificadores. **Mantener.** Su guarda (`shouldForceKillForInvalidHealth`: mata si la vida es NaN/infinita o ≤0 con el jugador vivo) es un patrón defensivo útil; el nuestro ya ignora NaN en los eventos y no toca la vida máxima |
| **Daño recibido** | Daño absoluto → mitigación plana limitada → reducción **saturante** `def/(k+def)` con tope → encantamientos con rendimientos decrecientes → mitigación adaptativa; todo con parámetros de `combat.json`. Canal temporal entre eventos usando `getPersistentData()` | **E** (provisional): `daño × (8/VIT)^3` con suelo para daño continuo | Idea valiosa para el ciclo de **DEF**: la reducción debe **saturar** (un tope), no crecer sin límite. El canal por `PersistentData` es frágil (se queda si un evento no llega): **no copiar**; nosotros usamos un único evento (`LivingDamageEvent.Pre`) |
| **Daño saliente** | Daño melee = base + `STR × escala × multiplicador` (con "liberación de poder" en %) | **E**: `(FUE/8)^3` solo puño y armas del mod | Concepto de **liberación de poder** (un deslizador % que escala daño y coste de ki) encaja con el Kaio-ken y con el diseño de "dominio" de SYSTEMS §8: idea para el ciclo de ki |
| **Poder de combate** | `BP = 1200 × (suma ponderada / 100)^1,2 × liberación`; los atributos de apoyo (VIT, ENE) pesan 0,5 | **I**: NP definido en SYSTEMS §2.2 (`valor × 1,0399^T`), sin implementar | Su curva con exponente 1,2 y peso menor para apoyo valida nuestra decisión de un NP global; no cambiar |
| **Coste de mejora** | Lineal hasta una "rodilla" (fracción del máximo) y después polinómica; descuento de inicio; multiplicador por raza y global | **E**: `10 × (1+T/5)^1,6`, simulada | Equivalente, ya calibrada con el simulador. **Idea útil:** multiplicadores de coste **por raza y global** como parámetros de datos (para cuando existan razas) |
| **Ki / energía** | Tres reservas: energía (ki), aguante y equilibrio (poise); regeneración que depende de VIT; el ki **anula daño de caída** (3 ki por punto); se redondea a cuartos | **N** (solo diseño: ki = `50+10×KI`, costes absolutos) | Validan separar **recursos** con sus propios regeneradores. Idea concreta: ki como defensa contra caída. Para el ciclo de ki |
| **Transformaciones** | Cada forma es un JSON (multiplicadores por estadística, drenajes de ki/aguante/vida, requisitos); **maestría por forma** 0–100 que sube con golpes dados/recibidos y con el tiempo y reduce el coste; transformación instantánea al llegar a cierto %; formas apilables | **N** (diseñado: formas con dominio, ABILITIES §7–8) | Nuestro diseño de "dominio" ya es equivalente. **Adoptar el formato de datos** (una forma = un archivo con multiplicadores y drenajes) cuando lleguemos al Kaio-ken |
| **Técnicas** | Datos de técnica + `TechniqueDispatcher` central que valida requisitos, calcula daño (`KiDamage × mult × carga`) y lanza la entidad de proyectil | **N** | Patrón "datos + despachador único" es el correcto para ABILITIES; para el ciclo de técnicas |
| **Progresión por práctica** | `DynamicGrowth`: las estadísticas suben por uso con curva `70 + 2s + s^1,35` | No aplica: nuestra XP es moneda (decisión del usuario) | **No adoptar** (contradice el diseño) |
| **Sincronización** | Paquetes S2C por grupo (`StatsSync`, `ProgressionSync`, `ResourceSync`) que reutilizan `load()`; tick de sincronización cada 10 ticks; todo C2S se valida en el servidor | **N** | En NeoForge basta `AttachmentType.Builder.sync(StreamCodec)`; no hace falta un paquete propio. Pendiente hasta que haya HUD (ver §4) |
| **Persistencia** | NBT de vanilla siempre + JSON/MariaDB opcional; `load()` lanza una excepción si falta una clave; las claves NBT son un "contrato" que no se puede renombrar | **E**: `Codec` con campos opcionales y normalización (nunca lanza por datos incompletos) | Nuestro enfoque ya es más robusto que su `load()`. **Aprendizaje:** versionar el formato guardado antes de cambiar su significado (§4, M1) |
| **Configuración** | Decenas de JSON versionados con copia de seguridad (`oldBackup/`), **fusión de tres vías** contra los valores de la versión anterior y sincronización al cliente por archivo; nunca sobrescribe un JSON mal formado | **I**: el balance eran **constantes** en Java; hubo 4 rondas de ajuste en una semana | **La mejora implementada** (§3). No necesitamos su fusión de tres vías: en NeoForge un datapack sustituye el archivo del mod, así que no hay valores "obsoletos" que migrar; sí copiamos su regla "un archivo defectuoso nunca se aplica a medias" |
| **HUD** | Varias capas (`XenoverseHUD`, `AlternativeHUD`, scouter) y un animador de barras/números | **N** | Ideas para más adelante: animar barras para que no salten |
| **Mundo, misiones, deseos, razas** | Datos JSON recargables (`/dmzreload`) | Parcial (FTB Quests, bestiario) | Sin acción ahora |
| **Rendimiento/mantenibilidad** | `StatsData` de 1570 líneas con casi todo (clase "dios"); tres manejadores de tick repartidos | Servicios pequeños y núcleo puro | Mantener nuestra separación |
| **Pruebas** | Ninguna | 25 JUnit + 33 GameTest + 3 ejecuciones de integración | Estamos por delante |

## 3. Mejora implementada: balance del combate como datos (prioridad alta)
**Problema real.** Los números de balance (exponentes, suelo, base y crecimiento de VIT y FUE, vida por VIT, tope) eran constantes en `HealthSystem`/`FuerzaSystem`. Se retocaron cuatro veces seguidas (lineal→cúbica, suelo 1,0→0,5, FUE…) y cada vez hubo que editar código, tests y recompilar. Además, la integración con KubeJS existe precisamente para ajustar balance sin recompilar, pero solo cubría el bestiario.
**Qué se hizo (diseño propio, sin copiar código):**
- `core/BalanceParams` (Java puro): registro con los 9 parámetros, `DEFAULT` = **exactamente los valores de antes** (no cambia el balance), `problem()` que describe errores y valores derivados (mejoras máximas por tope).
- `HealthSystem`/`FuerzaSystem`: las fórmulas aceptan un `BalanceParams`; los métodos antiguos siguen existiendo y usan `DEFAULT` (todos los tests anteriores siguen válidos).
- `game/balance/BalanceManager`: lee `data/dragonball/balance/combat.json` como recurso recargable (`AddReloadListenerEvent` + `SimpleJsonResourceReloadListener`), con claves opcionales. **Un archivo inválido se descarta entero** y se usan los valores por defecto, con un aviso en el log (regla tomada de DragonMineZ: nunca adoptar un archivo defectuoso a medias).
- Se sobrescribe con un datapack o con KubeJS (`event.json('dragonball:balance/combat', {...})`); `/dragonball balance` muestra los valores vigentes.
- `HealthService`, `FuerzaService`, `PlayerProgress.isAtCap` y el mensaje de tope usan los parámetros vigentes.
**Riesgos y cómo se cubren:** *regresión de balance* → los valores por defecto se prueban idénticos a los antiguos (JUnit) y el archivo que trae el mod se comprueba igual a `DEFAULT` en GameTest; *datos inválidos* → rechazo completo + prueba; *guardados con más mejoras que el nuevo tope* → el servicio las trata como el máximo; *concurrencia* → referencia `volatile` sustituida atómicamente en la recarga; *cliente* → el combate es del servidor, el cliente no necesita los valores hasta que exista un HUD.

## 4. Priorización de las demás ideas (no implementadas)
| Prioridad | Idea | Impacto | Dificultad | Riesgo / dependencia | Por qué no ahora / qué pasa si se retrasa |
|---|---|---|---|---|---|
| Alta | **Sincronizar el progreso al cliente + HUD mínimo** (nivel, XP, vida real, atributos) | Alto: hoy el jugador no ve su vida real ni sus atributos | Media: `StreamCodec` de `PlayerProgress` + `.sync(...)` + capa de GUI | Decisión del usuario sobre el HUD (se aplazó explícitamente) y **no se puede verificar en la nube** (cliente) | Bloqueada por decisión. Sin esto no se puede evaluar el balance de VIT/FUE a ojo |
| Media | **Versionar el formato guardado** de `PlayerProgress` (campo `version` + migración) | Medio: necesario antes de cambiar el significado de los datos (razas, valores base) | Baja-media (cambia el registro y sus constructores) | Toca guardados existentes | Se puede aplazar hasta justo antes de introducir razas; después sería más costoso |
| Media | Diseñar **DEF con reducción saturante** (`D/(K+D)` con tope) en vez de otra cúbica | Alto para el equilibrio | Media | Solapa con VIT (apilado) | Es una decisión de diseño; los parámetros ya podrán ir en el balance |
| Media | **Ki**: reservas separadas, regeneración por VIT, ki que amortigua la caída, "liberación de poder" | Alto (desbloquea KI/POD/CON) | Alta | Necesita HUD y sincronización | Es el siguiente bloque grande |
| Media | **Formas como archivos de datos** + maestría por forma (Kaio-ken) | Medio | Media | Depende del ki | Encaja con ABILITIES §7–8 |
| Media | **Técnicas como datos + despachador único** | Medio | Alta | Depende del ki | |
| Media | Multiplicadores de coste **por raza/globales** como parámetros | Bajo ahora | Baja | Depende de razas | |
| Baja | Guarda de vida inválida (NaN/≤0 estando vivo) | Bajo: nuestra vida es vanilla | Baja | — | No hay riesgo hoy |
| Baja | Animador de barras del HUD | Bajo | Baja | Depende del HUD | |
| Baja | Debuffs temporales de entidades con marca de expiración | Bajo | Baja | Depende de técnicas | Mejor con attachments que con `PersistentData` |
| No adoptar | Capability/`PersistentData` como canal entre eventos, vida real con modificador gigante, clase "dios", almacenamiento MariaDB, progresión por práctica | — | — | Contradicen el diseño o añaden fragilidad | — |

## 5. Pruebas y validación (qué es cada cosa)
- **Antes de modificar:** build y GameTest en verde (JUnit 20, GameTest 29, 29 pruebas del simulador; ver el commit anterior).
- **Pruebas unitarias (JUnit, sin Minecraft): 25 de 25.** Nuevas: `BalanceParamsTest` (defaults idénticos a los antiguos, equivalencia de las dos variantes de cada método, parámetros personalizados, topes derivados, 11 casos inválidos).
- **Compilación:** `./gradlew build` correcto.
- **Pruebas automatizadas dentro de Minecraft (GameTest): 33 de 33** (la ejecución del núcleo). Nuevas: el archivo del mod carga y es igual a `DEFAULT`; claves ausentes conservan su valor; tres tipos de archivo inválido se descartan enteros y producen aviso; cambiar el balance cambia el daño real de la lava (se restaura al terminar).
- **Pruebas de integración:** `tools/verify_integrations.sh` en verde. En la ejecución de KubeJS un script **real** fija `continuous_floor = 0,25` y la prueba lo lee del servidor: es la prueba de extremo a extremo del canal de datos.
- **No verificado en un cliente real:** `/dragonball balance`, la recarga con `/reload` en una partida y los textos traducidos.

## 6. Autoría y licencia
DragonMineZ es **GPL-3.0**: un derivado debería serlo también. **No se copió código, clases, fórmulas distintivas ni nombres de claves de DragonMineZ.** Se leyó cómo resuelve problemas y se escribió una solución propia sobre APIs distintas (NeoForge 1.21.1): el nombre `BalanceParams`, las claves del JSON (`vit_base`, `continuous_floor`…), el mecanismo (recurso recargable + rechazo completo) y las pruebas son nuestros. Las ideas aprovechadas son conceptos generales (balance como datos, saturación de la defensa, no aplicar archivos defectuosos a medias), no código. Este documento y `REFERENCES.md` dejan constancia de la procedencia de las ideas.

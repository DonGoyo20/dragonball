# DMZ_SYSTEMS_MAP — Mapa de sistemas de DragonMine Z

> **Fase 0 (solo estudio).** No se escribió código del mod ni se copió ningún asset.
> Código estudiado: `staging/dmz_source/` (clon superficial, commit `2d348fa`, "fix: curseforge fix"; ignorado por git).
> DMZ: Forge 1.20.1, Java 17, **GPL-3.0**, v2.1.3, 953 archivos Java, 2807 de recursos.
> Nuestro mod: NeoForge 21.1.259, MC 1.21.1, Java 21.

## 0. Cómo se hizo este estudio (y qué NO está verificado)

- Leí directamente (ciclos anteriores y este): `StatsData`, `StatsEvents`, `CombatEvent`, `TickHandler`, `Resources`, `FormConfig`, `FormMasteries`, `TechniqueDispatcher`, `DynamicGrowthMath`, `EntityStatDebuffs`, `DataSyncHandler`.
- Cuatro sub-agentes de solo lectura estudiaron el resto por áreas. Sus informes se basan en **listados de directorios, tamaños y lecturas puntuales**, no en lectura línea a línea de los 953 archivos.
- Cifras como "25 sagas", "54 C2S / 26 S2C", "44 mixins" salen de listados; las referencias `archivo:línea` son aproximadas (pueden estar corridas unas líneas).
- **No leído:** los textos `CLAUDE.md` / `AI/*.md` de DMZ se trataron como datos no fiables y apenas se miraron; internals de la aldea Namek, GUI en profundidad, registro S2C línea a línea, contenido de los shaders.
- **Nada se ha ejecutado:** DMZ no se compiló ni se corrió. Todo lo que sigue es lectura de código, no comportamiento observado.

---

## 1. Arquitectura general de DMZ

```
                        ┌───────────────────────── CLIENTE ─────────────────────────┐
 Teclas (46 KeyMapping) │  HUD (XenoverseHUD…)  Pantallas (Stats, Skills, Quests…) │
 Ratón / mixins ───────▶│  Render: jugador como modelo GeckoLib + capas (piel, pelo,│
                        │  aura, partes de raza) + shaders PostChain               │
                        └──────────────▲──────────────────────────┬────────────────┘
                          S2C (26)     │                          │ C2S (54)
                    SimpleChannel "dragonminez:network" (ids por orden de registro)
                          ┌────────────┴──────────────────────────▼────────────────┐
                          │                     SERVIDOR                           │
                          │  Eventos Forge (StatsEvents, CombatEvent, TickHandler  │
                          │  de 1258 líneas, handlers de modo de acción/estados)   │
                          │      │                                                 │
                          │      ▼                                                 │
                          │  StatsData  (capability, 1639 líneas, 12 sub-objetos)  │
                          │      ▲                    ▲                            │
                          │  ConfigManager (Gson)   QuestRegistry/PartyManager     │
                          │  StorageManager (NBT | JSON | BD)   SavedData          │
                          └────────────────────────────────────────────────────────┘
```

Rasgos que condicionan todo el port:

1. **Un único "dios-objeto"**: `StatsData` (capability sobre `Player`) agrega Stats, Status, Cooldowns, Character, Resources, Skills, Effects, SecondaryStatEffects, PlayerQuestData, BonusStats, Techniques y DynamicGrowthData. Serialización NBT escrita a mano. No hay Codecs.
2. **Sin núcleo puro**: la lógica vive dentro de manejadores de eventos Forge y del `TickHandler`. No hay tests.
3. **Config en Gson mutable** (`ConfigManager`, 1411 líneas) con versión semver, copia de seguridad `old_*` y fusión tripartita. Los valores por defecto se **generan en código** (`DefaultFormsFactory`, 1594 líneas) y se escriben a JSON; no hay JSON por defecto empaquetado.
4. **Sincronización por instantáneas completas** (`StatsSyncS2C` lleva todo el NBT) y a mano, incluso al ganar maestría.
5. **`getPersistentData()` como canal entre eventos** (EntityStatDebuffs, parry, ceguera Taiyoken): frágil.
6. **Evento propio `DMZEvent`** en el bus de Forge; las pasivas de clase dependen de él.
7. **Dependencias obligatorias**: GeckoLib 4.8.3, TerraBlender 3.0.1.10, Curios 5.14.1.
8. Ids de formas/skills/técnicas son **strings** (`grupo:forma`, `kaioken`), no registros.

---

## 2. Mapa de paquetes y dependencias

Raíz `com.dragonminez` (148 paquetes con Java). Abreviatura `J/`.

| Paquete | Contenido | Tamaño aprox. | Depende de |
|---|---|---|---|
| `common/stats` (+`character`, `extras`, `skills`, `techniques`) | `StatsData`, `Stats`, `Resources`, `Character`, `FormMasteries`, `KiAttackData`, `TechniqueDispatcher` | núcleo | config |
| `common/config` (16) | `ConfigManager`, `FormConfig`, `SkillsConfig`, `CombatConfig`, `RaceStatsConfig`, `RaceCharacterConfig`, `TechniqueConfig`… | ~237 KB | — |
| `common/combat` (`logic`, `clash`, `player`, `util`, `weapon`) | `PlayerAttackHelper`, `TargetHelper`, `BeamClash`, armas (linaje Better Combat) | | stats |
| `common/passives` | pasivas de clase (Warrior, MartialArtist, Spiritualist, Berserker, Paladin, Tank, Cleric) | | `DMZEvent` |
| `common/quest` (objectives 10, rewards 8) | `Quest`, `QuestRegistry`, `QuestParser`, `PlayerQuestData` (973 líneas), `PartyManager` | ~8,5 k líneas | stats, alineamiento |
| `common/alignment` | `NpcDispositionService`, `NpcAlignmentRules` | | stats |
| `common/dragonball`, `common/wish` | definiciones de dragón/esferas/radar, deseos | | stats, red |
| `common/init` (`entities`, `item`, `block`, `armor`, `fluid`, `particles`…) | ~203 registros de entidad, ~145 bloques, ~358 ítems | | todo |
| `common/network` (`C2S` 54, `S2C` 26) | paquetes | | stats, quest |
| `common/spacepod` | destinos JSON | | red |
| `server/events` (`players/{actionmode,combat,statuseffect}`) | `StatsEvents`, `CombatEvent`, `TickHandler`, `DashHandler`, `StrikeAttackHandler`, `TaiyokenHandler` | | stats |
| `server/commands` (26) | Brigadier + `DMZPermissions` | | stats |
| `server/storage`, `server/energy`, `server/dynamicgrowth` | NBT/JSON/BD, energía, crecimiento dinámico | | stats |
| `server/world` (`biome`, `dimension`, `feature`, `gen`, `npc`, `raid`, `region`, `structure`, `tree`, `data`) | 4 dimensiones, estructuras, raids | | TerraBlender |
| `client/gui` (`hud`, `radial`, `character`, `quest`, `tooltip`…) | ~480 KB solo pantallas de personaje | | red, stats |
| `client/render` (`layer`, `hair`, `shader`, `effects`, `firstperson`…) | jugador GeckoLib, aura, shaders | ~700 KB | GeckoLib |
| `client/{animation,clash,collision,flight,systems}` | animaciones, flight roll, kisense, taiyoken, impact frames | | mixins |
| `mixin/{common 10, client 34}` | 44 mixins (refmap Forge) | | |

Dependencias (flecha = "necesita"):

```
 Misiones/NPC/Saga ──▶ Stats(nivel, TP) ──▶ Config(razas, formas, skills)
        │                    ▲
        ▼                    │
 Alineamiento/Party ─────────┤
 Combate (mitigación, poise) ┤
 Formas (mult., maestría)  ──┤
 Técnicas ki (coste, daño) ──┘──▶ Entidades ki ──▶ Render GeckoLib/shaders
 Mundo/Estructuras ──▶ Misiones (objetivos Structure/Biome/Dimension)
```

**Conclusión de dependencias:** Stats+Config son la base; todo lo demás cuelga de ahí. Mundo y renderer son casi independientes de la lógica.

---

## 3. Sistemas, uno por uno

Leyenda de veredicto: **D** = directamente reimplementable (como datos/fórmulas puras), **A** = necesita adaptación, **NV** = no viable como port directo, **E** = crear desde cero.

### 3.1 Stats, atributos, capacidades, sincronización, modificadores

- **Estado:** `Stats` (STR, SKP, RES, VIT, PWR, ENE; ints, base 5 por raza/clase). `BonusStats` (planos y multiplicativos por stat).
- **Poder de combate** (`StatsData.getBattlePowerExact`, ~:152): `1200 × (rawPower/100)^1.2 × release`, stats de apoyo pesados 0,5.
- **Stat efectivo:** `(stat + multBonus) × escalado(raza/clase) × multiplicadorTotal + planoBonus × escalado`.
- **Multiplicador total:** forma × forma-apilada × efectos × carga/gravedad; configurable producto o suma.
- **Costes de stat:** `STAT_COST_PER_POINT = 1.25` con "rodilla" tardía; TP (puntos de entrenamiento) como moneda.
- **Puente a atributos vanilla:** atributos propios (`MainAttributes`); `RangedAttributeMixin` sube topes a `Float.MAX_VALUE`; modificador de `max_health` con UUID fijo; velocidad/peso con modificadores transitorios.
- **Crecimiento dinámico** (`DynamicGrowthService`): XP de práctica por stat; `requiredXp = 70 + 2·stat + floor(stat^1.35)`; tope por objetivo repetido (historial por UUID); ventana de combate 15 s.

```
 evento (golpe/tick) ─▶ StatsData (mutación) ─▶ NBT save() ─▶ disco (player data)
                              │
                              └─▶ StatsSyncS2C(NBT completo) ─▶ cliente.load(nbt)
```

- **Frente a nuestro mod:** nosotros tenemos 7 atributos (VIT, FUE, DEF, VEL, KI, POD, CON) en un `record PlayerProgress` + Codec, con fórmulas en `core/` (testeadas) y `AttachmentType.sync().copyOnDeath()`. DMZ tiene 6 (STR, SKP, RES, VIT, PWR, ENE). Equivalencias aproximadas: STR↔FUE, VIT↔VIT, RES↔DEF, PWR↔POD, ENE↔KI; SKP y CON/VEL no se corresponden uno a uno. **Decisión de diseño pendiente** (ver §6).
- **Veredicto:** fórmulas **D** (como constantes dorados para tests); estructura `StatsData` **A** (dividir en varios records con Codec, ids `ResourceLocation` en modificadores en 1.21.1); topes de atributo **A** (access transformer en lugar de mixin).

### 3.2 Ki / recursos / liberación / regeneración / gasto

- `Resources`: energía, stamina, poise, `powerRelease` (0–100), alineamiento (0–100, 100 por defecto).
- Regeneración por tick/5 s con constantes por clase (`baseHp5`, `hp5VitScaling`, `baseEp5`, `ep5EneScaling`, `baseSp5`, `sp5StmScaling`).
- **Stamina** además de energía y **poise** (guardia): no los tenemos.
- Liberación: el combate necesita `release ≥ 50` para ganar maestría; BP escala con `release`.
- **Frente a nuestro mod:** tenemos Ki (reserva `50+10×KI`), liberación 0–100 con límite inicial 50 %, factor `max(0, lib/50)`, castigo ×4 recibido / ×0,25 infligido, regeneración y gasto, HUD, `StatsScreen`. DMZ añade stamina y poise, y 3 recursos regenerados por clase.
- **Veredicto:** liberación/Ki **ya hechos** (nuestra versión es más limpia y testeada); stamina/poise **E** (crear con nuestra arquitectura si se decide); regen por clase **D**.

### 3.3 Formas / transformaciones / maestría

- **Definición** (`FormConfig`, ~70 campos por `FormData`): requisitos (`unlockOnSkillLevel`, `formRequisite`, `unlockOnMastery`), multiplicadores (STR, SKP, STM, DEF, VIT, PWR, ENE, velocidad, attackSpeed), consumos por segundo (energía, stamina, vida), maestría, apilado, incompatibilidades, coste en ítems, efectos de mob, y mucha **visualidad** (colores, pelo, aura, relámpagos, tinte, escala, contorno).
- **Formas por raza** (listas en `common/util/lists/*Forms.java`): Saiyan (oozaru, SS grados, SS2, SS3, SS4, legendarias), Humano (buffed, fullpower, android…), Namekiano, Frost Demon (evolución, metal), Majin, Bio-Android; formas apilables: Kaio-ken (x2…x100) y "ultimate". UI/Ultra Ego están comentadas.
- **Activación:** mantener G → `actionCharge` sube cada tick (`10 + min(15, mastery×0,2)`); al 100 → `attemptTransform` comprueba nivel de skill, maestría, costes de entrada (`max×0,1×drain`), incompatibilidades; aplica efecto TRANSFORMED.
- **Maestría:** por golpe dado/recibido, pasiva cada 5 s, compartida entre formas; multiplicador final `base × (1 + clamp(m/max) × (maxStatsMultiplier−1))` (por defecto tope ×1,25); coste baja hasta ×0,5.
- **Consumo:** `energía = base × max(1, sqrt(ofensiva/(maxEnergía×1,5))) + maxEnergía × drain × 0,01 × 0,75`.
- **Oozaru:** carga automática con luna llena; revierte sin cola.

```
 G mantenido ─▶ actionCharge++ ─(100)─▶ attemptTransform
     ├─ ¿skill nivel / maestría / requisito?   no ─▶ aborta
     ├─ paga coste de entrada (ki/stamina/vida/ítems)
     ├─ setActiveForm ─▶ DMZEvent.FormChange ─▶ efectos de mob
     └─ cada 1 s: handleActiveFormDrains ─▶ sin recursos ─▶ revertir
```

- **Frente a nuestro mod:** **no tenemos formas**. Nuestro diseño (SYSTEMS/RACES) las prevé; aquí DMZ es la referencia más valiosa.
- **Veredicto:** datos+matemática **D** (record+Codec, registro de datapack; dividir `FormData` en sub-records porque un `RecordCodecBuilder` admite como mucho 16 campos); activación/tick **A** (separar lo puro en `core/`); visuales (aura, pelo, contorno) **A con riesgo alto** (APIs de shader/RenderType cambiaron).

### 3.4 Habilidades / técnicas

- **Skills** (`SkillsConfig`): compra con TP por nivel (listas de enteros); ki one-shot (ki_barrage 1000 … soul_punisher 25000), strikes (3000–15000), pasivas con niveles (jump, sprint, fly, meditation, kisense, kicontrol…). `skillOfferings` define qué enseña cada maestro. Compra en `UpdateSkillC2S` (TOGGLE/UPGRADE/PURCHASE) con validación de raza y TP.
- **Técnicas** (`PredefinedTechniques`: 19 ki + 8 strikes; `KiType`: SMALL_BALL … AREA): fórmulas cerradas:
  - coste ki `max(5, (kiDmg×mult×0,5 + (tamaño×5 + vel×5 + pen×0,2)) × tipo × util × config × secundario × overload / 2)`
  - coste TP `(80 + complejidad×100) × tipo × util × secundario`, mínimo 10
  - cooldown `seg×20×(1 − 0,025·nivel, mín 0,5)`
  - daño `kiDamage × mult × config × carga(0,5–2,0) × salida`; sobrecarga hasta 175 %.
  - Técnicas **personalizables por el jugador** + exportar/importar código `DMZK1:`.
- **Ejecución:** `TechniqueDispatcher` crea entidades (`KiBlastEntity` 1333 líneas, `KiWaveEntity`, `KiLaserEntity`, `KiDiskEntity`, explosión, barrera, área). Strikes: `StrikeAttackHandler` (1299 líneas, secuenciador con teletransporte).
- **Pasivas de clase** (`IClassPassive`: ganchos onTick/onMeleeHit/regen/critBonus…); valores vienen de `stats.json` por raza/clase; pasivas raciales (Zenkai: vida ≤15 % 8 s, etc.).
- **Armas:** linaje Better Combat (`weapon_attributes/*.json`, `PlayerAttackHelper` 373 líneas) → **riesgo de licencia aparte**.
- **Frente a nuestro mod:** no tenemos skills/TP/técnicas. Tenemos XP como moneda de stats y diseño en ABILITIES.md.
- **Veredicto:** costes/fórmulas **D** (oro para tests); entidades ki **A** (SynchedEntityData y spawn cambian); strikes **E** (diseño nuevo); armas **E**; radial/teclas **E**.

### 3.5 Razas, personajes, renderer del jugador

- Raza = datos: `races/<raza>/character.json` (apariencia, modelo, `formSkillsCosts`, habilidad racial), `stats.json` (por clase: base, escalado, regen, multiplicadores de TP, pasiva), `forms/*.json`. Seis razas base: human, saiyan, namekian, frostdemon, bioandroid, majin; carpetas extra se descubren automáticamente.
- **Personaje** (`Character`, 29K): raza, género, clase (martial/spiritual/warrior), cuerpo/pelo/ojos/nariz/boca/tatuajes, colores, forma activa, maestro.
- **Pantallas:** selección de raza (21 K), personalización (79 K), stats (83 K), skills (66 K), árbol de misiones (140 K), creador de técnicas, editor de pelo, partido, config, minijuegos (~480 KB en total).
- **Renderer:** el jugador se dibuja con **GeckoLib** (`DMZPlayerRenderer`), con capas: piel (46 K), partes de raza (34 K), armadura custom, pelo, aura, capa, armas. `PlayerGeoAnimatableMixin` convierte `AbstractClientPlayer` en animable. 32 modelos `.geo.json` de raza (~413 KB).
- **Veredicto:** datos de raza **D**; pantallas **E/A**; renderer **NV como port, E en GeckoLib 5** (API distinta en 1.21.1; verificar versión antes de comprometerse). Sistema de pelo propio: **E**.

### 3.6 NPC / maestros / sagas / diálogos / misiones / enseñanza

- **Maestros:** `MastersEntity` (GeckoLib, `PathfinderMob`, objetivos mínimos) + 23 subclases triviales (Beerus, Whis, Piccolo, Roshi, Karin, Kaiosama…). Interacción: exige personaje creado, filtro de alineamiento, abre diálogo de misiones (`OpenQuestNPCDialogueS2C`). Genérico `QuestNPCEntity` dirigido por datos. Colocación desde `placements.json` → `SavedData`.
- **Enemigos de saga:** 25 clases (Raditz, Nappa, Vegeta, Saibaman, Zarbon, Dodoria, Ginyu, Freezer, Cell, Androides, Buu, Goku/Gohan/Piccolo/Trunks…) con `SagasCombatBrain`, `ComboManager`, `SkillManager`; stats se sobrescriben por misión y escalan con el tamaño de grupo. También redribbon, namek (aldea con defensa), animales.
- **Misiones:** `Quest` (id, tipo SAGA/SIDE, objetivos, recompensas, prerrequisitos AND/OR, dificultad), 10 objetivos (Kill, Item, Interact, TalkTo, Biome, Coords, Dimension, Structure, Skill, DragonSummon), 8 recompensas (Item, TPS, Skill, KiTechnique, Transformation, Alignment, Command, GenericItem). **217 archivos** de misiones por defecto: 121 de saga (android 15, buu 35, frieza 16, future 7, movies 40, saiyan 8) y 90 secundarias — pero se **generan desde código** (`QuestDefaults` 1158 + `SideQuestDefaults` 1315 líneas); `previousQuests` es solo para migración.
- **Diálogos:** claves de idioma (`en_us.json`, 3128 líneas). **Solo existe inglés**; Crowdin para traducir.
- **Enseñanza de skills:** pantalla del maestro (`MastersSkillsScreen`) + recompensas de misión.
- **Frente a nuestro mod:** tenemos FTB Quests/Patchouli/KubeJS (integraciones) y bestiario de datos; ningún NPC maestro ni sistema propio de misiones.
- **Veredicto:** modelo de misiones/objetivos/recompensas **D** (puro, solo Codec); contenido de misiones **reutilizable como datos pero los números de combate asumen la escala de poder de DMZ**; entidades **A**; GUI **A**; diálogos **E** (traducir al español).

### 3.7 Combate (daño, defensa, esquiva, choques)

- **Tubería** (`CombatEvent.onLivingHurt`, prioridad HIGH, 751 líneas): sustituye el daño melee por `getMeleeDamage()`; × multiplicador de combo; × momentum de vuelo (1,15–1,5); coste de stamina `ceil(dañoSinMult×0,083)×gravedad×…`; añade ki weapon/infusion y daño de arma vanilla; penetración de defensa `min(0,5, skill×0,025 + fuente + pasiva)`.
- **Mitigación** (`calculatePostMitigationDamage`): defensa saturante `def/(k+def)` con `k = max(12, defEsperada×0,11)`, tope 75 %; plano absorbido tope 82 %; protección de encantamientos con rendimientos decrecientes (bloques de 4, k=20, tope 85 %); término adaptativo (0,25→0 entre ratio 1 y 5, tope 0,65). `overrideVanillaArmorReduction` anula la armadura vanilla.
- **Bloqueo/poise/parry:** bloquear (clic derecho) reduce daño entre 5 % y 65 % según poise; daño de poise `post×0,25`; romper guardia ⇒ aturdimiento; parry si el bloqueo empezó <150 ms antes (el atacante sufre 4 s de penalización ×2 stamina).
- **Esquiva/dash** (`DashHandler`): evasión perfecta (dash <200 ms tras ser golpeado) cuesta `ceil(drenaje×0,65)` ki y teletransporta tras el atacante con golpe `max(1, melee×0,2)`.
- **Choque de rayos** (`BeamClash`): tira y afloja `biasT` en [0,1]; victoria en ≥0,8; QTE con punto dulce 0,78–0,96. Cliente: cámara cinemática + overlay.
- **Vuelo:** consumo de ki `(drenaje/4)×max(0,002, 0,03−nivel×0,005)×…`; colisión 5–35 % de vida máxima sobre velocidad 0,35; cliente con impulsos por doble toque y roll de cámara (mixin).
- **EntityStatDebuffs:** debuffs temporales de STR/PWR/DEF/HP_REGEN en NBT persistente de mobs; gana el factor más fuerte.
- **Kisense (F4)**, **Taiyoken** (ceguera por dirección de mirada, 45 s de enfriamiento), **impact frames**.

```
 daño entrante ─▶ [atacante] mods ─▶ [víctima] penetración ─▶ mitigación saturante
              ─▶ bloqueo/poise/parry ─▶ vida
```

- **Frente a nuestro mod:** tenemos reducción por VIT (cúbica, 3 categorías, suelo 0,5), FUE cúbica, castigo por liberación. **No tenemos** DEF real (saturante), bloqueo, poise, parry, esquiva, choque, vuelo, debuffs a mobs.
- **Veredicto:** matemática **D**; monolito del evento **A** (reconstruir como tubería explícita con `LivingIncomingDamageEvent` / `LivingDamageEvent.Pre`); `EntityStatDebuffs` **D** (como attachment); choque/vuelo/kisense/taiyoken **A** (la parte de shader es lo caro); armas/combos **E**.

### 3.8 Mundo (dimensiones, estructuras, raids)

- 4 dimensiones: Namek, Otherworld (carga **8 archivos `.mca` ≈ 28 MB** prehechos), Cámara del Tiempo, Sacred Kai Planet. Se registran con código (bootstrap) + noise routers propios.
- Overworld: una región TerraBlender (`OverworldRegion(40)`) + reglas de superficie + 8 features de relieve propias.
- Estructuras: 63 NBT (10,5 MB; Roshi, Kamilookout, Gero, Babidi, Cell arena, naves, aldeas Namek…), pero solo **3** estructuras con JSON de worldgen (`rrtower`, `village_ajissa`, `village_sacred`); el resto se coloca con tipos propios (`UniqueNearSpawn`, `FixedStructure`…).
- Raids (`RaidManager`, un tipo: `saiyan_assault`). Esferas del dragón por datos (conjuntos, radar). Naves espaciales con destinos JSON.
- **Veredicto:** NBT de estructuras **D/A** (puede requerir DataFixer); dimensiones y worldgen **A** (formatos de datapack cambiaron); `.mca` Otherworld **A con riesgo**; TerraBlender **A** (opcional: se puede evitar con biome modifiers); raids/esferas/deseos **D/A**.

### 3.9 Red (paquetes)

- Un `SimpleChannel` `dragonminez:network`, versión "1.0" y `s -> true` (sin negociación real), **ids posicionales** (reordenar rompe compatibilidad).
- C2S 54: personaje/stats, formas/acciones, vuelo, combate, técnicas, misiones/NPC/entrenamiento, partido, deseos/nave/gravedad/teletransporte instantáneo.
- S2C 26: sincronización (stats NBT completo, recursos, progresión, apariencia, config), registros (misiones, armas, deseos, destinos), UI (menús, toasts), efectos/animaciones.
- Validación en servidor: patrón `getSender()==null → return`, clamp de recursos, comprobación de distancia/propiedad en deseos. **Solo 2 paquetes** con limitador de frecuencia.
- **Veredicto:** **D** — cada paquete → `CustomPacketPayload` + `StreamCodec`. Recomendación: fusionar paquetes en "acción + enum" para reducir los 80 clases de boilerplate; mantener validación de servidor.

### 3.10 Archivos de configuración JSON

- Directorio `config/dragonminez/`: `general-user.json`, `general-server.json`, `combat.json`, `training.json`, `skills.json`, `techniques.json`, `entities.json`; por raza `races/<r>/{character,stats}.json` y `forms/<grupo>.json`; formas compartidas `forms/*.json`.
- Versión única `2.1.3` en `configVersion`; si falta o es antigua: copia `old_*`, genera defecto y fusiona valores del usuario (`@ConfigNonPreservable` fuerza el nuevo). JSON malformado: defaults en memoria y archivo intacto.
- Sync al conectar: cada archivo como `SyncServerConfigS2C(path, json comprimido, reset)`; el cliente usa copia "servidor" mientras dure la sesión.
- Almacenamiento de jugador: NBT por defecto; JSON o BD (JDBC) opcional, asíncronos.
- **Frente a nuestro mod:** ya tenemos `data/dragonball/balance/combat.json` → `BalanceParams` con `BalanceManager` (reload listener, rechazo del archivo entero si es inválido, KubeJS, sync por payload). Es el patrón correcto en 1.21.1.
- **Veredicto:** **A** — formas/razas/skills como datos de datapack con Codec (se sincronizan solos); config simple con `ModConfigSpec`; almacenamiento JSON/BD **no necesario**.

### 3.11 Cliente (GUI, HUD, teclas, animación, shaders), mixins y otros

- 46 `KeyMapping` (V stats, C cargar ki, G acción, X menú radial, H teletransporte instantáneo, Z fijar objetivo, F volar, R dash, F4 kisense, ranuras de técnicas Alt/Ctrl+1–4…).
- HUD (Xenoverse/Alternativo/Scouter), menú radial con árbol de nodos persistido, tooltips propios (4 mixins sobre `GuiGraphics`).
- Shaders con `PostChain`/`EffectInstance` (~1850 líneas: contorno de transformación con buffer de máscara, bloom de ki, gravedad roja, escala de grises de kisense, flash Taiyoken, impact frames) y compatibilidad Iris.
- **44 mixins**: ~20 eliminables en 1.21.1 (encantamientos y pools ahora son datos, branding, accessors → access transformer), ~10 difíciles (`Player.attack`, `Minecraft.startAttack`, `Camera.move`, `EntityRenderDispatcher`, `PlayerGeoAnimatable`, accessors de `RenderType`).
- Otros: comandos (26, Brigadier + permisos), JEI (reimplementable), WorldGuard por reflexión (descartar), Crowdin en runtime y verificación beta (descartar).
- **Veredicto:** teclas/HUD/capas **D/A** (`RegisterGuiLayersEvent`, que ya usamos); shaders **E**; mixins **mínimos** (cámara, `Entity.turn`); tooltips **E** vía `RenderTooltipEvent`.

---

## 4. Comparación con nuestra arquitectura (resumen)

| Sistema DMZ | Estado en nuestro mod | Veredicto | Notas |
|---|---|---|---|
| Fórmulas de stats/BP/costes | VIT, FUE, Ki hechas y testeadas | **D** | Usar valores de DMZ como referencia, no como verdad (SYSTEMS.md manda) |
| `StatsData` (god-object) | `PlayerProgress` record + Codec + attachments | **A** | Nuestro enfoque ya es el correcto; no portar la estructura |
| Sync de estado | `.sync` con predicado `hasChannel` | **A** | DMZ reenvía todo el NBT; nosotros delta/attachment |
| Ki / liberación | Hecho | — | Más sólido que DMZ |
| Stamina / poise | No | **E** | Decidir si entran en el diseño |
| Formas + maestría | No | **D** (datos) / **A** (tick) | Prioridad alta |
| Skills + TP | No (XP como moneda) | **D** | Decidir TP vs XP (§6) |
| Técnicas ki (fórmulas) | No | **D** | Núcleo puro + tests |
| Entidades ki | No | **A** | Proyectiles, renderers |
| Strikes / combos | No | **E** | Diseño nuevo |
| Pasivas de clase | No | **D** | Datos + interfaz pura |
| Razas (datos) | RACES.md/BETA_RACES.md (diseño) | **D** | Convertir a datapack |
| Renderer jugador | Vanilla | **E** | GeckoLib 5, alto riesgo |
| Pantallas de personaje | `StatsScreen` simple | **E** | |
| Mitigación saturante / bloqueo / poise / parry | Mitigación VIT cúbica | **D** (matemática) / **A** (evento) | Comparar contra nuestra fórmula antes de portar |
| Esquiva / dash | No | **D** | |
| Choque de rayos, vuelo, kisense, taiyoken | No | **A** | Coste en shaders/cámara |
| Alineamiento | No | **D** | |
| Party | No | **D** | |
| Deseos / esferas | No | **D/A** | |
| Misiones (modelo) | FTB Quests (integración) | **D** | ¿Reemplazar FTB? decisión del usuario |
| NPC maestros / sagas | Lobo salvaje + bestiario de datos | **A** | |
| Mundo / dimensiones | No | **A** | |
| Red (80 paquetes) | 2 payloads | **D** | Fusionar en acción+enum |
| Config JSON versionada | `BalanceManager` | **A** | Datapack reload en lugar de Gson |
| Comandos | `DragonBallCommands` | **D** | |
| Tooltips/mixins de render | — | **E** | |

---

## 5. Priorización recomendada

Criterio: dependencias → valor jugable → riesgo.

1. **Base de stats completa** (DEF, VEL, POD, CON con fórmulas propias) — desbloquea todo; riesgo bajo; ya tenemos el método.
2. **Formas y maestría** (datos + núcleo + activación) — corazón de Dragon Ball; riesgo medio.
3. **Mitigación/defensa, bloqueo, esquiva** — completa el combate; riesgo medio.
4. **Skills + TP + técnicas ki (solo fórmulas y datos)** — riesgo bajo.
5. **Razas como datos + creación de personaje** (sin modelos nuevos).
6. **Entidades de ki** (proyectiles con modelo vanilla/partículas) — riesgo medio.
7. **Alineamiento + party + deseos** — pequeños, independientes.
8. **Misiones / NPC / diálogos en español.**
9. **Mundo.**
10. **Renderer GeckoLib, aura, shaders, vuelo, kisense** — lo último y más arriesgado.
11. **Assets** (copia privada) cuando los sistemas que los usan existan.

---

## 6. Riesgos y bloqueos

1. **Licencia y alcance de lo declarado.** El usuario declara uso 100 % privado y sin distribución. Eso lo registro como su decisión. Hechos a tener presentes: (a) DMZ es GPL-3.0, y copiar su código literal obligaría a GPL-3.0 si algún día se distribuyera; **la regla de trabajo es reimplementar, no copiar código**. (b) Los nombres, personajes, música y sonidos de Dragon Ball son propiedad de terceros y no los cubre la GPL; el README de DMZ solo atribuye 2 sonidos (CC-BY) y no da lista por archivo del resto. (c) El módulo de armas parece derivado de Better Combat (licencia aparte). (d) Esto no es asesoría legal.
2. **GeckoLib 4 → 5 para 1.21.1** (renderer del jugador, animaciones, `GeoReplacedEntity`): API distinta; **no verificado** que exista la versión estable adecuada. Spike obligatorio antes de comprometerse.
3. **Shaders / PostChain / RenderType** cambiaron en 1.21.x: aura, contorno, bloom, kisense, taiyoken e impact frames se reconstruyen.
4. **`Player.attack` y `Minecraft.startAttack`** (combos estilo Better Combat) son los mixins más difíciles.
5. **`StatsData` toca todo**: portar por partes, nunca en bloque.
6. **Escala de números**: DMZ usa BP del orden de miles/millones y misiones con niveles 1700–2320; nuestro diseño usa stats ≤ rangos distintos. Los datos de DMZ no se pueden pegar sin re-escalar.
7. **Divergencia de diseño**: 6 stats de DMZ vs 7 nuestros; TP vs XP; stamina/poise. Hay que decidir qué manda (SYSTEMS.md prevalece sobre el GDD y sobre DMZ).
8. **Mundo**: noise routers de Namek/Kai y los `.mca` de Otherworld (28 MB) pueden no migrar limpiamente.
9. **Volumen**: ~953 archivos Java; incluso reimplementando solo lo esencial, el trabajo es de muchos ciclos.
10. **Contenido solo en inglés**: diálogos y misiones requieren traducción propia.
11. **Verificación**: casi nada de DMZ se puede validar sin ejecutarlo; los valores dorados salen de leer código. Los tests de fórmulas serán de regresión de "lo que dice el código", no de "lo que se siente en juego".

# DMZ_IMPLEMENTATION_ROADMAP — Hoja de ruta de reimplementación

> Basada en `DMZ_SYSTEMS_MAP.md` (léelo primero). **Fase 0 = estudio, ya hecha.**
> Regla de oro: **se reimplementa con nuestra arquitectura (NeoForge 1.21.1, DataAttachment, record + Codec, `core/` puro). No se copia código de DMZ.** DMZ aporta ideas, fórmulas y valores como referencia; SYSTEMS.md prevalece cuando choquen.
> Cada fase sigue el método de siempre: datos → núcleo puro + tests JUnit → capa `game/` + GameTest → cliente → verificación del usuario en su PC. Se dirá siempre qué está solo compilado y qué verificado en juego.

## Cómo leer las duraciones

Unidad: **ciclo** = una sesión de trabajo como la del Ki (diseño + código + tests + informe). Rango optimista–realista. **Son estimaciones mías, no medidas**; los ciclos de render/shader son los más inciertos. No incluyen el tiempo del usuario para probar en su PC.

## Resumen

| Fase | Contenido | Ciclos | Depende de | Riesgo |
|---|---|---|---|---|
| 1 | Cierre de stats base (DEF, VEL, POD, CON) | 2–3 | — | bajo |
| 2 | Defensa avanzada: mitigación (DEF) y esquiva; sin poise ni parry | 2 | 1 | medio |
| 3 | Razas y personaje como datos (sin modelos) | 2–3 | 1 | bajo |
| 4 | Formas y maestría (datos + núcleo + activación) | 3–4 | 1, 3 | medio |
| 5 | Skills (en XP) y técnicas ki (fórmulas y datos) | 3–4 | 1, 3 | bajo |
| 6 | Entidades de ki y ejecución de técnicas | 3–5 | 5 | medio |
| 7 | Alineamiento, party, deseos y esferas | 2–3 | 1, 5 | bajo |
| 8 | NPC maestros, diálogos (español) y contenido FTB | 3–4 | 3, 5, 7 | medio |
| 9 | Mundo: estructuras, dimensiones, raids | 4–6 | 8 | medio-alto |
| 10 | Cliente avanzado: pantallas, vuelo, radial | 3–5 | 4–6 | medio |
| 11 | Render: GeckoLib 5, aura, shaders, kisense | 5–8 (+spike) | 3, 4, 10 | **alto** |
| 12 | Assets privados | 1–2 por lote | sistemas que los usan | legal/IP |

Total orientativo: **33–50 ciclos** (tras las decisiones cerradas: sin poise y sin sistema de misiones propio). Las fases 1→2→(3,4,5) son la columna vertebral; 7, 9, 11 pueden ir en paralelo o posponerse.

```
 1 Stats ─┬─▶ 2 Defensa
          ├─▶ 3 Razas ─┬─▶ 4 Formas ─────┐
          │            └─▶ 5 Skills/TP ──┼─▶ 6 Ki entities ─▶ 10 Cliente ─▶ 11 Render
          └─────────────▶ 7 Alineam./Party/Deseos ─▶ 8 Misiones/NPC ─▶ 9 Mundo
                                                                     12 Assets (cuando haga falta)
```

---

## Decisiones cerradas (confirmadas por el usuario)

| # | Tema | Decisión | Efecto en la hoja de ruta |
|---|---|---|---|
| 1 | Atributos | **Se mantienen los 7 nuestros** (VIT, FUE, DEF, VEL, KI, POD, CON). No se adoptan los 6 de DMZ | DMZ solo aporta fórmulas de referencia; no hay migración de stats. Fase 1 completa DEF, POD y CON |
| 2 | Moneda | **XP**, no TP | Fases 4, 5 y 8: costes de skills, formas y técnicas en XP. Sin moneda nueva |
| 3 | Stamina / poise | **Sin stamina** (todo por Ki). **Sin poise por ahora** (necesita animaciones; posible en el futuro) | Fase 2 sin bloqueo con poise ni parry (el parry depende de stamina/poise). Fase 4: drenajes de formas solo en Ki y vida |
| 4 | Misiones | **Se mantiene FTB Quests**; no hay sistema propio | Fase 8 sin modelo de misiones propio; solo NPC, diálogos y contenido FTB |
| 5 | Idioma | **Español principal**; se mantienen las 8 variantes y el inglés como secundario | Todo texto nuevo se escribe primero en español y se añade a los 8 archivos de idioma + `en_us` |

Estas decisiones están **cerradas**; si cambian, se reabren explícitamente (ver también `HANDOFF.md` §7).

**Estado actual:** la Fase 1 **no ha empezado**. Se espera a que el usuario verifique en su PC el ciclo de Ki ajustado (liberación a trozos, VEL solo al correr, corazones y FOV).

---

## Fase 1 — Cierre de stats base (IMPLEMENTADA 2026-10-12, pendiente de verificar en el PC; ver `DEF_POD_IMPLEMENTATION.md`)
- **Qué:** DEF (mitigación propia), VEL (movimiento/salto/ataque), POD (daño de ki), CON (resistencia/regeneración) con el método VIT/FUE: fórmula en `core/`, parámetros en `balance/*.json`, tests, comandos, pantalla.
- **Referencia DMZ:** escalado por raza/clase, regeneración `baseHp5/…`, coste de stat.
- **Éxito:** 7 atributos con fórmula y tests JUnit; GameTests nuevos; JSON de balance con rechazo de archivo inválido; `StatsScreen` y comandos al día; sin regresiones (JUnit 41+, GameTest 52+); documento `*_IMPLEMENTATION.md` por stat.

## Fase 2 — Defensa avanzada
- **Qué:** DEF con mitigación saturante `def/(k+def)` (a comparar con nuestra fórmula), evasión/dash con coste de Ki, núcleo puro de la tubería (atacante → víctima). **Fuera por decisión:** poise, parry y stamina. Un bloqueo simple (reducción fija, sin poise) solo si el usuario lo pide más adelante.
- **Referencia DMZ:** `CombatEvent`, `calculatePostMitigationDamage`, `DashHandler` (sin la parte de poise/guardia).
- **Éxito:** tubería explícita en `core/` con tests de tablas de valores; engancha `LivingIncomingDamageEvent`/`LivingDamageEvent.Pre` sin romper VIT/FUE; GameTests con zombie (armadura 0); informe de diferencias entre fórmula DMZ y la nuestra.

## Fase 3 — Razas y personaje como datos
- **Qué:** registro de datapack `race` (stats base, escalado, regeneración, formas permitidas, pasiva), selección de raza/clase, `PlayerProgress` ampliado (migración de datos existente), comando y pantalla simples. **Sin modelos nuevos** (piel vanilla).
- **Referencia DMZ:** `character.json`, `stats.json`.
- **Éxito:** al menos las 3 razas de BETA_RACES jugables por datos; Codec con compatibilidad hacia atrás verificada por test; sync al cliente; KubeJS puede sobrescribir.

## Fase 4 — Formas y maestría
- **Qué:** registro `form` (sub-records para no superar 16 campos del codec), requisitos, multiplicadores, consumos **solo de Ki y vida** (sin stamina), maestría (golpe dado/recibido, pasiva), apilado (Kaio-ken), activación por mantener tecla, reversión por falta de recursos.
- **Referencia DMZ:** `FormConfig`, `FormModeHandler`, `getFormMultiplier`, `applyMasteryStatBonus`.
- **Éxito:** matemática pura en `core/` con valores dorados de DMZ en tests; ≥2 formas de ejemplo jugables; HUD/pantalla muestran forma y maestría; GameTests de transformar/revertir/drenar; sin aura ni pelo todavía (solo mensaje/partículas vanilla).

## Fase 5 — Skills (en XP) y técnicas ki (datos y fórmulas)
- **Qué:** registro `skill` con coste por nivel y razas permitidas, compra validada en servidor, registro `technique` con coste de ki y de XP, cooldown, daño y carga (hasta 175 %). Sin entidades todavía: efecto de prueba (daño directo en rayo/hitscan).
- **Referencia DMZ:** `SkillsConfig`, `KiAttackData` (fórmulas cerradas), `TechniqueConfig`.
- **Éxito:** fórmulas con tests; compra/mejora por pantalla y comando; ranuras de técnica con teclas; límites validados server-side; GameTests de coste y cooldown.

## Fase 6 — Entidades de ki
- **Qué:** proyectil base y variantes (bola, onda, rayo, disco, explosión, barrera) con renderer simple (partículas/modelo vanilla), colisión, daño según tipo; después clash de rayos (núcleo puro del tira y afloja).
- **Referencia DMZ:** `TechniqueDispatcher`, `AbstractKiProjectile`, `BeamClash`.
- **Éxito:** 3 técnicas clásicas (bola, Kamehameha, explosión) funcionando en GameTest y en el cliente headless; sync de entidad con `SynchedEntityData` de 1.21; sin lag con 10 proyectiles.

## Fase 7 — Alineamiento, party, deseos
- **Qué:** alineamiento 0–100 (reglas como datos), party con `SavedData`+Codec y PvP toggle, esferas/dragón/deseos con registro de tipos (`item`, `skill`, `tps`, …) y comandos protegidos por permisos.
- **Éxito:** tests puros de reglas; party persiste tras reinicio (GameTest/servidor); deseo `command` desactivado salvo permiso explícito.

## Fase 8 — Misiones, NPC maestros y diálogos
- **Qué (decisiones 4 y 5):** las misiones se quedan en **FTB Quests** (sin modelo propio); maestros (Roshi, Karin…) como una entidad genérica dirigida por datos que enseñan skills por XP; diálogos **en español** en los 8 archivos de idioma + inglés; sagas como capítulos FTB.
- **Referencia DMZ:** estructura de sagas, objetivos y recompensas como inspiración de contenido para FTB (no se porta el código de misiones). Los números de combate se **re-escalan**.
- **Éxito:** una saga corta completa (p. ej. Saiyan inicial) jugable de principio a fin; textos 100 % en español; las misiones se entregan como datos FTB; recompensas del mod (XP, skills) expuestas a FTB por comando o datos.

## Fase 9 — Mundo
- **Qué:** estructuras NBT (migradas/regeneradas) con colocación por datos; luego dimensión pequeña (Cámara del Tiempo) como prueba; Namek y Otherworld después; raids.
- **Éxito:** `/locate` y aparición verificadas en el servidor de pruebas; dimensión carga sin errores; decisión explícita sobre TerraBlender (usar biome modifiers si es posible).

## Fase 10 — Cliente avanzado
- **Qué:** pantallas de personaje/skills/formas completas, menú radial, teclas definitivas, HUD de formas y técnicas, vuelo (cliente+servidor), fijar objetivo.
- **Éxito:** captura en Xvfb de cada pantalla + verificación del usuario en su PC; vuelo sin mixin salvo la cámara.

## Fase 11 — Render y efectos (alto riesgo)
- **Spike previo (1 ciclo):** comprobar qué GeckoLib existe para NeoForge 1.21.1 y si permite reemplazar el modelo del jugador; si no, plan B con modelos vanilla y capas.
- **Qué:** modelo de raza, capas (pelo, partes), aura, contorno de transformación, kisense, flash Taiyoken.
- **Éxito:** spike con veredicto escrito; cada efecto verificado visualmente por el usuario (yo solo puedo verificar en Xvfb con GL por software); mod funciona sin Iris y se documenta si no.

## Fase 12 — Assets privados
- **Qué:** copiar a una copia privada los assets (texturas, sonidos, `geo`, animaciones, estructuras) **cuando el sistema que los usa exista**, listados en un manifiesto; sin subirlos al repositorio público si la rama se publica.
- **Éxito:** manifiesto de qué se copió y de dónde; ninguna dependencia del código de DMZ; recordatorio de licencias/atribuciones (CC-BY de 2 sonidos) en un archivo.
- **Nota:** el uso privado es decisión declarada del usuario; si algún día se distribuye, GPL-3.0 y la propiedad intelectual de Dragon Ball pasarían a importar (ver `DMZ_SYSTEMS_MAP.md` §6).

---

## Qué NO se portará (por ahora)
Almacenamiento JSON/BD externo, Crowdin en ejecución, verificación beta, compatibilidad WorldGuard, sistema de armas de linaje Better Combat, tooltips por mixin, pantalla de título propia.

## Seguimiento
Cada fase termina con: tests verdes, informe (`*_IMPLEMENTATION.md`), actualización de `PROJECT_STATUS.md`/`HANDOFF.md`, commit en la rama de trabajo, y lista de lo **pendiente de verificar en el PC del usuario**.

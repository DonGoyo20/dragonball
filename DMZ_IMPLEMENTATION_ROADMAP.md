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
| 2 | Defensa avanzada: mitigación, bloqueo, poise, esquiva | 2–3 | 1 | medio |
| 3 | Razas y personaje como datos (sin modelos) | 2–3 | 1 | bajo |
| 4 | Formas y maestría (datos + núcleo + activación) | 3–4 | 1, 3 | medio |
| 5 | Skills, TP y técnicas ki (fórmulas y datos) | 3–4 | 1, 3 | bajo |
| 6 | Entidades de ki y ejecución de técnicas | 3–5 | 5 | medio |
| 7 | Alineamiento, party, deseos y esferas | 2–3 | 1, 5 | bajo |
| 8 | Misiones, NPC maestros y diálogos (español) | 4–6 | 3, 5, 7 | medio |
| 9 | Mundo: estructuras, dimensiones, raids | 4–6 | 8 | medio-alto |
| 10 | Cliente avanzado: pantallas, vuelo, radial | 3–5 | 4–6 | medio |
| 11 | Render: GeckoLib 5, aura, shaders, kisense | 5–8 (+spike) | 3, 4, 10 | **alto** |
| 12 | Assets privados | 1–2 por lote | sistemas que los usan | legal/IP |

Total orientativo: **34–52 ciclos**. Las fases 1→2→(3,4,5) son la columna vertebral; 7, 9, 11 pueden ir en paralelo o posponerse.

```
 1 Stats ─┬─▶ 2 Defensa
          ├─▶ 3 Razas ─┬─▶ 4 Formas ─────┐
          │            └─▶ 5 Skills/TP ──┼─▶ 6 Ki entities ─▶ 10 Cliente ─▶ 11 Render
          └─────────────▶ 7 Alineam./Party/Deseos ─▶ 8 Misiones/NPC ─▶ 9 Mundo
                                                                     12 Assets (cuando haga falta)
```

---

## Decisiones del usuario necesarias antes de empezar (sólo las que son suyas)

1. **Stats:** ¿mantener nuestros 7 (VIT, FUE, DEF, VEL, KI, POD, CON) o adoptar los 6 de DMZ? Recomiendo **mantener los nuestros** y usar las fórmulas de DMZ solo como referencia.
2. **Moneda:** ¿XP (actual) o TP separados para skills/formas? Recomiendo seguir con XP para stats y decidir TP al llegar a la Fase 5.
3. **Stamina y poise:** ¿entran al diseño? (afectan Fases 2 y 4).
4. **Misiones:** ¿mantener FTB Quests o crear sistema propio? (Fase 8).
5. **Idioma:** confirmar que el contenido se traduce al español como lengua principal.

Si no hay respuesta, aplico las recomendaciones y lo dejo anotado como provisional.

---

## Fase 1 — Cierre de stats base
- **Qué:** DEF (mitigación propia), VEL (movimiento/salto/ataque), POD (daño de ki), CON (resistencia/regeneración) con el método VIT/FUE: fórmula en `core/`, parámetros en `balance/*.json`, tests, comandos, pantalla.
- **Referencia DMZ:** escalado por raza/clase, regeneración `baseHp5/…`, coste de stat.
- **Éxito:** 7 atributos con fórmula y tests JUnit; GameTests nuevos; JSON de balance con rechazo de archivo inválido; `StatsScreen` y comandos al día; sin regresiones (JUnit 41+, GameTest 52+); documento `*_IMPLEMENTATION.md` por stat.

## Fase 2 — Defensa avanzada
- **Qué:** decidir si se adopta mitigación saturante `def/(k+def)`, bloqueo, poise, parry y evasión; núcleo puro de la tubería (atacante → víctima → bloqueo).
- **Referencia DMZ:** `CombatEvent`, `calculatePostMitigationDamage`, `DashHandler`.
- **Éxito:** tubería explícita en `core/` con tests de tablas de valores; engancha `LivingIncomingDamageEvent`/`LivingDamageEvent.Pre` sin romper VIT/FUE; GameTests con zombie (armadura 0); informe de diferencias entre fórmula DMZ y la nuestra.

## Fase 3 — Razas y personaje como datos
- **Qué:** registro de datapack `race` (stats base, escalado, regeneración, formas permitidas, pasiva), selección de raza/clase, `PlayerProgress` ampliado (migración de datos existente), comando y pantalla simples. **Sin modelos nuevos** (piel vanilla).
- **Referencia DMZ:** `character.json`, `stats.json`.
- **Éxito:** al menos las 3 razas de BETA_RACES jugables por datos; Codec con compatibilidad hacia atrás verificada por test; sync al cliente; KubeJS puede sobrescribir.

## Fase 4 — Formas y maestría
- **Qué:** registro `form` (sub-records para no superar 16 campos del codec), requisitos, multiplicadores, consumos, maestría (golpe dado/recibido, pasiva), apilado (Kaio-ken), activación por mantener tecla, reversión por falta de recursos.
- **Referencia DMZ:** `FormConfig`, `FormModeHandler`, `getFormMultiplier`, `applyMasteryStatBonus`.
- **Éxito:** matemática pura en `core/` con valores dorados de DMZ en tests; ≥2 formas de ejemplo jugables; HUD/pantalla muestran forma y maestría; GameTests de transformar/revertir/drenar; sin aura ni pelo todavía (solo mensaje/partículas vanilla).

## Fase 5 — Skills, TP y técnicas ki (datos y fórmulas)
- **Qué:** registro `skill` con coste por nivel y razas permitidas, compra validada en servidor, registro `technique` con coste de ki/TP, cooldown, daño y carga (hasta 175 %). Sin entidades todavía: efecto de prueba (daño directo en rayo/hitscan).
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
- **Qué (según decisión 4):** modelo de misión/objetivos/recompensas en Codec; maestros (Roshi, Karin…) como una entidad genérica dirigida por datos; diálogos **en español**; estructura de sagas.
- **Referencia DMZ:** `Quest`, `QuestParser`, objetivos (10) y recompensas (8). Los números de combate se **re-escalan**.
- **Éxito:** una saga corta completa (p. ej. Saiyan inicial) jugable de principio a fin; textos 100 % en español; migración de la integración FTB documentada.

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

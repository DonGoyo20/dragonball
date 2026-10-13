# HANDOFF — Visión del proyecto y estado (2026-10-11)
Para alinear a otra IA con la visión del usuario y con el código actual. Rama: `claude/dragon-ball-minecraft-mod-dk0cgl` (repo `dongoyo20/dragonball`). Documentos en español. Prevalece `SYSTEMS.md` sobre `GDD.md` si discrepan.

## 1. Qué quiere el usuario
Mod de **Dragon Ball RPG para Minecraft Java 1.21.1 + NeoForge 21.1.259** (Java 21), mundo de un solo jugador, mod independiente. Núcleo = **progresión RPG**. Beta: **Dragon Ball clásico hasta la saga Saiyan; termina al derrotar a Vegeta**. Razas jugables en la beta: **Saiyajin y Humano**. La 1.0 será todo Z; Super después.

## 2. Mundo y gameplay (decisiones del usuario)
- **Mundo:** el Overworld de Minecraft se fusiona con el de Dragon Ball; estructuras únicas (una por mundo), lejos del inicio, que se localizan con pistas/radar.
- **Exploración sin marcadores:** un diario de ruta (solo palabras del NPC) y un mapa del explorador que se dibuja al recorrer. Importa saber **dónde estás**, no necesariamente adónde vas.
- **Muerte:** vas al **Otro Mundo** (Yemma, Camino de la Serpiente). Hay una espera mínima (15 min reales por defecto) antes de poder reaparecer, y luego tú decides cuándo volver; sirve para fomentar explorarlo. No pierdes zeni, nivel ni objetos. Al Otro Mundo solo se va muriendo. Teletransporte a NPC: después de la beta.
- **Sin fatiga clásica:** el cansancio es agotamiento por quedarte sin ki; entrenar con penalizaciones da más XP. Esferas del dragón con deseos propios y limitados.
- **Estilo de juego:** como Elden Ring / Breath of the Wild: historia como columna vertebral, muchos jefes secretos y rutas de personaje con buenas recompensas. Garantía de historia: cualquier build debe poder avanzar.
- **Combate en tiempo real con teclas.** Los **mobs de Minecraft** tienen nivel fijo (una vaca o un cerdo mueren casi de un golpe al inicio); los jefes vanilla se quedan pero se quitan si complican el código; los Saibamen van en grupos de 3.

## 3. Sistema de progresión (SYSTEMS.md v3)
- **7 atributos** (no 5): VIT (vitalidad), FUE (fuerza), DEF (defensa), VEL (velocidad), KI (ki máximo), POD (poder de ki), CON (control). Tope **99** por atributo (valor, no número de mejoras).
- **XP = moneda.** Se gasta en subir un atributo y en comprar habilidades. **Nivel total = 1 + nº de mejoras.** Coste de la siguiente mejora `10 × (1 + T/5)^1,6` (igual para todos los atributos). El progreso es lento y desgastante, pero con un mínimo de ritmo.
- **Poder efectivo** `E = valor × 1,0399^T × forma`. Daño por golpe como % de vida: `0,05 × clamp((A/D)^0,75)`, defensas `D = DEF^0,7 × VIT^0,3` (físico) y `DEF^0,4 × CON^0,3 × VIT^0,3` (ki). Esquiva por VEL (solo esquiva, sin dar acciones extra).
- **Ki:** barra, reserva `50 + 10 × KI`, costes de técnica **absolutos** reducidos por CON.
- **Habilidades:** se compran con XP, pero **usarlas exige atributos mínimos** (Kamehameha → CON/POD, Kaio-ken → VIT). Los requisitos se muestran siempre. Kaio-ken hasta "×3" con nombres no literales; es la puerta 1 y entra en la beta; Super Saiyajin queda fuera de la beta.
- **XP:** presupuesto 50 % combate / 30 % misiones / 20 % entrenamiento; recompensa fija por enemigo, anti-granjas (spawners ×0,25, rendimientos decrecientes).
- **Razas:** Saiyajin y Humano tienen valores base y crecimiento distintos (BETA_RACES.md). Rasgos: Zenkai (Saiyajin), Aprendiz nato (Humano).
- **Vida (decisión reciente):** vida como **fracción 0–1**; el jugador siempre tiene 20 HP vanilla y la "vida real" es derivada. Este ciclo: solo **VIT → salud**, tope 99 en `tryUpgrade`, VEL solo esquiva, sin HUD, razas no son prerrequisito.

## 4. Estado real (distinguiendo)
| Sistema | Diseñado | Implementado | Verificado |
|---|---|---|---|
| XP, mejoras, nivel total, logros de nivel (`level_2/5/10`) | sí | sí | JUnit (5) + GameTest + en juego |
| Bestiario como Data Map (59 de 119 entradas exportadas) | sí | parcial | GameTest; la XP por kill funciona en juego |
| Lobo salvaje (GeckoLib: modelo, caminar, atacar, botín) | sí | sí | en juego por el usuario; **la animación de mordisco no se ve** (reforzada, sin confirmar) |
| Patchouli: libro `dragonball:guia` | sí | sí (datos) | se abre en juego |
| FTB Quests: capítulo de prueba con 3 misiones | sí | datos | las 3 se completan en juego |
| KubeJS: script que cambia la XP de la araña | sí | datos | verificado (49 XP en vez de 10) |
| Traducciones | — | es_es + variantes es_mx/ar/cl/uy/ve/ec | **por verificar en el PC** |
| VIT → salud (`HealthSystem`, `LivingDamageEvent.Pre`, tope 99 en VIT) | sí | sí | fórmula cúbica `(8/VIT)^3`; tests automáticos (JUnit 41, GameTest 52); la versión lineal se probó en juego, la cúbica está pendiente de probar |
| Ki (reserva, gasto, regeneración), liberación de poder (C/X), stats efectivas de VIT/FUE/VEL, HUD (H), pantalla de estadísticas (V), sync del progreso (`KiService`, `KiSystem`, `PlayerHud`, `StatsScreen`) | sí | sí | tests automáticos (JUnit 41, GameTest 52) y cliente con pantalla virtual; **en el PC del usuario pendiente** |
| Balance del combate como datos (`BalanceParams`, `BalanceManager`, `data/dragonball/balance/combat.json`, `/dragonball balance`; defaults = valores de siempre) | sí | sí | tests automáticos (JUnit 41, GameTest 52, KubeJS); **en juego pendiente** |
| FUE → daño cuerpo a cuerpo (`FuerzaSystem`, `FuerzaEvents`, tag `dragonball:armas_del_mod`; `(FUE/8)^3`, sin tope; puño y armas del mod sí, vanilla/flechas/ki no) | sí | sí | tests automáticos (JUnit 41, GameTest 52); **en juego pendiente** |
| Efecto de POD (DEF, esquiva y razas ya implementadas), rasgos de raza (solo texto), técnicas, ki como arma, técnicas, razas, formas, maestros, Otro Mundo, HUD, sincronización al cliente | sí | **no** | — |
Código: Java en `src/main/java/com/dongoyo20/dragonball/` (`core/Progression` puro; `game/progress`, `game/bestiary`, `game/entity`, `game/command`, `game/advancement`; `client/WildWolfRenderer`). Comandos: `/dragonball progress | upgrade <vit|fue|def|vel|ki|pod|con> | givexp <n>`.
Tests: `./gradlew build` (JUnit), `./gradlew runGameTestServer` (6 GameTest), `tools/verify_integrations.sh` (núcleo + FTB + KubeJS). Simulador de equilibrio en Python (`tools/simulator`, 29 pruebas), datos en `design_data/beta/bestiary.json`.
Entorno: la nube no ve Minecraft con pantalla real; lo visual lo prueba el usuario en su PC (Windows). Hay una demo con pantalla virtual (`runClientDemo`, solo ayuda).

## 5. Integraciones (todas por datos salvo GeckoLib)
GeckoLib 4.9.3 (MIT, obligatoria), Patchouli 1.21.1-93 (CC-BY-NC-SA 3.0), FTB Quests 2101.1.36 (All Rights Reserved; solo datos), KubeJS 2101.7.2 (LGPL). **No adoptadas:** TerraBlender (sin biomas propios) y Curios (sin accesorios). Detalle en `INTEGRATIONS.md`. DragonMine Z (GPL-3.0) es solo referencia: no se copia código.

## 6. Reglas que debe respetar otra IA
- Los números de ki y liberación (gasto, regeneración, velocidades, VEL→movimiento) son provisionales míos: ver `KI_IMPLEMENTATION.md`.
- Los números de balance viven en `data/dragonball/balance/combat.json` (no en constantes); ver `DMZ_ANALYSIS.md`.
- DragonMineZ es GPL-3.0: solo se usa como referencia de ideas; no se copia código.
- No cambiar Minecraft 1.21.1 ni NeoForge; no inventar APIs, versiones ni resultados de pruebas; no declarar funcional algo que solo compila.
- Distinguir siempre: diseñado / implementado / compilado / verificado en juego.
- No copiar código ni arte de otros mods; arte original, estilo bloque.
  - **Actualización (Fase 0 DMZ):** el usuario declara uso 100 % privado y sin distribución. Los *assets* de DMZ podrán copiarse a una copia privada (Fase 12 de `DMZ_IMPLEMENTATION_ROADMAP.md`). El **código** de DMZ (GPL-3.0) NO se copia: se estudia y se reimplementa con nuestra arquitectura. Ver `DMZ_SYSTEMS_MAP.md` §6.
- Explicar con palabras simples (el usuario es principiante); preguntar solo lo que es decisión suya.
- Datos antes que código (JSON/data maps) cuando sea posible; no meter lógica crítica en KubeJS.

## 7. Próximo paso
VIT → salud está implementado (ver `HEALTH_IMPLEMENTATION.md`; confirmaciones del usuario: vida real derivada de la vanilla, fórmula provisional cúbica —deuda técnica en SYSTEMS §3.2.1—, todo el daño escala salvo vacío y /kill (continuo con suelo de 0,5 HP), base universal del Saiyajin). Después se implementó FUE (`FUERZA_IMPLEMENTATION.md`). Falta que el usuario pruebe VIT y FUE en el juego. Siguen: DEF, VEL, ki (KI/POD/CON) y HUD, a decidir. Preguntas de diseño: las de `BESTIARY.md` §15 están **cerradas** (2026-10-12: quinto luchador de Baba fuera de la beta; Saibamen se autodestruyen; Vegeta con fase Ozaru; ver `BETA_BOSSES.md`). Siguen abiertas: licencia del mod y GPU del usuario. Pendiente de diseño: estadísticas de la fase Ozaru.

### Decisiones cerradas de la reimplementación de DMZ (2026-10-12)
Confirmadas por el usuario tras la Fase 0 (`DMZ_SYSTEMS_MAP.md`, `DMZ_IMPLEMENTATION_ROADMAP.md`). **Cerradas: no se reabren sin que el usuario lo pida.**
1. **Atributos:** se mantienen los 7 nuestros (VIT, FUE, DEF, VEL, KI, POD, CON). No se adoptan los 6 de DMZ.
2. **Moneda:** XP. No se adopta TP.
3. **Stamina y poise:** sin stamina (todo por Ki); sin poise por ahora (requiere animaciones específicas; posible en el futuro). Por tanto tampoco parry.
4. **Misiones:** se mantiene FTB Quests; no se implementa sistema propio.
5. **Idioma:** español principal; se mantienen las 8 variantes y el inglés como secundario.

**Estado:** la Fase 1 de la hoja de ruta (cierre de stats base: DEF, POD, CON) **no ha empezado**. Se espera a que el usuario verifique en su PC el ciclo de Ki ajustado (liberación a trozos, VEL solo al correr, corazones y FOV).

### Entorno de pruebas del usuario (2026-10-13)
El usuario ahora prueba en **CachyOS (Arch Linux)**: CPU Ryzen 5 5600X, GPU **AMD Radeon RX 9060 XT**, 16 GB de RAM, KDE Plasma 6.7 sobre Wayland, shell fish, pantalla 2560×1440 con escala 1,5. Comandos: `./gradlew build | runGameTestServer | runClient` (Java 21: `jdk21-openjdk`; el Java por defecto del sistema es 27 y no sirve para el mod). Mantener el proyecto en `/home` (no en los discos NTFS de `/mnt`). Esto cierra la duda "GPU desconocida".


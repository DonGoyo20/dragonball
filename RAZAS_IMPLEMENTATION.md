# Razas como datos — Fase 3a de la reimplementación de DMZ (2026-10-12)

Estado: **implementado y probado con tests automáticos; no verificado todavía en el juego real** (pantalla de Stats incluida). La pantalla de selección de personaje es la Fase 3b y los efectos de los rasgos llegan en ciclos posteriores.

## Qué hay
| Raza | VIT | FUE | DEF | VEL | KI | POD | CON | Rasgos (solo texto) |
|---|---|---|---|---|---|---|---|---|
| **Terrícola** (por defecto) | 8 | 8 | 8 | 8 | 0 | 8 | 0 | Adaptable, Sin cola |
| **Saiyajin** | 9 | 10 | 9 | 10 | 0 | 8 | 0 | Zenkai, Cola, Apetito saiyajin |

(El encargo escribía "Apettito saiyajin": lo escribí "Apetito saiyajin".) Los rasgos **no tienen ningún efecto**; sus descripciones lo dicen ("(Aún sin efecto)").

## Cómo funciona
- **Datos:** `data/dragonball/races/<id>.json`. Cada archivo define `id` (igual al nombre del archivo), `display_name`, `description`, `base_stats` (los 7 atributos) y `traits` (`id`, `display_name`, `description`). Los textos son **claves de traducción** (8 variantes de español + inglés).
- **Valor de un atributo** = valor inicial de la raza + 1 × mejoras compradas, con tope 99 **en el valor**. Las mejoras cuestan lo mismo para todas las razas. Consecuencia: una raza que empieza más arriba tiene menos mejoras disponibles (Saiyajin FUE 10 → 89 mejoras; Terrícola FUE 8 → 91).
- **Liberación:** la función a trozos (sin tocarla) se aplica sobre el valor de la raza, con 8 como valor vanilla. Saiyajin FUE 10: 0 % → 8, 25 % → 9, 50 % → 10, 75 % → 15, 100 % → 20. Terrícola FUE 8: 8 / 8 / 16 como siempre. VIT, FUE, DEF y VEL de la raza se usan así; KI, POD y CON no cambian con la liberación.
- **Raza del jugador:** un attachment `String` (`ModAttachments.RACE`) con valor por defecto `terricola`, guardado con el mundo, `copyOnDeath` (no se reinicia al morir ni al cambiar de dimensión) y sincronizado solo con su propio cliente (igual que el progreso). Al entrar al mundo, si no tiene raza o la suya ya no existe, se le pone la por defecto (`RaceEvents`). Los jugadores existentes siguen siendo Terrícola con los mismos stats de siempre.
- **Sincronización de las razas:** el servidor manda las razas al cliente al entrar y tras `/reload` (`RaceSyncPayload`), porque el HUD (vida máxima) y la pantalla V necesitan los valores iniciales.
- **Carga:** `RaceManager` (recarga con `/reload`). Un archivo inválido se rechaza **entero** con un mensaje en el log y las demás razas siguen. Motivos de rechazo: id con formato inválido o distinto del nombre del archivo, falta algún atributo, atributo desconocido, valor negativo, VIT/FUE/DEF/VEL por debajo de 8 (vanilla), base sin margen para mejoras bajo el tope, rasgos repetidos o sin texto. `terricola` siempre existe: si falta o es inválida se usa una copia integrada idéntica a los datos.

## Comandos
- `/dragonball race info`: raza actual, descripción y rasgos.
- `/dragonball race list`: razas disponibles.
- `/dragonball race set <raza>`: **cambia la raza y reinicia las mejoras de atributo** a los valores base de la nueva raza (la XP, el ki y la liberación se conservan). **Requiere permisos de operador** (nivel 2), porque borra mejoras compradas. El mensaje lo confirma.

## Pantalla de Stats (V)
La línea "Raza:" ahora muestra el nombre de tu raza, y debajo de la tabla hay una sección **Rasgos** con nombre y descripción (con saltos de línea). La tabla muestra el valor del atributo con la raza. Si la ventana es pequeña, el contenido sube para que se vea el pie. **No he visto la pantalla**: solo compila.

## Desviaciones del encargo (y por qué)
1. **`Race` está en `core/raza/`, pero su Codec y `RaceManager` están en `game/raza/`.** El `core/` es Java puro y no puede usar el Codec (depende de las bibliotecas de datos de Minecraft; por eso no se pueden probar con JUnit). El round-trip del Codec y la carga se prueban con GameTest; la validación y las fórmulas, con JUnit.
2. **El attachment está en `ModAttachments` (`RACE`)**, junto al del progreso, en vez de una clase `RaceAttachment` aparte (un solo registro de attachments).
3. **`race set` exige permisos de operador.** El encargo no lo decía; lo puse porque reinicia las mejoras. Si prefieres que cualquiera pueda, es quitar un `requires`.
4. **Al cambiar de raza no se devuelve la XP gastada en mejoras** (el encargo dice "se reinician los stats base", no habla de reembolso).
5. Clases nuevas de apoyo: `Atributo` (enum de los 7 atributos), `Atributos` (valor y tope por raza), `RaceService` (raza y valor inicial de un jugador) y `RaceSyncPayload`.

## Qué NO se tocó
La fórmula de esquiva (`EsquivaSystem`, `EsquivaService`, `EsquivaEvents`) y la de liberación (`KiSystem.statEfectiva`) **no se modificaron**; tampoco `BalanceParams` ni las fórmulas de VIT, FUE, DEF, POD, CON y KI. Solo cambió **de dónde sale el valor inicial** de cada atributo (antes una constante del balance, ahora la raza). La esquiva usa la VEL de la raza (Saiyajin VEL 10 → 10 en lugar de 8 al 50 %).

## Archivos
- `core/raza/`: `Atributo`, `Atributos`, `Race`, `Rasgo` (nuevos).
- `game/raza/`: `RaceManager`, `RaceService`, `RaceEvents` (nuevos). `game/command/RaceCommand`, `game/network/RaceSyncPayload` (nuevos).
- Modificados: `ModAttachments`, `ModNetwork`, `PlayerProgress` (tope por raza), `ProgressService`, `AttributeValues`, `HealthService`, `FuerzaService`, `KiService`, `DragonBallCommands` (registra `race` y `upgrade` con tope por raza), `PlayerHud`, `StatsScreen`.
- Datos: `data/dragonball/races/terricola.json`, `saiyajin.json`; 8 archivos de idioma.
- Tests: `RaceTest` (JUnit), `RazaGameTests` (GameTest).

## Tests (ejecutados 2026-10-12)
- **JUnit 81/81** (14 nuevos en `RaceTest`): stats base de las dos razas, inmutabilidad, ids inválidos, atributos faltantes o negativos, rasgos inválidos, valores bajo vanilla, base sin margen, valor y tope por raza, recorte, base por defecto, y la liberación con FUE 10 (8 / 9 / 10 / 15 / 20) y FUE 8 (8 / 8 / 16).
- **GameTest 80/80** (13 nuevos en `RazaGameTests`): carga de las dos razas desde los datos; rechazo de 7 archivos inválidos y copia integrada de Terrícola; round-trip del Codec y de `RaceSyncPayload`; jugador nuevo = Terrícola; raza desconocida guardada → por defecto; `race set` cambia la raza y los stats; sin permisos o raza desconocida no cambia; `race info` y `race list`; reinicio de mejoras con XP conservada y mismo coste; tope 99 por raza; liberación 8/10/20 y 8/8/16 con jugadores reales; el puño de un Saiyajin pega ×1,95 al 50 %, ×1 al 0 % y ×15,6 al 100 %; el Saiyajin recibe menos daño que el Terrícola.
- **Integraciones** (`tools/verify_integrations.sh`): núcleo, FTB y KubeJS OK.

## Sin probar en el juego
- Que la raza **sobreviva a la muerte y a un cambio de dimensión** (está declarado con `copyOnDeath` y el attachment viaja con el jugador, pero no se pudo simular la muerte en GameTest).
- La sincronización real del attachment al cliente (los jugadores simulados no negocian canales; sí se probó el payload de las razas).
- La pantalla V y el HUD con la raza.

## Pendiente (otros ciclos)
Fase 3b: pantalla de selección de personaje. Efectos de los rasgos (Zenkai, Cola, Apetito saiyajin, Adaptable, Sin cola). Ozaru. Más razas.

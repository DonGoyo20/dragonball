# INTEGRATIONS — Librerías y mods de terceros (2026-10-11)

Leyenda de estado: **verificado en juego** = comprobado arrancando Minecraft (servidor de pruebas o cliente real en pantalla virtual) · **verificado por prueba automática** · **no verificado** = hay que probarlo en tu PC.

## Tabla resumen

| Librería | Versión | Licencia | Para qué | Estado |
|---|---|---|---|---|
| NeoForge (MDK) | 21.1.259 / MC 1.21.1 | MDK: MIT | Base del mod | Compila y arranca (servidor y cliente) |
| GeckoLib | 4.9.3 | MIT | Modelo y animaciones del Lobo salvaje | **Integrada.** Dependencia obligatoria. Modelo+textura dibujados en cliente real (captura `docs/img/`). **Verificado por el usuario en su PC (2026-10-11):** el lobo camina con animación, ataca y suelta objetos. La animación concreta de mordisco no se ha confirmado por separado |
| Patchouli | 1.21.1-93-NEOFORGE | CC-BY-NC-SA 3.0 | Libro de guía en el juego | **Parcial.** Solo se usa como dato (JSON), no se enlaza código. Servidor y cliente cargan `book.json` y 18 JSON de contenido sin errores. **Verificado por el usuario en su PC:** el libro se abre con `/give @s patchouli:guide_book[patchouli:book="dragonball:guia"]` y muestra título, texto de introducción y 4 categorías. Entradas internas sin revisar |
| FTB Quests (+ FTB Library, FTB Teams, Architectury) | 2101.1.36 / 2101.1.37 / 2101.1.11 / 13.0.8 | FTB Quests: "All Rights Reserved" (declarada en su mods.toml) | Misiones | **Parcial.** Solo dato (`integrations/ftbquests/quests`), sin código enlazado. Verificado por prueba: carga "1 capítulo, 3 misiones" y las referencias existen. **No verificado:** completar las misiones jugando |
| KubeJS | 2101.7.2-build.379 | LGPL-3.0 | Ajuste de equilibrio sin recompilar | **Integrada como dato.** Prueba automática: el script cambia la XP de la araña (8→50) y el zombi no cambia. Sin lógica crítica en scripts |
| TerraBlender | 4.1.0.8 | LGPL | Biomas propios | **No añadida** (ver abajo) |
| Curios | 9.5.1 | LGPL | Ranuras de accesorios | **No añadida** (ver abajo) |

Solo GeckoLib es dependencia obligatoria del mod. Patchouli, FTB Quests y KubeJS son opcionales (`localRuntime`, solo para desarrollo/pruebas); el mod funciona sin ellas, y en `build.gradle` FTB Quests y KubeJS solo se cargan con `-PwithFtbQuests` / `-PwithKubeJs`.

## Decisiones de arquitectura
1. **Integración por datos, no por código**, para FTB Quests, Patchouli y KubeJS: así no hay licencias que se contagien (FTB Quests es "All Rights Reserved", Patchouli es NC-SA) y nada del núcleo depende de ellas.
2. **Puente entre mod y misiones** usando cosas de NeoForge/vanilla: un criterio de logro propio (`dragonball:total_level`) y los logros `dragonball:level_2|5|10`; FTB los usa con su tarea "advancement". No hay adaptador Java.
3. **Bestiario como Data Map de NeoForge** (`data_maps/entity_type/bestiary.json`): asigna nivel/XP también a mobs vanilla, y KubeJS puede sobreescribirlo.
4. **Pruebas separadas por mod**: los jugadores simulados de GameTest no pueden recibir los paquetes de sincronización de KubeJS/FTB, así que hay tres ejecuciones independientes (núcleo, FTB, KubeJS).
5. **No se copió código de DragonMine Z (GPL-3.0)**: solo se consultó como referencia.

## Dependencias no adoptadas
- **TerraBlender:** no hay ningún bioma propio en la beta (el mundo es el Overworld con lugares especiales). Se añadirá si se diseñan biomas.
- **Curios:** no hay accesorios en el diseño de la beta (equipo y estadísticas vienen del sistema de atributos propio). Se reevalúa con el diseño de objetos.

## Cómo verificarlo
`tools/verify_integrations.sh` ejecuta las tres ejecuciones de prueba y exige que se ejecuten ≥1 pruebas y que aparezcan las líneas de log esperadas. Cliente con pantalla virtual: ver README.md.

## Limitaciones conocidas
- La licencia del propio mod sigue por decidir ("All Rights Reserved" provisional).
- El cliente headless usa renderizado por software y sin audio; solo sirve para detectar errores de carga y ver una captura, no para evaluar rendimiento ni sensación de juego.
- Textura del lobo: provisional generada por script.

## Verificado en juego por el usuario (2026-10-11, PC propio)
- Build de Gradle en Windows: BUILD SUCCESSFUL, 5 pruebas JUnit.
- `/dragonball givexp`, `upgrade vit`, `progress`: funcionan; XP subió de 90 a 160 tras combatir lobos.
- Lobo: camina, ataca (en supervivencia) y suelta objetos.
- Libro de Patchouli: se abre y se ve.
- Pendiente: misiones de FTB Quests jugando; KubeJS en juego.

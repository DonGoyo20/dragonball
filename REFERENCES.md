# REFERENCES — Investigación Fase 0 (2026-10-11)

> Metodología: búsquedas web. El acceso directo (WebFetch/curl) a Modrinth, NeoForged,
> JinGames y la API de GitHub estaba bloqueado por la red del entorno, así que **no se
> leyó ningún código ni página oficial directamente**. Todo lo de abajo viene de
> resultados de búsqueda y debe verificarse antes de comprometer decisiones.

## Mods de referencia

| Proyecto | Versión / loader | Código fuente | Notas | Confianza |
|---|---|---|---|---|
| Dragon Block C (JinGames) | Minecraft 1.7.10 (Forge antiguo). Un port 1.12.2 de terceros fue abandonado. | No se encontró repo público ni licencia. Asumir **cerrado / todos los derechos reservados**. | Creador original fallecido; desarrollo lento a cargo de su hermano. Sitio: main.jingames.net | Media |
| DragonMine Z (DMZ) | Minecraft 1.20.1, Forge. Requiere GeckoLib 4.8.3+, TerraBlender, Curios. Última versión vista: 2.1.3 | Repo público `DragonMineZ/dragonminez`, **GPL-3.0** según resultados. Existen forks y un port a 1.21.1 de terceros. | Es el candidato a estudiar; GPL-3.0 obliga a que un derivado sea GPL-3.0. Docs archivadas en mar-2026. | Media (licencia no leída del repo) |
| Dragon Block O | NeoForge 1.21.1 | Desconocido | Proyecto pequeño (~33 descargas). 5 razas, Kamehameha, Roshi y Whis como NPC. | Baja |
| Stupid DBC | NeoForge 1.21.1 | Desconocido | Hecho con MCreator, sin mobs. | Baja |
| Dragon Ball Daima Mod | NeoForge 1.21.1 / 1.21.8 | Desconocido | Solo se vio la ficha de hosting. | Baja |
| DragonLib | Fabric/Forge/NeoForge, 1.20.1 y 1.21.1 | Open source | Librería, no mod de DB. | Baja |

## Plataforma

- Minecraft abandonó la numeración 1.x tras **1.21.11** (dic-2025). Ahora es año.entrega: 26.1, 26.2…
- Desde **26.1** se requiere **Java 25**; se eliminó la ofuscación (nombres oficiales de Mojang).
- NeoForge 26.1 salió en beta; el propio equipo estimó (opinión personal) que 26.1 será la próxima versión estable de modding, en sustitución de 1.21.1.
- Herramientas citadas: ModDevGradle 2.0.141, NeoGradle 7.1.21.
- GeckoLib para NeoForge 1.21.1: **4.9.2** (jul-2026). La rama 5.x es para 1.21.8+.
- Un benchmark de agregador da Fabric ligeramente por delante de NeoForge en FPS; no es una fuente fiable para decidir.

## Fuentes consultadas
- https://github.com/DragonMineZ/dragonminez
- https://neoforged.net/news/26.1release/
- https://neoforged.net/news/26.1snapshots/
- https://www.modpackindex.com/mod/25540/jingames-dragon-block-c
- https://www.player.games/es-ES/creator-hub/minecraft/community/dragonblocko
- https://www.curseforge.com/minecraft/mc-mods/geckolib/files/8350073

## Pendiente de verificar
- Licencia real y estructura del código de DMZ (leer el repo).
- Última versión estable de NeoForge y de Minecraft hoy (el entorno no llega a maven.neoforged.net).
- ~~Modelo exacto de la GPU AMD~~: es una Radeon RX 9060 XT (2026-10-13).

## Canon de Dragon Ball verificado para el bestiario (búsquedas, 2026-10-11)
> Fuentes mayormente wikis de fans, no oficiales. Se citan para saber qué está confirmado y qué no.

- **Luchadores de Uranai Baba:** Fangs el Vampiro, See-Through (hombre invisible), Bandages la Momia (arena "Devil's Toilet") y Spike el Hombre Diablo (rayo Devilmite que amplifica el mal del objetivo). Existe un quinto luchador enmascarado; **"Man-Wolf" no aparece en ninguna fuente encontrada**, así que no se incluye.
  - https://dragonball.fandom.com/wiki/Bandages_the_Mummy
  - https://giantbomb.com/wiki/Characters/Fangs_the_Vampire
  - https://daddyjim.ai/pt/dragon-ball/saga/fortuneteller-baba-saga
- **Red Ribbon:** Comandante Red (líder), Oficial Black, Coronel Silver, Ninja Murasaki (Sargento Mayor Purple en japonés), Mayor Metallitron, Buyon (monstruo grande de poder poco claro), Tao Pai Pai (asesino a sueldo), General Blue.
  - https://en.wikipedia.org/wiki/Red_Ribbon_Army
- **Hijos de Piccolo Daimaō:** Tambourine (cazar a los participantes del torneo), Cymbal (reunir esferas; lo mata y se lo come Yajirobe), Drum (combatir a Tenshinhan), Piano (mano derecha del Rey Demonio).
  - https://villains.fandom.com/wiki/Tambourine
  - https://www.kanzenshuu.com/wiki/Cymbal
  - https://www.kanzenshuu.com/wiki/Drum
  - https://www.kanzenshuu.com/wiki/Piano
- **Sin verificar en esta sesión (de memoria):** banda de Pilaf (Pilaf, Shu, Mai y sus máquinas), saga Saiyan (Saibamen, Raditz, Nappa, Vegeta y su fase Ozaru).

## NeoForge 1.21.1 (búsqueda)
- Modificadores de bioma: `neoforge:add_spawns` (bioma o etiqueta, peso, tamaño mínimo/máximo de grupo), archivos en `data/<modid>/neoforge/biome_modifier/`. https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier
- Una entidad nueva necesita su restricción de aparición registrada con `RegisterSpawnPlacementsEvent`, o puede aparecer en el aire.
- **No verificado:** registro de entidades y atributos, IA, estructuras por datos, botín, flujo Blockbench → GeckoLib.

## DragonMineZ leído directamente (2026-10-12)
Clon local de `DragonMineZ/dragonminez` (versión 2.1.3, 1 commit, sin historial): **Minecraft 1.20.1, Forge 47.4.10, Java 17, licencia GPL-3.0** (confirmado en `LICENSE` y `mod_license`), GeckoLib + TerraBlender + Curios obligatorios, sin `src/test`. Esto sustituye a las suposiciones de la fase 0 ("licencia no leída"). Análisis completo y decisiones en `DMZ_ANALYSIS.md`. No se copió código.

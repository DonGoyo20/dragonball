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
- Modelo exacto de la GPU AMD.

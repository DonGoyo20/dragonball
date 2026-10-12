# BETA_BOSSES — Jefes de la beta (2026-10-12)
Resume los jefes de la beta y las decisiones cerradas. Datos de cada uno: `design_data/beta/bestiary.json`; descripción: `BESTIARY.md`. **Solo diseño: ningún jefe está implementado** (solo existe el Lobo salvaje).

## La saga Saiyajin en la beta
Es el tramo final de la beta (niveles ~95–171) y **termina al derrotar a Vegeta**. Orden: Raditz (95) → Saibamen en grupos de 3 (108) → Nappa (140) → Vegeta (171). Raditz, Nappa y Vegeta aparecen solo por misión o evento.

| Jefe | Nivel | Arquetipo | Notas de diseño | Estado |
|---|---|---|---|---|
| Raditz | 95 | guerrero | Primera amenaza grande de la historia | diseñado |
| Saibamen (3 por grupo) | 108 | veloz | **Autodestrucción:** al morir hacen daño en área al jugador cercano; no dañan a otros Saibamen; el daño escala con VIT (SYSTEMS §3.1) | diseñado; radio y daño sin definir |
| Nappa | 140 | bruto | Jefe de arco intermedio | diseñado |
| **Vegeta** | 171 | guerrero | Jefe final de la beta, **con fase Ozaru** (abajo) | diseñado; fase Ozaru sin simular |

### Vegeta y la fase Ozaru (decisión cerrada)
1. Primera fase: combate normal de Vegeta (guerrero, técnicas `melee`, `ki_blast`, `galick_ho`, `big_bang`).
2. Transformación en Ozaru: cambia el aspecto (modelo o escala) y aumentan las estadísticas.
3. Derrotarlo cierra la saga Saiyajin y la beta.
4. Alternativa aceptada: si el modelo 3D del mono gigante es demasiado costoso, se representa como cambio de fase con aura y estadísticas, sin cambiar el modelo.
**Pendiente antes de implementar:** definir el multiplicador y arquetipo de la fase Ozaru, simular el combate de dos fases y comprobar la garantía de historia (que cualquier build pueda vencerlo, `SYSTEMS.md` §2.3).

## Otros jefes de la beta (resumen)
Red Ribbon: Murasaki, Blue, Metallitron, Buyon, Tao Pai Pai (jefe final del arco). Baba: Fangs, See-Through, Bandages, Spike. Hijos de Piccolo Daimaō: Tambourine, Cymbal, Drum, Piano. Secretos: Dinosaurio ancestral, Oso gigante de montaña, Espíritu guerrero. Opcionales/removibles de vanilla: Wither, Warden, Dragón del End. Personajes de misión (no jefes de combate): Pilaf, Black, Red.

## Fuera de la beta
- **Quinto luchador enmascarado de Baba:** fuera por decisión del usuario (canon poco claro); se podrá añadir en la 1.0 si se aclara.
- **Man-Wolf:** no aparece en ninguna fuente comprobada.
- Todo lo posterior a la saga Saiyajin: Freezer, Androides, Cell, Majin Buu, dioses, GT y Super (`CONTENT_DESIGN.md` §8.2).

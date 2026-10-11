# BESTIARY — Bestiario de la beta

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Los datos viven en
> `design_data/beta/bestiary.json` (así los leerá el juego) y se validan y simulan con
> `tools/simulator/bestiary.py`. Las entidades `dragonball:*` son **nombres propuestos**;
> los niveles de los mobs de Minecraft son propuestas mías. Las referencias a la serie son de memoria, sin consultar fuentes.

## 1. Cómo se define un enemigo
Cada enemigo es una entrada de datos; ninguna regla vive en el código.

| Campo | Qué es |
|---|---|
| `id`, `name`, `source` | Identificador, nombre y entidad base (vanilla o del mod) |
| `level` | **Nivel total** fijo (no escala con el jugador, como Elden Ring) |
| `archetype` | Reparto de sus atributos (§2) |
| `kind` | `normal`, `elite`, `boss` (opcional), `story` (jefe de historia), `secret` (jefe secreto) |
| `techniques` | Lista de técnicas que usa |
| `regions` | Dónde aparece (§5) |
| `power_mult` | Calibración de dificultad (§4); por defecto 1 |
| `xp` | Opcional: sustituye al XP calculado |
| `secret_hint`, `reward` | Solo secretos: la pista y la recompensa |

## 2. Arquetipos y atributos
Un enemigo de nivel `N` tiene `T = N − 1` mejoras repartidas según su arquetipo. Cada atributo vale `5 + reparto × T` y el poder efectivo sigue las mismas fórmulas que el jugador (`SYSTEMS.md` §2.2), multiplicado por `power_mult`.

| Arquetipo | VIT | FUE | DEF | VEL | KI | POD | CON | Carácter |
|---|---|---|---|---|---|---|---|---|
| bruto | .25 | .35 | .20 | .10 | 0 | 0 | .10 | Golpea fuerte, sin ki |
| veloz | .15 | .25 | .10 | .40 | 0 | 0 | .10 | Esquiva mucho |
| tirador | .15 | .05 | .15 | .10 | .15 | .30 | .10 | Ataca con ki |
| guerrero | .18 | .22 | .15 | .15 | .08 | .14 | .08 | Mixto |
| tanque | .35 | .20 | .35 | .05 | 0 | 0 | .05 | Aguanta |
| bestia | .30 | .35 | .15 | .20 | 0 | 0 | 0 | Animal |
| equilibrado | ≈ .143 en todo | | | | | | | Como un jugador equilibrado |

## 3. Reglas de XP
Se usa la tabla guía de `SYSTEMS.md` §2.5 y, para los jefes, un **porcentaje de la XP acumulada** hasta su nivel (así un jefe recompensa lo mismo, en proporción, en cualquier punto de la historia):

| Tipo | XP |
|---|---|
| normal | tabla guía |
| élite | ×3 la tabla guía |
| jefe de historia (`story`) | **15 %** de la XP acumulada hasta ese nivel |
| jefe opcional (`boss`) | 10 % |
| jefe secreto (`secret`) | 8 % |

Ejemplos: Raditz ≈ 1.725 XP (3,3 mejoras), Nappa ≈ 14.755 (7,7), Vegeta ≈ 29.607 (10,0). Los enemigos normales dan entre 0,2 y 0,5 mejoras.

*Por qué no ×25 sobre la tabla guía:* con ese multiplicador, Raditz daba un tercio de toda la XP acumulada hasta entonces y Vegeta solo un 6 %. El porcentaje evita esa desproporción.

## 4. Dificultad calibrada con el simulador
Con `power_mult` = 1 los jefes eran demasiado fáciles: Vegeta perdía el 0 % de las veces contra todos los builds. Se añadió `power_mult` y se calibró así:

- **Jefes de historia:** el más duro que cumple la garantía (`CONTENT_DESIGN.md` §2.1) **con margen**: peor build normal ≥ 40 % y peor extremo (con +1,4 h de juego) ≥ 78 % en calibración; la regla real es 30 % y 70 %.
- **Élite:** que el jugador gane de media ≈ 75 % de los combates.
- **Jefes opcionales y secretos:** más duros, el jugador gana de media ≈ 35 % (no obligatorios; sin garantía de que todos los builds puedan).

Resultado verificado con semillas distintas a la de calibración:

| Jefe de historia | Nivel | Mult. | Peor build normal | Peor extremo (+1,4 h) |
|---|---|---|---|---|
| Raditz | 55 | 1,05 | 79 % | 100 % |
| Nappa | 130 | 1,25 | 42–46 % | 97–98 % |
| Vegeta | 171 | 1,9 | 50–54 % | 82 % |

Los combates normales cuestan entre un 13 % y un 71 % de la vida del jugador (medido con el simulador).

## 5. Regiones y niveles del mundo
Cada enemigo tiene un nivel fijo; **el nivel de una zona lo marcan los enemigos que aparecen en ella**. Rangos orientativos (P):

| Región | Niveles | Ejemplos |
|---|---|---|
| Pradera y bosque (inicio) | 1 – 30 | zombi, lobo, bandido, jabalí |
| Montaña, desierto, pantano, costa | 10 – 50 | husk, pterodáctilo, oso, tiranosaurio |
| Nieve, mar | 10 – 45 | vagabundo, ahogado |
| Cuevas | 6 – 70 | araña de las cuevas, dinosaurio ancestral (secreto) |
| Camino de la Serpiente (Otro Mundo) | 45 – 120 | demonios, espíritu guerrero (secreto) |
| Invasión y jefes de historia | 55 – 171 | Raditz, Saibamen, Nappa, Vegeta |
| Nether | 70 – 95 | piglins, blaze, ghast, esqueleto wither |
| End | 120 – 150 | shulker, dragón |
| Cueva profunda | 140 | warden |

Los mobs de Minecraft **no escalan**: un zombi nivel 3 pasa a no dar XP 33 niveles por encima y se vuelve decoración, como en un RPG de mundo abierto.

## 6. Jefes secretos de la beta (P)
| Jefe | Nivel | Pista | Recompensa (cualitativa) |
|---|---|---|---|
| Dinosaurio ancestral | 70 | Cazadores del desierto hablan de huesos enormes en una cueva | Pesas ancestrales: objeto de entrenamiento con +0,5 al multiplicador de penalización (el tope ×5 no cambia) |
| Asesino Tao | 95 | Un asesino a sueldo viaja por los caminos del desierto | Técnica única Dodonpa (se compra; atributos para usarla) |
| Espíritu guerrero | 120 | Un guerrero legendario espera en un recodo del Camino de la Serpiente | Atajo de dominio: +30 % instantáneo en una forma que ya poseas |

Cumplen la regla de `CONTENT_DESIGN.md` §2: recompensas cualitativas, nunca niveles regalados.

## 7. Mecánicas especiales
- **Saibaman:** aparece en grupos de 6; su autodestrucción es un ataque de ki que lo mata a él. En el simulador se modela como técnica con `self_destruct`.
- **Creeper:** igual que el Saibaman.
- **Warden y Wither:** jefes vanilla opcionales; el Warden ataca por sonido (se modela como ki).

## 8. Lista completa (47 enemigos)
Generada desde `bestiary.json`. "Mejoras equiv." es el XP del enemigo dividido por el coste de una mejora a su nivel.

| Enemigo | Entidad base | Nivel | Tipo | Arquetipo | XP | Mejoras equiv. | Mult. | Regiones |
|---|---|---|---|---|---|---|---|---|
| Zombi | `minecraft:zombie` | 3 | normal | bruto | 8 | 0.5 | 1 | pradera, bosque |
| Araña | `minecraft:spider` | 4 | normal | veloz | 10 | 0.5 | 1 | pradera, bosque |
| Lobo salvaje | `dragonball:wild_wolf` | 4 | normal | bestia | 10 | 0.5 | 1 | pradera, bosque |
| Esqueleto | `minecraft:skeleton` | 5 | normal | tirador | 12 | 0.5 | 1 | pradera, bosque |
| Jabalí | `dragonball:boar` | 6 | normal | bestia | 14 | 0.5 | 1 | bosque |
| Slime | `minecraft:slime` | 6 | normal | bestia | 14 | 0.5 | 1 | pantano, cueva |
| Creeper | `minecraft:creeper` | 7 | normal | bruto | 16 | 0.5 | 1 | pradera, bosque |
| Araña de las cuevas | `minecraft:cave_spider` | 8 | normal | veloz | 18 | 0.4 | 1 | cueva |
| Ladrón | `dragonball:thief` | 8 | normal | veloz | 18 | 0.4 | 1 | pradera, bosque |
| Ahogado | `minecraft:drowned` | 10 | normal | bruto | 23 | 0.4 | 1 | costa, mar |
| Dinosaurio pequeño | `dragonball:small_dino` | 12 | normal | veloz | 28 | 0.4 | 1 | pradera, desierto |
| Fantasma | `minecraft:phantom` | 12 | normal | veloz | 28 | 0.4 | 1 | pradera, montana |
| Husk | `minecraft:husk` | 12 | normal | bruto | 28 | 0.4 | 1 | desierto |
| Bandido | `dragonball:bandit` | 14 | normal | guerrero | 33 | 0.4 | 1 | pradera, bosque, desierto |
| Oso pardo | `dragonball:brown_bear` | 14 | normal | bestia | 33 | 0.4 | 1 | bosque, montana |
| Vagabundo | `minecraft:stray` | 14 | normal | tirador | 33 | 0.4 | 1 | nieve |
| Bruja | `minecraft:witch` | 16 | normal | tirador | 38 | 0.4 | 1 | pantano |
| Saqueador | `minecraft:pillager` | 18 | normal | tirador | 44 | 0.4 | 1 | pradera, montana |
| Pterodáctilo | `dragonball:pterodactyl` | 22 | normal | veloz | 55 | 0.4 | 1 | montana, costa |
| Vindicador | `minecraft:vindicator` | 24 | normal | bruto | 61 | 0.4 | 1 | bosque, montana |
| Enderman | `minecraft:enderman` | 26 | normal | veloz | 68 | 0.4 | 1 | pradera, desierto, montana |
| Capitán bandido | `dragonball:bandit_captain` | 30 | élite | guerrero | 240 | 1.1 | 1 | desierto |
| Soldado de la Patrulla Roja | `dragonball:red_ribbon_soldier` | 36 | normal | guerrero | 99 | 0.4 | 1 | montana |
| Triceratops | `dragonball:triceratops` | 38 | élite | tanque | 318 | 1.1 | 1.05 | pradera |
| Evocador | `minecraft:evoker` | 40 | élite | tirador | 338 | 1.0 | 1.1 | bosque |
| Demonio menor | `dragonball:minor_demon` | 45 | normal | bruto | 129 | 0.3 | 1 | camino_serpiente |
| Devastador | `minecraft:ravager` | 45 | élite | bruto | 388 | 1.0 | 0.85 | montana |
| Tiranosaurio | `dragonball:tyrannosaur` | 45 | élite | bestia | 388 | 1.0 | 0.8 | desierto, pradera |
| Raditz | `dragonball:raditz` | 55 | jefe de historia | guerrero | 1725 | 3.3 | 1.05 | historia |
| Dinosaurio ancestral | `dragonball:ancient_dino` | 70 | jefe secreto | bestia | 1666 | 2.2 | 1.4 | cueva |
| Piglin zombi | `minecraft:zombified_piglin` | 70 | normal | bruto | 214 | 0.3 | 1 | nether |
| Piglin | `minecraft:piglin` | 75 | normal | guerrero | 231 | 0.3 | 1 | nether |
| Cubo de magma | `minecraft:magma_cube` | 78 | normal | bestia | 241 | 0.3 | 1 | nether |
| Hoglin | `minecraft:hoglin` | 80 | normal | bestia | 248 | 0.3 | 1 | nether |
| Blaze | `minecraft:blaze` | 85 | normal | tirador | 265 | 0.3 | 1 | nether |
| Saibaman | `dragonball:saibaman` | 85 | normal | veloz | 265 | 0.3 | 1 | invasion |
| Demonio del camino | `dragonball:snake_way_demon` | 90 | normal | guerrero | 282 | 0.3 | 1 | camino_serpiente |
| Ghast | `minecraft:ghast` | 90 | élite | tirador | 845 | 0.8 | 1.95 | nether |
| Esqueleto wither | `minecraft:wither_skeleton` | 95 | élite | guerrero | 895 | 0.8 | 1.25 | nether |
| Asesino Tao | `dragonball:mercenary_tao` | 95 | jefe secreto | guerrero | 3569 | 3.0 | 1.75 | desierto |
| Espíritu guerrero | `dragonball:warrior_spirit` | 120 | jefe secreto | guerrero | 6427 | 3.8 | 1.85 | camino_serpiente |
| Shulker | `minecraft:shulker` | 120 | normal | tanque | 380 | 0.2 | 1 | end |
| Nappa | `dragonball:nappa` | 130 | jefe de historia | bruto | 14755 | 7.7 | 1.25 | historia |
| Wither | `minecraft:wither` | 130 | jefe opcional | tirador | 9837 | 5.1 | 1.55 | jefe_opcional |
| Warden | `minecraft:warden` | 140 | jefe opcional | bruto | 11870 | 5.5 | 1.55 | cueva_profunda |
| Dragón del End | `minecraft:ender_dragon` | 150 | jefe opcional | bestia | 14142 | 5.9 | 1.75 | end |
| Vegeta | `dragonball:vegeta` | 171 | jefe de historia | guerrero | 29607 | 10.0 | 1.9 | historia |

## 9. Errores encontrados al hacerlo (para transparencia)
1. **Jefes demasiado fáciles** con los datos iniciales (Vegeta 0 % de derrotas): se añadió `power_mult`.
2. **Calibración sobreajustada:** calibrando justo en el umbral, una verificación con otra semilla dio Raditz 28 %, Nappa 19 % y Vegeta 25 % para el peor build (la regla pide 30 %). Se añadió un margen y se verificó con dos semillas distintas.
3. **Dato inválido:** el Warden tenía una sola técnica; el validador lo detectó.
4. **Pruebas mal acotadas:** al añadir la raza `npc` al simulador, las pruebas empezaron a recorrerla como si fuera jugable; ahora recorren solo las razas jugables.
5. **Referencia de prueba errónea:** comparaba la XP acumulada del nivel 171 con la cifra del nivel 175.

## 10. Límites
- El simulador no mide alcance, movimiento, esquivas con habilidad ni daño continuo; un jefe real se sentirá distinto.
- Los multiplicadores y las técnicas de los jefes son propuestas para ajustar con combate real en la Fase 2.
- Los nombres de entidades `dragonball:*` y los comportamientos especiales están por diseñar.
- Los mobs vanilla fuera de la lista (golem, aldeanos, animales) no tienen nivel todavía.

## 11. Preguntas abiertas
1. ¿Te parece bien que los mobs de Minecraft tengan nivel fijo y se queden como "decoración" cuando subes?
2. ¿Quieres los jefes vanilla (Wither, Warden, Dragón del End) como opcionales dentro de la beta, o que queden para después?
3. ¿Los Saibamen en grupos de 6 te parecen bien como la primera prueba de combate múltiple?

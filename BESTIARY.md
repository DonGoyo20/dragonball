# BESTIARY — Bestiario de la beta (Dragon Ball clásico → saga Saiyan)

> Estado: **diseño sobre el papel, 2026-10-11 (v2).** Los datos viven en
> `design_data/beta/bestiary.json` (119 entradas) y se validan y simulan con
> `tools/simulator/bestiary.py`. Plan técnico y preguntas de aprobación: `BESTIARY_PLAN.md`.
> Nada de esto está implementado en el juego. Las entidades `dragonball:*` son **nombres propuestos**.
> Distingo **canon verificado** (comprobado en búsquedas de esta sesión, fuentes de fans), **de memoria** y **original**.

## 1. Alcance de la beta
Dragon Ball clásico hasta el final de la saga Saiyan. **Fuera:** Freezer y Ginyu, Cell, Majin Buu, GT, Super y películas posteriores.

## 2. Cómo se define un enemigo
Cada enemigo es una entrada de datos; ninguna regla vive en el código.

| Campo | Qué es |
|---|---|
| `id`, `name`, `source` | Identificador, nombre y entidad base (vanilla o del mod) |
| `level` | **Nivel total** fijo (no escala con el jugador, como Elden Ring) |
| `archetype` | Reparto de sus atributos (§3) |
| `kind` | `normal`, `elite`, `boss` (opcional o de arena), `story` (jefe de historia), `secret` (jefe secreto), `passive` (animal pasivo), `npc` (no combatiente) |
| `behavior` | Comportamiento de IA (§6) |
| `techniques` | Técnicas que usa |
| `regions` | Dónde aparece (§7) |
| `power_mult` | Calibración de dificultad (§5) |
| `fragility` | Multiplica el daño recibido (animales pasivos) |
| `group_size` | Tamaño de grupo (Saibamen: 3) |
| `named`, `canon` | Personaje con nombre y su estado de verificación |
| `special` | Habilidad especial y si es canon o decisión de diseño |
| `optional_removable` | Se puede quitar si complica el código |
| `xp` | Opcional: sustituye al XP calculado |
| `secret_hint`, `reward` | Solo secretos |

## 3. Arquetipos y atributos
Un enemigo de nivel `N` tiene `T = N − 1` mejoras repartidas según su arquetipo. Cada atributo vale `5 + reparto × T`; el poder efectivo sigue las fórmulas del jugador (`SYSTEMS.md` §2.2), multiplicado por `power_mult`.

| Arquetipo | VIT | FUE | DEF | VEL | KI | POD | CON | Carácter |
|---|---|---|---|---|---|---|---|---|
| bruto | .25 | .35 | .20 | .10 | 0 | 0 | .10 | Golpea fuerte, sin ki |
| veloz | .15 | .25 | .10 | .40 | 0 | 0 | .10 | Esquiva mucho |
| tirador | .15 | .05 | .15 | .10 | .15 | .30 | .10 | Ataca a distancia |
| guerrero | .18 | .22 | .15 | .15 | .08 | .14 | .08 | Mixto |
| tanque | .35 | .20 | .35 | .05 | 0 | 0 | .05 | Aguanta |
| bestia | .30 | .35 | .15 | .20 | 0 | 0 | 0 | Animal |
| equilibrado / pasivo | ≈ .143 en todo | | | | | | | Como un jugador equilibrado |

## 4. Reglas de XP
Tabla guía de `SYSTEMS.md` §2.5; los jefes pagan un **porcentaje de la XP acumulada** hasta su nivel:

| Tipo | XP |
|---|---|
| normal | tabla guía |
| élite | ×3 la tabla guía |
| jefe de historia (`story`) | **6 %** de la XP acumulada hasta su nivel |
| jefe opcional/arena (`boss`) | 4 % |
| jefe secreto (`secret`) | 3 % |
| animal pasivo, NPC | **0** (se matan para conseguir comida, no para subir) |

**Presupuesto (comprobado con script):** todos los jefes únicos suman 47,294 XP = **24 %** de la XP total hasta el nivel 171, es decir, 48 % del presupuesto de combate (50 %). El resto lo ponen los enemigos normales y élite. *Con los porcentajes de la primera versión (15 / 10 / 8 %) los jefes sumaban el 60 % del total, el 120 % del presupuesto de combate; al ampliar el alcance hubo que bajarlos.*

Ejemplos: Raditz ≈ 2,677 XP, Nappa ≈ 7,122, Vegeta ≈ 11,843. Los enemigos normales dan entre 0,2 y 0,5 mejoras.

## 5. Dificultad calibrada con el simulador
Con `power_mult` = 1 los jefes eran demasiado fáciles. Se calibró así:

- **Jefes de historia:** el más duro que cumple la garantía de historia (`CONTENT_DESIGN.md` §2.1) **con margen**: peor build normal ≥ 40 % y peor extremo (con +1,4 h) ≥ 78 % en calibración; la regla real es 30 % y 70 %.
- **Élite:** que el jugador gane de media ≈ 75 %.
- **Jefes opcionales, de arena y secretos:** el jugador gana de media ≈ 35 % (no obligatorios).
- **Animales pasivos:** `fragility` 10. El Saiyajin de nivel 1 mata una vaca, un cerdo o una gallina en **1,2 golpes** y el Humano en **1,7** (medido con el simulador).

Verificación con una semilla distinta a la de calibración (800 duelos por pareja):

| Jefe de historia | Nivel | Mult. | Peor build normal | Peor extremo (+1,4 h) | XP |
|---|---|---|---|---|---|
| Ninja Murasaki | 32 | 0.9 | 47 % | 100 % | 188 |
| General Blue | 40 | 0.9 | 60 % | 100 % | 319 |
| Mayor Metallitron | 44 | 0.95 | 64 % | 100 % | 401 |
| Buyon | 47 | 0.7 | 87 % | 100 % | 470 |
| Tao Pai Pai | 52 | 0.85 | 56 % | 100 % | 602 |
| Tambourine | 62 | 0.8 | 64 % | 100 % | 926 |
| Piano | 76 | 1.1 | 49 % | 100 % | 1533 |
| Raditz | 95 | 1.2 | 56 % | 100 % | 2677 |
| Nappa | 140 | 1.05 | 74 % | 99 % | 7122 |
| Vegeta | 171 | 1.9 | 51 % | 84 % | 11843 |

## 6. Comportamientos
| Comportamiento | Qué hace |
|---|---|
| `pasivo` | No ataca ni huye; sirve de comida |
| `huida` | Escapa al verte |
| `neutral` | Solo ataca si lo atacas |
| `territorial` | Defiende un territorio y persigue dentro de él |
| `manada` | Ataca en grupo y pide ayuda |
| `hostil` | Ataca al verte |
| `emboscada` | Espera oculto o cerca y ataca primero |
| `patrulla` | Recorre una ruta y combate; puede dar la alarma |
| `guardia` | Protege un lugar y no lo abandona |

Estos son comportamientos de **diseño**. Cómo se implementan en la IA del juego es **hipótesis** hasta leer la documentación.

## 7. Regiones y niveles del mundo
Cada enemigo tiene nivel fijo; **el nivel de una zona lo marcan los enemigos que aparecen en ella**.

| Región | Niveles | Ejemplos |
|---|---|---|
| Pradera y bosque (inicio) | 1 – 32 | vaca, zombi, lobo, bandido, líder de banda |
| Montaña, desierto, pantano, costa, río | 9 – 50 | serpiente, águila, cocodrilo, tiranosaurio |
| Mar | 10 – 45 | piraña, tiburón |
| Cuevas | 6 – 70 | murciélago gigante, dinosaurio ancestral (secreto) |
| Base de Pilaf | 9 – 26 | soldados, guardias, robots, máquina, Shu, Mai |
| Ejército Red Ribbon | 20 – 52 | soldados, tiradores, guardias, élite, Murasaki, Blue, Metallitron, Buyon, Tao Pai Pai |
| Palacio de Baba (arena) | 40 – 52 | Fangs, See-Through, Bandages, Spike |
| Hijos de Piccolo Daimaō | 62 – 76 | Tambourine, Cymbal, Drum, Piano |
| Camino de la Serpiente | 45 – 120 | demonios, espíritu guerrero (secreto) |
| Invasión saiyajin | 95 – 171 | Raditz, Saibamen (grupos de 3), Nappa, Vegeta |
| Nether / End / cueva profunda | 70 – 150 | piglins, blaze, ghast, shulker, warden, dragón |

Los mobs de Minecraft **no escalan**: se quedan como decoración cuando subes.

## 8. Facciones, campamentos e instalaciones ([DISEÑO])
Los jefes **no aparecen al azar**: tienen lugar y condición.

| Facción | Instalación | Mecánicas propuestas |
|---|---|---|
| Bandidos (originales) | Campamentos | Líder de banda como élite; cofres y recursos; patrullas; emboscadas |
| Pilaf | Fortaleza | Alarma, trampas, robots de patrulla, refuerzos, infiltración/sabotaje; Pilaf, Shu y Mai como personajes de misión (Shu y Mai pueden combatir) |
| Red Ribbon | Bases, Torre Músculo | Patrullas y escuadrones, tiradores, alarma que llama refuerzos, **recompensa por desmantelar la instalación**, comandantes como jefes, Black y Red como objetivos de misión |
| Baba | Arena | Cuatro combates especiales en secuencia, con regla propia por luchador |
| Piccolo Daimaō | Zonas protegidas | Cacería de Tambourine, Cymbal y Drum; Piano cierra la categoría; ataques contra NPC |
| Saga Saiyan | Eventos | Saibamen (grupos de 3, con autodestrucción) en la invasión; Raditz, Nappa y Vegeta (con fase Ozaru) solo por misión/evento. Ver `BETA_BOSSES.md` |

## 9. Habilidades especiales: canon o decisión de diseño
| Entidad | Habilidad | Estado |
|---|---|---|
| Spike | Rayo Devilmite (amplifica el mal del objetivo) | **Confirmado** por la fuente. En el juego: daño de ki que ignora parte de la defensa de quien tenga el control bajo (**adaptación de diseño**) |
| See-Through | Invisibilidad | **Confirmado**: el combate gira en torno a localizarlo. Duración y contraataque por sonido: decisión de diseño |
| Fangs | Mordisco con drenaje de energía | La fuente solo confirma un luchador con mordisco; el drenaje es **decisión de diseño** |
| Bandages | Agarres | **Decisión de diseño** |
| Murasaki, Blue, Metallitron, Buyon, Tao Pai Pai | Técnicas genéricas del juego (golpe pesado, ki, cañón) | **Decisión de diseño**; no se atribuyen poderes canónicos concretos |
| Buyon | Nivel entre Metallitron y Tao | La fuente dice que su poder no está claro: **decisión de diseño** |
| Tambourine, Cymbal, Drum, Piano | Técnicas genéricas | **Decisión de diseño**; se diferencian por rol (cazador, recolector, combatiente, lugarteniente) |
| Saibamen | Autodestrucción | **De memoria**; **decisión cerrada (2026-10-12): SÍ.** Al morir el Saibaman se autodestruye con daño en área al jugador cercano; no daña a otros Saibamen; el daño escala con VIT como el de cualquier criatura (§15) |
| Vegeta | Fase Ozaru | **De memoria**; **decisión cerrada (2026-10-12): SÍ, como fase final del jefe.** Diseñada, **no simulada** (cambia de arquetipo y técnicas a mitad del combate; las estadísticas de la fase están sin definir) |

No se ha inventado ningún poder canónico: lo no confirmado está marcado como adaptación.

## 10. Verificación del canon
16 personajes con nombre **verificados** en búsqueda (fuentes de fans), 6 **de memoria** (Pilaf, Shu, Mai, Raditz, Nappa, Vegeta) y el resto **original** (fauna, bandidos y unidades genéricas). **No incluidos:** "Man-Wolf" (no aparece en ninguna fuente) y el quinto luchador enmascarado de Baba (**fuera de la beta por decisión del usuario, 2026-10-12**; se podrá añadir en la 1.0 si se aclara el canon).

## 11. Jefes secretos de la beta ([DISEÑO])
| Jefe | Nivel | Pista | Recompensa (cualitativa) |
|---|---|---|---|
| Dinosaurio ancestral | 70 | Cazadores hablan de huesos enormes en una cueva | Pesas ancestrales: +0,5 al multiplicador de penalización (el tope ×5 no cambia) |
| Oso gigante de montaña | 85 | Árboles marcados con garras enormes en lo alto de una montaña | Pelaje de la montaña: material para equipo resistente al frío (sin bonus de XP) |
| Espíritu guerrero | 120 | Un guerrero legendario espera en el Camino de la Serpiente | Atajo de dominio: +30 % instantáneo en una forma que ya poseas |

(El "Asesino Tao" que figuraba aquí se eliminó: **Tao Pai Pai** es ahora el jefe canónico del Red Ribbon.)

## 12. Lista completa
Generada desde `bestiary.json`. "Mejoras equiv." = XP del enemigo ÷ coste de una mejora a su nivel. Tipos: boss 9, elite 20, normal 59, npc 5, passive 13, secret 3, story 10.

| Enemigo | Nivel | Tipo | Arquetipo | Comportamiento | XP | Mejoras equiv. | Mult. | Canon | Regiones |
|---|---|---|---|---|---|---|---|---|---|
| Aldeano | 1 | NPC (no combatiente) | equilibrado | huida | 0 | 0.0 | 1 |  | pradera |
| Burro | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Caballo | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Cerdo | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera, bosque |
| Conejo | 1 | pasivo | pasivo | huida | 0 | 0.0 | 1 |  | pradera, desierto |
| Gallina | 1 | pasivo | pasivo | huida | 0 | 0.0 | 1 |  | pradera |
| Gato | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Gaviota | 1 | pasivo | pasivo | huida | 0 | 0.0 | 1 |  | costa |
| Golem de nieve | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Mercader errante | 1 | NPC (no combatiente) | equilibrado | huida | 0 | 0.0 | 1 |  | pradera |
| Mula | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Ocelote | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Oveja | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Pez | 1 | pasivo | pasivo | huida | 0 | 0.0 | 1 |  | costa, mar |
| Vaca | 1 | pasivo | pasivo | pasivo | 0 | 0.0 | 1 |  | pradera |
| Zombi | 3 | normal | bruto | hostil | 8 | 0.5 | 1 |  | pradera, bosque |
| Zorro | 3 | normal | bestia | neutral | 8 | 0.5 | 1 |  | bosque, nieve |
| Araña | 4 | normal | veloz | hostil | 10 | 0.5 | 1 |  | pradera, bosque |
| Cabra | 4 | normal | bestia | neutral | 10 | 0.5 | 1 |  | montana |
| Lobo | 4 | normal | bestia | neutral | 10 | 0.5 | 1 |  | bosque, nieve |
| Lobo salvaje | 4 | normal | bestia | manada | 10 | 0.5 | 1 |  | pradera, bosque |
| Zombi aldeano | 4 | normal | bruto | hostil | 10 | 0.5 | 1 |  | pradera, bosque |
| Esqueleto | 5 | normal | tirador | hostil | 12 | 0.5 | 1 |  | pradera, bosque |
| Llama | 5 | normal | bestia | neutral | 12 | 0.5 | 1 |  | montana |
| Llama del mercader | 5 | normal | bestia | neutral | 12 | 0.5 | 1 |  | pradera |
| Pez de plata | 5 | normal | veloz | hostil | 12 | 0.5 | 1 |  | cueva, montana |
| Jabalí | 6 | normal | bestia | hostil | 14 | 0.5 | 1 |  | bosque |
| Slime | 6 | normal | bestia | hostil | 14 | 0.5 | 1 |  | pantano, cueva |
| Creeper | 7 | normal | bruto | hostil | 16 | 0.5 | 1 |  | pradera, bosque |
| Araña de las cuevas | 8 | normal | veloz | hostil | 18 | 0.4 | 1 |  | cueva |
| Endermite | 8 | normal | veloz | hostil | 18 | 0.4 | 1 |  | end |
| Ladrón | 8 | normal | veloz | emboscada | 18 | 0.4 | 1 |  | pradera, bosque |
| Serpiente | 9 | normal | veloz | emboscada | 20 | 0.4 | 1 |  | desierto, pantano |
| Soldado de Pilaf | 9 | normal | guerrero | patrulla | 20 | 0.4 | 1 | memoria | pilaf |
| Ahogado | 10 | normal | bruto | hostil | 23 | 0.4 | 1 |  | costa, mar |
| Oso polar | 10 | normal | bestia | territorial | 23 | 0.4 | 1 |  | nieve |
| Piraña | 10 | normal | veloz | manada | 23 | 0.4 | 1 |  | rio, pantano |
| Dinosaurio pequeño | 12 | normal | veloz | territorial | 28 | 0.4 | 1 |  | pradera, desierto |
| Fantasma | 12 | normal | veloz | hostil | 28 | 0.4 | 1 |  | pradera, montana |
| Husk | 12 | normal | bruto | hostil | 28 | 0.4 | 1 |  | desierto |
| Murciélago gigante | 12 | normal | veloz | manada | 28 | 0.4 | 1 |  | cueva |
| Pilaf | 12 | NPC (no combatiente) | equilibrado | huida | 0 | 0.0 | 1 | memoria | pilaf |
| Bandido | 14 | normal | guerrero | hostil | 33 | 0.4 | 1 |  | pradera, bosque, desierto |
| Bogged | 14 | normal | tirador | hostil | 33 | 0.4 | 1 |  | pantano |
| Guardia de Pilaf | 14 | normal | tanque | guardia | 33 | 0.4 | 1 | memoria | pilaf |
| Oso pardo | 14 | normal | bestia | territorial | 33 | 0.4 | 1 |  | bosque, montana |
| Vagabundo | 14 | normal | tirador | hostil | 33 | 0.4 | 1 |  | nieve |
| Águila | 16 | normal | veloz | territorial | 38 | 0.4 | 1 |  | montana |
| Bruja | 16 | normal | tirador | hostil | 38 | 0.4 | 1 |  | pantano |
| Bandido armado | 18 | normal | guerrero | patrulla | 44 | 0.4 | 1 | original | bosque, desierto |
| Saqueador | 18 | normal | tirador | hostil | 44 | 0.4 | 1 |  | pradera, montana |
| Shu | 18 | élite | guerrero | guardia | 132 | 1.2 | 1.05 | memoria | pilaf |
| Tigre | 18 | normal | veloz | territorial | 44 | 0.4 | 1 |  | bosque, jungla |
| Cocodrilo | 20 | normal | bestia | emboscada | 50 | 0.4 | 1 |  | pantano, rio |
| Robot de patrulla de Pilaf | 20 | normal | tanque | patrulla | 50 | 0.4 | 1 | memoria | pilaf |
| Soldado raso del Red Ribbon | 20 | normal | guerrero | patrulla | 50 | 0.4 | 1 | memoria | rr |
| Pterodáctilo | 22 | normal | veloz | hostil | 55 | 0.4 | 1 |  | montana, costa |
| Tirador del Red Ribbon | 22 | normal | tirador | patrulla | 55 | 0.4 | 1 | memoria | rr |
| Vex | 22 | normal | veloz | hostil | 55 | 0.4 | 1 |  | bosque |
| Estegosaurio | 24 | normal | tanque | neutral | 61 | 0.4 | 1 |  | pradera, bosque |
| Mai | 24 | élite | tirador | guardia | 184 | 1.2 | 0.95 | memoria | pilaf |
| Vindicador | 24 | normal | bruto | hostil | 61 | 0.4 | 1 |  | bosque, montana |
| Golem de hierro | 25 | élite | tanque | neutral | 150 | 0.9 | 1 |  | pradera |
| Enderman | 26 | normal | veloz | hostil | 68 | 0.4 | 1 |  | pradera, desierto, montana |
| Guardia del Red Ribbon | 26 | normal | tanque | guardia | 68 | 0.4 | 1 | memoria | rr |
| Máquina de combate de Pilaf | 26 | élite | tanque | guardia | 203 | 1.2 | 1.05 | memoria | pilaf |
| Rinoceronte | 28 | normal | tanque | neutral | 74 | 0.4 | 1 |  | pradera |
| Coronel Silver | 28 | élite | guerrero | guardia | 222 | 1.1 | 1.1 | verificado | rr |
| Capitán bandido | 30 | élite | guerrero | hostil | 240 | 1.1 | 1 |  | desierto |
| Guardián | 30 | normal | tirador | hostil | 80 | 0.4 | 1 |  | mar |
| Monstruo del pantano | 30 | élite | bestia | territorial | 240 | 1.1 | 0.85 |  | pantano |
| Líder de banda | 32 | élite | guerrero | guardia | 260 | 1.1 | 0.95 | original | bosque, desierto |
| Ninja Murasaki | 32 | jefe de historia | veloz | emboscada | 188 | 0.8 | 0.9 | verificado | rr |
| Unidad de élite del Red Ribbon | 34 | élite | guerrero | patrulla | 279 | 1.1 | 0.95 | memoria | rr |
| Tiburón | 34 | élite | bestia | territorial | 279 | 1.1 | 0.9 |  | mar |
| Oficial Black | 36 | NPC (no combatiente) | equilibrado | huida | 0 | 0.0 | 1 | verificado | rr |
| Breeze | 38 | normal | veloz | hostil | 106 | 0.4 | 1 |  | cueva |
| Máquina de combate del Red Ribbon | 38 | élite | tanque | guardia | 318 | 1.1 | 1.05 | memoria | rr |
| Triceratops | 38 | élite | tanque | territorial | 318 | 1.1 | 1.05 |  | pradera |
| General Blue | 40 | jefe de historia | guerrero | guardia | 319 | 1.0 | 0.9 | verificado | rr |
| Evocador | 40 | élite | tirador | hostil | 338 | 1.0 | 1.1 |  | bosque |
| Fangs el Vampiro | 40 | jefe opcional/arena | veloz | guardia | 213 | 0.7 | 1 | verificado | baba |
| Comandante Red | 40 | NPC (no combatiente) | equilibrado | huida | 0 | 0.0 | 1 | verificado | rr |
| Mayor Metallitron | 44 | jefe de historia | tanque | guardia | 401 | 1.1 | 0.95 | verificado | rr |
| See-Through, el hombre invisible | 44 | jefe opcional/arena | veloz | emboscada | 267 | 0.7 | 1.05 | verificado | baba |
| Demonio menor | 45 | normal | bruto | hostil | 129 | 0.3 | 1 |  | camino_serpiente |
| Devastador | 45 | élite | bruto | hostil | 388 | 1.0 | 0.85 |  | montana |
| Guardián anciano | 45 | élite | tirador | hostil | 388 | 1.0 | 1 |  | mar |
| Tiranosaurio | 45 | élite | bestia | territorial | 388 | 1.0 | 0.8 |  | desierto, pradera |
| Buyon | 47 | jefe de historia | bestia | guardia | 470 | 1.1 | 0.7 | verificado | rr |
| Bandages la Momia | 48 | jefe opcional/arena | bruto | guardia | 330 | 0.8 | 1.45 | verificado | baba |
| Spike el Hombre Diablo | 52 | jefe opcional/arena | tirador | guardia | 401 | 0.8 | 2.15 | verificado | baba |
| Tao Pai Pai | 52 | jefe de historia | veloz | emboscada | 602 | 1.3 | 0.85 | verificado | rr |
| Tambourine | 62 | jefe de historia | veloz | emboscada | 926 | 1.5 | 0.8 | verificado | daimao |
| Cymbal | 66 | jefe opcional/arena | guerrero | guardia | 720 | 1.1 | 1.65 | verificado | daimao |
| Dinosaurio ancestral | 70 | jefe secreto | bestia | guardia | 625 | 0.8 | 1.4 |  | cueva |
| Piglin zombi | 70 | normal | bruto | hostil | 214 | 0.3 | 1 |  | nether |
| Drum | 72 | jefe opcional/arena | bruto | guardia | 894 | 1.1 | 1.45 | verificado | daimao |
| Piglin | 75 | normal | guerrero | hostil | 231 | 0.3 | 1 |  | nether |
| Piano | 76 | jefe de historia | guerrero | guardia | 1533 | 1.8 | 1.1 | verificado | daimao |
| Cubo de magma | 78 | normal | bestia | hostil | 241 | 0.3 | 1 |  | nether |
| Hoglin | 80 | normal | bestia | hostil | 248 | 0.3 | 1 |  | nether |
| Zoglin | 82 | normal | bestia | hostil | 255 | 0.3 | 1 |  | nether |
| Blaze | 85 | normal | tirador | hostil | 265 | 0.3 | 1 |  | nether |
| Oso gigante de montaña | 85 | jefe secreto | bestia | territorial | 1013 | 1.0 | 1.4 | original | montana |
| Piglin bruto | 85 | élite | guerrero | hostil | 795 | 0.8 | 1 |  | nether |
| Demonio del camino | 90 | normal | guerrero | hostil | 282 | 0.3 | 1 |  | camino_serpiente |
| Ghast | 90 | élite | tirador | hostil | 845 | 0.8 | 1.95 |  | nether |
| Esqueleto wither | 95 | élite | guerrero | hostil | 895 | 0.8 | 1.25 |  | nether |
| Raditz | 95 | jefe de historia | guerrero | guardia | 2677 | 2.3 | 1.2 | memoria | historia |
| Saibaman | 108 | normal | veloz | emboscada | 341 | 0.2 | 1 |  | invasion · grupos de 3 |
| Espíritu guerrero | 120 | jefe secreto | guerrero | guardia | 2410 | 1.4 | 1.85 |  | camino_serpiente |
| Guerrero saiyajin genérico | 120 | élite | guerrero | patrulla | 1140 | 0.7 | 1.55 | original | invasion |
| Shulker | 120 | normal | tanque | hostil | 380 | 0.2 | 1 |  | end |
| Wither | 130 | jefe opcional/arena | tirador | guardia | 3935 | 2.0 | 1.55 |  | jefe_opcional · removible |
| Nappa | 140 | jefe de historia | bruto | guardia | 7122 | 3.3 | 1.05 | memoria | historia |
| Warden | 140 | jefe opcional/arena | bruto | guardia | 4748 | 2.2 | 1.55 |  | cueva_profunda · removible |
| Dragón del End | 150 | jefe opcional/arena | bestia | guardia | 5657 | 2.3 | 1.75 |  | end · removible |
| Vegeta | 171 | jefe de historia | guerrero | guardia | 11843 | 4.0 | 1.9 | memoria | historia |

### 12.1 Mobs vanilla añadidos el 2026-10-12 (25 entradas)
Motivo: el usuario mató un Golem de hierro y no recibió XP, porque `KillXpHandler` solo da XP a entidades que están en el bestiario. Un mob que falta en el bestiario equivale a 0 XP; las entradas con 0 XP documentan que la omisión es intencionada. Todas son **propuestas de diseño** a ajustar con combate real; el XP sale de la fórmula de §4 salvo el golem, cuya XP fija la pidió el usuario.

| Mob | Nivel | Tipo | XP | Justificación |
|---|---|---|---|---|
| **Golem de hierro** | 25 | élite · tanque · neutral | **150** (fija) | 100 HP y golpes muy fuertes: el neutral más peligroso de vanilla. Élite por tamaño de amenaza (no existe un tipo "neutral": la neutralidad es el comportamiento). Contra un jugador de nivel 1 paga +48 % → 222 XP |
| Lobo | 4 | normal · bestia · neutral | 10 | Igual que el Lobo salvaje del mod |
| Zorro | 3 | normal · bestia · neutral | 8 | Pequeño y poco peligroso |
| Cabra | 4 | normal · bestia · neutral | 10 | Embiste |
| Llama, Llama del mercader | 5 | normal · bestia · neutral | 12 | Escupe y embiste, daño bajo |
| Oso polar | 10 | normal · bestia · territorial | 23 | Fuerte y territorial |
| Zombi aldeano | 4 | normal · bruto · hostil | 10 | Variante del zombi |
| Pez de plata | 5 | normal · veloz · hostil | 12 | Enjambre pequeño |
| Endermite | 8 | normal · veloz · hostil | 18 | Pequeño, aparece con perlas |
| Bogged | 14 | normal · tirador · hostil | 33 | Variante del vagabundo |
| Vex | 22 | normal · veloz · hostil | 55 | Invocado por el evocador (nivel 40) |
| Guardián | 30 | normal · tirador · hostil | 80 | Monumento oceánico |
| Breeze | 38 | normal · veloz · hostil | 106 | Cámaras de prueba, junto al evocador |
| Guardián anciano | 45 | élite · tirador · hostil | 388 | Jefe del monumento |
| Zoglin | 82 | normal · bestia · hostil | 255 | Cerca del hoglin (80) |
| Piglin bruto | 85 | élite · guerrero · hostil | 795 | Más fuerte que el piglin (75) |
| Golem de nieve, Gato, Ocelote, Caballo, Burro, Mula | 1 | pasivo | 0 | Inofensivos, mascotas o monturas: matarlos no debe dar XP |
| Aldeano, Mercader errante | 1 | NPC | 0 | No combatientes |
| Abeja, panda, tortuga, etc. | — | — | 0 (sin entrada) | Siguen sin entrada = sin XP |

**Aviso de granjas:** el golem de hierro se puede granjear en aldeas (las granjas de golems son comunes). La regla anti-granjas del diseño (rendimientos decrecientes por tipo, `GDD.md` §3) **no está implementada**; solo existe el ×0,25 para mobs de generadores (`MobSpawnType.SPAWNER`). Hasta entonces, una granja de golems da XP sin límite. Queda como deuda.

## 13. Errores encontrados al hacerlo
1. **Jefes demasiado fáciles** con los datos iniciales (Vegeta 0 % de derrotas): se añadió `power_mult`.
2. **Calibración sobreajustada** en el umbral: otra semilla dio 28 %, 19 % y 25 % para el peor build; se añadió margen y verificación con dos semillas.
3. **Dato inválido:** el Warden tenía una sola técnica; el validador lo detectó.
4. **Pruebas mal acotadas:** al añadir la raza `npc` al simulador, las pruebas la recorrieron como jugable.
5. **Presupuesto de XP roto** al ampliar el alcance: los jefes sumaban el 120 % del presupuesto de combate; se bajaron los porcentajes.
6. **Asesino Tao inventado:** duplicaba a Tao Pai Pai, personaje canónico; se eliminó.

## 14. Límites
- El simulador no mide alcance, movimiento, esquivas con habilidad, daño continuo ni mecánicas como invisibilidad o alarmas; un jefe real se sentirá distinto.
- Multiplicadores, técnicas y niveles de los personajes canónicos son **propuestas** para ajustar con combate real.
- Las fuentes de canon son wikis de fans, no material oficial.
- Mobs vanilla fuera de la lista: ver §12.1 (golem, aldeanos y otros añadidos el 2026-10-12). Los que siguen sin entrada (abeja, panda, tortuga, ajolote…) no dan XP.

## 15. Preguntas abiertas — RESUELTAS (2026-10-12)
Las tres preguntas están cerradas por decisión del usuario. No se ha cambiado el alcance de la beta ni `design_data/beta/bestiary.json` (los datos ya encajan: el Saibaman ya tiene la técnica `autodestruccion` y el quinto luchador no figura).

| # | Pregunta | Decisión | Justificación |
|---|---|---|---|
| 1 | ¿El quinto luchador enmascarado de Baba entra en la beta? | **FUERA de la beta.** Se podrá añadir en la 1.0 si se aclara el canon. | No está claro en el canon clásico qué personaje es; se deja fuera para no arriesgar. La arena de Baba queda con 4 luchadores verificados (Fangs, See-Through, Bandages, Spike). |
| 2 | ¿La autodestrucción de los Saibamen? | **SÍ.** Cuando un Saibaman muere se autodestruye: daño en área al jugador dentro del radio; **no daña a otros Saibamen** (para no romper el combate en grupo); el daño **escala con VIT** como cualquier criatura (categoría ESCALA_SIN_SUELO, `SYSTEMS.md` §3.1). | Mantiene la identidad del Saibaman. Nota de implementación futura: una explosión de vanilla dañaría a los otros Saibamen; hará falta daño en área propio filtrado, con el Saibaman como causante para que se clasifique como criatura. Radio y daño: sin definir. |
| 3 | ¿Vegeta con fase Ozaru en la beta? | **SÍ, como fase final del jefe.** Primera fase de combate normal; después se transforma en Ozaru (cambia el aspecto —modelo o escala— y suben las estadísticas). Es el jefe final de la beta: derrotarlo cierra la saga Saiyajin. Si el modelo 3D del mono gigante es demasiado costoso, se representa como cambio de fase con aura y estadísticas, sin cambiar el modelo. | Es lo más arriesgado de implementar: cambia de arquetipo y técnicas a mitad del combate. **Pendiente:** definir las estadísticas de la fase Ozaru y simularla (garantía de historia) antes de implementarla. |

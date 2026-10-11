# RACES — Diseño detallado de razas

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Todo es propuesta (P) hasta que lo
> apruebes. Los números son iniciales y se ajustan jugando (Fase 2–3). Se apoya en
> `GDD.md` (atributos, escala de poder) y `WORLD_DESIGN.md` (puertas de forma, dominio de forma).
> Las referencias a la serie son de memoria; no consulté fuentes durante esta sesión.

## 1. Cómo se define una raza
Cada raza es un archivo de datos (`data/<modid>/races/<id>.json`) con estos bloques. Ninguna regla de una raza vive en el código central: el código solo sabe leer estos bloques.

| Bloque | Qué controla |
|---|---|
| Atributos base | Valores iniciales de los 7 atributos |
| Modificadores de crecimiento | Cuánto rinde cada punto invertido en cada atributo |
| Rasgos | Ventajas y desventajas pasivas |
| Condición de entrenamiento | Qué la hace progresar de forma especial |
| Estilos y especializaciones | Caminos de build (elegibles en el nivel 10, ver §9) |
| Técnicas propias | Lista que solo esa raza puede aprender o aprende mejor |
| Escalera de formas | Hasta 5 formas, una por puerta (WORLD_DESIGN §4.2) |
| Identidad visual | Colores, aura, efectos |

Atributos (en este orden): **VIT** vitalidad, **FUE** fuerza, **DEF** defensa, **VEL** velocidad, **KI** ki máximo, **POD** poder de ki, **CON** control.

## 2. Comparativa y equilibrio
Modelo simple: total de atributos = base + 5 puntos por nivel × crecimiento medio (puntos repartidos por igual, sin formas ni rasgos). Calculado con un script.

| Raza | Base | Crec. medio | Nivel 1 | Nivel 20 | Nivel 50 | Nivel 100 |
|---|---|---|---|---|---|---|
| Saiyajin | 48 | 1,036 | 48 | 146 | 302 | 561 |
| Raza de Freezer | 59 | 0,857 | 59 | 140 | 269 | 483 |
| Terrícola | 40 | 1,071 | 40 | 142 | 302 | 570 |
| Namekiano | 49 | 1,014 | 49 | 145 | 298 | 551 |
| Majin | 47 | 0,971 | 47 | 139 | 285 | 528 |
| Androide | 54 | 0,921 | 54 | 142 | 280 | 510 |

Lectura:
- **Freezer** empieza muy por delante y acaba por detrás: compensa con formas baratas y de acceso temprano.
- **Terrícola** empieza último y acaba primero: compensa con maestros, técnicas y versatilidad.
- **Saiyajin** va a mitad de tabla en bruto: su ventaja real son el *Zenkai* y las formas con dominio (§3).
- **Androide** compensa su techo bajo con módulos (§8).
- Las diferencias en bruto son pequeñas a propósito: lo que separa a las razas son rasgos, formas y estilos, no solo números.

**Pendiente:** este modelo ignora formas y rasgos. Hay que simular el combate completo en Fase 2 para ver si alguna raza domina.

## 3. Saiyajin

**Concepto.** Guerreros que se hacen más fuertes al sobrevivir a la derrota. Empiezan sin ventajas llamativas; su fuerza está en la constancia, el combate y las formas.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 8 | 10 | 6 | 8 | 5 | 7 | 4 |
| Crecimiento | | | | | | |
| 1,00 | 1,25 | 1,00 | 1,10 | 0,90 | 1,15 | 0,85 |

**Rasgos**
- **Zenkai (ventaja):** tras un combate donde acabas por debajo del 25 % de vida **y sobrevives**, ganas XP extra proporcional a la razón de poder del rival (con tope). No se activa vendo a enemigos triviales ni dejándote matar.
- **Orgullo guerrero (ventaja):** +XP al derrotar a un rival de poder igual o superior.
- **Ki bajo (desventaja):** regeneración de ki menor y CON inicial bajo.
- **Sangre de guerrero (neutral):** sin recuperación pasiva fuera de combate; se cura con comida y senzu.

**Entrenamiento especial.** Aprovecha mejor las penalizaciones físicas (bonus de gravedad y peso +15 %, WORLD_DESIGN §5). Mejoran sobre todo con combate real, no con meditación (meditación rinde −20 %).

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Guerrero de élite | Cuerpo a cuerpo | FUE, VEL, DEF |
| Saiyajin de ki | Ráfagas y técnicas energéticas | POD, KI, CON |
| Berserker (futuro) | Poder alto con riesgo, vinculado a la luna | FUE, VIT |

**Técnicas propias (P):** ráfaga de ki, Galick Ho, Final Flash, Big Bang Attack, Ki Blast Cannon; técnicas compartidas aprendibles con maestros (Kamehameha, etc.).

**Escalera de formas (P)**
| Puerta | Forma | Hito de desbloqueo (P) | Notas |
|---|---|---|---|
| 1 | Kaio-ken | Entrenar con Kaio-sama en el Otro Mundo (final del Camino de la Serpiente) | Multiplicador temporal con coste de vida; **entra en la beta** |
| 2 | Super Saiyajin | Dominio de Kaio-ken ≥ 60 % + sobrevivir a un jefe poderoso con vida < 10 % | Mecánica de furia; **fuera de la beta** |
| 3 | Super Saiyajin 2 | Dominio SSJ ≥ 60 % + prueba de resistencia | |
| 4 | Super Saiyajin 3 / Dios | Misión de arco largo | Coste de ki muy alto |
| 5 | Azul / Ultra Instinto | Entrenamiento divino (Whis) y prueba final | Forma suprema |

**Identidad visual:** pelo que se alza, aura dorada y luego azul/plateada, chispas eléctricas, ojos que cambian de color.

> **Cambio (2026-10-11):** la primera puerta (nivel 15, ~5 h de juego) es temprana para el Super Saiyajin, que en la serie llega tras Vegeta. Para la beta (Saga Saiyan, hasta Vegeta, nivel 35) solo se usa la **puerta 1**. En Saiyajin y Terrícola la forma 1 es el **Kaio-ken**.

## 4. Raza de Freezer (Arcosiano)

**Concepto.** Empiezan con un poder impresionante pero crecen poco entrenando. Su gran fuerza está en las formas que ya llevan dentro.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 9 | 8 | 9 | 10 | 8 | 9 | 6 |
| Crecimiento | | | | | | |
| 0,80 | 0,80 | 0,85 | 0,90 | 0,85 | 0,90 | 0,90 |

**Rasgos**
- **Cuerpo superior (ventaja):** resistencia a ambientes extremos (frío, vacío, gravedad), útil en el espacio en fases posteriores.
- **Poder contenido (peculiar):** las formas ya están "dentro"; no se aprenden, se **liberan**. Cada puerta de forma se desbloquea con un hito más sencillo que en otras razas (p. ej. alcanzar el nivel, sin misión larga).
- **Crecimiento bajo (desventaja):** el entrenamiento rinde −15 % de XP. Las formas compensan.
- **Tensión corporal (desventaja):** al mantener formas altas, un indicador de estrés sube; si se llena, la forma se desactiva sola y hay breve aturdimiento. El dominio de forma (WORLD_DESIGN §4.4) reduce la tensión.
- **Soberbia (desventaja):** −XP al derrotar enemigos muy débiles (aún más penalizado que otras razas).

**Entrenamiento especial.** Entrenan poco; progresan sobre todo con combate y eventos. Las penalizaciones de entrenamiento solo rinden al 70 %.

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Tirano | Rayos y técnicas energéticas a distancia | POD, KI, CON |
| Guerrero cósmico | Velocidad y combate cuerpo a cuerpo | VEL, DEF, FUE |
| Comandante (futuro) | Invoca y controla aliados NPC | CON, VIT |

**Técnicas propias (P):** rayo de la muerte (Death Beam), disco destructor, Supernova, Death Ball.

**Escalera de formas (P)** — evoluciones del propio cuerpo
| Puerta | Forma | Hito (P) |
|---|---|---|
| 1 | Forma 2 | Alcanzar nivel de puerta y superar un duelo |
| 2 | Forma 3 | Duelo contra jefe |
| 3 | Forma final | Misión corta |
| 4 | Forma completa (100 %) | Misión + dominio ≥ 60 % de la anterior |
| 5 | Golden | Entrenamiento extraordinario (misión larga) |

**Identidad visual:** cuerpo blanco con detalles de color, cambios de silueta marcados en cada forma, aura morada/dorada.

## 5. Terrícola

**Concepto.** La raza más débil al empezar, pero la que más aprende de los demás. Fuerza = maestros, técnicas y voluntad.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 6 | 6 | 5 | 6 | 5 | 5 | 7 |
| Crecimiento | | | | | | |
| 1,10 | 1,00 | 1,00 | 1,10 | 1,00 | 1,00 | 1,30 |

**Rasgos**
- **Aprendiz nato (ventaja):** +30 % de XP en entrenamientos con maestros y −25 % de coste de puntos de técnica.
- **Versátil (ventaja):** puede aprender **cualquier técnica** que no esté marcada como racial exclusiva. Las demás razas tienen restricciones.
- **Voluntad (ventaja):** al caer por debajo del 15 % de vida, un breve bonus de DEF y regeneración de ki (una vez por combate).
- **Frágil (desventaja):** atributos base bajos y sin rasgo defensivo; los primeros niveles son duros.
- **Sin ventaja de formas fáciles (desventaja):** las formas se obtienen con maestros, no por herencia.

**Entrenamiento especial.** Las penalizaciones de entrenamiento (pesas, gravedad) rinden +10 %. El entrenamiento de maestros es su vía principal.

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Artista marcial | Técnica y esquiva | VEL, CON, FUE |
| Místico | Energía espiritual, regeneración, sensor | KI, CON, VIT |
| Táctico / tecnológico | Objetos, cápsulas, gadgets | CON, DEF |

**Técnicas propias (P):** Kamehameha, Taiyoken, Kienzan, Kikoho, teletransporte instantáneo (tardío), Genkidama (con Kaio).

**Escalera de formas (P)**
| Puerta | Forma | Hito (P) |
|---|---|---|
| 1 | Kaio-ken | Entrenamiento con Kaio-sama |
| 2 | Potencial liberado | Ritual con Kaioshin o anciano (misión) |
| 3 | Ultimate | Entrenamiento con maestro de la raza |
| 4 | Bestia | Hito de furia en jefe |
| 5 | por definir | — |

**Identidad visual:** aura roja/rosa en Kaio-ken, aura blanca del potencial, ropa de artes marciales.

## 6. Namekiano

**Concepto.** Resistentes, regeneradores, con técnicas de apoyo y la capacidad de fusión.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 9 | 6 | 7 | 6 | 7 | 6 | 8 |
| Crecimiento | | | | | | |
| 1,10 | 0,90 | 1,00 | 0,95 | 1,05 | 1,00 | 1,10 |

**Rasgos**
- **Regeneración (ventaja):** cura pasiva fuera de combate; cuesta ki. Pasivo muy lento sin ki.
- **Cuerpo de agua (neutral):** no necesitan comida; necesitan agua. Sin agua accesible, el cuerpo se debilita. Se nutre de fuentes, ríos y de lluvia.
- **Extremidades elásticas (ventaja):** alcance de ataque cuerpo a cuerpo mayor.
- **Fusión (ventaja):** puede fusionarse con un NPC namekiano compatible para reforzarse temporalmente (se desbloquea con misión).
- **Fuerza baja (desventaja):** FUE inicial y crecimiento bajos.

**Entrenamiento especial.** Meditación y contacto con la naturaleza rinden +25 %. Las pesas rinden −10 %.

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Guerrero | Combate físico con técnicas | VEL, DEF, POD |
| Sanador | Cura a sí mismo y NPC, soporte | CON, KI, VIT |
| Clan del Dragón (futuro) | Crear objetos/esferas | CON |

**Técnicas propias (P):** Makankosappo (cañón de rayo especial), regeneración, fusión, crear ropa y objetos simples.

**Escalera de formas (P)**
| Puerta | Forma | Hito (P) |
|---|---|---|
| 1 | Gigante | Entrenamiento en una zona sagrada |
| 2 | Súper Namekiano | Fusión + misión |
| 3 | por definir | — |
| 4 | por definir | — |
| 5 | por definir | — |

**Identidad visual:** piel verde, antenas, aura verde/blanca.

## 7. Majin

**Concepto.** Una masa casi indestructible que puede absorber para crecer. Gran vitalidad y regeneración, poco control.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 12 | 7 | 8 | 5 | 6 | 6 | 3 |
| Crecimiento | | | | | | |
| 1,25 | 1,00 | 1,15 | 0,80 | 0,90 | 1,00 | 0,70 |

**Rasgos**
- **Cuerpo elástico (ventaja):** reduce parte del daño físico y se regenera incluso sin ki (el cuerpo "se reforma").
- **Regeneración extrema (ventaja):** cura más rápida que cualquier otra raza, pero consume ki.
- **Absorción (ventaja, tardía):** al derrotar a ciertos enemigos puede absorber parte de su poder (tope, temporal o permanente según forma). Desbloqueo por misión.
- **Control bajo (desventaja):** técnicas más caras y menos precisas.
- **Lentitud (desventaja):** VEL baja.

**Entrenamiento especial.** Soportan penalizaciones de gravedad mejor (+20 %), pero meditar rinde −30 %.

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Titán | Aguante y fuerza bruta | VIT, DEF, FUE |
| Caramelo | Técnicas de transformación y control a distancia | POD, KI |
| Absorbente (futuro) | Absorbe poder de los rivales | CON |

**Técnicas propias (P):** rayo de caramelo, bola de energía, regeneración en explosión, absorción.

**Escalera de formas (P):** por definir al detallar la raza en Fase 3. Se propone una evolución de carácter: formas más malévolas o puras.

**Identidad visual:** cuerpo rosa, apéndices, aura rosa/roja.

## 8. Androide

**Concepto.** Máquinas con reserva de energía continua, sin ki orgánico. Crecen mediante **módulos** más que por puntos, y su ki no se ve en el radar.

| VIT | FUE | DEF | VEL | KI | POD | CON |
|---|---|---|---|---|---|---|
| 8 | 9 | 9 | 9 | 7 | 7 | 5 |
| Crecimiento | | | | | | |
| 0,90 | 0,95 | 0,95 | 0,95 | 0,90 | 0,90 | 0,90 |

**Rasgos**
- **Reactor (ventaja):** el ki no se agota, pero las técnicas generan **calor**; si se llena, las técnicas se bloquean unos segundos.
- **No detectable (ventaja):** no aparecen en el radar de ki de otros.
- **Sin senzu (desventaja):** no se curan con comida orgánica; usan kits de reparación.
- **Módulos (peculiar):** objetos y misiones dan módulos instalables (potencia, enfriamiento, reactor), que sustituyen en parte a las formas.
- **Techo bajo (desventaja):** crecimiento por puntos menor; el techo real se desbloquea con módulos.

**Entrenamiento especial.** Las penalizaciones físicas rinden normal; la meditación no sirve. Necesitan piezas y laboratorio.

**Estilos / especializaciones (P)**
| Camino | Enfoque | Atributos |
|---|---|---|
| Combate | Cuerpo a cuerpo y absorción de energía | FUE, DEF, VEL |
| Artillería | Ráfagas continuas | POD, KI |
| Soporte | Drones, escudos | CON |

**Escalera de mejoras (P):** en vez de formas, 5 "versiones" (Mejora 1–5) que se instalan con módulos en laboratorios (Doctor Gero, Capsule Corp).

**Identidad visual:** metal, luces, sin aura orgánica, chispas y vapor.

## 9. Especializaciones
A partir del nivel 10, el jugador elige un camino dentro de su raza (ya listados). Cada camino da:
- Bonus de crecimiento en 2–3 atributos.
- Acceso a técnicas propias del camino.
- Una pasiva única.

Se puede reasignar con coste (WORLD_DESIGN §4.5). Es la base del "personaje especializado" que describiste.

## 10. Futuras razas (para después)
Híbrido saiyajin-terrícola, Kaioshin, Yardrat, Dragón. Se añaden como un archivo de datos nuevo.

## 11. Primera implementación
Solo **Saiyajin** y **Terrícola**, porque son los extremos más distintos (crecimiento diferente, rasgos distintos) y bastan para probar que añadir una raza es añadir un archivo.

## 12. Lo que sigue
1. Tu revisión de las razas: ¿falta algo que quieras?
2. Detallar las formas de Majin y Namekiano (hoy hay huecos).
3. Detallar los hitos de desbloqueo de cada forma (Super Saiyajin primero).

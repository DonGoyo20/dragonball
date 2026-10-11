# BESTIARY_PLAN — Análisis previo y plan de trabajo del bestiario de la beta

> Estado: **análisis inicial para tu aprobación (2026-10-11).** Responde a la "primera tarea"
> del prompt del bestiario. No hay código del juego todavía y no se ha instalado nada.
> Separo siempre **[VERIFICADO]** (comprobado en esta sesión), **[HIPÓTESIS]** (de memoria o
> deducido; hay que comprobarlo) y **[DISEÑO]** (decisión nuestra).

## 1. Qué existe hoy (comprobado)
- **[VERIFICADO]** No existe ningún proyecto de Minecraft: cero archivos `build.gradle`, `neoforge.mods.toml` o `.java` en el repositorio.
- **[VERIFICADO]** Lo que sí hay: documentos de diseño, `design_data/beta/bestiary.json` (94 entradas) y un simulador de equilibrio en Python con 23 pruebas.
- **[VERIFICADO]** Herramientas del entorno: Java 21, Gradle 8.14.3.
- **[VERIFICADO]** Red: todos los hosts necesarios para construir un mod devuelven error (`maven.neoforged.net`, `docs.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`, `blockbench.net`), Maven Central responde 429 y GitHub 403. **En esta sesión no se puede descargar Minecraft ni NeoForge ni leer su documentación directamente.**

## 2. Las seis preguntas del prompt

### 2.1 Versión de Minecraft y plataforma
- **[VERIFICADO]** No hay ninguna elegida en código.
- **[DISEÑO]** Propuesta vigente, **sin aprobación explícita tuya**: Minecraft 1.21.1, NeoForge, Java 21.
- **[VERIFICADO por búsqueda]** Existe documentación de NeoForge para 1.21.1 y GeckoLib 4.9.2 para NeoForge 1.21.1. Minecraft usa ahora numeración 26.x y requiere Java 25 desde la 26.1 (fuentes de la fase 0).
- **Necesito que confirmes o cambies esto** (§5).

### 2.2 Sistemas de entidades que ya existen
- En el proyecto: **ninguno**. Lo que existe son reglas de diseño (atributos, ki, XP, habilidades) y datos.
- Lo que ofrece el juego base (mobs, IA con objetivos, loot tables, apariciones): **[HIPÓTESIS]** de memoria; hay que leerlo en la documentación al tener red.

### 2.3 Componentes a desarrollar
| Componente | Para qué | Estado | Evidencia |
|---|---|---|---|
| Núcleo RPG en Java puro (`core`) | Fórmulas de poder, XP, requisitos, anti-farmeo | Diseñado y simulado en Python | Simulador + 23 pruebas |
| Cargador y validador de datos | Leer `bestiary.json` | Diseñado; validador en Python | `bestiary.py` |
| Entidad base configurable | Una clase con arquetipo, nivel, técnicas y comportamiento por datos | Diseñado | — |
| Vida en porcentaje y daño por razón de poder | Que el combate use nuestras fórmulas | Diseñado | **[HIPÓTESIS]** hay que ver cómo interceptar el daño y cómo se muestra la barra |
| Comportamientos de IA | Huida, manada, territorial, emboscada, patrulla, guardia | Diseñado (§4 de `BESTIARY.md`) | **[HIPÓTESIS]** sobre el sistema de IA del juego |
| Aparición | Biomas, estructuras, misiones | Diseñado | **[VERIFICADO por búsqueda]** `neoforge:add_spawns` (biomas, peso, tamaño de grupo) y registro de restricciones con `RegisterSpawnPlacementsEvent` |
| Campamentos y bases | Bandidos, Pilaf, Red Ribbon | Diseñado | **[HIPÓTESIS]** estructuras por datos; no verificado |
| Recompensas y botín | XP fija, drops, objetos de misión | Diseñado | **[HIPÓTESIS]** tablas de botín por datos |
| Gancho de muerte y XP | Dar XP una sola vez, sin duplicados | Diseñado | **[HIPÓTESIS]** evento de muerte; comprobar el motivo de aparición para el anti-granjas |
| Encuentros de jefe | Fases, arena, mecánicas | Diseñado | — |
| Alarmas y refuerzos | Red Ribbon, Pilaf | Diseñado | — |
| Habilidades especiales | Invisibilidad, agarre, autodestrucción, rayo | Diseñado como datos | **[HIPÓTESIS]** invisibilidad y render |
| Guardado | Jefes derrotados, instalaciones desmanteladas | Diseñado | **[HIPÓTESIS]** |

### 2.4 Herramientas y recursos para modelos y texturas
- **[VERIFICADO por búsqueda]** GeckoLib existe para NeoForge 1.21.1. **No encontré** una guía del flujo de trabajo Blockbench → GeckoLib (hay un complemento llamado GeckoMesh, opcional).
- **[HIPÓTESIS]** Blockbench para modelar y animar, y un editor de píxeles para texturas. Hay que comprobarlo con documentación oficial al tener red.
- **Necesitamos de ti:** un PC con interfaz gráfica para modelar/probar, y definir quién hace el arte (tú, yo con modelos de cubos, o recursos con licencia clara).
- **[DISEÑO]** Arte **original** y estilo bloque. No extraer modelos, texturas ni sonidos de otros mods ni de la serie. Dragon Ball es propiedad de sus titulares: el mod usa nombres y personajes de la serie y conviene tenerlo presente al publicar.

### 2.5 Dificultades técnicas que afectan al bestiario
1. **Volumen:** 94 entradas. Muchas son de tipo humano o animal y comparten modelo con textura distinta (§2.6 etapa 2). Modelos únicos reales: unos 40.
2. **Entorno:** en la nube no se puede ejecutar Minecraft con pantalla. Sí podría ejecutarse un servidor sin pantalla y pruebas automáticas cuando haya red **[HIPÓTESIS]**. Lo visual se prueba en tu PC.
3. **Red bloqueada:** hoy no se puede ni compilar un mod mínimo.
4. **Jefes de vanilla** (Wither, Warden, Dragón del End): modificarlos puede ser complejo **[HIPÓTESIS]**; están marcados como removibles, como pediste.
5. **Vegeta con fase Ozaru:** requiere cambiar de comportamiento y modelo a mitad del combate. Es lo más arriesgado de la lista (§2.6 etapa 4).
6. **Invisibilidad (See-Through):** render y detección **[HIPÓTESIS]**.
7. **Rendimiento:** muchos NPC con IA y grupos de 3 Saibamen; límites de aparición configurables.
8. **Canon:** parte de la lista es de memoria (ver `BESTIARY.md` §10). Las habilidades no confirmadas son **decisiones de diseño** y están marcadas.

### 2.6 División del trabajo para llegar a una beta funcional
Cada etapa se cierra con una tabla de estado: *diseñado → implementado → compilado → probado en juego*. Hoy todo está solo en **diseñado**.

| Etapa | Contenido | Depende de |
|---|---|---|
| **0. Infraestructura** | Proyecto NeoForge mínimo que compile y arranque; núcleo en Java con las fórmulas; cargador de `bestiary.json` | Tu aprobación (§5), red abierta, PC |
| **1. Prototipo** | 1 criatura salvaje (lobo), 1 bandido, 1 soldado del Red Ribbon, 1 enemigo de ataque especial (Saibaman), 1 jefe sencillo (Ninja Murasaki). Aparecen, se mueven, combaten, mueren y dan XP/botín | Etapa 0 |
| **2. Fauna y básicos** | Pasivos, fauna, reptiles, acuática, aérea, bandidos y campamentos, soldados de Pilaf y Red Ribbon | Etapa 1 |
| **3. Especiales** | Robots, comandantes del Red Ribbon, luchadores de Baba, hijos de Piccolo Daimaō | Etapa 2 |
| **4. Saga Saiyan** | Saibamen en grupo de 3, Raditz, Nappa, Vegeta (Ozaru si es viable) | Etapa 3 |
| **5. Pruebas y equilibrio** | Estadísticas, recompensas, apariciones, guardado/carga, rendimiento; documentar errores | Etapa 4 |

Lo que **ya** está hecho sobre el papel en cada etapa: datos y niveles de todas las entidades, XP calibrada, dificultad de los jefes comprobada con el simulador.

## 3. Mods de referencia
Mantengo la conclusión de la fase 0: DragonMine Z es el único candidato con código público (GPL-3.0 **según resultados de búsqueda**, no leído). Estudiar sus soluciones sí; copiar código implicaría que nuestro mod fuera GPL-3.0. Dragon Block C parece de código cerrado. **No se ha leído el código de ninguno en esta sesión** por el bloqueo de red.

## 4. Lo que ya cambió con tu prompt (se aplica a los datos)
- Pilaf, Red Ribbon, luchadores de Baba e hijos de Piccolo Daimaō están en `bestiary.json` con nivel, arquetipo, comportamiento y calibración.
- La saga Saiyan se **desplazó**: Raditz pasa del nivel 55 al 95 y Nappa del 130 al 140, porque ahora van después de los arcos clásicos. Vegeta sigue en 171.
- El **Asesino Tao** que yo había inventado como secreto se eliminó: **Tao Pai Pai** es ahora el jefe canónico del Red Ribbon (nivel 52).
- La XP de jefes se bajó (6 % / 4 % / 3 %) porque con tantos jefes pagaban el 120 % del presupuesto de combate.

## 5. Decisiones que necesito de ti antes de pasar a código
1. **Plataforma:** ¿confirmas Minecraft 1.21.1 + NeoForge + Java 21? (La otra opción era una 26.x con Java 25.)
2. **Permiso para crear el proyecto Gradle y descargar dependencias** (NeoForge, GeckoLib), y **abrir la red** en el entorno para esos hosts. Sin eso no puedo pasar de la etapa 0.
3. **Dónde se compila y prueba:** ¿en esta sesión (compilar y pruebas automáticas, sin ver el juego) o en tu PC? Lo visual solo puede verificarse en tu PC.
4. **Arte:** ¿empezamos con modelos de cubos provisionales y texturas originales sencillas?
5. **Alcance:** los 94 enemigos son mucho para una primera pasada. ¿Aceptas que entren por etapas y que algunas entidades usen modelo compartido con otra textura?

Hasta que respondas, no creo el proyecto ni instalo nada.

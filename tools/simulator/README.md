# Simulador de diseño (prototipo en Python)

Herramienta para **probar los números del diseño antes de que exista el juego**. No es código del juego (el juego será Java); sirve para detectar fallos de equilibrio con datos en vez de intuición.

Refleja las fórmulas de `SYSTEMS.md` y `ABILITIES.md`. Solo usa la biblioteca estándar de Python 3.

## Cómo ejecutarlo
Desde esta carpeta:
```
python3 sim.py            # matriz de duelos entre builds de Saiyajin y Humano (nivel total 171)
python3 sweep2.py         # ¿qué palancas de diseño equilibran las especializaciones?
python3 sweep.py          # barrido de exponentes de daño y de valor
python3 story_check.py    # garantía de historia: ¿puede cada build vencer al jefe?
python3 bestiary.py                      # valida el bestiario y comprueba los jefes de historia
python3 bestiary.py --calibrate          # sugiere power_mult para jefes y élite (~2 min)
python3 -m unittest discover -s tests    # pruebas (29)
```
Los parámetros están en `params.py`.

## Qué modela y qué no
Modela: atributos con tope 99, poder efectivo, daño por razón de poder, esquiva, ki absoluto, técnicas con requisitos, Kaio-ken con drenaje de vida y de ki. Un golpe por segundo por combatiente, orden aleatorio.

**No** modela: alcance, vuelo, movimiento, esquivar proyectiles con habilidad, objetos, ni decisiones de un jugador real. Un duelo aquí es una medida de **equilibrio relativo**, no una predicción de cómo se jugará.

## Hallazgos (2026-10-11)
1. **El ki no servía de nada con costes en porcentaje.** Si todo coste es un % de la reserva, el atributo KI no cambia cuántas técnicas lanzas. Los costes de técnica pasan a ser **absolutos** (SYSTEMS §4).
2. **Sin técnicas de especialista, el equilibrado domina** (65–92 % de victorias medias según los builds). El daño es proporcional a ataque ÷ defensa, así que gana quien tiene mayor ataque × defensa, y ese producto se maximiza repartiendo. La ventaja de un especialista tiene que venir de **habilidades que solo él puede usar** (requisitos altos).
3. **Con técnicas de especialista** y builds que mantienen algo de supervivencia: Equilibrado 35 % / Guerrero 55 % / Maestro de ki 60 % de victorias medias.
4. **Que la velocidad dé más acciones por segundo rompe el equilibrio** (el Maestro de ki cae al 0–5 %). No se adopta.
5. **La regla del 15 % de las puertas (ABILITIES §4) detectó un fallo:** Kaio-ken grado 1 con VIT ≥ 18 cuesta al Humano 11 mejoras (15,7 %). Con VIT ≥ 16: Saiyajin 11,4 %, Humano 14,3 %.
6. **Humano y Saiyajin no quedan igual** en duelos del mismo nivel: el Saiyajin guerrero vence al Humano guerrero el 91 % de las veces. El Humano no entra en la cuenta con sus ventajas de maestros y precios.

7. **Garantía de historia:** todos los builds normales vencen al jefe de su nivel en 44–71 %; los extremos, con 10 mejoras más (≈ 1,4 h), en 79–100 % (`story_check.py`).

8. **Bestiario de la beta** (`design_data/beta/bestiary.json`, `BESTIARY.md`): los jefes de historia cumplen la garantía con margen; ver los errores que salieron al hacerlo en `BESTIARY.md` §9.

9. **Alcance clásico → Saiyan (94 entradas):** al ampliar el alcance, los jefes únicos pagaban el 120 % del presupuesto de combate (se bajaron los porcentajes); varios jefes calibrados al límite fallaron con otra semilla (se añadió margen); animales pasivos a ~1,2–1,7 golpes con `fragility`.

## Corrección de un error propio
La primera versión hacía que el combatiente de la fila atacara siempre primero, con lo que **ambos lados parecían ganar** (76 % y 95 % a la vez). Corregido con orden aleatorio por segundo; añadidas pruebas de simetría (`test_matchup_is_symmetric`, `test_no_first_mover_bias_mirror_match`). Las cifras de arriba son las de la versión corregida.

## Límites de la evidencia
- 1.000 duelos por pareja: error de unos ±3 puntos.
- Las técnicas de especialista y sus números (multiplicador y coste) son propuestas mías; el simulador solo muestra que **hacen falta**, no cuáles son los valores correctos.
- Todo debe revalidarse con combate real en la Fase 2.

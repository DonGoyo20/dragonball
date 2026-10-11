# ABILITIES — Habilidades, técnicas, transformaciones y sus requisitos

> Estado: **borrador v1 (2026-10-11), sobre el papel.** Todo lo que sea una habilidad
> especial (técnicas de ki, vuelo, formas, pasivas raciales) se define con el mismo
> esquema de datos y exige **niveles mínimos en ciertos atributos**. Los valores son
> propuestas (P), ajustables en archivos. Se apoya en `SYSTEMS.md` y `RACES.md`.
> Las referencias a la serie son de memoria, sin consultar fuentes.

## 1. Idea (decisión tuya)
Una habilidad no se puede usar solo por tenerla aprendida: **tu personaje tiene que ser capaz de ejecutarla**.
- El **Kamehameha** exige control de ki.
- El **Kaio-ken** exige resistencia corporal (Vitalidad), porque desgasta físicamente a quien lo usa.
- Los requisitos hacen que *tu build decida qué habilidades tienes*, igual que en Elden Ring con las armas.

## 2. Tipos de habilidad
| Tipo | Ejemplos | Notas |
|---|---|---|
| Técnica de ki | Ráfaga de ki, Kamehameha, Galick Ho | Gasta % de ki |
| Especial | Vuelo, detectar ki, imagen residual | Utilidad |
| Forma / transformación | Kaio-ken, Super Saiyajin | Puertas de forma (WORLD_DESIGN §4.2) |
| Pasiva / racial | Zenkai, regeneración | Se cumple por raza |

Todas comparten el mismo esquema de datos (§6): no hay lógica especial por habilidad salvo efectos únicos.

## 3. Cómo se aprende y cómo se usa
Dos comprobaciones distintas:

**Aprender** (una vez): necesita *todo* lo siguiente.
- Una **fuente**: maestro, misión, objeto o descubrimiento (secreto).
- **Puntos de técnica** (SYSTEMS §2.8).
- Los **requisitos de atributo** de la habilidad cumplidos *en ese momento* (el maestro no enseña a quien no puede ejecutarlo).
- Si aplica: raza, forma previa, nivel total mínimo, misión previa.

**Usar** (cada vez): hay que seguir cumpliendo los requisitos de atributo.

Reglas:
- **Se comparan valores de atributo base**, es decir, el número que ves (1–99). No cuentan buffs temporales ni comida: así no se hace trampa con consumibles.
- **Si bajas de un requisito** (por reasignar mejoras), la habilidad se **bloquea pero no se pierde**. Vuelve cuando recuperas el valor.
- **La interfaz muestra los requisitos** de cada habilidad conocida, con lo que te falta en rojo.
- Un maestro puede revelar un requisito que aún no conocías (esto encaja con el diario de ruta).
- El requisito es de **valor de atributo**, no de mejoras: a cada raza le cuesta un número distinto de mejoras llegar al mismo valor. Es intencionado (a un Terrícola le sale más barato el control; a un Saiyajin, la fuerza).

## 4. Equilibrio: que los requisitos no te obliguen a ser híbrido
Tú quieres personajes extremos (fuerza pura, o control de ki enorme). Los requisitos no deben obligar a todo el mundo a ser equilibrado. Reglas de diseño:

1. **Formas obligatorias de puerta** (las que hacen falta para avanzar): máximo **un** atributo exigido y un coste en mejoras **≤ 15 %** de las que tienes en esa puerta.
2. **Habilidades opcionales y de poder** pueden exigir más y deben hacerlo: son lo que define tu especialidad.
3. **Prueba automática:** un script (y un test en `core`) comprueba, para cada raza, que la forma de cada puerta cumple la regla 1 con un personaje que haya puesto todas sus mejoras en otros atributos. Si no, falla la compilación del contenido.

Comprobación hecha con un Saiyajin: el Kaio-ken grado 1 exige Vitalidad 18, que cuesta 10 mejoras = 14 % de las 70 de la puerta 1. Cumple.

## 5. Tabla inicial de habilidades de la beta (P)
Los atributos usan las abreviaturas: VIT, FUE, DEF, VEL, KI, POD, CON. "Fuente" es quien enseña. Los valores son de partida para ajustar.

| Habilidad | Tipo | Requisitos | Fuente | Notas |
|---|---|---|---|---|
| Ráfaga de ki | Técnica | Ninguno | Inicial | Todos la tienen |
| Vuelo | Especial | CON ≥ 10 | Kami-sama | Movilidad; coste mínimo de ki |
| Detectar ki | Especial | CON ≥ 15 | Kami-sama o Piccolo | Radar de nivel relativo |
| Imagen residual | Especial | VEL ≥ 40 | Piccolo | Esquiva mejorada |
| Kamehameha | Técnica | CON ≥ 20, POD ≥ 20 | Roshi | Un guerrero puro no puede usarlo |
| Galick Ho | Técnica | POD ≥ 25, KI ≥ 20 | Saiyajin (innata, se activa al cumplir) | Racial |
| Makankosappo | Técnica | POD ≥ 45, CON ≥ 35 | Piccolo | Perforante, carga lenta |
| Genkidama | Técnica | KI ≥ 45, CON ≥ 40, POD ≥ 35 | Kaio-sama (ruta difícil) | Solo builds muy de ki |
| **Kaio-ken** | Forma (puerta 1) | Ver §7 | Kaio-sama | Desgasta la vida |

Ejemplo con tres Saiyajin del mismo nivel total (171):
| Build | Kamehameha | Galick Ho | Kaio-ken grado 1 | Kaio-ken grado 2 |
|---|---|---|---|---|
| Equilibrado | sí (CON 24,7) | sí | sí | sí |
| Guerrero | no (CON 4) | no | sí | no |
| Maestro de ki | sí | sí | sí | no |

Lectura: el Guerrero puro no puede usar técnicas de ki, y el Maestro de ki no llega a los grados altos del Kaio-ken (su Vitalidad es baja): cada especialización tiene su techo y su hueco. El equilibrado accede a casi todo lo básico pero a nada extremo. Los umbrales se revisan con pruebas reales.

## 6. Esquema de datos
```json
{
  "id": "kamehameha",
  "type": "technique",
  "requirements": {
    "attributes": { "CON": 20, "POD": 20 },
    "sources": ["master:roshi"],
    "race": null,
    "total_level": null
  },
  "cost": { "ki_pct": 25 },
  "scaling": { "damage_attribute": "POD", "multiplier": 3.0 },
  "effects": ["beam"],
  "drawbacks": []
}
```
```json
{
  "id": "kaioken",
  "type": "form",
  "gate": 1,
  "can_stabilize": false,
  "grades": [
    { "grade": 1, "multiplier": 1.6, "requires": { "VIT": 18 } },
    { "grade": 2, "multiplier": 2.0, "requires": { "VIT": 30, "CON": 20 } },
    { "grade": 3, "multiplier": 2.5, "requires": { "VIT": 45, "CON": 30 } },
    { "grade": 4, "multiplier": 3.0, "requires": { "VIT": 60, "CON": 40 } }
  ],
  "drain": { "hp_pct_per_s_base": 0.5, "formula": "base * grade^2 * 50 / (50 + VIT)" },
  "cost": { "ki_pct_per_s": 1.0 }
}
```
Ninguna regla de habilidad vive en el código: solo se leen estos campos. Lo único con código propio son los **efectos** (haz, esfera, disco…), que se reutilizan entre habilidades.

## 7. Kaio-ken en detalle (P)
La puerta 1 de Saiyajin y Terrícola, y el **primer caso de prueba** de las transformaciones.

| Grado | Multiplicador | Requisitos | Vida perdida (VIT típica) |
|---|---|---|---|
| 1 | ×1,6 | VIT ≥ 18 | 0,28 %/s (VIT 38) → ~6 min |
| 2 | ×2,0 | VIT ≥ 30, CON ≥ 20 | 1,05 %/s (VIT 45) → ~95 s |
| 3 | ×2,5 | VIT ≥ 45, CON ≥ 30 | 2,05 %/s (VIT 60) → ~49 s |
| 4 | ×3,0 | VIT ≥ 60, CON ≥ 40 | 3,20 %/s (VIT 75) → ~31 s |

Reglas:
- **Desgaste físico:** la Vitalidad reduce la vida que pierdes (fórmula `0,5 % × grado² × 50 / (50 + VIT)` por segundo). Es el efecto "desgasta físicamente al usuario".
- **También gasta ki** (1 % por segundo en el borrador), con las reglas de agotamiento de SYSTEMS §5.
- **Seguridad:** el Kaio-ken se desactiva solo cuando tu vida baja del **10 %** (no te mata por sí mismo).
- **No se estabiliza:** el dominio reduce el drenaje de vida hasta un −50 % y el coste de ki, pero **nunca pasa a coste cero** (`can_stabilize: false`). Es una técnica de esfuerzo, no un estado normal.
- **Cómo se obtiene:** hito de entrenamiento con Kaio-sama al final del Camino de la Serpiente. Los grados 2–4 piden dominio ≥ 50 % del grado anterior más los requisitos de atributo.
- **Puerta 1:** con el grado 1 desbloqueado se pasa la puerta (nivel total 71).

*Aviso:* en la serie el multiplicador sube en pasos de ×2, ×3, ×4…; mis valores (×1,6 a ×3) son más bajos a propósito, porque el combate se calcula por razón de poder. Se ajusta en la Fase 2.

## 8. Efectos sobre otros sistemas
- `SYSTEMS.md` §8 (dominio de forma): añadir la excepción `can_stabilize: false`.
- **Maestros:** enseñan solo a quien cumple los requisitos y pueden aconsejar qué atributo subir.
- **Misiones:** las misiones de desbloqueo de habilidad pueden exigir primero cumplir los requisitos.
- **Interfaz:** ficha de habilidad con requisitos y lo que falta.
- **Pruebas automáticas (`core`):** test de accesibilidad de puertas (§4) y test de requisitos.

## 9. Preguntas abiertas
1. ¿El **Kaio-ken** debe poder subirse hasta ×4 en la beta (con VIT 60+, a nivel total ~170 no se alcanza), o solo los grados 1–2?
2. ¿Quieres que los valores de requisito se muestren **siempre** o que haya que descubrirlos con un maestro?
3. ¿La **Vitalidad** representa bien lo que llamas "resistencia del personaje", o prefieres que sea un atributo aparte?

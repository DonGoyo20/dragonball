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

## 3. Cómo se consigue y cómo se usa (decisión tuya: se compra con XP)
La **XP es la moneda del juego**: sirve para subir atributos (SYSTEMS §2.1) y también para **comprar habilidades**, como las runas en Elden Ring. Gastar XP en una habilidad no baja tu nivel, pero esa XP ya no la usas para mejorar atributos: es una decisión real.

**Comprar** (una vez):
- Necesitas una **fuente**: un maestro que la enseñe, una misión, un objeto o un descubrimiento (secreto).
- Pagas el **precio en XP** de la habilidad.
- **No hace falta cumplir los requisitos de atributo para comprarla.** Puedes comprar el Kaio-ken antes de tener la Vitalidad para usarlo. La interfaz muestra los requisitos *antes* de pagar, para que nadie compre a ciegas.
- Si aplica: raza, forma previa, nivel total mínimo, misión previa.

**Usar** (cada vez): hay que cumplir los requisitos de atributo.

Reglas:
- **Se comparan valores de atributo base**, el número que ves (1–99). No cuentan buffs temporales ni comida: así no se hace trampa con consumibles.
- **Si bajas de un requisito** (por reasignar mejoras), la habilidad se **bloquea pero no se pierde**.
- **La interfaz muestra los requisitos siempre** de cada habilidad conocida (decisión tuya), con lo que te falta en rojo.
- El requisito es de **valor de atributo**, no de mejoras: a cada raza le cuesta un número distinto de mejoras llegar al mismo valor. Es intencionado (a un Humano le sale más barato el control; a un Saiyajin, la fuerza).

### 3.1 Precios
El precio se fija como un **porcentaje de la XP acumulada** al nivel donde se espera comprarla, así escala solo con el ritmo del juego:
| Tipo | % de la XP acumulada |
|---|---|
| Técnica básica | 10 % |
| Técnica intermedia | 15 % |
| Técnica avanzada | 20 % |
| Forma de puerta | 20 % |
| Grado extra de una forma | 8 % |

El diseñador puede ajustar cada precio a mano en los datos.

## 4. Equilibrio: que los requisitos no te obliguen a ser híbrido
Tú quieres personajes extremos (fuerza pura, o control de ki enorme). Los requisitos no deben obligar a todo el mundo a ser equilibrado. Reglas de diseño:

1. **Formas obligatorias de puerta** (las que hacen falta para avanzar): máximo **un** atributo exigido y un coste en mejoras **≤ 15 %** de las que tienes en esa puerta.
2. **Habilidades opcionales y de poder** pueden exigir más y deben hacerlo: son lo que define tu especialidad.
3. **Prueba automática:** un script (y un test en `core`) comprueba, para cada raza, que la forma de cada puerta cumple la regla 1 con un personaje que haya puesto todas sus mejoras en otros atributos. Si no, falla la compilación del contenido.

Comprobación hecha con un Saiyajin: el Kaio-ken grado 1 exige Vitalidad 18, que cuesta 10 mejoras = 14 % de las 70 de la puerta 1. Cumple.

## 5. Tabla inicial de habilidades de la beta (P)
Atributos: VIT, FUE, DEF, VEL, KI, POD, CON. "Fuente" es quien enseña. "Precio" en XP, calculado para el nivel de compra indicado (§3.1). Valores de partida para ajustar.

| Habilidad | Tipo | Requisitos de uso | Fuente | Nivel de compra | Precio (XP) |
|---|---|---|---|---|---|
| Ráfaga de ki | Técnica | Ninguno | Inicial | — | gratis |
| Vuelo | Especial | CON ≥ 10 | Kami-sama | ~30 | 270 |
| Detectar ki | Especial | CON ≥ 15 | Kami-sama o Piccolo | ~40 | 530 |
| Imagen residual | Especial | VEL ≥ 40 | Piccolo | ~50 | 910 |
| Kamehameha | Técnica | CON ≥ 20, POD ≥ 20 | Roshi | ~46 | 1.120 |
| Galick Ho | Técnica | POD ≥ 25, KI ≥ 20 | Saiyajin (racial, incluida) | — | incluida |
| Makankosappo | Técnica | POD ≥ 45, CON ≥ 35 | Piccolo | ~120 | 16.070 |
| Genkidama | Técnica | KI ≥ 45, CON ≥ 40, POD ≥ 35 | Kaio-sama (ruta difícil) | ~150 | 28.280 |
| **Kaio-ken** (grado 1) | Forma (puerta 1) | VIT ≥ 18 | Kaio-sama | 71 | 4.320 |
| Kaio-ken ×2 | Grado | VIT ≥ 30, CON ≥ 20 | Kaio-sama | ~100 | 4.060 |
| Kaio-ken ×3 | Grado | VIT ≥ 45, CON ≥ 30 | Kaio-sama | ~140 | 9.500 |

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
  "price_xp": 1120,
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
  "price_xp": 4320,
  "can_stabilize": false,
  "grades": [
    { "grade": 1, "name": "Kaio-ken",    "multiplier": 1.6, "requires": { "VIT": 18 } },
    { "grade": 2, "name": "Kaio-ken ×2", "multiplier": 2.0, "requires": { "VIT": 30, "CON": 20 }, "price_xp": 4060 },
    { "grade": 3, "name": "Kaio-ken ×3", "multiplier": 2.5, "requires": { "VIT": 45, "CON": 30 }, "price_xp": 9500 }
  ],
  "drain": { "hp_pct_per_s_base": 0.5, "formula": "base * grade^2 * 50 / (50 + VIT)" },
  "cost": { "ki_pct_per_s": 1.0 }
}
```
Ninguna regla de habilidad vive en el código: solo se leen estos campos. Lo único con código propio son los **efectos** (haz, esfera, disco…), que se reutilizan entre habilidades.

## 7. Kaio-ken en detalle (P)
La puerta 1 de Saiyajin y Terrícola, y el **primer caso de prueba** de las transformaciones.

| Grado | Nombre | Multiplicador real | Requisitos de uso | Vida perdida (VIT típica) |
|---|---|---|---|---|
| 1 | Kaio-ken | ×1,6 | VIT ≥ 18 | 0,28 %/s (VIT 38) → ~6 min |
| 2 | Kaio-ken ×2 | ×2,0 | VIT ≥ 30, CON ≥ 20 | 1,05 %/s (VIT 45) → ~95 s |
| 3 | Kaio-ken ×3 | ×2,5 | VIT ≥ 45, CON ≥ 30 | 2,05 %/s (VIT 60) → ~49 s |

**Los nombres son de sabor, no literales** (decisión tuya): "×2" y "×3" son el nombre del grado, pero el multiplicador real es el de la tabla. El tope en la beta es el ×3. El grado 4 queda reservado para más adelante.

Reglas:
- **Desgaste físico:** la Vitalidad reduce la vida que pierdes (fórmula `0,5 % × grado² × 50 / (50 + VIT)` por segundo). Es el efecto "desgasta físicamente al usuario".
- **También gasta ki** (1 % por segundo en el borrador), con las reglas de agotamiento de SYSTEMS §5.
- **Seguridad:** el Kaio-ken se desactiva solo cuando tu vida baja del **10 %** (no te mata por sí mismo).
- **No se estabiliza:** el dominio reduce el drenaje de vida hasta un −50 % y el coste de ki, pero **nunca pasa a coste cero** (`can_stabilize: false`). Es una técnica de esfuerzo, no un estado normal.
- **Cómo se obtiene:** hito de entrenamiento con Kaio-sama al final del Camino de la Serpiente + **compra con XP** (4.320). Los grados ×2 y ×3 se compran después y piden dominio ≥ 50 % del grado anterior, además de los requisitos de atributo para usarlos.
- **Puerta 1:** comprar el grado 1 abre la puerta (nivel total 71). Hasta entonces, la XP se acumula sin poder gastarse en más mejoras: esa XP guardada es la que paga el Kaio-ken.

*Aviso:* en la serie el multiplicador sube en pasos de ×2, ×3, ×4…; mis valores (×1,6 a ×3) son más bajos a propósito, porque el combate se calcula por razón de poder. Se ajusta en la Fase 2.

## 8. Efectos sobre otros sistemas
- `SYSTEMS.md` §8 (dominio de forma): añadir la excepción `can_stabilize: false`.
- **Maestros:** enseñan solo a quien cumple los requisitos y pueden aconsejar qué atributo subir.
- **Misiones:** las misiones de desbloqueo de habilidad pueden exigir primero cumplir los requisitos.
- **Interfaz:** ficha de habilidad con requisitos y lo que falta.
- **Pruebas automáticas (`core`):** test de accesibilidad de puertas (§4) y test de requisitos.

## 9. Decisiones cerradas
1. Kaio-ken con **3 grados** (hasta "×3"); los nombres no son literales (§7).
2. Los requisitos **se ven siempre**; las habilidades **se compran con XP** y se necesitan los atributos para **usarlas**, no para comprarlas (§3).
3. **La Vitalidad es la "resistencia" del personaje**; no se crea un atributo aparte. Es mi decisión (me dijiste "como consideres"): evita un octavo atributo que casi no se usaría y la Vitalidad ya cubre salud, aguante y desgaste.

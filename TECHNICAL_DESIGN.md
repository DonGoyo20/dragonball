# TECHNICAL_DESIGN — Arquitectura propuesta

> Estado: **propuesta, no aprobada.** Asume (P) Minecraft 1.21.1 + NeoForge + Java 21 +
> GeckoLib 4.x. Las APIs de NeoForge citadas son de memoria y **se verifican en la
> Fase 1** antes de escribir código.

## 1. Principios
- Lógica del RPG separada del juego: así se prueba con tests automáticos sin abrir Minecraft.
- Contenido (razas, formas, misiones, maestros) en archivos de datos JSON, no en código.
- Sin dependencias que no aporten un beneficio claro. Solo GeckoLib (animación) en la versión inicial.
- Cambios pequeños, un sistema cada vez.

## 2. Capas
| Capa | Contiene | Depende de Minecraft |
|---|---|---|
| `core` | Fórmulas de poder, costes, recompensas, anti-farmeo, reglas de requisitos | **No** (Java puro, con tests) |
| `data` | Carga y validación de JSON de razas, formas, misiones, maestros | Mínimo |
| `game` | Eventos, atributos, entidades, persistencia, comandos | Sí |
| `client` | Pantallas, HUD, teclas, modelos, partículas | Sí (solo cliente) |

## 3. Estructura de carpetas
```
src/main/java/<paquete>/
  core/        # fórmulas y reglas puras
  data/        # modelos y cargadores de datos
  game/        # eventos, persistencia, comandos
  client/      # interfaz y render
src/main/resources/data/<modid>/
  races/  forms/  quests/  masters/  techniques/  balance/
src/test/java/<paquete>/core/
```

## 4. Datos del personaje
Un registro por jugador: raza, etapa, puntos gastados por atributo, puntos libres, ki actual, técnicas aprendidas, formas desbloqueadas, misiones (estado y progreso), contadores anti-farmeo, **número de versión del esquema**.
Persistencia prevista: sistema de adjuntos de datos de NeoForge (a verificar). Guardado seguro: validar al cargar, valores por defecto si falta algo, copia del dato anterior si hay error y registro en log.
Migración: cada cambio de esquema sube la versión y tiene una función `migrarDeVnAVn+1`.

## 5. Eventos de juego (a verificar nombres)
- Muerte de entidad: concede recompensa al jugador que la causó; deduplicado con un identificador de entidad ya recompensada (evita duplicados).
- Daño: se intercepta para aplicar `mult_daño(r)` (ver GDD §2.1).
- Tick de jugador: regeneración de ki, mantenimiento de formas.

## 6. Escala de poder y límites de Minecraft
Los atributos vanilla tienen límites (la vida máxima, según recuerdo, 1024 por defecto). No intentaremos meter el NP en atributos de vanilla: se mantiene propio y se aplica por razón al daño. **Verificar en Fase 2.**

## 7. Ejemplo de dato de raza (borrador)
```json
{
  "id": "saiyan",
  "base": { "vitality": 8, "strength": 10, "defense": 6, "speed": 8, "ki": 6, "ki_power": 8, "control": 5 },
  "progression": { "strength": 1.2, "ki_power": 1.1, "control": 0.9 },
  "traits": ["zenkai", "ki_regen_low"],
  "forms": ["super_saiyan"],
  "techniques": ["ki_blast", "kamehameha"]
}
```

## 8. Ejemplo de misión (borrador)
```json
{
  "id": "roshi_turtle_shell",
  "giver": "roshi",
  "requires": { "stage": 0 },
  "objectives": [
    { "type": "carry_weight", "ticks": 6000 },
    { "type": "kill", "entity": "minecraft:zombie", "count": 20 }
  ],
  "rewards": { "attribute_points": 3, "unlock": ["technique:kamehameha"] }
}
```

## 9. Pruebas
- Automáticas (JUnit) sobre `core`: curvas, topes, anti-farmeo, requisitos.
- Manuales documentadas en `TESTING.md`: matar mobs, gastar puntos, cerrar y reabrir mundo.
- Cada funcionalidad se marca en 4 estados: *código escrito → compila → prueba funcional → verificada en el juego*.

## 10. Rendimiento (Ryzen 5 5600X, 16 GB)
Objetivos iniciales: pocos NPC activos, sin efectos pesados; límite de partículas configurable. Medir antes de optimizar.

## 11. Multijugador
No es requisito. Se separa lógica de servidor (`game`) y de cliente (`client`) desde el principio, porque es barato y evita reescribir después.

## 12. Requisitos de habilidad (añadido 2026-10-11)
Toda habilidad (técnica, especial, forma, pasiva) se carga desde datos y tiene requisitos de atributo, de fuente y de raza (ver `ABILITIES.md`). El motor de requisitos vive en `core` (Java puro) y se prueba con tests: comprobar requisitos al aprender y al usar, bloquear sin perder, y el **test de accesibilidad de puertas** (la forma de cada puerta debe costar ≤ 15 % de las mejoras para cualquier build).

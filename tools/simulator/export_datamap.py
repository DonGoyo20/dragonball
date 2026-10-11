"""Exporta el bestiario de diseño al mapa de datos de NeoForge (dragonball:bestiary, registro entity_type).

Fuente de verdad: design_data/beta/bestiary.json. El juego solo lee el resultado.
Solo se exportan entidades que ya EXISTEN en el juego: todas las de `minecraft:` y las del
mod listadas en IMPLEMENTED_MOD_ENTITIES (un id desconocido en un mapa de datos no debe llegar al juego).

Uso:  python3 export_datamap.py            # escribe el archivo
      python3 export_datamap.py --check    # falla si el archivo del juego no coincide con la fuente
"""
import json
import os
import sys

import bestiary as B

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.normpath(os.path.join(
    HERE, "..", "..", "src", "main", "resources", "data", "dragonball", "data_maps", "entity_type", "bestiary.json"))

# Entidades propias ya implementadas en el mod (ampliar al añadir cada una)
IMPLEMENTED_MOD_ENTITIES = {"dragonball:wild_wolf"}


def entry_for(e):
    out = {
        "level": e["level"],
        "kind": e["kind"],
        "archetype": e["archetype"],
        "xp": B.xp_for(e),
    }
    if e.get("group_size"):
        out["group_size"] = e["group_size"]
    return out


def build(data=None):
    data = data or B.load()
    values = {}
    for e in data["enemies"]:
        src = e["source"]
        if src.startswith("minecraft:") or src in IMPLEMENTED_MOD_ENTITIES:
            values[src] = entry_for(e)
    return {"values": dict(sorted(values.items()))}


def main():
    doc = build()
    text = json.dumps(doc, ensure_ascii=False, indent=2) + "\n"
    if "--check" in sys.argv:
        current = open(OUT, encoding="utf8").read() if os.path.exists(OUT) else ""
        if current != text:
            print("El mapa de datos del juego NO coincide con el bestiario de diseño. Ejecuta export_datamap.py.")
            sys.exit(1)
        print("OK: el mapa de datos coincide con el bestiario de diseño.")
        return
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    open(OUT, "w", encoding="utf8").write(text)
    print(f"{len(doc['values'])} entidades exportadas a {OUT}")


if __name__ == "__main__":
    main()

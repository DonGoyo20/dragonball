"""Garantía de historia: ¿puede cada raza y build vencer a un jefe de historia del mismo nivel?
El jefe de ejemplo es un Saiyajin equilibrado de nivel total 171 (stand-in de Vegeta)."""
import params as P, sim

THRESHOLD = 0.30   # % mínimo de victorias frente al jefe de historia al nivel esperado

def boss():
    return sim.Fighter("saiyajin", sim.BUILDS["Equilibrado"], use_kaioken=True, name="Jefe")

def table(n=1000):
    rows = []
    for race in P.PLAYABLE_RACES:
        for bn, ups in sim.BUILDS.items():
            wa, wb, d = sim.win_rate(lambda r=race, u=ups: sim.Fighter(r, u), boss, n=n)
            rows.append((f"{race} / {bn}", wa))
    return rows

if __name__ == "__main__":
    print(f"Victorias contra el jefe de historia (nivel igual), umbral de diseño {THRESHOLD*100:.0f}%:")
    for name, w in table():
        print(f"  {name:24} {w*100:>4.0f}%  {'OK' if w >= THRESHOLD else 'POR DEBAJO'}")


# --- Garantía para builds extremos: con pocas mejoras extra en el atributo principal se vence al jefe
MAIN_ATTACK_ATTR = {"Equilibrado": None, "Guerrero": "FUE", "Maestro de ki": "POD"}
EXTRA_UPS = 10          # ≈ 1,4 h de juego a nivel 171
EXTREME_THRESHOLD = 0.70

def extreme_with_extra(n=1000):
    rows = []
    for race in P.PLAYABLE_RACES:
        for bn, ups in sim.EXTREME_BUILDS.items():
            attr = MAIN_ATTACK_ATTR[bn]
            if not attr:
                continue            # el equilibrado no es un build extremo: lo cubre table()
            u = list(ups)
            u[P.ATTRS.index(attr)] += EXTRA_UPS
            wa, wb, d = sim.win_rate(lambda r=race, x=tuple(u): sim.Fighter(r, x), boss, n=n)
            rows.append((f"{race} / {bn} (+{EXTRA_UPS})", wa))
    return rows

if __name__ == "__main__":
    print(f"\nBuilds extremos con +{EXTRA_UPS} mejoras en su atributo principal (umbral {EXTREME_THRESHOLD*100:.0f}%):")
    for name, w in extreme_with_extra():
        print(f"  {name:34} {w*100:>4.0f}%  {'OK' if w >= EXTREME_THRESHOLD else 'POR DEBAJO'}")

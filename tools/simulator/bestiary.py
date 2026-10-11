"""Carga, valida y simula el bestiario de la beta (design_data/beta/bestiary.json).
Prototipo de diseño en Python: NO es código del juego."""
import json
import os

import params as P
import sim

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, "..", "..", "design_data", "beta", "bestiary.json")

# --- Reglas de XP (BESTIARY.md §3)
BOSS_PCT = {"story": 0.15, "boss": 0.10, "secret": 0.08}   # % de la XP acumulada hasta ese nivel
ELITE_MULT = 3.0


def need(L):
    return 50 * L ** 1.6


def kills_per_level(L):
    return 10 + 0.5 * L


def cost_next_upgrade(N):
    """Coste de la siguiente mejora estando en nivel total N."""
    return 10 * (1 + (N - 1) / 5) ** 1.6


def cumulative_xp(N):
    return sum(cost_next_upgrade(n) for n in range(1, N))


def guide_xp(N):
    L = 1 + (N - 1) / 5
    return need(L) / kills_per_level(L)


def xp_for(enemy):
    if "xp" in enemy:
        return enemy["xp"]
    N, kind = enemy["level"], enemy["kind"]
    if kind in BOSS_PCT:
        return round(BOSS_PCT[kind] * cumulative_xp(N))
    mult = ELITE_MULT if kind == "elite" else 1.0
    return round(guide_xp(N) * mult)


def load(path=DATA):
    with open(path, encoding="utf8") as f:
        return json.load(f)


def validate(data):
    errors = []
    arch, techs, enemies = data["archetypes"], data["techniques"], data["enemies"]
    for name, shares in arch.items():
        if set(shares) != set(P.ATTRS):
            errors.append(f"arquetipo {name}: atributos incorrectos")
        total = sum(shares.values())
        if not 0.999 <= total <= 1.001:
            errors.append(f"arquetipo {name}: el reparto suma {total:.3f}, no 1")
    ids = set()
    for e in enemies:
        if e["id"] in ids:
            errors.append(f"id repetido: {e['id']}")
        ids.add(e["id"])
        if not 1 <= e["level"] <= 180:
            errors.append(f"{e['id']}: nivel fuera de rango")
        if e["archetype"] not in arch:
            errors.append(f"{e['id']}: arquetipo desconocido {e['archetype']}")
        for t in e["techniques"]:
            if t not in techs:
                errors.append(f"{e['id']}: técnica desconocida {t}")
        if e["kind"] == "secret" and not (e.get("secret_hint") and e.get("reward")):
            errors.append(f"{e['id']}: un jefe secreto necesita pista y recompensa")
        if e["kind"] in ("boss", "story", "secret") and len(e["techniques"]) < 2:
            errors.append(f"{e['id']}: un jefe necesita al menos 2 técnicas")
        if e["kind"] not in ("normal", "elite", "boss", "story", "secret"):
            errors.append(f"{e['id']}: tipo desconocido {e['kind']}")
    return errors


def make_fighter(enemy, data):
    shares = data["archetypes"][enemy["archetype"]]
    T = enemy["level"] - 1
    ups = tuple(shares[a] * T for a in P.ATTRS)
    techs = {t: {**data["techniques"][t], "req": {}} for t in enemy["techniques"]}
    if "melee" not in techs:
        techs["melee"] = {**data["techniques"]["melee"], "req": {}}
    return sim.Fighter("npc", ups, use_kaioken=False, name=enemy["id"], techniques=techs,
                       extra_mult=enemy.get("power_mult", 1.0))


# --- Garantía de historia a cualquier nivel
HOURS_EXTRA = 1.4
SECONDS_PER_FIGHT = 90
THRESHOLD, EXTREME_THRESHOLD = 0.30, 0.70
MAIN_ATTR = {"Guerrero": "FUE", "Maestro de ki": "POD"}


def scaled(ups, N):
    k = (N - 1) / 170.0
    return tuple(u * k for u in ups)


def extra_upgrades(N):
    kills = HOURS_EXTRA * 3600 / SECONDS_PER_FIGHT
    per_upgrade = kills_per_level(1 + (N - 1) / 5) / 5
    return round(kills / per_upgrade)


def boss_check(enemy, data, n=600, seed=7):
    """Devuelve (mínimo normal, mínimo extremo con extra) entre razas jugables y builds."""
    N = enemy["level"]
    worst_n, worst_e = 1.0, 1.0
    detail = []
    for race in P.PLAYABLE_RACES:
        for bn, ups in sim.BUILDS.items():
            w = sim.win_rate(lambda r=race, u=scaled(ups, N): sim.Fighter(r, u),
                             lambda: make_fighter(enemy, data), n=n, seed=seed)[0]
            worst_n = min(worst_n, w)
            detail.append((race, bn, w))
        for bn, ups in sim.EXTREME_BUILDS.items():
            attr = MAIN_ATTR.get(bn)
            if not attr:
                continue
            u = list(scaled(ups, N))
            u[P.ATTRS.index(attr)] += extra_upgrades(N)
            w = sim.win_rate(lambda r=race, x=tuple(u): sim.Fighter(r, x),
                             lambda: make_fighter(enemy, data), n=n, seed=seed)[0]
            worst_e = min(worst_e, w)
    return worst_n, worst_e, detail


if __name__ == "__main__":
    data = load()
    errs = validate(data)
    print("VALIDACIÓN:", "OK" if not errs else "")
    for e in errs:
        print("  -", e)
    print(f"\n{'id':20}{'nivel':>6} {'tipo':8}{'arquetipo':12}{'XP':>8}  mejoras equiv.")
    for e in sorted(data["enemies"], key=lambda x: x["level"]):
        xp = xp_for(e)
        print(f"{e['id']:20}{e['level']:>6} {e['kind']:8}{e['archetype']:12}{xp:>8}  {xp / cost_next_upgrade(e['level']):>5.1f}")
    print("\nGARANTÍA DE HISTORIA (jefes de historia, secretos y opcionales):")
    print(f"  umbrales: builds normales >= {THRESHOLD:.0%}; extremos con +{HOURS_EXTRA} h >= {EXTREME_THRESHOLD:.0%}")
    for e in sorted(data["enemies"], key=lambda x: x["level"]):
        if e["kind"] in ("story", "boss", "secret"):
            wn, we, _ = boss_check(e, data)
            ok = "OK" if (wn >= THRESHOLD and we >= EXTREME_THRESHOLD) else "REVISAR"
            print(f"  {e['id']:20} N={e['level']:>3}  peor normal {wn*100:>3.0f}%  peor extremo(+{extra_upgrades(e['level'])}) {we*100:>3.0f}%  {ok}")


# --- Calibración (--calibrate): el enemigo más duro que cumple la regla de su tipo, con margen
CAL_MARGIN_NORMAL, CAL_MARGIN_EXTREME = 0.40, 0.78   # margen sobre 30 % y 70 % por el ruido de la simulación
ELITE_TARGET_MEAN, OPTIONAL_TARGET_MEAN = 0.75, 0.35


def calibrate(enemy, data, lo=0.5, hi=4.0, steps=9, n=400):
    """Devuelve power_mult.
    story:  mayor mult con peor build normal >= 40 % y peor extremo (+1,4 h) >= 78 % (margen de la regla 30 %/70 %).
    elite:  mayor mult con media de builds normales >= 75 %.
    boss/secret (opcionales): mayor mult con media >= 35 %."""
    def ok(pm):
        e = dict(enemy)
        e["power_mult"] = pm
        wn, we, det = boss_check(e, data, n=n)
        mean = sum(w for _, _, w in det) / len(det)
        if enemy["kind"] == "story":
            return wn >= CAL_MARGIN_NORMAL and we >= CAL_MARGIN_EXTREME
        if enemy["kind"] == "elite":
            return mean >= ELITE_TARGET_MEAN
        return mean >= OPTIONAL_TARGET_MEAN
    if not ok(lo):
        return lo
    for _ in range(steps):
        mid = (lo + hi) / 2
        if ok(mid):
            lo = mid
        else:
            hi = mid
    return round(lo * 20) / 20   # a pasos de 0,05


def hp_left(enemy, data, n=300, seed=5):
    """Vida media que le queda al jugador (builds normales de las razas jugables) cuando gana."""
    import random
    rng = random.Random(seed)
    total, count = 0.0, 0
    for race in P.PLAYABLE_RACES:
        for bn, ups in sim.BUILDS.items():
            for _ in range(n // 6):
                a = sim.Fighter(race, scaled(ups, enemy["level"]))
                e = make_fighter(enemy, data)
                if sim.duel(a, e, rng) is a:
                    total += a.hp
                    count += 1
    return total / count if count else 0.0


if __name__ == "__main__":
    import sys
    if "--calibrate" in sys.argv:
        d = load()
        print("\nCALIBRACIÓN SUGERIDA (power_mult):")
        for e in sorted(d["enemies"], key=lambda x: x["level"]):
            if e["kind"] in ("story", "boss", "secret", "elite"):
                print(f"  {e['id']:20} N={e['level']:>3} {e['kind']:7} -> {calibrate(e, d)}")

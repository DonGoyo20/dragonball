"""Simulador de duelos de diseño (1 acción por segundo por combatiente)."""
import math
import random
from dataclasses import dataclass, field

import params as P


@dataclass
class Fighter:
    race: str
    ups: tuple                 # mejoras por atributo, orden P.ATTRS
    use_kaioken: bool = True
    name: str = ""
    techniques: dict = None      # si es None, usa P.TECHNIQUES (jugador)
    extra_mult: float = 1.0      # calibración de jefes: multiplica todos los E
    hp: float = 1.0
    ki: float = 0.0
    kaio_grade: int = 0
    _t: int = field(init=False, default=0)

    def __post_init__(self):
        self._t = int(sum(self.ups))
        self.ki = self.ki_pool

    # -- atributos
    def value(self, attr):
        i = P.ATTRS.index(attr)
        r = P.RACES[self.race]
        return min(P.ATTR_CAP, r["base"][i] + r["growth"][i] * self.ups[i])

    @property
    def level(self):
        return 1 + self._t

    @property
    def ki_pool(self):
        return P.KI_POOL_BASE + P.KI_POOL_PER_POINT * self.value("KI")

    def meets(self, req):
        return all(self.value(a) >= v for a, v in req.items())

    def form_mult(self):
        if not self.kaio_grade:
            return 1.0
        return P.KAIOKEN[self.kaio_grade - 1]["mult"]

    def E(self, attr):
        return (self.value(attr) ** P.VALUE_EXP) * (P.GU ** self._t) * self.form_mult() * self.extra_mult

    @property
    def np(self):
        return sum(self.E(a) for a in P.ATTRS)

    def best_kaioken_grade(self):
        best = 0
        for g in P.KAIOKEN:
            if self.meets(g["req"]):
                best = g["grade"]
        return best


def hit_fraction(att, defn, tech):
    A = att.E(tech["attr"]) * tech["mult"]
    if tech["kind"] == "phys":
        D = defn.E("DEF") ** 0.7 * defn.E("VIT") ** 0.3
    else:
        D = defn.E("DEF") ** 0.4 * defn.E("CON") ** 0.3 * defn.E("VIT") ** 0.3
    ratio = min(max(A / D, 1e-12) ** P.DMG_EXPONENT, P.RATIO_MAX)
    return min(P.HIT_FRACTION * max(ratio, P.RATIO_MIN), 1.0)


def choose_tech(f, target):
    techs = f.techniques or P.TECHNIQUES
    melee = techs.get("melee", P.TECHNIQUES["melee"])
    best, best_dmg = "melee", hit_fraction(f, target, melee)
    for name, t in techs.items():
        if name == "melee" or not f.meets(t.get("req", {})):
            continue
        cost = t["cost"] * P.ki_cost_multiplier(f.value("CON"))
        if f.ki < cost:
            continue
        d = hit_fraction(f, target, t)
        if d > best_dmg:
            best, best_dmg = name, d
    return best


def tick(a, b, rng):
    """a ataca a b una vez."""
    name = choose_tech(a, b)
    t = (a.techniques or P.TECHNIQUES).get(name) or P.TECHNIQUES[name]
    a.ki -= t["cost"] * P.ki_cost_multiplier(a.value("CON"))
    hit = rng.random() >= P.dodge_probability(b.E("VEL"), a.E("VEL"))
    if hit:
        b.hp -= hit_fraction(a, b, t)
    if t.get("self_destruct"):
        a.hp = 0.0


def upkeep(f):
    f.ki = min(f.ki + P.KI_REGEN_COMBAT_PCT * f.ki_pool, f.ki_pool)
    if f.use_kaioken:
        if f.kaio_grade == 0 and f.hp > P.KAIOKEN_SAFETY_HP * 2:
            f.kaio_grade = f.best_kaioken_grade()
        if f.kaio_grade:
            g = f.kaio_grade
            f.hp -= P.KAIOKEN_HP_BASE * g * g * 50 / (50 + f.value("VIT"))
            f.ki -= P.KAIOKEN_KI_PER_S * P.ki_cost_multiplier(f.value("CON"))
            if f.hp < P.KAIOKEN_SAFETY_HP or f.ki <= 0:
                f.kaio_grade = 0


def action_rate(x, y):
    if P.ATTACK_RATE_EXP == 0.0:
        return 1.0
    r = (x.E("VEL") / y.E("VEL")) ** P.ATTACK_RATE_EXP
    return min(max(r, P.ATTACK_RATE_MIN), P.ATTACK_RATE_MAX)


def duel(a, b, rng, max_ticks=900):
    acc = {id(a): 0.0, id(b): 0.0}
    for s in range(max_ticks):
        # quién actúa primero en cada segundo es aleatorio: sin ventaja de orden
        order = ((a, b), (b, a)) if rng.random() < 0.5 else ((b, a), (a, b))
        for x, y in order:
            upkeep(x)
            acc[id(x)] += action_rate(x, y)
            while acc[id(x)] >= 1.0:
                acc[id(x)] -= 1.0
                tick(x, y, rng)
                if y.hp <= 0:
                    return x
                if x.hp <= 0:
                    return y
    return None


def win_rate(make_a, make_b, n=1000, seed=7):
    rng = random.Random(seed)
    wa = wb = 0
    for _ in range(n):
        a, b = make_a(), make_b()
        w = duel(a, b, rng)
        if w is a:
            wa += 1
        elif w is b:
            wb += 1
    return wa / n, wb / n, 1 - (wa + wb) / n


# ---- builds de referencia a nivel total 171 (170 mejoras)
EXTREME_BUILDS = {
    "Equilibrado": (24.3,) * 7,
    "Guerrero": (30, 60, 35, 45, 0, 0, 0),
    "Maestro de ki": (20, 0, 0, 0, 50, 55, 45),
}
# especializados pero con algo de supervivencia (como el vigor en Elden Ring)
BUILDS = {
    "Equilibrado": (24.3,) * 7,
    "Guerrero": (35, 55, 40, 40, 0, 0, 0),
    "Maestro de ki": (30, 0, 20, 0, 35, 45, 40),
}


def fighters():
    out = {}
    for race in P.PLAYABLE_RACES:
        for bn, ups in BUILDS.items():
            out[f"{race[:3]}-{bn}"] = (race, ups)
    return out


def gate_tax(race, attr, req_value, mejoras_puerta=70):
    """Mejoras necesarias para llegar a un valor de atributo, y % de las de la puerta."""
    i = P.ATTRS.index(attr)
    r = P.RACES[race]
    need = max(0, math.ceil((req_value - r["base"][i]) / r["growth"][i]))
    return need, need / mejoras_puerta * 100


if __name__ == "__main__":
    F = fighters()
    names = list(F)
    print("NP a N=171:")
    for n in names:
        race, ups = F[n]
        f = Fighter(race, ups)
        print(f"  {n:24} NP={f.np:>9.3g} valores={[round(f.value(a)) for a in P.ATTRS]} Kaio-ken máx. grado={f.best_kaioken_grade()}")
    print("\nVictorias de FILA contra COLUMNA (%, 1000 duelos; sin empates en la tabla):")
    print(" " * 24 + "".join(f"{n[:11]:>12}" for n in names))
    for a in names:
        row = []
        for b in names:
            if a == b:
                row.append("      --    ")
                continue
            wa, wb, d = win_rate(lambda a=a: Fighter(*F[a]), lambda b=b: Fighter(*F[b]))
            row.append(f"{wa*100:>11.0f}%")
        print(f"{a:24}" + "".join(row))
    print("\nImpuesto de la puerta 1 (70 mejoras) para Kaio-ken grado 1, VIT >= 16:")
    for race in P.PLAYABLE_RACES:
        need, pct = gate_tax(race, "VIT", 16)
        print(f"  {race:9} {need} mejoras = {pct:.1f}%  ({'cumple' if pct <= 15 else 'NO cumple'} la regla del 15 %)")

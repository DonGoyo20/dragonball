"""¿Qué palancas hacen que las especializaciones valgan la pena? (diseño actual: exp. daño 0.75, valor lineal)"""
import itertools
import params as P, sim
from sweep import cls, CLASSES

BASE_TECH = dict(P.TECHNIQUES_CORE)

def run(builds, n=250):
    F = {f"{r[:3]}-{bn}": (r, ups) for r in P.PLAYABLE_RACES for bn, ups in builds.items()}
    score = {c: [] for c in builds}
    for a, b in itertools.permutations(F, 2):
        if cls(a) == cls(b): continue
        wa, wb, d = sim.win_rate(lambda a=a: sim.Fighter(*F[a]), lambda b=b: sim.Fighter(*F[b]), n=n)
        score[cls(a)].append(wa)
    return {c: sum(v) / len(v) * 100 for c, v in score.items()}

def show(title, r):
    print(f"{title:62} " + " / ".join(f"{r[c]:>4.0f}" for c in r))

extreme = sim.EXTREME_BUILDS
# builds especializados pero con algo de supervivencia (como en Elden Ring: vigor siempre)
sensible = sim.BUILDS
print(f"{'escenario':62} Equilibrado / Guerrero / Maestro de ki  (% victorias medias)")
for rate in (0.0, 0.3):
    for spec in (False, True):
        for bname, b in (("extremos", extreme), ("con supervivencia", sensible)):
            P.ATTACK_RATE_EXP = rate
            P.TECHNIQUES.clear(); P.TECHNIQUES.update(BASE_TECH)
            if spec: P.TECHNIQUES.update(P.SPECIALIST_TECHNIQUES)
            show(f"velocidad->acciones {rate} | téc. de especialista {'sí' if spec else 'no'} | {bname}", run(b))

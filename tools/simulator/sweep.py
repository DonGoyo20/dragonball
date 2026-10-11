"""Barrido de parámetros: ¿qué hace que las especializaciones valgan la pena?"""
import itertools, sys
import params as P, sim

F = sim.fighters()
CLASSES = ("Equilibrado", "Guerrero", "Maestro de ki")

def cls(n): return n.split("-", 1)[1]

def run(dmg_exp, val_exp, n=250):
    P.DMG_EXPONENT, P.VALUE_EXP = dmg_exp, val_exp
    score = {c: [] for c in CLASSES}
    for a, b in itertools.permutations(F, 2):
        if cls(a) == cls(b):
            continue
        wa, wb, d = sim.win_rate(lambda a=a: sim.Fighter(*F[a]), lambda b=b: sim.Fighter(*F[b]), n=n)
        score[cls(a)].append(wa)
    return {c: sum(v) / len(v) * 100 for c, v in score.items()}

if __name__ == "__main__":
    print("exp.daño  exp.valor | %victorias medias vs otras clases: Equilibrado / Guerrero / Maestro de ki")
    for de, ve in itertools.product((0.75, 1.0, 1.25), (1.0, 1.3, 1.6)):
        r = run(de, ve)
        print(f"  {de:<7}  {ve:<8} | {r['Equilibrado']:>5.0f} / {r['Guerrero']:>5.0f} / {r['Maestro de ki']:>5.0f}")

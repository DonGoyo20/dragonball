"""Parámetros del simulador de diseño. Espejo de SYSTEMS.md y ABILITIES.md.
Prototipo de diseño en Python: NO es código del juego (el juego será Java)."""
import math

ATTRS = ("VIT", "FUE", "DEF", "VEL", "KI", "POD", "CON")

# Factor de poder por mejora: +21,6 % cada 5 mejoras (SYSTEMS §2.2)
GU = 1.2159 ** 0.2
ATTR_CAP = 99
# Exponente de convexidad del valor de atributo en el poder efectivo (1.0 = lineal)
VALUE_EXP = 1.0

# base y crecimiento por mejora, en el orden de ATTRS (RACES.md)
RACES = {
    "saiyajin": {"base": (8, 10, 6, 8, 5, 7, 4), "growth": (1.0, 1.25, 1.0, 1.1, 0.9, 1.15, 0.85)},
    "humano":   {"base": (6, 6, 5, 6, 5, 5, 7),  "growth": (1.1, 1.0, 1.0, 1.1, 1.0, 1.0, 1.3)},
}

# Combate (SYSTEMS §3)
HIT_FRACTION = 0.05
DMG_EXPONENT = 0.75
RATIO_MIN, RATIO_MAX = 0.001, 200.0

# Ki (SYSTEMS §4): reserva absoluta; los costes de técnica son absolutos
KI_POOL_BASE, KI_POOL_PER_POINT = 50, 10
KI_REGEN_COMBAT_PCT = 0.001  # 0,1 %/s de la reserva en combate

# Técnicas (ABILITIES §5). cost = ki absoluto
TECHNIQUES_CORE = {
    "melee":      {"kind": "phys", "attr": "FUE", "mult": 1.0, "cost": 0,   "req": {}},
    "ki_blast":   {"kind": "ki",   "attr": "POD", "mult": 1.5, "cost": 4,   "req": {}},
    "kamehameha": {"kind": "ki",   "attr": "POD", "mult": 3.0, "cost": 120, "req": {"CON": 20, "POD": 20}},
}

# Kaio-ken (ABILITIES §7): multiplicador real, requisitos de uso
KAIOKEN = [
    {"grade": 1, "mult": 1.6, "req": {"VIT": 16}},
    {"grade": 2, "mult": 2.0, "req": {"VIT": 30, "CON": 20}},
    {"grade": 3, "mult": 2.5, "req": {"VIT": 45, "CON": 30}},
]
KAIOKEN_HP_BASE = 0.005   # fracción de vida por segundo, ×grado² ×50/(50+VIT)
KAIOKEN_KI_PER_S = 1.5
KAIOKEN_SAFETY_HP = 0.10  # se desactiva solo bajo el 10 % de vida


def ki_cost_multiplier(con_value):
    return 1.0 / (1.0 + con_value / 60.0)


def dodge_probability(vel_def, vel_att):
    return min(max(0.10 + 0.20 * math.log(vel_def / vel_att), 0.0), 0.6)


# Palanca de diseño (a decidir): la velocidad también da más acciones por segundo.
# 0.0 = sin efecto (diseño actual). Valores típicos a probar: 0.3
ATTACK_RATE_EXP = 0.0
ATTACK_RATE_MIN, ATTACK_RATE_MAX = 0.5, 3.0

# Técnicas de especialista, con requisitos altos. El simulador demostró que SIN ellas
# el personaje equilibrado gana 77-97 % de los duelos (ver README.md).
SPECIALIST_TECHNIQUES = {
    "combo_furioso": {"kind": "phys", "attr": "FUE", "mult": 3.0, "cost": 30,  "req": {"FUE": 55, "VEL": 45}},
    "makankosappo":  {"kind": "ki",   "attr": "POD", "mult": 5.0, "cost": 200, "req": {"POD": 45, "CON": 35}},
}

TECHNIQUES = {**TECHNIQUES_CORE, **SPECIALIST_TECHNIQUES}

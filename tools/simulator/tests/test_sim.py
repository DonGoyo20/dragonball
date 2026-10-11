import math
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import params as P  # noqa: E402
import sim  # noqa: E402


class TestFormulas(unittest.TestCase):
    def test_np_balanced_saiyan_level_171(self):
        f = sim.Fighter("saiyajin", (24.3,) * 7)
        self.assertEqual(f.level, 171)
        self.assertAlmostEqual(f.np, 1.73e5, delta=0.03e5)

    def test_attribute_cap_99(self):
        f = sim.Fighter("saiyajin", (500, 500, 500, 500, 500, 500, 500))
        for a in P.ATTRS:
            self.assertLessEqual(f.value(a), 99)

    def test_dodge_equal_speed_is_10_percent(self):
        self.assertAlmostEqual(P.dodge_probability(100, 100), 0.10)

    def test_dodge_is_capped(self):
        self.assertEqual(P.dodge_probability(1e9, 1), 0.6)
        self.assertEqual(P.dodge_probability(1, 1e9), 0.0)

    def test_more_strength_more_damage(self):
        a = sim.Fighter("saiyajin", (10, 10, 10, 10, 10, 10, 10))
        b = sim.Fighter("saiyajin", (10, 40, 10, 10, 10, 10, 10))
        d = sim.Fighter("saiyajin", (10,) * 7)
        melee = P.TECHNIQUES["melee"]
        self.assertGreater(sim.hit_fraction(b, d, melee), sim.hit_fraction(a, d, melee))

    def test_hit_never_exceeds_full_health(self):
        a = sim.Fighter("saiyajin", (0, 90, 0, 0, 0, 0, 0))
        d = sim.Fighter("humano", (0,) * 7)
        self.assertLessEqual(sim.hit_fraction(a, d, P.TECHNIQUES["melee"]), 1.0)

    def test_requirements_block_use_not_ownership(self):
        warrior = sim.Fighter("saiyajin", (35, 55, 40, 40, 0, 0, 0))
        self.assertFalse(warrior.meets(P.TECHNIQUES["kamehameha"]["req"]))
        self.assertTrue(warrior.meets(P.TECHNIQUES["combo_furioso"]["req"]))


class TestGateAccessibility(unittest.TestCase):
    """Regla de ABILITIES §4: la forma de cada puerta cuesta <= 15 % de las mejoras de la puerta."""

    def test_kaioken_grade1_all_playable_races(self):
        req = P.KAIOKEN[0]["req"]
        for race in P.PLAYABLE_RACES:
            for attr, value in req.items():
                need, pct = sim.gate_tax(race, attr, value)
                self.assertLessEqual(pct, 15.0, f"{race} {attr}>={value}: {pct:.1f}%")

    def test_original_vit_18_would_have_failed_for_humans(self):
        # Documenta el fallo que encontró el simulador: VIT 18 costaba 15,7 % al Humano
        need, pct = sim.gate_tax("humano", "VIT", 18)
        self.assertGreater(pct, 15.0)


class TestDuels(unittest.TestCase):
    def test_no_first_mover_bias_mirror_match(self):
        mk = lambda: sim.Fighter("saiyajin", sim.BUILDS["Equilibrado"])
        wa, wb, draw = sim.win_rate(mk, mk, n=1500, seed=11)
        self.assertAlmostEqual(wa, wb, delta=0.06)

    def test_matchup_is_symmetric(self):
        a = lambda: sim.Fighter("saiyajin", sim.BUILDS["Guerrero"])
        b = lambda: sim.Fighter("humano", sim.BUILDS["Maestro de ki"])
        wa1, wb1, d1 = sim.win_rate(a, b, n=1500, seed=5)
        wa2, wb2, d2 = sim.win_rate(b, a, n=1500, seed=6)
        self.assertAlmostEqual(wa1, wb2, delta=0.06)  # A gana igual de frecuente haya sido fila o columna

    def test_duel_is_deterministic_with_seed(self):
        a = lambda: sim.Fighter("saiyajin", sim.BUILDS["Guerrero"])
        b = lambda: sim.Fighter("humano", sim.BUILDS["Maestro de ki"])
        self.assertEqual(sim.win_rate(a, b, n=50, seed=3), sim.win_rate(a, b, n=50, seed=3))

    def test_kaioken_drains_health(self):
        f = sim.Fighter("saiyajin", (40, 20, 20, 20, 20, 20, 20))
        f.kaio_grade = 1
        before = f.hp
        sim.upkeep(f)
        self.assertLess(f.hp, before)


if __name__ == "__main__":
    unittest.main()

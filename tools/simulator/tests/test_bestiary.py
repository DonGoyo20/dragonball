import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import bestiary as B  # noqa: E402

DATA = B.load()


class TestBestiaryData(unittest.TestCase):
    def test_data_validates(self):
        self.assertEqual(B.validate(DATA), [])

    def test_archetype_shares_sum_to_one(self):
        for name, shares in DATA["archetypes"].items():
            self.assertAlmostEqual(sum(shares.values()), 1.0, delta=0.002, msg=name)

    def test_guide_xp_matches_systems_table(self):
        self.assertAlmostEqual(B.guide_xp(1), 4.8, delta=0.1)
        self.assertAlmostEqual(B.guide_xp(171), 537, delta=5)

    def test_cumulative_xp_matches_systems_table(self):
        # la tabla de SYSTEMS §2.1 incluye el coste de la siguiente mejora
        total = B.cumulative_xp(175) + B.cost_next_upgrade(175)
        self.assertAlmostEqual(total, 212427, delta=0.005 * 212427)

    def test_boss_xp_is_percent_of_cumulative(self):
        vegeta = next(e for e in DATA["enemies"] if e["id"] == "vegeta")
        self.assertEqual(B.xp_for(vegeta), round(0.15 * B.cumulative_xp(171)))

    def test_normal_enemy_xp_never_decreases_with_level(self):
        normals = sorted((e for e in DATA["enemies"] if e["kind"] == "normal"), key=lambda e: e["level"])
        xps = [B.xp_for(e) for e in normals]
        self.assertEqual(xps, sorted(xps))

    def test_story_bosses_are_ordered_by_level(self):
        story = sorted((e for e in DATA["enemies"] if e["kind"] == "story"), key=lambda e: e["level"])
        self.assertEqual([e["id"] for e in story], ["raditz", "nappa", "vegeta"])


class TestStoryGuaranteeForBestiary(unittest.TestCase):
    """Cada jefe de historia cumple la garantía (CONTENT_DESIGN §2.1) con una semilla distinta a la de calibración."""

    def test_story_bosses_are_beatable_by_every_build(self):
        for e in DATA["enemies"]:
            if e["kind"] != "story":
                continue
            wn, we, _ = B.boss_check(e, DATA, n=400, seed=321)
            self.assertGreaterEqual(wn, B.THRESHOLD, f"{e['id']}: peor build normal {wn:.0%}")
            self.assertGreaterEqual(we, B.EXTREME_THRESHOLD, f"{e['id']}: peor extremo {we:.0%}")


if __name__ == "__main__":
    unittest.main()

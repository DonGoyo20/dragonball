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
        self.assertEqual(B.xp_for(vegeta), round(B.BOSS_PCT["story"] * B.cumulative_xp(171)))

    def test_normal_enemy_xp_never_decreases_with_level(self):
        normals = sorted((e for e in DATA["enemies"] if e["kind"] == "normal"), key=lambda e: e["level"])
        xps = [B.xp_for(e) for e in normals]
        self.assertEqual(xps, sorted(xps))

    def test_story_bosses_are_ordered_by_level(self):
        story = sorted((e for e in DATA["enemies"] if e["kind"] == "story"), key=lambda e: e["level"])
        self.assertEqual([e["id"] for e in story], ["raditz", "nappa", "vegeta"])


class TestPassiveAnimalsAndBudget(unittest.TestCase):
    def test_passive_animals_die_in_about_one_hit_for_a_level_1_player(self):
        import params as P
        import sim
        for e in DATA["enemies"]:
            if e["kind"] != "passive":
                continue
            target = B.make_fighter(e, DATA)
            for race in P.PLAYABLE_RACES:
                player = sim.Fighter(race, (0,) * 7)
                hits = 1 / sim.hit_fraction(player, target, P.TECHNIQUES["melee"])
                self.assertLessEqual(hits, 2.0, f"{e['id']} / {race}: {hits:.1f} golpes")

    def test_passive_and_npc_give_no_xp(self):
        for e in DATA["enemies"]:
            if e["kind"] in ("passive", "npc"):
                self.assertEqual(B.xp_for(e), 0, e["id"])

    def test_unique_bosses_stay_within_half_the_combat_xp_budget(self):
        # Los jefes únicos no deben pagar más de la mitad del presupuesto de combate (50 % de la XP total)
        total = B.cumulative_xp(171)
        boss_xp = sum(B.xp_for(e) for e in DATA["enemies"] if e["kind"] in ("story", "boss", "secret"))
        self.assertLessEqual(boss_xp, 0.5 * (0.5 * total) + 1)

    def test_saibaman_groups_of_three(self):
        s = next(e for e in DATA["enemies"] if e["id"] == "saibaman")
        self.assertEqual(s.get("group_size"), 3)

    def test_vanilla_bosses_are_removable(self):
        for bid in ("wither", "warden", "ender_dragon"):
            e = next(x for x in DATA["enemies"] if x["id"] == bid)
            self.assertTrue(e.get("optional_removable"), bid)

    def test_named_canon_characters_declare_their_verification(self):
        for e in DATA["enemies"]:
            if e.get("named"):
                self.assertIn(e.get("canon"), ("verificado", "memoria"), e["id"])


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

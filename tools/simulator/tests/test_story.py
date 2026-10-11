"""Garantía de historia: cualquier raza y build puede avanzar (ver CONTENT_DESIGN §2.1)."""
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import story_check as S  # noqa: E402


class TestStoryGuarantee(unittest.TestCase):
    def test_normal_builds_beat_story_boss_at_equal_level(self):
        for name, w in S.table(n=600):
            self.assertGreaterEqual(w, S.THRESHOLD, f"{name}: {w:.0%}")

    def test_extreme_builds_beat_story_boss_with_a_little_extra_leveling(self):
        for name, w in S.extreme_with_extra(n=600):
            self.assertGreaterEqual(w, S.EXTREME_THRESHOLD, f"{name}: {w:.0%}")


if __name__ == "__main__":
    unittest.main()

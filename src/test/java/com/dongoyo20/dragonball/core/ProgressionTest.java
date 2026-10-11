package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Los valores esperados vienen de SYSTEMS.md §2.1 / §2.5 (calculados con el simulador de Python). */
class ProgressionTest {

    @Test
    void costOfNextUpgradeMatchesDesignTable() {
        assertEquals(10, Math.round(Progression.costNextUpgrade(1)));
        assertEquals(131, Math.round(Progression.costNextUpgrade(21)));
        assertEquals(398, Math.round(Progression.costNextUpgrade(46)));
        assertEquals(1207, Math.round(Progression.costNextUpgrade(96)));
        assertEquals(3063, Math.round(Progression.costNextUpgrade(175)));
    }

    @Test
    void cumulativeXpMatchesDesignTable() {
        // La tabla de SYSTEMS incluye el coste de la siguiente mejora
        double at175 = Progression.cumulativeXp(175) + Progression.costNextUpgrade(175);
        assertEquals(212427, at175, 212427 * 0.005);
    }

    @Test
    void levelFactorRewardsStrongerAndFadesForWeakerEnemies() {
        assertEquals(1.0, Progression.levelFactor(10, 10), 1e-9);
        assertEquals(1.04, Progression.levelFactor(1, 3), 1e-9);          // enemigo 2 niveles más fuerte
        assertEquals(1.5, Progression.levelFactor(1, 200), 1e-9);         // tope
        assertEquals(0.85, Progression.levelFactor(15, 10), 1e-9);        // 5 niveles más débil
        assertEquals(0.0, Progression.levelFactor(100, 20), 1e-9);        // muy débil: nada
    }

    @Test
    void spawnerMobsGiveQuarterXp() {
        assertEquals(100, Progression.killXp(100, 5, 5, false));
        assertEquals(25, Progression.killXp(100, 5, 5, true));
    }

    @Test
    void costGrowsMonotonically() {
        double prev = 0;
        for (int n = 1; n <= 500; n++) {
            double c = Progression.costNextUpgrade(n);
            assertTrue(c > prev, "el coste debe crecer en el nivel " + n);
            prev = c;
        }
    }
}

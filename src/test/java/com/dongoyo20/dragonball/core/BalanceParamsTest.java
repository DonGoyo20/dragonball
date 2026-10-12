package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** El balance pasó de constantes a datos: los valores por defecto deben ser exactamente los de antes. */
class BalanceParamsTest {
    private static final BalanceParams D = BalanceParams.DEFAULT;

    @Test
    void defaultsAreTheValuesFromBeforeBalanceWasConfigurable() {
        assertEquals(8.0, D.vitBase());
        assertEquals(1.0, D.vitGrowth());
        assertEquals(12.5, D.hpPerVit());
        assertEquals(3.0, D.damageExponent());
        assertEquals(0.5, D.continuousFloor());
        assertEquals(8.0, D.fueBase());
        assertEquals(1.0, D.fueGrowth());
        assertEquals(3.0, D.strengthExponent());
        assertEquals(99, D.attributeCap());
        assertEquals(91, D.vitMaxUpgrades());
        assertEquals(91, D.fueMaxUpgrades());
        assertNull(D.problem());
    }

    @Test
    void methodsWithoutParamsBehaveLikeDefaults() {
        for (int vit = 8; vit <= 99; vit += 7) {
            for (TipoDanio t : TipoDanio.values()) {
                assertEquals(HealthSystem.danioAplicado(4.0, vit, t, D), HealthSystem.danioAplicado(4.0, vit, t), 0.0);
            }
            assertEquals(HealthSystem.vidaRealMaxima(vit, D), HealthSystem.vidaRealMaxima(vit), 0.0);
            assertEquals(FuerzaSystem.multiplicadorFuerza(vit, D), FuerzaSystem.multiplicadorFuerza(vit), 0.0);
        }
        assertEquals(12, HealthSystem.vitValue(4, D) + 0);
        assertEquals(HealthSystem.VIT_MAX_UPGRADES, D.vitMaxUpgrades());
        assertEquals(FuerzaSystem.FUE_MAX_UPGRADES, D.fueMaxUpgrades());
    }

    @Test
    void customParamsChangeTheResults() {
        BalanceParams gentler = new BalanceParams(8, 1, 12.5, 2.0, 0.25, 8, 1, 2.0, 99);
        assertNull(gentler.problem());
        // exponente 2 en vez de 3: 3 × (8/18)^2 = 0,593
        assertEquals(3.0 * Math.pow(8.0 / 18.0, 2), HealthSystem.danioAplicado(3.0, 18, TipoDanio.ESCALA_SIN_SUELO, gentler), 1e-9);
        // suelo 0,25
        assertEquals(0.25, HealthSystem.danioAplicado(4.0, 99, TipoDanio.ESCALA_CON_SUELO, gentler), 1e-9);
        // FUE con exponente 2: (26/8)^2
        assertEquals(Math.pow(26.0 / 8.0, 2), FuerzaSystem.multiplicadorFuerza(26, gentler), 1e-9);
    }

    @Test
    void capAndGrowthDeriveTheMaxUpgrades() {
        BalanceParams p = new BalanceParams(10, 2.0, 12.5, 3.0, 0.5, 8, 0.5, 3.0, 50);
        assertNull(p.problem());
        assertEquals(20, p.vitMaxUpgrades());     // (50-10)/2
        assertEquals(84, p.fueMaxUpgrades());     // (50-8)/0.5
        assertEquals(50, HealthSystem.vitValue(20, p));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vitValue(21, p));
        assertTrue(HealthSystem.canUpgradeVit(19, p));
        assertTrue(!HealthSystem.canUpgradeVit(20, p));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vidaRealMaxima(51, p));
    }

    @Test
    void invalidParamsAreReportedNotThrown() {
        assertNotNull(new BalanceParams(0, 1, 12.5, 3, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 0, 12.5, 3, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 1, -1, 3, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 0.1, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 9, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, -0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 7, 99).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 1).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 8).problem());   // el tope no deja ninguna mejora
        assertNotNull(new BalanceParams(Double.NaN, 1, 12.5, 3, 0.5, 8, 1, 3, 99).problem());
        assertNotNull(new BalanceParams(8, Double.POSITIVE_INFINITY, 12.5, 3, 0.5, 8, 1, 3, 99).problem());
    }

    @Test
    void kiAndReleaseDefaultsAreValidAndTheShortConstructorUsesThem() {
        assertNull(D.ki().problem());
        assertNull(D.release().problem());
        assertEquals(KiParams.DEFAULT, D.ki());
        assertEquals(ReleaseParams.DEFAULT, D.release());
        assertEquals(D, new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99));
        assertEquals(50.0, D.release().initialLimit());
        assertEquals(50.0, D.release().referencePercent());
    }

    @Test
    void invalidKiAndReleaseParamsAreReported() {
        BalanceParams ok = D;
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, new KiParams(0, 1, 50, 10, 0, 1, 0, 0.2, 2, 50), ok.release()).problem());   // con_scale 0
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, new KiParams(0, 1, 50, 10, 0, 1, 60, -1, 2, 50), ok.release()).problem());  // coste negativo
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, new KiParams(0, 1, 50, 10, 0, 1, 60, 0.2, 200, 50), ok.release()).problem()); // regen > 100 %
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, new KiParams(0, 1, 0, 0, 0, 1, 60, 0.2, 2, 50), ok.release()).problem());     // reserva 0
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, ok.ki(), new ReleaseParams(120, 50, 10, 20, 8, 1, 0.5, 2.5, 0.25, 1.8)).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, ok.ki(), new ReleaseParams(50, 0, 10, 20, 8, 1, 0.5, 2.5, 0.25, 1.8)).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, ok.ki(), new ReleaseParams(50, 50, 10, 20, 8, 1, 0.5, 0.5, 0.25, 1.8)).problem()); // speed_max < 1
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, null, ok.release()).problem());
    }

    @Test
    void defenseAndPodDefaultsAndMaxUpgrades() {
        assertEquals(8.0, D.defense().defBase());
        assertEquals(1.0, D.defense().defGrowth());
        assertEquals(3.0, D.defense().damageExponent());
        assertEquals(1.25, D.defense().multiplier());
        assertEquals(8.0, D.pod().podBase());
        assertEquals(1.0, D.pod().podGrowth());
        assertEquals(91, D.defMaxUpgrades());
        assertEquals(91, D.podMaxUpgrades());
    }

    @Test
    void dodgeDefaults() {
        assertEquals(0.10, D.dodge().baseChance());
        assertEquals(0.20, D.dodge().logMultiplier());
        assertEquals(0.60, D.dodge().maxChance());
        assertEquals(5.0, D.dodge().kiCostBase());
        assertEquals(0.1, D.dodge().kiCostPerDamage());
        assertEquals(60.0, D.dodge().conReduction());
    }
}

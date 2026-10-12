package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** DEF (defensa) y POD (poder de ki): valores, tope, reducción de daño y validación de los datos. */
class DefensaSystemTest {
    private static final BalanceParams P = BalanceParams.DEFAULT;

    @Test
    void defValueGrowsOnePerUpgradeFromEightUpToNinetyNine() {
        assertEquals(8, DefensaSystem.defValue(0, P));
        assertEquals(18, DefensaSystem.defValue(10, P));
        assertEquals(99, DefensaSystem.defValue(91, P));
        assertEquals(91, P.defMaxUpgrades());
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.defValue(92, P));
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.defValue(-1, P));
        assertTrue(DefensaSystem.canUpgradeDef(90, P));
        assertFalse(DefensaSystem.canUpgradeDef(91, P));
    }

    @Test
    void podValueGrowsOnePerUpgradeFromEightUpToNinetyNine() {
        assertEquals(8, KiSystem.podValue(0, P));
        assertEquals(20, KiSystem.podValue(12, P));
        assertEquals(99, KiSystem.podValue(91, P));
        assertEquals(91, P.podMaxUpgrades());
        assertThrows(IllegalArgumentException.class, () -> KiSystem.podValue(92, P));
    }

    @Test
    void defOfEightMeansNoExtraReduction() {
        assertEquals(1.0, DefensaSystem.multiplicador(8, 50, P), 1e-12);
        assertEquals(10.0, DefensaSystem.danioTrasDefensa(10.0, 8, 50, P), 1e-12);
    }

    @Test
    void defReducesDamageCubically() {
        assertEquals(Math.pow(8.0 / 18.0, 3), DefensaSystem.multiplicador(18, 50, P), 1e-12);   // ×0,088
        assertEquals(Math.pow(8.0 / 26.0, 3), DefensaSystem.multiplicador(26, 50, P), 1e-12);
        assertEquals(10.0 * Math.pow(8.0 / 99.0, 3), DefensaSystem.danioTrasDefensa(10.0, 99, 50, P), 1e-9);
    }

    @Test
    void defFollowsThePiecewiseRelease() {
        // 0 %: vanilla para cualquier DEF
        for (int def : new int[] {8, 18, 99}) assertEquals(1.0, DefensaSystem.multiplicador(def, 0, P), 1e-12, "DEF " + def);
        // 25 %: DEF 18 → efectiva 13
        assertEquals(Math.pow(8.0 / 13.0, 3), DefensaSystem.multiplicador(18, 25, P), 1e-12);
        // 100 %: DEF 18 → efectiva 36; DEF 8 → 16
        assertEquals(Math.pow(8.0 / 36.0, 3), DefensaSystem.multiplicador(18, 100, P), 1e-12);
        assertEquals(0.125, DefensaSystem.multiplicador(8, 100, P), 1e-12);
    }

    @Test
    void defAndVitStack() {
        double vit = HealthSystem.danioAplicadoLiberado(3.0, 18, 50, TipoDanio.ESCALA_SIN_SUELO, P);
        double both = vit * DefensaSystem.multiplicador(18, 50, P);
        assertEquals(3.0 * Math.pow(8.0 / 18.0, 6), both, 1e-9);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.multiplicador(7, 50, P));
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.multiplicador(100, 50, P));
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.multiplicador(18, 101, P));
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.multiplicadorEfectivo(0, P));
        assertThrows(IllegalArgumentException.class, () -> DefensaSystem.danioTrasDefensa(-1, 8, 50, P));
    }

    @Test
    void defAndPodParamsAreValidated() {
        assertNull(P.problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                new DefenseParams(0, 1, 3), PodParams.DEFAULT).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                new DefenseParams(8, 0, 3), PodParams.DEFAULT).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                new DefenseParams(8, 1, 9), PodParams.DEFAULT).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                DefenseParams.DEFAULT, new PodParams(-1, 1)).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                DefenseParams.DEFAULT, new PodParams(8, 0)).problem());
        assertNotNull(new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT, null, PodParams.DEFAULT).problem());
    }
}

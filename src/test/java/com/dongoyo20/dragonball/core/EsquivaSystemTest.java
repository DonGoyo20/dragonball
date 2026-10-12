package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/** Esquiva: probabilidad por VEL efectiva, coste en ki, tirada y validación de los datos. */
class EsquivaSystemTest {
    private static final BalanceParams P = BalanceParams.DEFAULT;

    @Test
    void chanceAtTheDocumentedPoints() {
        assertEquals(0.10, EsquivaSystem.probabilidadEsquiva(8), 1e-12);
        assertEquals(0.262, EsquivaSystem.probabilidadEsquiva(18), 1e-3);     // 0,10 + 0,20 × ln(2,25)
        assertEquals(0.4008, EsquivaSystem.probabilidadEsquiva(36), 1e-3);     // 0,10 + 0,20 × ln(4,5)
        assertEquals(0.60, EsquivaSystem.probabilidadEsquiva(99), 1e-12);     // tope (la fórmula daría 0,603)
    }

    @Test
    void chanceHasAFloorOfTenPercentForLowVel() {
        assertEquals(0.10, EsquivaSystem.probabilidadEsquiva(4), 1e-12);
        assertEquals(0.10, EsquivaSystem.probabilidadEsquiva(0), 1e-12);
        assertEquals(0.10, EsquivaSystem.probabilidadEsquiva(7.99), 1e-12);
    }

    @Test
    void chanceIsMonotonicAndBounded() {
        double prev = -1;
        for (double vel = 0; vel <= 300; vel += 0.5) {
            double c = EsquivaSystem.probabilidadEsquiva(vel);
            assertTrue(c >= prev - 1e-12 && c >= 0.10 - 1e-12 && c <= 0.60 + 1e-12, "VEL " + vel + " → " + c);
            prev = c;
        }
    }

    @Test
    void chanceFollowsThePiecewiseRelease() {
        // VEL 18: 0 % → efectiva 8 (10 %); 50 % → 18; 100 % → 36
        assertEquals(0.10, EsquivaSystem.probabilidadEsquiva(KiSystem.statEfectiva(18, 8, 0, P), P), 1e-12);
        assertEquals(0.262, EsquivaSystem.probabilidadEsquiva(KiSystem.statEfectiva(18, 8, 50, P), P), 1e-3);
        assertEquals(0.4008, EsquivaSystem.probabilidadEsquiva(KiSystem.statEfectiva(18, 8, 100, P), P), 1e-3);
        // VEL 8 (personaje nuevo): 100 % → efectiva 16
        assertEquals(0.10 + 0.20 * Math.log(2.0), EsquivaSystem.probabilidadEsquiva(KiSystem.statEfectiva(8, 8, 100, P), P), 1e-9);
    }

    @Test
    void kiCostMatchesTheDesign() {
        assertEquals(5, EsquivaSystem.costeKiEsquiva(3, 0));          // 5,3 → 5
        assertEquals(8, EsquivaSystem.costeKiEsquiva(30, 0));         // 5 + 3
        assertEquals(4, EsquivaSystem.costeKiEsquiva(30, 60));        // 8 / (1 + 60/60)
        assertEquals(5, EsquivaSystem.costeKiEsquiva(0, 0));
        assertEquals(1, EsquivaSystem.costeKiEsquiva(0, 99_000));     // nunca gratis
    }

    @Test
    void kiCostDropsWithConAndGrowsWithDamage() {
        int prev = Integer.MAX_VALUE;
        for (int con = 0; con <= 99; con += 9) {
            int c = EsquivaSystem.costeKiEsquiva(30, con);
            assertTrue(c <= prev, "CON " + con);
            prev = c;
        }
        assertTrue(EsquivaSystem.costeKiEsquiva(100, 0) > EsquivaSystem.costeKiEsquiva(10, 0));
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.probabilidadEsquiva(-1));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.probabilidadEsquiva(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.probabilidadEsquiva(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.costeKiEsquiva(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.costeKiEsquiva(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.costeKiEsquiva(3, -1));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.sale(1.5, 0.5));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.sale(0.5, 1.0));
        assertThrows(IllegalArgumentException.class, () -> EsquivaSystem.sale(0.5, -0.1));
    }

    @Test
    void rollSucceedsOnlyBelowTheChance() {
        assertTrue(EsquivaSystem.sale(0.10, 0.0));
        assertTrue(EsquivaSystem.sale(0.10, 0.0999));
        assertFalse(EsquivaSystem.sale(0.10, 0.10));
        assertFalse(EsquivaSystem.sale(0.10, 0.9999));
        assertFalse(EsquivaSystem.sale(0.0, 0.0));
    }

    @Test
    void manyRollsConvergeToTheChance() {
        Random random = new Random(12345);
        for (double vel : new double[] {8, 18, 36}) {
            double chance = EsquivaSystem.probabilidadEsquiva(vel);
            int n = 200_000, hits = 0;
            for (int i = 0; i < n; i++) if (EsquivaSystem.sale(chance, random.nextDouble())) hits++;
            assertEquals(chance, hits / (double) n, 0.005, "VEL " + vel);
        }
    }

    @Test
    void dodgeParamsAreValidated() {
        assertNull(P.problem());
        assertNotNull(withDodge(new DodgeParams(-0.1, 0.2, 0.6, 5, 0.1, 60)).problem());
        assertNotNull(withDodge(new DodgeParams(0.1, -1, 0.6, 5, 0.1, 60)).problem());
        assertNotNull(withDodge(new DodgeParams(0.1, 0.2, 0.05, 5, 0.1, 60)).problem());   // tope por debajo de la base
        assertNotNull(withDodge(new DodgeParams(0.1, 0.2, 1.5, 5, 0.1, 60)).problem());
        assertNotNull(withDodge(new DodgeParams(0.1, 0.2, 0.6, -1, 0.1, 60)).problem());
        assertNotNull(withDodge(new DodgeParams(0.1, 0.2, 0.6, 5, -0.1, 60)).problem());
        assertNotNull(withDodge(new DodgeParams(0.1, 0.2, 0.6, 5, 0.1, 0)).problem());
        assertNotNull(withDodge(new DodgeParams(Double.NaN, 0.2, 0.6, 5, 0.1, 60)).problem());
        assertNotNull(withDodge(null).problem());
    }

    @Test
    void dodgeParamsAreConfigurable() {
        BalanceParams custom = withDodge(new DodgeParams(0.2, 0.1, 0.5, 10, 0.5, 30));
        assertNull(custom.problem());
        assertEquals(0.2, EsquivaSystem.probabilidadEsquiva(8, custom), 1e-12);
        assertEquals(0.5, EsquivaSystem.probabilidadEsquiva(99_999, custom), 1e-12);
        assertEquals(20, EsquivaSystem.costeKiEsquiva(20, 0, custom));                       // 10 + 20 × 0,5
        assertEquals(10, EsquivaSystem.costeKiEsquiva(20, 30, custom));                      // / (1 + 30/30)
    }

    private static BalanceParams withDodge(DodgeParams dodge) {
        return new BalanceParams(8, 1, 12.5, 3, 0.5, 8, 1, 3, 99, KiParams.DEFAULT, ReleaseParams.DEFAULT,
                DefenseParams.DEFAULT, PodParams.DEFAULT, dodge);
    }
}

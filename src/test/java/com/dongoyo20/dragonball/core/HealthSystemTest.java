package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Valores de HEALTH_FEASIBILITY.md / SYSTEMS.md §3. El daño de entorno real se prueba en el GameTest (necesita DamageSource). */
class HealthSystemTest {

    @Test
    void maxRealHealthIsLinearInVit() {
        assertEquals(100.0, HealthSystem.vidaRealMaxima(8), 1e-9);
        assertEquals(1237.5, HealthSystem.vidaRealMaxima(99), 1e-9);
        assertEquals(225.0, HealthSystem.vidaRealMaxima(18), 1e-9);
    }

    @Test
    void currentRealHealthIsDerivedFromVanillaHealth() {
        assertEquals(100.0, HealthSystem.vidaRealActual(20.0, 100.0), 1e-9);
        assertEquals(85.0, HealthSystem.vidaRealActual(17.0, 100.0), 1e-9);
        assertEquals(0.0, HealthSystem.vidaRealActual(0.0, 1237.5), 1e-9);
        // subir VIT con la barra a 17/20 mantiene la fracción y sube la vida real
        assertEquals(0.85 * 225.0, HealthSystem.vidaRealActual(17.0, 225.0), 1e-9);
    }

    @Test
    void damageIsVanillaAtBaseVitAndShrinksWithVit() {
        assertEquals(3.0, HealthSystem.danioAplicado(3.0, 8), 1e-9);
        assertEquals(1.3333, HealthSystem.danioAplicado(3.0, 18), 1e-3);
        assertEquals(0.0, HealthSystem.danioAplicado(0.0, 50), 1e-9);
        double previous = Double.MAX_VALUE;
        for (int vit = 8; vit <= 99; vit++) {
            double d = HealthSystem.danioAplicado(3.0, vit);
            assertTrue(d <= previous && d > 0, "monótono decreciente en VIT " + vit);
            previous = d;
        }
    }

    @Test
    void vitOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vidaRealMaxima(7));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vidaRealMaxima(100));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(3.0, 7));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(3.0, 100));
    }

    @Test
    void invalidDamageThrows() {
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(-1.0, 8));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(Double.NaN, 8));
    }

    @Test
    void environmentalDamageMeansNoAttacker() {
        assertTrue(HealthSystem.esDanioDeEntorno(false)); // caída, lava, ahogamiento: sin atacante
        assertFalse(HealthSystem.esDanioDeEntorno(true)); // ataque de zombi: hay atacante
    }

    @Test
    void vitCapIs99AndAllows91Upgrades() {
        assertEquals(8, HealthSystem.vitValue(0));
        assertEquals(99, HealthSystem.vitValue(91));
        assertEquals(91, HealthSystem.VIT_MAX_UPGRADES);
        assertTrue(HealthSystem.canUpgradeVit(90));
        assertFalse(HealthSystem.canUpgradeVit(91));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vitValue(92));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vitValue(-1));
    }
}

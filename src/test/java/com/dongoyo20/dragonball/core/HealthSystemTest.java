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

    private static final TipoDanio SIN = TipoDanio.ESCALA_SIN_SUELO;
    private static final TipoDanio CON = TipoDanio.ESCALA_CON_SUELO;
    private static final TipoDanio NO = TipoDanio.NO_ESCALA;

    @Test
    void pointDamageIsVanillaAtBaseVitAndShrinksCubically() {
        assertEquals(3.0, HealthSystem.danioAplicado(3.0, 8, SIN), 1e-9);
        assertEquals(0.2634, HealthSystem.danioAplicado(3.0, 18, SIN), 1e-3);     // 3 × (8/18)^3
        assertEquals(0.0569, HealthSystem.danioAplicado(3.0, 30, SIN), 1e-3);     // 3 × (8/30)^3
        assertEquals(20.25, HealthSystem.danioAplicado(20.25, 8, SIN), 1e-9);     // creeper a bocajarro, VIT base
        assertEquals(1.778, HealthSystem.danioAplicado(20.25, 18, SIN), 1e-3);
        assertEquals(17.0 * Math.pow(8.0 / 26.0, 3), HealthSystem.danioAplicado(17.0, 26, SIN), 1e-9); // caída de 20 bloques
        assertEquals(0.495, HealthSystem.danioAplicado(17.0, 26, SIN), 1e-3);
        assertTrue(HealthSystem.danioAplicado(3.0, 99, SIN) > 0 && HealthSystem.danioAplicado(3.0, 99, SIN) < 0.003,
                "VIT 99: daño muy pequeño pero no cero");
        assertEquals(0.0, HealthSystem.danioAplicado(0.0, 50, SIN), 1e-9);
    }

    @Test
    void pointDamageIsMonotonicallyDecreasingInVit() {
        double previous = Double.MAX_VALUE;
        for (int vit = 8; vit <= 99; vit++) {
            double d = HealthSystem.danioAplicado(3.0, vit, SIN);
            assertTrue(d <= previous && d > 0, "monótono decreciente en VIT " + vit);
            previous = d;
        }
    }

    @Test
    void continuousDamageKeepsAFloorOfHalfAnHp() {
        assertEquals(0.5, HealthSystem.SUELO_CONTINUO, 1e-12);
        assertEquals(4.0, HealthSystem.danioAplicado(4.0, 8, CON), 1e-9);   // lava con VIT base: sin cambio
        assertEquals(0.5, HealthSystem.danioAplicado(4.0, 26, CON), 1e-9);  // 4 × (8/26)^3 = 0,116 → suelo
        assertEquals(0.5, HealthSystem.danioAplicado(4.0, 99, CON), 1e-9);
        // por encima del suelo se comporta como la cúbica
        assertEquals(4.0 * Math.pow(8.0 / 12.0, 3), HealthSystem.danioAplicado(4.0, 12, CON), 1e-9); // 1,185
        // el suelo nunca aumenta un daño original menor que el suelo: min(0,3; max(escalado; 0,5)) = 0,3
        assertEquals(0.3, HealthSystem.danioAplicado(0.3, 99, CON), 1e-9);
        assertEquals(0.3, HealthSystem.danioAplicado(0.3, 8, CON), 1e-9);
        assertEquals(0.5, HealthSystem.danioAplicado(0.5, 99, CON), 1e-9);
        assertEquals(0.0, HealthSystem.danioAplicado(0.0, 99, CON), 1e-9);
    }

    @Test
    void unscaledDamageIsReturnedAsIsForAnyVit() {
        for (int vit : new int[] {8, 18, 26, 99}) {
            assertEquals(4.0, HealthSystem.danioAplicado(4.0, vit, NO), 1e-9, "VIT " + vit);
            assertEquals(Float.MAX_VALUE, HealthSystem.danioAplicado(Float.MAX_VALUE, vit, NO), 1e-3 * Float.MAX_VALUE);
        }
    }

    @Test
    void vitOutOfRangeThrows() {
        for (TipoDanio t : TipoDanio.values()) {
            assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(3.0, 7, t));
            assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(3.0, 100, t));
        }
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vidaRealMaxima(7));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.vidaRealMaxima(100));
    }

    @Test
    void invalidDamageThrows() {
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(-1.0, 8, SIN));
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicado(Double.NaN, 8, CON));
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

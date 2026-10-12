package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FuerzaSystemTest {

    @Test
    void multiplierIsCubicInFue() {
        assertEquals(1.0, FuerzaSystem.multiplicadorFuerza(8), 1e-9);
        assertEquals(11.39, FuerzaSystem.multiplicadorFuerza(18), 0.01);   // (18/8)^3
        assertEquals(34.33, FuerzaSystem.multiplicadorFuerza(26), 0.01);   // (26/8)^3
        // (99/8)^3 = 1895,1 (el encargo decía ~1896)
        assertEquals(1895.1, FuerzaSystem.multiplicadorFuerza(99), 0.1);
    }

    @Test
    void outgoingDamageScalesFromTheFist() {
        assertEquals(1.0, FuerzaSystem.danioSaliente(1.0, 8), 1e-9);
        assertEquals(34.33, FuerzaSystem.danioSaliente(1.0, 26), 0.01);
        assertEquals(0.0, FuerzaSystem.danioSaliente(0.0, 99), 1e-9);
        assertTrue(FuerzaSystem.danioSaliente(1.0, 26) > 20.0, "mata a un mob de 20 HP de un puñetazo");
    }

    @Test
    void multiplierIsMonotonicAndSymmetricToVit() {
        double previous = 0;
        for (int fue = 8; fue <= 99; fue++) {
            double m = FuerzaSystem.multiplicadorFuerza(fue);
            assertTrue(m > previous, "creciente en FUE " + fue);
            previous = m;
            // simetría con VIT: el multiplicador de FUE es la inversa del factor de daño recibido de VIT
            assertEquals(1.0, m * HealthSystem.danioAplicado(1.0, fue, TipoDanio.ESCALA_SIN_SUELO), 1e-9);
        }
    }

    @Test
    void fueOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.multiplicadorFuerza(7));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.multiplicadorFuerza(100));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSaliente(1.0, 7));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSaliente(1.0, 100));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSaliente(-1.0, 8));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSaliente(Double.NaN, 8));
    }

    @Test
    void fueCapIs99AndAllows91Upgrades() {
        assertEquals(8, FuerzaSystem.fueValue(0));
        assertEquals(99, FuerzaSystem.fueValue(91));
        assertEquals(91, FuerzaSystem.FUE_MAX_UPGRADES);
        assertTrue(FuerzaSystem.canUpgradeFue(90));
        assertFalse(FuerzaSystem.canUpgradeFue(91));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.fueValue(92));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.fueValue(-1));
    }

    @Test
    void meleeClassification() {
        // mano vacía → puño
        assertEquals(DanioCuerpo.PUNO, DanioCuerpo.clasificar(true, false, false));
        // objeto sin daño de ataque (un bloque, comida) → también puño
        assertEquals(DanioCuerpo.PUNO, DanioCuerpo.clasificar(false, false, false));
        // espada o hacha de vanilla → no escala
        assertEquals(DanioCuerpo.ARMA_VANILLA, DanioCuerpo.clasificar(false, false, true));
        // arma del mod (tag) → escala, aunque también tenga daño de ataque
        assertEquals(DanioCuerpo.ARMA_DEL_MOD, DanioCuerpo.clasificar(false, true, true));
        assertTrue(DanioCuerpo.PUNO.escalaConFuerza());
        assertTrue(DanioCuerpo.ARMA_DEL_MOD.escalaConFuerza());
        assertFalse(DanioCuerpo.ARMA_VANILLA.escalaConFuerza());
    }
}

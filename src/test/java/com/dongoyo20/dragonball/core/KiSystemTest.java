package com.dongoyo20.dragonball.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KiSystemTest {
    private static final BalanceParams P = BalanceParams.DEFAULT;

    @Test
    void reserveIs50PlusTenPerKi() {
        assertEquals(50, KiSystem.reservaMaxima(0));
        assertEquals(230, KiSystem.reservaMaxima(18));
        assertEquals(1040, KiSystem.reservaMaxima(99));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.reservaMaxima(-1));
    }

    @Test
    void holdingCostScalesWithReleaseAndIsReducedByControl() {
        assertEquals(0.01, KiSystem.costeLiberacion(0, 100), 1e-12);         // 0,2 ki/s a 20 ticks/s
        assertEquals(0.005, KiSystem.costeLiberacion(0, 50), 1e-12);
        assertEquals(0.0, KiSystem.costeLiberacion(0, 0), 1e-12);
        assertEquals(0.005, KiSystem.costeLiberacion(60, 100), 1e-12);       // 1 / (1 + 60/60) = 1/2
        assertEquals(0.01 / 3, KiSystem.costeLiberacion(120, 100), 1e-12);
        assertThrows(IllegalArgumentException.class, () -> KiSystem.costeLiberacion(-1, 50));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.costeLiberacion(0, 100.1));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.costeLiberacion(0, -0.1));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.costeLiberacion(0, Double.NaN));
    }

    @Test
    void regenerationOnlyBelowHalfAndMaximalAtZero() {
        assertEquals(0.0, KiSystem.regeneracionPorTick(0, 50), 0.0);
        assertEquals(0.0, KiSystem.regeneracionPorTick(0, 80), 0.0);
        assertEquals(0.05, KiSystem.regeneracionPorTick(0, 0), 1e-12);       // 2 % de 50 por segundo, a 20 ticks/s
        assertEquals(0.025, KiSystem.regeneracionPorTick(0, 25), 1e-12);     // lineal hasta el 50 %
        assertEquals(0.23, KiSystem.regeneracionPorTick(18, 0), 1e-12);      // usa el KI (reserva 230)
        assertTrue(KiSystem.regeneracionPorTick(0, 49.9) > 0.0 && KiSystem.regeneracionPorTick(0, 49.9) < 0.001);
    }

    @Test
    void effectiveStatIsBaseTimesRelease() {
        assertEquals(4.0, KiSystem.statEfectiva(8, 50), 1e-12);
        assertEquals(8.0, KiSystem.statEfectiva(8, 100), 1e-12);
        assertEquals(0.0, KiSystem.statEfectiva(8, 0), 1e-12);
        assertThrows(IllegalArgumentException.class, () -> KiSystem.statEfectiva(-1.0, 50));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.statEfectiva(8, 101));
    }

    @Test
    void combatFactorKeepsTheVerifiedBalanceAtTheInitialRelease() {
        assertEquals(1.0, KiSystem.factorLiberacion(50, P), 1e-12);
        assertEquals(2.0, KiSystem.factorLiberacion(100, P), 1e-12);
        assertEquals(0.5, KiSystem.factorLiberacion(25, P), 1e-12);
        assertEquals(0.5, KiSystem.factorLiberacion(0, P), 1e-12);           // suelo
        assertEquals(0.5, KiSystem.factorLiberacion(10, P), 1e-12);
    }

    @Test
    void releaseRisesOnlyWhenStillAndNeverPastTheLimit() {
        assertEquals(10.0, KiSystem.siguientePorcentaje(0, 50, 1, true, 20, P), 1e-9);     // 1 s = +10 %
        assertEquals(0.0, KiSystem.siguientePorcentaje(0, 50, 1, false, 20, P), 1e-9);     // en movimiento no sube
        assertEquals(50.0, KiSystem.siguientePorcentaje(40, 50, 1, true, 400, P), 1e-9);   // tope = límite
        assertEquals(30.0, KiSystem.siguientePorcentaje(0, 30, 1, true, 400, P), 1e-9);
        assertEquals(30.0, KiSystem.siguientePorcentaje(50, 30, 0, true, 0, P), 1e-9);     // el límite bajó
        assertEquals(30.0, KiSystem.siguientePorcentaje(50, 100, -1, false, 20, P), 1e-9); // bajar: -20 %/s, sin estar quieto
        assertEquals(0.0, KiSystem.siguientePorcentaje(5, 100, -1, true, 100, P), 1e-9);   // nunca negativo
        assertEquals(50.0, KiSystem.siguientePorcentaje(50, 100, 0, true, 100, P), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> KiSystem.siguientePorcentaje(50, 101, 1, true, 1, P));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.siguientePorcentaje(50, 100, 1, true, -1, P));
    }

    @Test
    void stepDrainsRegeneratesAndDepletes() {
        // 50 % con KI 0: gasta 0,005 por tick, no regenera
        KiSystem.Estado drained = KiSystem.avanzar(new KiSystem.Estado(50, 50), 0, 0, 100, P);
        assertEquals(49.5, drained.ki(), 1e-9);
        assertEquals(50.0, drained.liberacion(), 1e-9);
        // 0 %: no gasta y regenera 0,05 por tick
        KiSystem.Estado regen = KiSystem.avanzar(new KiSystem.Estado(10, 0), 0, 0, 100, P);
        assertEquals(15.0, regen.ki(), 1e-9);
        // sin ki: la liberación baja a 0
        KiSystem.Estado empty = KiSystem.avanzar(new KiSystem.Estado(0.01, 50), 0, 0, 20, P);
        assertEquals(0.0, empty.ki(), 1e-12);
        assertEquals(0.0, empty.liberacion(), 1e-12);
        // la reserva no se desborda
        assertEquals(50.0, KiSystem.avanzar(new KiSystem.Estado(49.99, 0), 0, 0, 1000, P).ki(), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> KiSystem.avanzar(new KiSystem.Estado(10, 0), 0, 0, -1, P));
    }

    @Test
    void thereIsAnEquilibriumReleaseWhereKiDoesNotRunOut() {
        // Con KI 0 y CON 0, mantener ~40 % regenera más de lo que gasta; mantener 50 % solo gasta.
        KiSystem.Estado at40 = KiSystem.avanzar(new KiSystem.Estado(25, 40), 0, 0, 200, P);
        assertTrue(at40.ki() > 25.0, "a 40 % el ki sube");
        KiSystem.Estado at50 = KiSystem.avanzar(new KiSystem.Estado(25, 50), 0, 0, 200, P);
        assertTrue(at50.ki() < 25.0, "a 50 % el ki baja");
        // más CON abarata el gasto
        assertTrue(KiSystem.avanzar(new KiSystem.Estado(25, 50), 0, 60, 200, P).ki() > at50.ki());
    }

    @Test
    void velocityFactorsAreNeutralAtTheInitialReleaseAndCapped() {
        assertEquals(1.0, KiSystem.factorVelocidad(8, 50, P), 1e-12);
        assertEquals(1.0, KiSystem.factorSalto(8, 50, P), 1e-12);
        assertEquals(Math.sqrt(2.0), KiSystem.factorVelocidad(8, 100, P), 1e-9);
        assertEquals(Math.pow(2.0, 0.25), KiSystem.factorSalto(8, 100, P), 1e-9);
        assertEquals(2.5, KiSystem.factorVelocidad(26, 100, P), 1e-9);       // tope de velocidad
        assertEquals(1.8, KiSystem.factorSalto(99, 100, P), 1e-9);           // tope de salto
        assertEquals(Math.sqrt(0.5), KiSystem.factorVelocidad(8, 0, P), 1e-9);
        assertTrue(KiSystem.factorVelocidad(1, 0, P) >= 0.5, "nunca por debajo del 0,5");
        assertThrows(IllegalArgumentException.class, () -> KiSystem.factorVelocidad(0, 50, P));
    }

    @Test
    void attributeValuesFollowTheirBaseAndGrowth() {
        assertEquals(0, KiSystem.kiValue(0, P));
        assertEquals(18, KiSystem.kiValue(18, P));
        assertEquals(99, KiSystem.kiValue(99, P));
        assertEquals(8, KiSystem.velValue(0, P));
        assertEquals(99, KiSystem.velValue(91, P));
        assertEquals(0, KiSystem.conValue(0, P));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.kiValue(100, P));
        assertThrows(IllegalArgumentException.class, () -> KiSystem.velValue(92, P));
        assertEquals(99, P.kiMaxUpgrades());
        assertEquals(91, P.velMaxUpgrades());
        assertEquals(99, P.conMaxUpgrades());
    }

    @Test
    void effectiveCombatStatsUseTheNewOverloads() {
        // VIT efectiva 16 (liberación 100 %): el golpe de 3 HP se reduce a 3 × (8/16)^3
        assertEquals(0.375, HealthSystem.danioAplicadoEfectivo(3.0, 16, TipoDanio.ESCALA_SIN_SUELO, P), 1e-9);
        // VIT efectiva 4 (liberación 0 % con el suelo del factor): ×8
        assertEquals(24.0, HealthSystem.danioAplicadoEfectivo(3.0, 4, TipoDanio.ESCALA_SIN_SUELO, P), 1e-9);
        // el suelo del daño continuo sigue valiendo
        assertEquals(0.5, HealthSystem.danioAplicadoEfectivo(4.0, 40, TipoDanio.ESCALA_CON_SUELO, P), 1e-9);
        assertEquals(4.0, HealthSystem.danioAplicadoEfectivo(4.0, 1, TipoDanio.NO_ESCALA, P), 1e-9);
        // FUE efectiva 16: ×8
        assertEquals(8.0, FuerzaSystem.danioSalienteEfectivo(1.0, 16, P), 1e-9);
        assertEquals(0.125, FuerzaSystem.danioSalienteEfectivo(1.0, 4, P), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> HealthSystem.danioAplicadoEfectivo(3.0, 0, TipoDanio.ESCALA_SIN_SUELO, P));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSalienteEfectivo(1.0, -1, P));
        assertThrows(IllegalArgumentException.class, () -> FuerzaSystem.danioSalienteEfectivo(1.0, Double.POSITIVE_INFINITY, P));
    }
}

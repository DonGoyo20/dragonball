package com.dongoyo20.dragonball.core.raza;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.KiSystem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Razas como datos puros: validación, valores iniciales, tope por raza y la liberación sobre el valor de la raza. */
class RaceTest {
    private static final BalanceParams P = BalanceParams.DEFAULT;

    private static Map<Atributo, Integer> stats(int vit, int fue, int def, int vel, int ki, int pod, int con) {
        Map<Atributo, Integer> m = new EnumMap<>(Atributo.class);
        m.put(Atributo.VIT, vit); m.put(Atributo.FUE, fue); m.put(Atributo.DEF, def); m.put(Atributo.VEL, vel);
        m.put(Atributo.KI, ki); m.put(Atributo.POD, pod); m.put(Atributo.CON, con);
        return m;
    }

    private static Race terricola() {
        return new Race("terricola", "race.dragonball.terricola", "race.dragonball.terricola.description", stats(8, 8, 8, 8, 0, 8, 0),
                List.of(new Rasgo("adaptable", "trait.a", "trait.a.d"), new Rasgo("sin_cola", "trait.b", "trait.b.d")));
    }

    private static Race saiyajin() {
        return new Race("saiyajin", "race.dragonball.saiyajin", "race.dragonball.saiyajin.description", stats(9, 10, 9, 10, 0, 8, 0),
                List.of(new Rasgo("zenkai", "t.z", "t.z.d"), new Rasgo("cola", "t.c", "t.c.d"), new Rasgo("apetito_saiyajin", "t.a", "t.a.d")));
    }

    @Test
    void bothRacesHaveTheDocumentedBaseStats() {
        Race t = terricola(), s = saiyajin();
        int[] terr = {8, 8, 8, 8, 0, 8, 0};
        int[] sai = {9, 10, 9, 10, 0, 8, 0};
        for (Atributo a : Atributo.values()) {
            assertEquals(terr[a.index()], t.base(a), "Terrícola " + a);
            assertEquals(sai[a.index()], s.base(a), "Saiyajin " + a);
        }
        assertEquals(2, t.traits().size());
        assertEquals(3, s.traits().size());
        assertNull(t.problem(P));
        assertNull(s.problem(P));
    }

    @Test
    void aRaceIsImmutable() {
        Map<Atributo, Integer> mutable = stats(8, 8, 8, 8, 0, 8, 0);
        Race r = new Race("x", "n", "d", mutable, List.of());
        mutable.put(Atributo.FUE, 99);
        assertEquals(8, r.base(Atributo.FUE));
        assertThrows(UnsupportedOperationException.class, () -> r.baseStats().put(Atributo.FUE, 1));
        assertThrows(UnsupportedOperationException.class, () -> r.traits().add(new Rasgo("a", "b", "c")));
    }

    @Test
    void invalidIdsAreRejected() {
        for (String bad : new String[] {"", "Saiyajin", "1abc", "a-b", "a b", "_x", null}) {
            assertFalse(Race.isValidId(bad), "'" + bad + "'");
            assertNotNull(new Race(bad, "n", "d", stats(8, 8, 8, 8, 0, 8, 0), List.of()).structuralProblem(), "'" + bad + "'");
        }
        for (String good : new String[] {"a", "saiyajin", "mi_raza_2"}) assertTrue(Race.isValidId(good), good);
    }

    @Test
    void missingOrNegativeStatsAreRejected() {
        Map<Atributo, Integer> missing = stats(8, 8, 8, 8, 0, 8, 0);
        missing.remove(Atributo.POD);
        assertNotNull(new Race("x", "n", "d", missing, List.of()).structuralProblem());
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 8, 8, -1, 8, 0), List.of()).structuralProblem());
        assertNotNull(new Race("x", "", "d", stats(8, 8, 8, 8, 0, 8, 0), List.of()).structuralProblem());
        assertNotNull(new Race("x", "n", " ", stats(8, 8, 8, 8, 0, 8, 0), List.of()).structuralProblem());
    }

    @Test
    void badTraitsAreRejected() {
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 8, 8, 0, 8, 0), List.of(new Rasgo("Mal", "a", "b"))).structuralProblem());
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 8, 8, 0, 8, 0), List.of(new Rasgo("a", "", "b"))).structuralProblem());
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 8, 8, 0, 8, 0), List.of(new Rasgo("a", "n", "d"), new Rasgo("a", "n2", "d2"))).structuralProblem());
    }

    @Test
    void combatStatsCannotStartBelowVanilla() {
        assertNotNull(new Race("x", "n", "d", stats(7, 8, 8, 8, 0, 8, 0), List.of()).problem(P));   // VIT
        assertNotNull(new Race("x", "n", "d", stats(8, 7, 8, 8, 0, 8, 0), List.of()).problem(P));   // FUE
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 7, 8, 0, 8, 0), List.of()).problem(P));   // DEF
        assertNotNull(new Race("x", "n", "d", stats(8, 8, 8, 7, 0, 8, 0), List.of()).problem(P));   // VEL
        assertNull(new Race("x", "n", "d", stats(8, 8, 8, 8, 0, 0, 0), List.of()).problem(P));      // POD puede ser 0
    }

    @Test
    void aBaseThatLeavesNoRoomForUpgradesIsRejected() {
        assertNotNull(new Race("x", "n", "d", stats(99, 8, 8, 8, 0, 8, 0), List.of()).problem(P));
        assertNull(new Race("x", "n", "d", stats(98, 8, 8, 8, 0, 8, 0), List.of()).problem(P));
    }

    @Test
    void upgradesAreTheSameForEveryRaceButTheCapIsOnTheValue() {
        assertEquals(10, Atributos.valor(Atributo.FUE, 10, 0, P));
        assertEquals(99, Atributos.valor(Atributo.FUE, 10, 89, P));
        assertEquals(99, Atributos.valor(Atributo.FUE, 8, 91, P));
        assertEquals(89, Atributos.maxMejoras(Atributo.FUE, 10, P));   // Saiyajin: 2 mejoras menos que Terrícola
        assertEquals(91, Atributos.maxMejoras(Atributo.FUE, 8, P));
        assertEquals(99, Atributos.maxMejoras(Atributo.KI, 0, P));
        assertThrows(IllegalArgumentException.class, () -> Atributos.valor(Atributo.FUE, 10, 90, P));
        assertThrows(IllegalArgumentException.class, () -> Atributos.valor(Atributo.FUE, 10, -1, P));
        assertTrue(Atributos.puedeMejorar(Atributo.FUE, 10, 88, P));
        assertFalse(Atributos.puedeMejorar(Atributo.FUE, 10, 89, P));
        assertFalse(Atributos.puedeMejorar(Atributo.FUE, 8, 91, P));
    }

    @Test
    void clippedValueNeverExceedsTheCap() {
        assertEquals(99, Atributos.valorRecortado(Atributo.FUE, 10, 500, P));
        assertEquals(10, Atributos.valorRecortado(Atributo.FUE, 10, -5, P));
        assertEquals(15, Atributos.valorRecortado(Atributo.FUE, 10, 5, P));
    }

    @Test
    void defaultBaseIsTheTerricolaValue() {
        int[] terr = {8, 8, 8, 8, 0, 8, 0};
        for (Atributo a : Atributo.values()) assertEquals(terr[a.index()], Atributos.baseDefecto(a, P), a.toString());
    }

    private static double eff(int base, double lib) {
        return KiSystem.statEfectiva(base, 8.0, lib, P);
    }

    @Test
    void releaseOnSaiyanFue10GivesEightTenTwenty() {
        assertEquals(8.0, eff(10, 0), 1e-12);      // vanilla
        assertEquals(9.0, eff(10, 25), 1e-12);
        assertEquals(10.0, eff(10, 50), 1e-12);    // los del mod: ya más fuerte que vanilla
        assertEquals(15.0, eff(10, 75), 1e-12);
        assertEquals(20.0, eff(10, 100), 1e-12);   // el doble
    }

    @Test
    void releaseOnTerricolaFue8IsUnchanged() {
        assertEquals(8.0, eff(8, 0), 1e-12);
        assertEquals(8.0, eff(8, 50), 1e-12);
        assertEquals(16.0, eff(8, 100), 1e-12);
    }

    @Test
    void releaseUsesTheRaceBaseForEveryCombatStat() {
        assertEquals(18.0, eff(9, 100), 1e-12);
        assertEquals(20.0, eff(10, 100), 1e-12);
        assertEquals(9.0, eff(9, 50), 1e-12);
        assertEquals(8.0, eff(9, 0), 1e-12);
    }

    @Test
    void attributeIdsAndOrderMatchTheRestOfTheMod() {
        String[] ids = {"vit", "fue", "def", "vel", "ki", "pod", "con"};
        for (int i = 0; i < ids.length; i++) {
            assertEquals(ids[i], Atributo.values()[i].id());
            assertEquals(i, Atributo.values()[i].index());
            assertEquals(Atributo.values()[i], Atributo.fromId(ids[i]).orElseThrow());
        }
        assertTrue(Atributo.fromId("nope").isEmpty());
    }
}

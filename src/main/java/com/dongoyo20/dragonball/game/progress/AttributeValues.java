package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.KiSystem;

/** Valor de cada atributo a partir de sus mejoras (común a servidor y cliente: la pantalla de estadísticas lo usa). */
public final class AttributeValues {
    private AttributeValues() {}

    /** El atributo no tiene todavía valores definidos (DEF y POD: ciclos pendientes). */
    public static final int UNDEFINED = -1;

    /** Si el atributo escala con la liberación (VIT, FUE, VEL); KI, POD y CON no cambian con ella. */
    public static boolean scalesWithRelease(int index) {
        return index == 0 || index == 1 || index == 3;
    }

    /** Valor base del atributo {@code index} (orden VIT, FUE, DEF, VEL, KI, POD, CON) o {@link #UNDEFINED}. */
    public static int baseValue(int index, PlayerProgress progress, BalanceParams p) {
        int up = progress.upgrades().get(index);
        return switch (index) {
            case 0 -> HealthSystem.vitValue(Math.min(up, p.vitMaxUpgrades()), p);
            case 1 -> FuerzaSystem.fueValue(Math.min(up, p.fueMaxUpgrades()), p);
            case 3 -> KiSystem.velValue(Math.min(up, p.velMaxUpgrades()), p);
            case 4 -> KiSystem.kiValue(Math.min(up, p.kiMaxUpgrades()), p);
            case 6 -> KiSystem.conValue(Math.min(up, p.conMaxUpgrades()), p);
            default -> UNDEFINED;
        };
    }

    /** Valor mostrado como "efectivo": {@code base × liberación / 100} para VIT, FUE y VEL; igual a la base en el resto. */
    public static double effectiveValue(int index, int base, double releasePercent) {
        return scalesWithRelease(index) ? KiSystem.statEfectiva((double) base, releasePercent) : base;
    }
}

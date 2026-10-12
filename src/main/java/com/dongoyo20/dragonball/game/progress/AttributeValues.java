package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Atributos;

/** Valor de cada atributo a partir de sus mejoras (común a servidor y cliente: la pantalla de estadísticas lo usa). */
public final class AttributeValues {
    private AttributeValues() {}

    /** El atributo no tiene valores definidos (hoy los siete los tienen; se conserva por si se añade otro). */
    public static final int UNDEFINED = -1;

    /** Si el atributo escala con la liberación (VIT, FUE, DEF, VEL); KI, POD y CON no cambian con ella. */
    public static boolean scalesWithRelease(int index) {
        return index == 0 || index == 1 || index == 2 || index == 3;
    }

    /** Valor del atributo {@code index} (orden VIT, FUE, DEF, VEL, KI, POD, CON) con el valor inicial por defecto (Terrícola). */
    public static int baseValue(int index, PlayerProgress progress, BalanceParams p) {
        return baseValue(index, progress, p, Atributos.baseDefecto(Atributo.values()[index], p));
    }

    /**
     * Valor del atributo {@code index}: valor inicial de la raza + crecimiento × mejoras. Un guardado con más mejoras que el
     * tope no debe romper el juego: se trata como el máximo.
     */
    public static int baseValue(int index, PlayerProgress progress, BalanceParams p, int raceBase) {
        return Atributos.valorRecortado(Atributo.values()[index], raceBase, progress.upgrades().get(index), p);
    }

    /**
     * Valor "efectivo" según la liberación (función a trozos: vanilla 8 a 0 %, el del mod a 50 %, el doble a 100 %) para
     * VIT, FUE y VEL; igual a la base en el resto.
     */
    public static double effectiveValue(int index, int base, double releasePercent, BalanceParams p) {
        if (!scalesWithRelease(index)) return base;
        double vanilla = switch (index) { case 0 -> p.vitBase(); case 1 -> p.fueBase(); case 2 -> p.defense().defBase(); default -> p.release().velBase(); };
        return KiSystem.statEfectiva(base, vanilla, releasePercent, p);
    }
}

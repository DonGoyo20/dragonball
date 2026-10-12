package com.dongoyo20.dragonball.core;

/**
 * Números provisionales del recurso Ki (reserva, gasto, regeneración). Forman parte de {@link BalanceParams} y se
 * pueden cambiar con datos. Varios son decisiones mías, no del diseño (están marcados): hay que ajustarlos jugando.
 *
 * @param kiBase                valor inicial del atributo KI (con 0 la reserva es la plana: 50)
 * @param kiGrowth              cuánto sube KI por mejora
 * @param kiFlat                parte plana de la reserva máxima (SYSTEMS.md §4.1: 50)
 * @param kiPerPoint            ki extra por punto de KI (SYSTEMS.md §4.1: 10)
 * @param conBase               valor inicial de CON (solo se usa para abaratar el gasto de la liberación)
 * @param conGrowth             cuánto sube CON por mejora
 * @param conScale              {@code coste = base / (1 + CON / conScale)} (SYSTEMS.md §4.2: 60)
 * @param costPerSecondAtFull   <b>provisional</b>: ki por segundo que cuesta mantener el 100 % con CON 0; a otro % el coste
 *                              es proporcional
 * @param regenPercentPerSecond <b>provisional</b>: % de la reserva que se regenera por segundo con liberación 0 %
 * @param regenBelowPercent     por encima de este % de liberación no se regenera (decisión del usuario: 50)
 */
public record KiParams(
        double kiBase, double kiGrowth, double kiFlat, double kiPerPoint,
        double conBase, double conGrowth, double conScale,
        double costPerSecondAtFull, double regenPercentPerSecond, double regenBelowPercent) {

    public static final KiParams DEFAULT = new KiParams(0.0, 1.0, 50.0, 10.0, 0.0, 1.0, 60.0, 0.2, 2.0, 50.0);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!finiteNonNegative(kiBase)) return "ki.ki_base debe ser un número >= 0";
        if (!finitePositive(kiGrowth)) return "ki.ki_growth debe ser un número > 0";
        if (!finiteNonNegative(kiFlat)) return "ki.ki_flat debe ser un número >= 0";
        if (!finiteNonNegative(kiPerPoint)) return "ki.ki_per_point debe ser un número >= 0";
        if (kiFlat + kiPerPoint * kiBase < 1) return "la reserva inicial de ki debe ser al menos 1";
        if (!finiteNonNegative(conBase)) return "ki.con_base debe ser un número >= 0";
        if (!finitePositive(conGrowth)) return "ki.con_growth debe ser un número > 0";
        if (!finitePositive(conScale)) return "ki.con_scale debe ser un número > 0";
        if (!finiteNonNegative(costPerSecondAtFull) || costPerSecondAtFull > 1000) return "ki.cost_per_second_at_full debe estar entre 0 y 1000";
        if (!finiteNonNegative(regenPercentPerSecond) || regenPercentPerSecond > 100) return "ki.regen_percent_per_second debe estar entre 0 y 100";
        if (!(regenBelowPercent > 0 && regenBelowPercent <= 100)) return "ki.regen_below_percent debe estar entre 0 (excluido) y 100";
        return null;
    }

    private static boolean finitePositive(double v) { return Double.isFinite(v) && v > 0; }
    private static boolean finiteNonNegative(double v) { return Double.isFinite(v) && v >= 0; }
}

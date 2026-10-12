package com.dongoyo20.dragonball.core;

/**
 * Números provisionales de DEF (defensa). Forman parte de {@link BalanceParams} y se pueden cambiar con datos.
 *
 * @param defBase        valor inicial de DEF (8 = vanilla: sin reducción extra)
 * @param defGrowth      cuánto sube DEF por mejora
 * @param damageExponent <b>provisional</b>: exponente de la reducción de daño por DEF, {@code (defBase / DEF efectiva)^exponente}.
 *                       Es el mismo (cúbico) que usan VIT y FUE, por simetría
 */
public record DefenseParams(double defBase, double defGrowth, double damageExponent) {

    public static final DefenseParams DEFAULT = new DefenseParams(8.0, 1.0, 3.0);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!(Double.isFinite(defBase) && defBase >= 1)) return "defense.def_base debe ser un número >= 1";
        if (!(Double.isFinite(defGrowth) && defGrowth > 0)) return "defense.def_growth debe ser un número > 0";
        if (!(damageExponent >= 0.5 && damageExponent <= 6.0)) return "defense.damage_exponent debe estar entre 0,5 y 6";
        return null;
    }
}

package com.dongoyo20.dragonball.core;

/**
 * Números provisionales de DEF (defensa). Forman parte de {@link BalanceParams} y se pueden cambiar con datos.
 *
 * @param defBase        valor inicial de DEF (8 = vanilla: sin reducción extra)
 * @param defGrowth      cuánto sube DEF por mejora
 * @param damageExponent <b>provisional</b>: exponente de la reducción de daño por DEF, {@code (defBase / DEF efectiva)^exponente}.
 *                       Es el mismo (cúbico) que usan VIT y FUE, por simetría
 * @param multiplier     multiplicador global de la reducción de DEF (decisión del usuario, 2026-10-12: 1,25 = el jugador
 *                       recibe un 25 % más de daño que con la fórmula pura). Reducción = {@code multiplier × (defBase / DEF efectiva)^exponente},
 *                       con tope de 1 para que DEF nunca empeore el daño (DEF 8 y liberación 0 % siguen siendo vanilla)
 */
public record DefenseParams(double defBase, double defGrowth, double damageExponent, double multiplier) {

    public static final DefenseParams DEFAULT = new DefenseParams(8.0, 1.0, 3.0, 1.25);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!(Double.isFinite(defBase) && defBase >= 1)) return "defense.def_base debe ser un número >= 1";
        if (!(Double.isFinite(defGrowth) && defGrowth > 0)) return "defense.def_growth debe ser un número > 0";
        if (!(damageExponent >= 0.5 && damageExponent <= 6.0)) return "defense.damage_exponent debe estar entre 0,5 y 6";
        if (!(Double.isFinite(multiplier) && multiplier > 0 && multiplier <= 100)) return "defense.defense_multiplier debe estar entre 0 (excluido) y 100";
        return null;
    }
}

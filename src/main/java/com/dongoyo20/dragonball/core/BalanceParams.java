package com.dongoyo20.dragonball.core;

/**
 * Números de balance del combate, en Java puro. Antes eran constantes dentro de {@link HealthSystem} y
 * {@link FuerzaSystem}; ahora se leen de datos (datapack o KubeJS, ver {@code game/balance/BalanceManager}) para poder
 * ajustarlos sin recompilar. Los valores por defecto son <b>exactamente</b> los de antes: este cambio no toca el balance.
 *
 * <p>El constructor no lanza excepciones (así un archivo de datos defectuoso se puede rechazar con un mensaje en vez
 * de romper la carga); {@link #problem()} dice qué está mal y el juego solo adopta parámetros sin problemas.
 *
 * @param vitBase          valor inicial de VIT (y referencia de la reducción de daño)
 * @param vitGrowth        cuánto sube VIT por mejora
 * @param hpPerVit         puntos de vida real por punto de VIT
 * @param damageExponent   exponente de la reducción de daño por VIT
 * @param continuousFloor  suelo del daño de estado continuo, en HP de vanilla por aplicación
 * @param fueBase          valor inicial de FUE (y referencia del multiplicador)
 * @param fueGrowth        cuánto sube FUE por mejora
 * @param strengthExponent exponente del multiplicador de daño por FUE
 * @param attributeCap     tope del valor de un atributo
 */
public record BalanceParams(
        double vitBase, double vitGrowth, double hpPerVit, double damageExponent, double continuousFloor,
        double fueBase, double fueGrowth, double strengthExponent, int attributeCap) {

    /** Los valores vigentes antes de que el balance fuera configurable (SYSTEMS.md §3.1, §3.2.1 y §3.2.2). */
    public static final BalanceParams DEFAULT = new BalanceParams(8.0, 1.0, 12.5, 3.0, 0.5, 8.0, 1.0, 3.0, 99);

    /** Mejoras de VIT que caben bajo el tope (8 + 91 = 99 con los valores por defecto). */
    public int vitMaxUpgrades() {
        return (int) Math.floor((attributeCap - vitBase) / vitGrowth + 1e-9);
    }

    /** Mejoras de FUE que caben bajo el tope. */
    public int fueMaxUpgrades() {
        return (int) Math.floor((attributeCap - fueBase) / fueGrowth + 1e-9);
    }

    /** {@code null} si los parámetros son válidos; si no, una frase que explica el primer problema. */
    public String problem() {
        if (!finitePositive(vitBase) || vitBase < 1) return "vit_base debe ser un número >= 1";
        if (!finitePositive(vitGrowth)) return "vit_growth debe ser un número > 0";
        if (!finitePositive(hpPerVit)) return "hp_per_vit debe ser un número > 0";
        if (!(damageExponent >= 0.5 && damageExponent <= 6.0)) return "damage_exponent debe estar entre 0,5 y 6";
        if (!(continuousFloor >= 0.0 && continuousFloor <= 20.0)) return "continuous_floor debe estar entre 0 y 20";
        if (!finitePositive(fueBase) || fueBase < 1) return "fue_base debe ser un número >= 1";
        if (!finitePositive(fueGrowth)) return "fue_growth debe ser un número > 0";
        if (!(strengthExponent >= 0.5 && strengthExponent <= 6.0)) return "strength_exponent debe estar entre 0,5 y 6";
        if (attributeCap < 2 || attributeCap > 10_000) return "attribute_cap debe estar entre 2 y 10000";
        if (attributeCap < vitBase + vitGrowth) return "attribute_cap debe permitir al menos una mejora de VIT";
        if (attributeCap < fueBase + fueGrowth) return "attribute_cap debe permitir al menos una mejora de FUE";
        return null;
    }

    private static boolean finitePositive(double v) {
        return Double.isFinite(v) && v > 0;
    }
}

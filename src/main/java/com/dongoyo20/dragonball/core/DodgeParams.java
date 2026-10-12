package com.dongoyo20.dragonball.core;

/**
 * Números provisionales de la esquiva (diseño propuesto por el usuario, 2026-10-12). Forman parte de {@link BalanceParams}.
 *
 * @param baseChance      probabilidad de esquivar con VEL efectiva 8 (vanilla): 0,10
 * @param logMultiplier   cuánto sube la probabilidad por cada unidad de {@code ln(VEL efectiva / 8)}: 0,20
 * @param maxChance       tope de la probabilidad: 0,60
 * @param kiCostBase      ki base de cada esquiva: 5
 * @param kiCostPerDamage ki extra por cada HP de vanilla del golpe esquivado: 0,1 (un golpe de 30 HP cuesta 3 más)
 * @param conReduction    {@code coste = (base + daño × porDaño) / (1 + CON / conReduction)}; 60, igual que el gasto de la liberación
 */
public record DodgeParams(double baseChance, double logMultiplier, double maxChance,
                          double kiCostBase, double kiCostPerDamage, double conReduction) {

    public static final DodgeParams DEFAULT = new DodgeParams(0.10, 0.20, 0.60, 5.0, 0.1, 60.0);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!(baseChance >= 0 && baseChance <= 1)) return "dodge.base_chance debe estar entre 0 y 1";
        if (!(Double.isFinite(logMultiplier) && logMultiplier >= 0 && logMultiplier <= 10)) return "dodge.log_multiplier debe estar entre 0 y 10";
        if (!(maxChance >= baseChance && maxChance <= 1)) return "dodge.max_chance debe estar entre base_chance y 1";
        if (!(Double.isFinite(kiCostBase) && kiCostBase >= 0 && kiCostBase <= 10_000)) return "dodge.ki_cost_base debe estar entre 0 y 10000";
        if (!(Double.isFinite(kiCostPerDamage) && kiCostPerDamage >= 0 && kiCostPerDamage <= 10_000)) return "dodge.ki_cost_per_damage debe estar entre 0 y 10000";
        if (!(Double.isFinite(conReduction) && conReduction > 0)) return "dodge.con_reduction debe ser un número > 0";
        return null;
    }
}

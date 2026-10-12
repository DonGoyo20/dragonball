package com.dongoyo20.dragonball.core;

/**
 * Esquiva en Java puro (Fase 2 de la reimplementación de DMZ; sin poise ni parry, decisión cerrada).
 *
 * <p><b>Probabilidad:</b> {@code clamp(0,10 + 0,20 × ln(VEL efectiva / 8), 0,10; 0,60)}. La VEL efectiva sigue la función a
 * trozos de la liberación ({@link KiSystem#statEfectiva}) y nunca baja de 8 (el valor vanilla), así que 10 % es el suelo.
 * Valores con los números por defecto (los de SYSTEMS.md §3.3):
 * <pre>
 *   VEL  8 → 0,100        VEL 18 → 0,262        VEL 36 → 0,401        VEL 99 → 0,60 (tope; la fórmula daría 0,603)
 * </pre>
 * (El encargo daba 0,32 y 0,46 para VEL 18 y 36; con {@code ln} y ×0,20 salen 0,262 y 0,401. Se mantiene la fórmula, que es la
 * de SYSTEMS.md; los números del ejemplo estaban mal calculados.)
 *
 * <p><b>Coste en ki:</b> {@code round((5 + daño × 0,1) / (1 + CON / 60))}, mínimo 1. Con CON 0: golpe de 3 HP → 5, de 30 HP → 8;
 * con CON 60: el de 30 HP → 4. El "daño" es el daño de vanilla del golpe (antes de armadura y de las reducciones del mod).
 */
public final class EsquivaSystem {
    private EsquivaSystem() {}

    private static final BalanceParams D = BalanceParams.DEFAULT;

    public static double probabilidadEsquiva(double velEfectiva) { return probabilidadEsquiva(velEfectiva, D); }
    public static int costeKiEsquiva(double danioEsquivado, int con) { return costeKiEsquiva(danioEsquivado, con, D); }

    /** Probabilidad de esquivar un golpe con la VEL <b>efectiva</b> dada (por debajo de 8 se trata como 8). */
    public static double probabilidadEsquiva(double velEfectiva, BalanceParams p) {
        if (Double.isNaN(velEfectiva) || velEfectiva < 0 || Double.isInfinite(velEfectiva)) {
            throw new IllegalArgumentException("VEL efectiva inválida: " + velEfectiva);
        }
        DodgeParams d = p.dodge();
        double vanilla = p.release().velBase();
        double raw = d.baseChance() + d.logMultiplier() * Math.log(Math.max(velEfectiva, vanilla) / vanilla);
        return Math.max(d.baseChance(), Math.min(d.maxChance(), raw));
    }

    /** Ki que cuesta esquivar un golpe de {@code danioEsquivado} HP de vanilla con CON {@code con}. */
    public static int costeKiEsquiva(double danioEsquivado, int con, BalanceParams p) {
        if (Double.isNaN(danioEsquivado) || danioEsquivado < 0 || Double.isInfinite(danioEsquivado)) {
            throw new IllegalArgumentException("daño inválido: " + danioEsquivado);
        }
        if (con < 0) throw new IllegalArgumentException("CON negativo: " + con);
        DodgeParams d = p.dodge();
        double raw = (d.kiCostBase() + danioEsquivado * d.kiCostPerDamage()) / (1.0 + con / d.conReduction());
        return (int) Math.max(1L, Math.round(raw));
    }

    /** ¿Sale la tirada? {@code roll} es un número en [0, 1); se esquiva si es menor que la probabilidad. */
    public static boolean sale(double probabilidad, double roll) {
        if (Double.isNaN(probabilidad) || probabilidad < 0 || probabilidad > 1) throw new IllegalArgumentException("probabilidad fuera de [0, 1]: " + probabilidad);
        if (Double.isNaN(roll) || roll < 0 || roll >= 1) throw new IllegalArgumentException("tirada fuera de [0, 1): " + roll);
        return roll < probabilidad;
    }
}

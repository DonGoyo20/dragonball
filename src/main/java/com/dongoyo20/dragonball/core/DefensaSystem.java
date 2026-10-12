package com.dongoyo20.dragonball.core;

/**
 * DEF (defensa) en Java puro. El daño instantáneo recibido (golpes, proyectiles, explosiones, caídas…) se multiplica por
 * {@code min(1, defense_multiplier × (8 / DEF efectiva)^3)}, con {@code defense_multiplier = 1,25} (decisión del usuario,
 * 2026-10-12: con la fórmula pura VIT 18 + DEF 18 era ×0,0077, casi inmune; el multiplicador la deja en un 25 % más de daño).
 * <ul>
 *   <li>DEF 8 (o liberación 0 %): ×1, vanilla. El tope {@code min(1, …)} evita que el multiplicador empeore el daño por
 *       debajo de DEF ~8,6 (sin él, DEF 8 recibiría ×1,25 por culpa de la propia mejora).</li>
 *   <li>DEF 18 sola: {@code 1,25 × 0,0878 = ×0,110}.</li>
 *   <li>VIT 18 + DEF 18 (se suman a la reducción de VIT, ver {@link HealthSystem}): {@code 0,0878 × 0,110 = ×0,0097}.</li>
 *   <li>Liberación 100 %: la DEF efectiva se duplica; el multiplicador sigue siendo 1,25.</li>
 * </ul>
 *
 * <p>La DEF efectiva depende de la liberación igual que VIT, FUE y VEL ({@link KiSystem#statEfectiva}): a 0 % es vanilla, a
 * 50 % la del mod, a 100 % el doble.
 *
 * <p>No afecta al daño de estado continuo (lava, fuego, veneno…), que ya tiene su suelo, ni al vacío ni a {@code /kill}.
 *
 * <p><b>Deuda técnica:</b> el diseño (SYSTEMS.md §3.2) usa {@code D = DEF^0,7 × VIT^0,3} contra el poder del atacante y una
 * defensa distinta contra el ki ({@code DEF^0,4 × CON^0,3 × VIT^0,3}). Esa versión necesita el poder de ataque de los
 * enemigos y las técnicas de ki; mientras tanto DEF es un multiplicador propio.
 */
public final class DefensaSystem {
    private DefensaSystem() {}

    private static final BalanceParams D = BalanceParams.DEFAULT;

    public static int defValue(int upgrades) { return defValue(upgrades, D); }

    /** Valor de DEF tras {@code upgrades} mejoras. */
    public static int defValue(int upgrades, BalanceParams p) {
        int max = p.defMaxUpgrades();
        if (upgrades < 0 || upgrades > max) {
            throw new IllegalArgumentException("mejoras de DEF fuera de [0, " + max + "]: " + upgrades);
        }
        return (int) Math.round(p.defense().defBase() + p.defense().defGrowth() * upgrades);
    }

    /** ¿Se puede comprar otra mejora de DEF sin pasar del tope? */
    public static boolean canUpgradeDef(int upgrades, BalanceParams p) {
        return upgrades >= 0 && upgrades < p.defMaxUpgrades();
    }

    /**
     * Multiplicador del daño recibido por una DEF <b>efectiva</b> (ya con la liberación aplicada), que debe ser positiva:
     * {@code min(1, multiplicador × (defBase / defEfectiva)^exponente)}.
     */
    public static double multiplicadorEfectivo(double defEfectiva, BalanceParams p) {
        if (!(defEfectiva > 0) || Double.isInfinite(defEfectiva)) {
            throw new IllegalArgumentException("DEF efectiva debe ser positiva: " + defEfectiva);
        }
        double raw = p.defense().multiplier() * Math.pow(p.defense().defBase() / defEfectiva, p.defense().damageExponent());
        return Math.min(1.0, raw);
    }

    /** Multiplicador del daño recibido con la DEF del jugador y su liberación (en %). */
    public static double multiplicador(int defBase, double porcentaje, BalanceParams p) {
        if (defBase < p.defense().defBase() || defBase > p.attributeCap()) {
            throw new IllegalArgumentException("DEF fuera de [" + p.defense().defBase() + ", " + p.attributeCap() + "]: " + defBase);
        }
        return multiplicadorEfectivo(KiSystem.statEfectiva(defBase, p.defense().defBase(), porcentaje, p), p);
    }

    /** Daño instantáneo tras la DEF. */
    public static double danioTrasDefensa(double danio, int defBase, double porcentaje, BalanceParams p) {
        if (danio < 0 || Double.isNaN(danio)) throw new IllegalArgumentException("daño negativo o NaN: " + danio);
        return danio * multiplicador(defBase, porcentaje, p);
    }
}

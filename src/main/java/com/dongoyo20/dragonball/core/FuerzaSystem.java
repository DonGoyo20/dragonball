package com.dongoyo20.dragonball.core;

/**
 * Reglas de FUE (fuerza) en Java puro, para probarlas sin arrancar Minecraft.
 *
 * <p>Fórmula provisional, <b>simétrica a la de VIT</b> ({@link HealthSystem}): el daño que hace el jugador cuerpo a
 * cuerpo se multiplica por {@code (FUE / 8)^3}. Con FUE 8 el daño no cambia; con FUE 18 es ×11,4; con FUE 26, ×34,3.
 *
 * <p><b>Sin tope de daño</b> (decisión del usuario, 2026-10-12): con FUE 99 el multiplicador es ~1895. Prevalece la
 * coherencia con Dragon Ball sobre el equilibrio del Minecraft de vanilla. El único límite es el tope 99 del valor del
 * atributo (SYSTEMS.md §2.1).
 *
 * <p><b>Balance configurable:</b> los números salen de {@link BalanceParams}; los métodos sin ese parámetro usan
 * {@link BalanceParams#DEFAULT} (los valores de siempre).
 *
 * <p><b>Deuda técnica:</b> el diseño (SYSTEMS.md §3.2) calcula el daño como un porcentaje de la vida del defensor con
 * {@code (A/D)^0,75}. Esta versión escala el daño de vanilla porque los enemigos aún tienen vida de Minecraft y no
 * existe su poder de ataque ni de defensa. Se sustituirá cuando existan.
 */
public final class FuerzaSystem {
    private FuerzaSystem() {}

    private static final BalanceParams D = BalanceParams.DEFAULT;

    /** Valor inicial de FUE. Provisional y universal (como VIT) hasta que existan las razas. */
    public static final double FUE_BASE = D.fueBase();
    /** Tope del valor de un atributo (SYSTEMS.md §2.1). */
    public static final int FUE_MAX = D.attributeCap();
    /** Cuánto sube el valor de FUE por mejora (provisional, igual que VIT). */
    public static final double FUE_GROWTH = D.fueGrowth();
    /** Mejoras de FUE que caben bajo el tope: 8 + 91 = 99. */
    public static final int FUE_MAX_UPGRADES = D.fueMaxUpgrades();
    /** Exponente del multiplicador (igual que {@link HealthSystem#DANIO_EXPONENTE}). */
    public static final int EXPONENTE = (int) D.strengthExponent();

    // ---------- variantes con los valores por defecto

    public static int fueValue(int upgrades) { return fueValue(upgrades, D); }
    public static boolean canUpgradeFue(int upgrades) { return canUpgradeFue(upgrades, D); }
    public static double multiplicadorFuerza(int fue) { return multiplicadorFuerza(fue, D); }
    public static double danioSaliente(double danioVanilla, int fue) { return danioSaliente(danioVanilla, fue, D); }

    // ---------- variantes con balance configurable

    /** Valor de FUE tras {@code upgrades} mejoras. */
    public static int fueValue(int upgrades, BalanceParams p) {
        int max = p.fueMaxUpgrades();
        if (upgrades < 0 || upgrades > max) {
            throw new IllegalArgumentException("mejoras de FUE fuera de [0, " + max + "]: " + upgrades);
        }
        return (int) Math.round(p.fueBase() + p.fueGrowth() * upgrades);
    }

    /** ¿Se puede comprar otra mejora de FUE sin pasar del tope? */
    public static boolean canUpgradeFue(int upgrades, BalanceParams p) {
        return upgrades >= 0 && upgrades < p.fueMaxUpgrades();
    }

    /** {@code (FUE / fueBase)^exponente}. */
    public static double multiplicadorFuerza(int fue, BalanceParams p) {
        if (fue < p.fueBase() || fue > p.attributeCap()) {
            throw new IllegalArgumentException("FUE fuera de [" + p.fueBase() + ", " + p.attributeCap() + "]: " + fue);
        }
        return Math.pow(fue / p.fueBase(), p.strengthExponent());
    }

    /** Daño que hace el jugador: el daño de vanilla por el multiplicador. */
    public static double danioSaliente(double danioVanilla, int fue, BalanceParams p) {
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        return danioVanilla * multiplicadorFuerza(fue, p);
    }

    /**
     * Daño con una FUE <b>efectiva</b> (FUE × factor de liberación): puede ser menor o mayor que la base.
     * Debe ser positiva.
     */
    public static double danioSalienteEfectivo(double danioVanilla, double fueEfectiva, BalanceParams p) {
        if (!(fueEfectiva > 0) || Double.isInfinite(fueEfectiva)) {
            throw new IllegalArgumentException("FUE efectiva debe ser positiva: " + fueEfectiva);
        }
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        return danioVanilla * Math.pow(fueEfectiva / p.fueBase(), p.strengthExponent());
    }
}

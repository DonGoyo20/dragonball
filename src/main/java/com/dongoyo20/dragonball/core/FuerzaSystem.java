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
 * <p><b>Deuda técnica:</b> el diseño (SYSTEMS.md §3.2) calcula el daño como un porcentaje de la vida del defensor con
 * {@code (A/D)^0,75}. Esta versión escala el daño de vanilla porque los enemigos aún tienen vida de Minecraft y no
 * existe su poder de ataque ni de defensa. Se sustituirá cuando existan.
 */
public final class FuerzaSystem {
    private FuerzaSystem() {}

    /** Valor inicial de FUE. Provisional y universal (como VIT) hasta que existan las razas. */
    public static final double FUE_BASE = 8.0;
    /** Tope del valor de un atributo (SYSTEMS.md §2.1). */
    public static final int FUE_MAX = 99;
    /** Cuánto sube el valor de FUE por mejora (provisional, igual que VIT). */
    public static final double FUE_GROWTH = 1.0;
    /** Mejoras de FUE que caben bajo el tope: 8 + 91 = 99. */
    public static final int FUE_MAX_UPGRADES = (int) Math.round((FUE_MAX - FUE_BASE) / FUE_GROWTH);
    /** Exponente del multiplicador (igual que {@link HealthSystem#DANIO_EXPONENTE}). */
    public static final int EXPONENTE = 3;

    /** Valor de FUE tras {@code upgrades} mejoras. */
    public static int fueValue(int upgrades) {
        if (upgrades < 0 || upgrades > FUE_MAX_UPGRADES) {
            throw new IllegalArgumentException("mejoras de FUE fuera de [0, " + FUE_MAX_UPGRADES + "]: " + upgrades);
        }
        return (int) Math.round(FUE_BASE + FUE_GROWTH * upgrades);
    }

    /** ¿Se puede comprar otra mejora de FUE sin pasar del tope 99? */
    public static boolean canUpgradeFue(int upgrades) {
        return upgrades >= 0 && upgrades < FUE_MAX_UPGRADES;
    }

    /** {@code (FUE / 8)^3}. */
    public static double multiplicadorFuerza(int fue) {
        if (fue < FUE_BASE || fue > FUE_MAX) {
            throw new IllegalArgumentException("FUE fuera de [" + (int) FUE_BASE + ", " + FUE_MAX + "]: " + fue);
        }
        return Math.pow(fue / FUE_BASE, EXPONENTE);
    }

    /** Daño que hace el jugador: el daño de vanilla por el multiplicador. */
    public static double danioSaliente(double danioVanilla, int fue) {
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        return danioVanilla * multiplicadorFuerza(fue);
    }
}

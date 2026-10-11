package com.dongoyo20.dragonball.core;

/**
 * Reglas de vida del jugador en Java puro (sin Minecraft), para poder probarlas sin arrancar el juego.
 *
 * <p>Modelo: el jugador siempre tiene 20 HP de vanilla (la barra de corazones). La "vida real" no se guarda:
 * es {@code vidaReal = (hpVanilla / 20) × vidaRealMaxima(VIT)}.
 *
 * <p><b>Deuda técnica (fórmula provisional):</b> SYSTEMS.md §3.2 define el daño como
 * {@code 0,05 × (A/D)^0,75} con {@code D = DEF^0,7 × VIT^0,3}. Aquí VIT reduce el daño de forma <i>lineal</i>
 * ({@code VIT_BASE / VIT}), que es más fuerte que el diseño, porque todavía no existen DEF ni el poder de ataque
 * {@code A} de los enemigos. Se sustituirá cuando existan; la interfaz de este archivo debería sobrevivir.
 */
public final class HealthSystem {
    private HealthSystem() {}

    /** Valor inicial de VIT. Provisional: el del Saiyajin (BETA_RACES.md §4) hasta que existan las razas. */
    public static final double VIT_BASE = 8.0;
    /** Tope del valor de un atributo (decisión del usuario, SYSTEMS.md §2.1). */
    public static final int VIT_MAX = 99;
    /** Cuánto sube el valor de VIT por mejora (crecimiento del Saiyajin: 1,00). */
    public static final double VIT_GROWTH = 1.0;
    /** Mejoras de VIT que caben bajo el tope: 8 + 91 = 99. */
    public static final int VIT_MAX_UPGRADES = (int) Math.round((VIT_MAX - VIT_BASE) / VIT_GROWTH);
    /** Puntos de vida real por punto de valor de VIT. */
    public static final double VIDA_BASE = 12.5;
    /** Vida máxima de vanilla del jugador que usamos como barra. */
    public static final double VANILLA_MAX_HP = 20.0;

    /** Valor de VIT tras {@code upgrades} mejoras. */
    public static int vitValue(int upgrades) {
        if (upgrades < 0 || upgrades > VIT_MAX_UPGRADES) {
            throw new IllegalArgumentException("mejoras de VIT fuera de [0, " + VIT_MAX_UPGRADES + "]: " + upgrades);
        }
        return (int) Math.round(VIT_BASE + VIT_GROWTH * upgrades);
    }

    /** ¿Se puede comprar otra mejora de VIT sin pasar del tope 99? */
    public static boolean canUpgradeVit(int upgrades) {
        return upgrades >= 0 && upgrades < VIT_MAX_UPGRADES;
    }

    public static double vidaRealMaxima(int vit) {
        checkVit(vit);
        return VIDA_BASE * vit;
    }

    /** Vida real actual a partir de la vida vanilla (0–20). */
    public static double vidaRealActual(double hpVanilla, double vidaMaxima) {
        if (hpVanilla < 0 || vidaMaxima < 0 || Double.isNaN(hpVanilla) || Double.isNaN(vidaMaxima)) {
            throw new IllegalArgumentException("vida negativa o NaN: " + hpVanilla + ", " + vidaMaxima);
        }
        return (hpVanilla / VANILLA_MAX_HP) * vidaMaxima;
    }

    /** Daño en HP vanilla que se resta de verdad: {@code danioVanilla × VIT_BASE / VIT}. */
    public static double danioAplicado(double danioVanilla, int vitActual) {
        checkVit(vitActual);
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        return danioVanilla * (VIT_BASE / vitActual);
    }

    /**
     * El daño de entorno (caída, lava, ahogamiento, fuego, vacío, /kill…) no tiene entidad atacante y no se
     * escala (SYSTEMS.md §3.1). El juego decide {@code hayAtacante} a partir del DamageSource.
     */
    public static boolean esDanioDeEntorno(boolean hayAtacante) {
        return !hayAtacante;
    }

    private static void checkVit(int vit) {
        if (vit < VIT_BASE || vit > VIT_MAX) {
            throw new IllegalArgumentException("VIT fuera de [" + (int) VIT_BASE + ", " + VIT_MAX + "]: " + vit);
        }
    }
}

package com.dongoyo20.dragonball.core;

/**
 * Reglas de vida del jugador en Java puro (sin Minecraft), para poder probarlas sin arrancar el juego.
 *
 * <p>Modelo: el jugador siempre tiene 20 HP de vanilla (la barra de corazones). La "vida real" no se guarda:
 * es {@code vidaReal = (hpVanilla / 20) × vidaRealMaxima(VIT)}.
 *
 * <p><b>Fórmula provisional (decisión del usuario, 2026-10-12):</b> el daño aplicado es
 * {@code danioVanilla × (VIT_BASE / VIT)^3}. La primera versión era lineal ({@code ^1}) y en pruebas reales un zombi
 * de nivel 1 seguía quitando medio corazón con VIT 18: demasiado suave. Con el cubo, VIT 8 mantiene el daño de
 * vanilla (la amenaza inicial no cambia), VIT 18 lo deja en ~9 % y VIT 30 en ~2 %.
 *
 * <p><b>Alcance (2026-10-12):</b> todo el daño escala con VIT salvo el vacío y {@code /kill} ({@link TipoDanio}); el de
 * estado continuo tiene un suelo de 1 HP. Ver SYSTEMS.md §3.1.
 *
 * <p><b>Deuda técnica:</b> SYSTEMS.md §3.2 define el daño como {@code 0,05 × (A/D)^0,75} con
 * {@code D = DEF^0,7 × VIT^0,3}. Esta fórmula es mucho más fuerte que el diseño (VIT^3 frente a VIT^0,3) y no
 * depende del poder del atacante, porque todavía no existen DEF ni el poder de ataque {@code A} de los enemigos.
 * Cuando existan, se combinará con ellos; la interfaz de este archivo debería sobrevivir.
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
    /** Exponente de la reducción de daño por VIT (provisional; ver el JavaDoc de la clase). */
    public static final int DANIO_EXPONENTE = 3;
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

    /** Suelo del daño de estado continuo, en HP vanilla por aplicación (evita que VIT 99 sea inmune a la lava). */
    public static final double SUELO_CONTINUO = 1.0;

    /**
     * Daño en HP vanilla que se resta de verdad.
     * <ul>
     *   <li>{@code ESCALA_SIN_SUELO}: {@code danioVanilla × (VIT_BASE / VIT)^3}.</li>
     *   <li>{@code ESCALA_CON_SUELO}: lo mismo, pero nunca menos de {@link #SUELO_CONTINUO} (ni más que el daño
     *       original: el suelo no puede aumentar un daño que ya era menor que 1).</li>
     *   <li>{@code NO_ESCALA}: el daño original (vacío y /kill).</li>
     * </ul>
     */
    public static double danioAplicado(double danioVanilla, int vitActual, TipoDanio tipo) {
        checkVit(vitActual);
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        double escalado = danioVanilla * Math.pow(VIT_BASE / vitActual, DANIO_EXPONENTE);
        return switch (tipo) {
            case ESCALA_SIN_SUELO -> escalado;
            case ESCALA_CON_SUELO -> Math.min(danioVanilla, Math.max(escalado, SUELO_CONTINUO));
            case NO_ESCALA -> danioVanilla;
        };
    }

    private static void checkVit(int vit) {
        if (vit < VIT_BASE || vit > VIT_MAX) {
            throw new IllegalArgumentException("VIT fuera de [" + (int) VIT_BASE + ", " + VIT_MAX + "]: " + vit);
        }
    }
}

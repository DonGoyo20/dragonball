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
 * estado continuo tiene un suelo de 0,5 HP por aplicación. Ver SYSTEMS.md §3.1.
 *
 * <p><b>Balance configurable:</b> los números salen de {@link BalanceParams}. Los métodos sin parámetro de balance usan
 * {@link BalanceParams#DEFAULT} (los valores de siempre); el juego usa las variantes con {@code BalanceParams} para
 * respetar lo que diga el datapack.
 *
 * <p><b>Deuda técnica:</b> SYSTEMS.md §3.2 define el daño como {@code 0,05 × (A/D)^0,75} con
 * {@code D = DEF^0,7 × VIT^0,3}. Esta fórmula es mucho más fuerte que el diseño (VIT^3 frente a VIT^0,3) y no
 * depende del poder del atacante, porque todavía no existen DEF ni el poder de ataque {@code A} de los enemigos.
 * Cuando existan, se combinará con ellos; la interfaz de este archivo debería sobrevivir.
 */
public final class HealthSystem {
    private HealthSystem() {}

    private static final BalanceParams D = BalanceParams.DEFAULT;

    /** Valor inicial de VIT. Provisional: el del Saiyajin (BETA_RACES.md §4) hasta que existan las razas. */
    public static final double VIT_BASE = D.vitBase();
    /** Tope del valor de un atributo (decisión del usuario, SYSTEMS.md §2.1). */
    public static final int VIT_MAX = D.attributeCap();
    /** Cuánto sube el valor de VIT por mejora (crecimiento del Saiyajin: 1,00). */
    public static final double VIT_GROWTH = D.vitGrowth();
    /** Mejoras de VIT que caben bajo el tope: 8 + 91 = 99. */
    public static final int VIT_MAX_UPGRADES = D.vitMaxUpgrades();
    /** Puntos de vida real por punto de valor de VIT. */
    public static final double VIDA_BASE = D.hpPerVit();
    /** Exponente de la reducción de daño por VIT (provisional; ver el JavaDoc de la clase). */
    public static final int DANIO_EXPONENTE = (int) D.damageExponent();
    /** Vida máxima de vanilla del jugador que usamos como barra. */
    public static final double VANILLA_MAX_HP = 20.0;
    /**
     * Suelo del daño de estado continuo, en HP vanilla por aplicación (evita que VIT 99 sea inmune a la lava).
     * Era 1,0; se bajó a 0,5 (2026-10-12) porque con 1,0 la lava seguía siendo demasiado dañina para un personaje
     * con VIT alta, que debería ser casi inmune a los daños ambientales (sin llegar a serlo del todo).
     */
    public static final double SUELO_CONTINUO = D.continuousFloor();

    // ---------- variantes con los valores por defecto

    public static int vitValue(int upgrades) { return vitValue(upgrades, D); }
    public static boolean canUpgradeVit(int upgrades) { return canUpgradeVit(upgrades, D); }
    public static double vidaRealMaxima(int vit) { return vidaRealMaxima(vit, D); }
    public static double danioAplicado(double danioVanilla, int vitActual, TipoDanio tipo) {
        return danioAplicado(danioVanilla, vitActual, tipo, D);
    }

    // ---------- variantes con balance configurable

    /** Valor de VIT tras {@code upgrades} mejoras. */
    public static int vitValue(int upgrades, BalanceParams p) {
        int max = p.vitMaxUpgrades();
        if (upgrades < 0 || upgrades > max) {
            throw new IllegalArgumentException("mejoras de VIT fuera de [0, " + max + "]: " + upgrades);
        }
        return (int) Math.round(p.vitBase() + p.vitGrowth() * upgrades);
    }

    /** ¿Se puede comprar otra mejora de VIT sin pasar del tope? */
    public static boolean canUpgradeVit(int upgrades, BalanceParams p) {
        return upgrades >= 0 && upgrades < p.vitMaxUpgrades();
    }

    public static double vidaRealMaxima(int vit, BalanceParams p) {
        checkVit(vit, p);
        return p.hpPerVit() * vit;
    }

    /** Vida real actual a partir de la vida vanilla (0–20). */
    public static double vidaRealActual(double hpVanilla, double vidaMaxima) {
        if (hpVanilla < 0 || vidaMaxima < 0 || Double.isNaN(hpVanilla) || Double.isNaN(vidaMaxima)) {
            throw new IllegalArgumentException("vida negativa o NaN: " + hpVanilla + ", " + vidaMaxima);
        }
        return (hpVanilla / VANILLA_MAX_HP) * vidaMaxima;
    }

    /**
     * Daño en HP vanilla que se resta de verdad.
     * <ul>
     *   <li>{@code ESCALA_SIN_SUELO}: {@code danioVanilla × (vitBase / VIT)^exponente}.</li>
     *   <li>{@code ESCALA_CON_SUELO}: lo mismo, pero nunca menos del suelo (ni más que el daño original: el suelo
     *       no puede aumentar un daño original menor que el suelo).</li>
     *   <li>{@code NO_ESCALA}: el daño original (vacío y /kill).</li>
     * </ul>
     */
    public static double danioAplicado(double danioVanilla, int vitActual, TipoDanio tipo, BalanceParams p) {
        checkVit(vitActual, p);
        if (danioVanilla < 0 || Double.isNaN(danioVanilla)) {
            throw new IllegalArgumentException("daño negativo o NaN: " + danioVanilla);
        }
        double escalado = danioVanilla * Math.pow(p.vitBase() / vitActual, p.damageExponent());
        return switch (tipo) {
            case ESCALA_SIN_SUELO -> escalado;
            case ESCALA_CON_SUELO -> Math.min(danioVanilla, Math.max(escalado, p.continuousFloor()));
            case NO_ESCALA -> danioVanilla;
        };
    }

    private static void checkVit(int vit, BalanceParams p) {
        if (vit < p.vitBase() || vit > p.attributeCap()) {
            throw new IllegalArgumentException("VIT fuera de [" + p.vitBase() + ", " + p.attributeCap() + "]: " + vit);
        }
    }
}

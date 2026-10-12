package com.dongoyo20.dragonball.core;

/**
 * Recurso Ki y liberación de poder en Java puro (sin Minecraft). Decisiones del usuario (2026-10-12): reserva
 * {@code 50 + 10 × KI}; gasto por mantener la liberación {@code base / (1 + CON/60)}; solo se regenera con la liberación
 * por debajo del 50 % (máxima a 0 %); al quedarse sin ki la liberación baja a 0; solo sube si el jugador está quieto.
 *
 * <p>Los números que el diseño no fijaba (gasto base, regeneración, velocidad de subida, factor mínimo…) son provisionales
 * y viven en {@link KiParams} / {@link ReleaseParams}.
 *
 * <p><b>Cómo se aplica la liberación (función a trozos, decisión del usuario).</b> {@link #statEfectiva} es la ÚNICA
 * regla: a 0 % el jugador es vanilla (todos sus atributos valen 8); de 0 a 50 % sube linealmente hasta sus atributos del
 * mod; a 50 % tiene los del mod; de 50 a 100 % se multiplican hasta el doble.
 * <pre>
 *   liberación &lt;= 50:  efectiva = vanilla + (base - vanilla) × (liberación / 50)
 *   liberación &gt;  50:  efectiva = base × (liberación / 50)
 * </pre>
 * Es continua en 50 % (ambas ramas dan {@code base}). Con base 18 y vanilla 8: 0 % → 8, 25 % → 13, 50 % → 18,
 * 75 % → 27, 100 % → 36. Con base 8 (personaje nuevo): 0 % → 8, 50 % → 8, 100 % → 16.
 *
 * <p>VIT, FUE y VEL usan esa estadística efectiva en sus fórmulas de siempre ({@code (8/VIT)^3}, {@code (FUE/8)^3},
 * {@code (VEL/8)^exp}). No existe castigo por falta de ki: sin ki la liberación baja a 0 % y el jugador es vanilla
 * (no peor que vanilla). El aviso "¡Sin Ki!" es solo informativo.
 */
public final class KiSystem {
    private KiSystem() {}

    private static final BalanceParams D = BalanceParams.DEFAULT;
    public static final double TICKS_PER_SECOND = 20.0;

    /** Estado del recurso: ki actual y liberación actual (%). */
    public record Estado(double ki, double liberacion) {}

    // ---------- valores de los atributos

    public static int kiValue(int upgrades, BalanceParams p) {
        return (int) Math.round(p.ki().kiBase() + p.ki().kiGrowth() * checkUpgrades(upgrades, p.kiMaxUpgrades(), "KI"));
    }

    public static int conValue(int upgrades, BalanceParams p) {
        return (int) Math.round(p.ki().conBase() + p.ki().conGrowth() * checkUpgrades(upgrades, p.conMaxUpgrades(), "CON"));
    }

    /** Valor de POD tras {@code upgrades} mejoras (sin efecto todavía: llegará con las técnicas de ki). */
    public static int podValue(int upgrades, BalanceParams p) {
        return (int) Math.round(p.pod().podBase() + p.pod().podGrowth() * checkUpgrades(upgrades, p.podMaxUpgrades(), "POD"));
    }

    public static int velValue(int upgrades, BalanceParams p) {
        return (int) Math.round(p.release().velBase() + p.release().velGrowth() * checkUpgrades(upgrades, p.velMaxUpgrades(), "VEL"));
    }

    private static int checkUpgrades(int upgrades, int max, String name) {
        if (upgrades < 0 || upgrades > max) {
            throw new IllegalArgumentException("mejoras de " + name + " fuera de [0, " + max + "]: " + upgrades);
        }
        return upgrades;
    }

    // ---------- reserva, gasto y regeneración (variantes con los valores por defecto y con balance configurable)

    public static int reservaMaxima(int kiBase) { return reservaMaxima(kiBase, D); }
    public static double costeLiberacion(int con, double porcentaje) { return costeLiberacion(con, porcentaje, D); }
    public static double regeneracionPorTick(int kiBase, double porcentaje) { return regeneracionPorTick(kiBase, porcentaje, D); }

    /** {@code kiFlat + kiPerPoint × KI}: 50 con KI 0, 230 con KI 18. */
    public static int reservaMaxima(int kiValue, BalanceParams p) {
        if (kiValue < 0) throw new IllegalArgumentException("KI negativo: " + kiValue);
        return (int) Math.round(p.ki().kiFlat() + p.ki().kiPerPoint() * kiValue);
    }

    /** Ki que cuesta mantener la liberación durante un tick: {@code base × (liberación/100) / (1 + CON/60)}. */
    public static double costeLiberacion(int con, double porcentaje, BalanceParams p) {
        if (con < 0) throw new IllegalArgumentException("CON negativo: " + con);
        checkPorcentaje(porcentaje);
        double porTick = p.ki().costPerSecondAtFull() / TICKS_PER_SECOND;
        return porTick * (porcentaje / 100.0) / (1.0 + con / p.ki().conScale());
    }

    /** Ki que se regenera por tick: 0 con liberación >= 50 %; máxima a 0 %; lineal entre medias. Usa KI base. */
    public static double regeneracionPorTick(int kiValue, double porcentaje, BalanceParams p) {
        checkPorcentaje(porcentaje);
        double umbral = p.ki().regenBelowPercent();
        if (porcentaje >= umbral) return 0.0;
        double reserva = reservaMaxima(kiValue, p);
        double maxPorTick = reserva * (p.ki().regenPercentPerSecond() / 100.0) / TICKS_PER_SECOND;
        return maxPorTick * (1.0 - porcentaje / umbral);
    }

    /**
     * Estadística efectiva según la liberación (función a trozos, continua en el pivote). {@code statVanilla} es el
     * valor de un jugador sin poderes (8) y {@code pivote} el % en que se accede a la estadística del mod (50).
     * Ver el JavaDoc de la clase para los ejemplos.
     */
    public static double statEfectiva(double statBase, double statVanilla, double porcentaje, double pivote) {
        checkPorcentaje(porcentaje);
        if (statBase < 0 || Double.isNaN(statBase)) throw new IllegalArgumentException("estadística negativa o NaN: " + statBase);
        if (statVanilla < 0 || Double.isNaN(statVanilla)) throw new IllegalArgumentException("stat vanilla negativa o NaN: " + statVanilla);
        if (!(pivote > 0 && pivote <= 100)) throw new IllegalArgumentException("pivote fuera de (0, 100]: " + pivote);
        if (porcentaje <= pivote) return statVanilla + (statBase - statVanilla) * (porcentaje / pivote);
        return statBase * (porcentaje / pivote);
    }

    /** {@link #statEfectiva(double, double, double, double)} con el pivote del balance. */
    public static double statEfectiva(double statBase, double statVanilla, double porcentaje, BalanceParams p) {
        return statEfectiva(statBase, statVanilla, porcentaje, p.release().referencePercent());
    }

    // ---------- liberación: subida y bajada

    /**
     * Nuevo porcentaje tras {@code ticks} ticks. {@code direccion}: 1 sube (solo si {@code quieto}), -1 baja, 0 nada.
     * Nunca supera el límite ni baja de 0; si el límite se redujo, la liberación se recorta a él.
     */
    public static double siguientePorcentaje(double actual, double limite, int direccion, boolean quieto, int ticks, BalanceParams p) {
        checkPorcentaje(actual);
        if (limite < 0 || limite > 100) throw new IllegalArgumentException("límite fuera de [0, 100]: " + limite);
        if (ticks < 0) throw new IllegalArgumentException("ticks negativos: " + ticks);
        double seconds = ticks / TICKS_PER_SECOND;
        double next = actual;
        if (direccion > 0 && quieto) next += p.release().rampUpPerSecond() * seconds;
        else if (direccion < 0) next -= p.release().rampDownPerSecond() * seconds;
        return Math.max(0.0, Math.min(Math.min(next, limite), 100.0));
    }

    // ---------- paso de simulación

    /**
     * Aplica {@code ticks} ticks de gasto y regeneración. Si el ki llega a 0 la liberación pasa a 0 (decisión del usuario).
     * El ki nunca supera la reserva máxima ni baja de 0.
     */
    public static Estado avanzar(Estado e, int kiValue, int conValue, int ticks, BalanceParams p) {
        checkPorcentaje(e.liberacion());
        if (ticks < 0) throw new IllegalArgumentException("ticks negativos: " + ticks);
        double max = reservaMaxima(kiValue, p);
        double ki = Math.max(0.0, Math.min(e.ki(), max));
        double lib = e.liberacion();
        if (lib > 0.0) ki -= costeLiberacion(conValue, lib, p) * ticks;
        ki += regeneracionPorTick(kiValue, lib, p) * ticks;
        ki = Math.max(0.0, Math.min(ki, max));
        if (ki <= 0.0) lib = 0.0;
        return new Estado(ki, lib);
    }

    // ---------- VEL: movimiento y salto

    /** Multiplicador de la velocidad de movimiento: {@code clamp((VEL efectiva / base)^exponente, 0,5; máximo)}; a 0 % vale 1 (vanilla). */
    public static double factorVelocidad(int velValue, double porcentaje, BalanceParams p) {
        return factorMovimiento(velValue, porcentaje, p.release().speedExponent(), p.release().speedMax(), p);
    }

    /** Multiplicador del salto (misma idea con su propio exponente y tope). */
    public static double factorSalto(int velValue, double porcentaje, BalanceParams p) {
        return factorMovimiento(velValue, porcentaje, p.release().jumpExponent(), p.release().jumpMax(), p);
    }

    private static double factorMovimiento(int velValue, double porcentaje, double exponente, double maximo, BalanceParams p) {
        if (velValue < 1) throw new IllegalArgumentException("VEL debe ser >= 1: " + velValue);
        double velEfectiva = statEfectiva(velValue, p.release().velBase(), porcentaje, p);
        double raw = Math.pow(velEfectiva / p.release().velBase(), exponente);
        return Math.max(0.5, Math.min(maximo, raw));
    }

    /**
     * Corrección del FOV para que el bonus de VEL no estire la cámara. Vanilla usa {@code ((velocidad / velocidadAndando) + 1) / 2};
     * con el bonus (multiplicador total {@code 1 + amount}) la velocidad sin él es {@code velocidad / (1 + amount)}. Devuelve
     * {@code FOV sin bonus / FOV con bonus}: 1 sin bonus, menor que 1 con bonus.
     */
    public static double correccionFov(double velocidadConBonus, double velocidadAndando, double amount) {
        if (velocidadAndando <= 0 || amount <= -1.0) return 1.0;
        double con = (velocidadConBonus / velocidadAndando + 1.0) / 2.0;
        double sin = (velocidadConBonus / (1.0 + amount) / velocidadAndando + 1.0) / 2.0;
        return con <= 0 ? 1.0 : sin / con;
    }

    private static void checkPorcentaje(double porcentaje) {
        if (Double.isNaN(porcentaje) || porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("porcentaje de liberación fuera de [0, 100]: " + porcentaje);
        }
    }
}

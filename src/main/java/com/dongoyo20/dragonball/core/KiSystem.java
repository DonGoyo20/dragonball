package com.dongoyo20.dragonball.core;

/**
 * Recurso Ki y liberación de poder en Java puro (sin Minecraft). Decisiones del usuario (2026-10-12): reserva
 * {@code 50 + 10 × KI}; gasto por mantener la liberación {@code base / (1 + CON/60)}; solo se regenera con la liberación
 * por debajo del 50 % (máxima a 0 %); al quedarse sin ki la liberación baja a 0; solo sube si el jugador está quieto.
 *
 * <p>Los números que el diseño no fijaba (gasto base, regeneración, velocidad de subida, factor mínimo…) son provisionales
 * y viven en {@link KiParams} / {@link ReleaseParams}.
 *
 * <p><b>Cómo se aplica la liberación al combate.</b> La fórmula del usuario es {@code stat_efectiva = stat_base ×
 * (liberación / 100)} ({@link #statEfectiva}); es lo que muestra la pantalla de estadísticas. Pero las fórmulas de combate
 * ya verificadas (VIT 8 = daño de vanilla, FUE 8 = ×1) suponen la estadística "completa". Aplicarlas tal cual con el límite
 * inicial de 50 % dejaría a un personaje nuevo con VIT efectiva 4 (recibe ×8 de daño) y FUE efectiva 4 (hace ×0,125).
 * Por eso el combate usa {@link #factorLiberacion}: {@code max(minFactor, liberación / referencia)}, con referencia = 50 %.
 * Con la liberación inicial el factor es 1 y nada cambia respecto a lo ya verificado; al 100 % vale 2; al 0 % vale el mínimo.
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
    public static double statEfectiva(int statBase, double porcentaje) { return statEfectiva((double) statBase, porcentaje); }

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

    /** La fórmula del usuario: {@code stat_base × (liberación / 100)}. */
    public static double statEfectiva(double statBase, double porcentaje) {
        checkPorcentaje(porcentaje);
        if (statBase < 0 || Double.isNaN(statBase)) throw new IllegalArgumentException("estadística negativa o NaN: " + statBase);
        return statBase * (porcentaje / 100.0);
    }

    /** Factor que se aplica a VIT, FUE y VEL en combate y movimiento (ver el JavaDoc de la clase). */
    public static double factorLiberacion(double porcentaje, BalanceParams p) {
        checkPorcentaje(porcentaje);
        return Math.max(p.release().minFactor(), porcentaje / p.release().referencePercent());
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

    /** Multiplicador de la velocidad de movimiento: {@code clamp((VEL efectiva / base)^exponente, 0,5; máximo)}. */
    public static double factorVelocidad(int velValue, double porcentaje, BalanceParams p) {
        return factorMovimiento(velValue, porcentaje, p.release().speedExponent(), p.release().speedMax(), p);
    }

    /** Multiplicador del salto (misma idea con su propio exponente y tope). */
    public static double factorSalto(int velValue, double porcentaje, BalanceParams p) {
        return factorMovimiento(velValue, porcentaje, p.release().jumpExponent(), p.release().jumpMax(), p);
    }

    private static double factorMovimiento(int velValue, double porcentaje, double exponente, double maximo, BalanceParams p) {
        if (velValue < 1) throw new IllegalArgumentException("VEL debe ser >= 1: " + velValue);
        double velEfectiva = velValue * factorLiberacion(porcentaje, p);
        double raw = Math.pow(velEfectiva / p.release().velBase(), exponente);
        return Math.max(0.5, Math.min(maximo, raw));
    }

    private static void checkPorcentaje(double porcentaje) {
        if (Double.isNaN(porcentaje) || porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("porcentaje de liberación fuera de [0, 100]: " + porcentaje);
        }
    }
}

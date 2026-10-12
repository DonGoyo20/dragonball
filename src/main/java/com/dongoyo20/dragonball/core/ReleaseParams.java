package com.dongoyo20.dragonball.core;

/**
 * Números provisionales de la liberación de poder y de VEL (velocidad de movimiento y salto). Forman parte de
 * {@link BalanceParams}. Los marcados como provisionales son decisiones mías, no del diseño.
 *
 * @param initialLimit    límite de liberación de un personaje nuevo, en % (decisión del usuario: 50)
 * @param referencePercent % de liberación en el que el jugador accede a sus atributos del mod (decisión del usuario: 50);
 *                        es el punto de quiebre de la función a trozos ({@link KiSystem#statEfectiva}). Por debajo se
 *                        interpola desde vanilla; por encima se multiplican hasta el doble al 100 %
 * @param rampUpPerSecond <b>provisional</b>: % que sube la liberación por segundo manteniendo la tecla (solo quieto)
 * @param rampDownPerSecond <b>provisional</b>: % que baja por segundo manteniendo la tecla
 * @param velBase         valor inicial de VEL
 * @param velGrowth       cuánto sube VEL por mejora
 * @param speedExponent   <b>provisional</b>: exponente de la velocidad de movimiento respecto a VEL efectiva / base
 * @param speedMax        <b>provisional</b>: tope del multiplicador de velocidad de movimiento
 * @param jumpExponent    <b>provisional</b>: exponente del salto
 * @param jumpMax         <b>provisional</b>: tope del multiplicador de salto
 */
public record ReleaseParams(
        double initialLimit, double referencePercent,
        double rampUpPerSecond, double rampDownPerSecond,
        double velBase, double velGrowth, double speedExponent, double speedMax, double jumpExponent, double jumpMax) {

    public static final ReleaseParams DEFAULT = new ReleaseParams(50.0, 50.0, 10.0, 20.0, 8.0, 1.0, 0.5, 2.5, 0.25, 1.8);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!(initialLimit >= 0 && initialLimit <= 100)) return "release.initial_limit debe estar entre 0 y 100";
        if (!(referencePercent > 0 && referencePercent <= 100)) return "release.reference_percent debe estar entre 0 (excluido) y 100";
        if (!(rampUpPerSecond > 0 && rampUpPerSecond <= 1000)) return "release.ramp_up_per_second debe estar entre 0 (excluido) y 1000";
        if (!(rampDownPerSecond > 0 && rampDownPerSecond <= 1000)) return "release.ramp_down_per_second debe estar entre 0 (excluido) y 1000";
        if (!(velBase >= 1 && Double.isFinite(velBase))) return "release.vel_base debe ser un número >= 1";
        if (!(velGrowth > 0 && Double.isFinite(velGrowth))) return "release.vel_growth debe ser un número > 0";
        if (!(speedExponent >= 0 && speedExponent <= 4)) return "release.speed_exponent debe estar entre 0 y 4";
        if (!(speedMax >= 1 && speedMax <= 10)) return "release.speed_max debe estar entre 1 y 10";
        if (!(jumpExponent >= 0 && jumpExponent <= 4)) return "release.jump_exponent debe estar entre 0 y 4";
        if (!(jumpMax >= 1 && jumpMax <= 10)) return "release.jump_max debe estar entre 1 y 10";
        return null;
    }
}

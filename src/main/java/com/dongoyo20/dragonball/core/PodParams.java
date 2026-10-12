package com.dongoyo20.dragonball.core;

/**
 * Números provisionales de POD (poder de ki). POD no tiene efecto todavía: solo existen su valor y su compra. Su efecto
 * (daño de las técnicas de ki) llegará con las técnicas.
 *
 * @param podBase   <b>provisional</b>: valor inicial de POD (8, como VIT, FUE, DEF y VEL)
 * @param podGrowth cuánto sube POD por mejora
 */
public record PodParams(double podBase, double podGrowth) {

    public static final PodParams DEFAULT = new PodParams(8.0, 1.0);

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!(Double.isFinite(podBase) && podBase >= 0)) return "pod.pod_base debe ser un número >= 0";
        if (!(Double.isFinite(podGrowth) && podGrowth > 0)) return "pod.pod_growth debe ser un número > 0";
        return null;
    }
}

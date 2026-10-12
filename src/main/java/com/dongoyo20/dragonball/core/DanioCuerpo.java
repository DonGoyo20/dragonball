package com.dongoyo20.dragonball.core;

/**
 * Con qué golpea el jugador cuerpo a cuerpo y si eso escala con FUE (decisión del usuario, 2026-10-12).
 * (Se escribe {@code PUNO} sin eñe para evitar problemas de codificación en el código fuente.)
 */
public enum DanioCuerpo {
    /** Sin arma (mano vacía o un objeto que no es un arma): escala con FUE. */
    PUNO(true),
    /** Arma del mod (ítem del tag {@code dragonball:armas_del_mod}): escala con FUE. */
    ARMA_DEL_MOD(true),
    /** Arma o herramienta de vanilla (espada, hacha, maza…): no escala. */
    ARMA_VANILLA(false);

    private final boolean escalaConFuerza;

    DanioCuerpo(boolean escalaConFuerza) {
        this.escalaConFuerza = escalaConFuerza;
    }

    public boolean escalaConFuerza() {
        return escalaConFuerza;
    }

    /**
     * Clasificación pura (sin Minecraft): el juego calcula los tres datos a partir del {@code ItemStack}.
     * El tag del mod manda sobre todo lo demás.
     *
     * @param manoVacia          no hay nada en la mano principal
     * @param enTagArmasDelMod   el ítem está en {@code dragonball:armas_del_mod}
     * @param tieneDanioDeAtaque el ítem añade daño de ataque (es un arma o herramienta de vanilla)
     */
    public static DanioCuerpo clasificar(boolean manoVacia, boolean enTagArmasDelMod, boolean tieneDanioDeAtaque) {
        if (enTagArmasDelMod) return ARMA_DEL_MOD;
        if (manoVacia || !tieneDanioDeAtaque) return PUNO;
        return ARMA_VANILLA;
    }
}

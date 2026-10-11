package com.dongoyo20.dragonball.core;

/**
 * Reglas de progresión en Java puro (sin Minecraft), para poder probarlas sin arrancar el juego.
 * Espejo de SYSTEMS.md §2 y del simulador (tools/simulator/bestiary.py); los mismos casos se prueban en ambos.
 */
public final class Progression {
    private Progression() {}

    /** Atributos en el orden de SYSTEMS.md: VIT, FUE, DEF, VEL, KI, POD, CON. */
    public static final int ATTRIBUTE_COUNT = 7;
    /** Tope de cada atributo (decisión del usuario). */
    public static final int ATTRIBUTE_CAP = 99;
    /** Multiplicador de XP para mobs que vienen de generadores (anti-granjas). */
    public static final double SPAWNER_XP_MULTIPLIER = 0.25;

    /** Coste en XP de la siguiente mejora estando en nivel total {@code level} (>= 1). */
    public static double costNextUpgrade(int level) {
        return 10.0 * Math.pow(1.0 + (level - 1) / 5.0, 1.6);
    }

    /** XP acumulada necesaria para llegar al nivel total {@code level} desde el 1. */
    public static double cumulativeXp(int level) {
        double total = 0;
        for (int n = 1; n < level; n++) {
            total += costNextUpgrade(n);
        }
        return total;
    }

    /**
     * Factor de XP según la diferencia de nivel (SYSTEMS.md §2.5).
     * Enemigo más fuerte: +2 % por nivel (tope 1,5). Más débil: -3 % por nivel, mínimo 0.
     */
    public static double levelFactor(int playerLevel, int enemyLevel) {
        int dif = playerLevel - enemyLevel;
        if (dif < 0) {
            return 1.0 + Math.min(0.02 * -dif, 0.5);
        }
        return Math.max(0.0, 1.0 - 0.03 * dif);
    }

    /** XP final por derrotar a un enemigo con {@code baseXp}. */
    public static int killXp(int baseXp, int playerLevel, int enemyLevel, boolean fromSpawner) {
        double origin = fromSpawner ? SPAWNER_XP_MULTIPLIER : 1.0;
        return (int) Math.round(baseXp * levelFactor(playerLevel, enemyLevel) * origin);
    }
}

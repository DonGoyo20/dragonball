package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Progreso RPG del jugador (se guarda con el mundo como adjunto de datos).
 * XP sin gastar y mejoras por atributo; el nivel total es 1 + número de mejoras.
 */
public record PlayerProgress(long xp, List<Integer> upgrades) {

    public static final PlayerProgress EMPTY = new PlayerProgress(0, zeros());

    public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("xp", 0L).forGetter(PlayerProgress::xp),
            Codec.INT.listOf().optionalFieldOf("upgrades", zeros()).forGetter(PlayerProgress::upgrades)
    ).apply(i, PlayerProgress::normalized));

    private static List<Integer> zeros() {
        List<Integer> l = new ArrayList<>();
        for (int n = 0; n < Progression.ATTRIBUTE_COUNT; n++) l.add(0);
        return List.copyOf(l);
    }

    /** Garantiza siete valores no negativos aunque el guardado esté incompleto o dañado. */
    private static PlayerProgress normalized(long xp, List<Integer> upgrades) {
        List<Integer> fixed = new ArrayList<>();
        for (int n = 0; n < Progression.ATTRIBUTE_COUNT; n++) {
            fixed.add(n < upgrades.size() ? Math.max(0, upgrades.get(n)) : 0);
        }
        return new PlayerProgress(Math.max(0, xp), List.copyOf(fixed));
    }

    /** Nivel total = 1 + número de mejoras. */
    public int totalLevel() {
        int sum = 0;
        for (int u : upgrades) sum += u;
        return 1 + sum;
    }

    public PlayerProgress withXp(long newXp) {
        return new PlayerProgress(Math.max(0, newXp), upgrades);
    }

    public PlayerProgress addXp(long amount) {
        return withXp(xp + amount);
    }

    /** Coste de la próxima mejora al nivel actual. */
    public long nextUpgradeCost() {
        return Math.round(Progression.costNextUpgrade(totalLevel()));
    }

    /** ¿El atributo ya está en el tope 99? Por ahora solo VIT (0) y FUE (1) tienen valores definidos; el resto, pendiente. */
    public boolean isAtCap(int index) {
        return switch (index) {
            case 0 -> !HealthSystem.canUpgradeVit(upgrades.get(0), BalanceManager.get());
            case 1 -> !FuerzaSystem.canUpgradeFue(upgrades.get(1), BalanceManager.get());
            default -> false;
        };
    }

    /** Intenta comprar una mejora en el atributo {@code index}; devuelve null si no hay XP o el atributo ya está al tope. */
    public PlayerProgress tryUpgrade(int index) {
        if (index < 0 || index >= Progression.ATTRIBUTE_COUNT) return null;
        if (isAtCap(index)) return null;
        long cost = nextUpgradeCost();
        if (xp < cost) return null;
        List<Integer> next = new ArrayList<>(upgrades);
        next.set(index, next.get(index) + 1);
        return new PlayerProgress(xp - cost, List.copyOf(next));
    }
}

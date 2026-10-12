package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.ki.KiData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Progreso RPG del jugador (se guarda con el mundo como adjunto de datos).
 * XP sin gastar y mejoras por atributo; el nivel total es 1 + número de mejoras.
 */
public record PlayerProgress(long xp, List<Integer> upgrades, KiData ki) {

    /** Constructor sin ki: un personaje con el ki inicial (reserva llena y liberación inicial). */
    public PlayerProgress(long xp, List<Integer> upgrades) {
        this(xp, upgrades, KiData.INITIAL);
    }

    public static final PlayerProgress EMPTY = new PlayerProgress(0, zeros());

    /** Los guardados anteriores al ki no tienen el campo "ki": se cargan con el ki inicial. */
    public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("xp", 0L).forGetter(PlayerProgress::xp),
            Codec.INT.listOf().optionalFieldOf("upgrades", zeros()).forGetter(PlayerProgress::upgrades),
            KiData.CODEC.optionalFieldOf("ki", KiData.INITIAL).forGetter(PlayerProgress::ki)
    ).apply(i, PlayerProgress::normalized));

    /** Sincronización al cliente (se envía como NBT del mismo códec que se guarda, así los dos formatos no divergen). */
    public static final StreamCodec<ByteBuf, PlayerProgress> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private static List<Integer> zeros() {
        List<Integer> l = new ArrayList<>();
        for (int n = 0; n < Progression.ATTRIBUTE_COUNT; n++) l.add(0);
        return List.copyOf(l);
    }

    /** Garantiza siete valores no negativos aunque el guardado esté incompleto o dañado. */
    private static PlayerProgress normalized(long xp, List<Integer> upgrades, KiData ki) {
        List<Integer> fixed = new ArrayList<>();
        for (int n = 0; n < Progression.ATTRIBUTE_COUNT; n++) {
            fixed.add(n < upgrades.size() ? Math.max(0, upgrades.get(n)) : 0);
        }
        return new PlayerProgress(Math.max(0, xp), List.copyOf(fixed), ki);
    }

    /** Nivel total = 1 + número de mejoras. */
    public int totalLevel() {
        int sum = 0;
        for (int u : upgrades) sum += u;
        return 1 + sum;
    }

    public PlayerProgress withXp(long newXp) {
        return new PlayerProgress(Math.max(0, newXp), upgrades, ki);
    }

    public PlayerProgress withKi(KiData newKi) {
        return new PlayerProgress(xp, upgrades, newKi);
    }

    public PlayerProgress addXp(long amount) {
        return withXp(xp + amount);
    }

    /** Coste de la próxima mejora al nivel actual. */
    public long nextUpgradeCost() {
        return Math.round(Progression.costNextUpgrade(totalLevel()));
    }

    /**
     * ¿El atributo ya está en el tope? Solo tienen valores definidos VIT (0), FUE (1), VEL (3), KI (4) y CON (6);
     * DEF (2) y POD (5) aún no (ciclos pendientes) y no tienen tope.
     */
    public boolean isAtCap(int index) {
        BalanceParams p = BalanceManager.get();
        return switch (index) {
            case 0 -> !HealthSystem.canUpgradeVit(upgrades.get(0), p);
            case 1 -> !FuerzaSystem.canUpgradeFue(upgrades.get(1), p);
            case 3 -> upgrades.get(3) >= p.velMaxUpgrades();
            case 4 -> upgrades.get(4) >= p.kiMaxUpgrades();
            case 6 -> upgrades.get(6) >= p.conMaxUpgrades();
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
        return new PlayerProgress(xp - cost, List.copyOf(next), ki);
    }
}

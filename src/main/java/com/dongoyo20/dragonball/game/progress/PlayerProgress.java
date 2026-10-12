package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Atributos;
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
     * ¿El atributo ya está en el tope (valor 99)? Con el valor inicial por defecto (Terrícola). Con otra raza hay que pasar su
     * valor inicial: {@link #isAtCap(int, int)}.
     */
    public boolean isAtCap(int index) {
        return isAtCap(index, Atributos.baseDefecto(Atributo.values()[index], BalanceManager.get()));
    }

    /** ¿El atributo ya está en el tope, partiendo del valor inicial {@code raceBase} de la raza? (menos mejoras cuanto más alto empieza) */
    public boolean isAtCap(int index, int raceBase) {
        return !Atributos.puedeMejorar(Atributo.values()[index], raceBase, upgrades.get(index), BalanceManager.get());
    }

    /** Intenta comprar una mejora en el atributo {@code index}; devuelve null si no hay XP o el atributo ya está al tope. */
    public PlayerProgress tryUpgrade(int index) {
        if (index < 0 || index >= Progression.ATTRIBUTE_COUNT) return null;
        return tryUpgrade(index, Atributos.baseDefecto(Atributo.values()[index], BalanceManager.get()));
    }

    /** Igual, partiendo del valor inicial {@code raceBase} de la raza (el coste es el mismo para todas las razas). */
    public PlayerProgress tryUpgrade(int index, int raceBase) {
        if (index < 0 || index >= Progression.ATTRIBUTE_COUNT) return null;
        if (isAtCap(index, raceBase)) return null;
        long cost = nextUpgradeCost();
        if (xp < cost) return null;
        List<Integer> next = new ArrayList<>(upgrades);
        next.set(index, next.get(index) + 1);
        return new PlayerProgress(xp - cost, List.copyOf(next), ki);
    }
}

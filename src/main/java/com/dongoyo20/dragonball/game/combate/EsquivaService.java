package com.dongoyo20.dragonball.game.combate;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.EsquivaSystem;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.ki.KiService;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Resuelve la esquiva de un jugador (ver {@link EsquivaSystem}). La probabilidad usa la VEL efectiva del jugador (función a
 * trozos de la liberación); esquivar cuesta ki y, sin ki suficiente, el golpe se recibe y se avisa (como mucho cada 2 s).
 */
public final class EsquivaService {
    private EsquivaService() {}

    /** Aviso de "sin ki para esquivar" como máximo una vez cada 40 ticks por jugador. */
    public static final int NO_KI_WARNING_COOLDOWN_TICKS = 40;

    private static final Map<UUID, Long> LAST_NO_KI_WARNING = new ConcurrentHashMap<>();
    private static volatile DoubleSupplier rollOverride = null;
    private static BiConsumer<ServerPlayer, Component> messageSink = (player, text) -> player.displayClientMessage(text, true);

    /** Solo para pruebas: fija la tirada (null = aleatoria) y devuelve la anterior. */
    public static DoubleSupplier setRollForTesting(DoubleSupplier roll) {
        DoubleSupplier old = rollOverride;
        rollOverride = roll;
        return old;
    }

    /** Solo para pruebas: cambia el destino de los avisos y devuelve el anterior. */
    public static BiConsumer<ServerPlayer, Component> setMessageSinkForTesting(BiConsumer<ServerPlayer, Component> sink) {
        BiConsumer<ServerPlayer, Component> old = messageSink;
        messageSink = sink;
        return old;
    }

    public static void forget(UUID player) {
        LAST_NO_KI_WARNING.remove(player);
    }

    /**
     * ¿Es un golpe que se puede esquivar? Solo el daño causado por una entidad (mob, jugador, proyectil). No el de entorno
     * (lava, fuego, caída, ahogamiento, congelación, rayos), ni las explosiones, ni lo que salta la invulnerabilidad
     * (el vacío y {@code /kill}).
     */
    public static boolean esEsquivable(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_LIGHTNING)) {
            return false;
        }
        return source.getEntity() != null || source.getDirectEntity() != null;
    }

    /** VEL efectiva del jugador según su liberación (vanilla 8 a 0 %, la del mod a 50 %, el doble a 100 %). */
    public static double velEfectiva(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        return KiSystem.statEfectiva(KiService.velValue(player), p.release().velBase(), KiService.liberacion(player), p);
    }

    /** Probabilidad actual de esquivar un golpe. */
    public static double probabilidad(ServerPlayer player) {
        return EsquivaSystem.probabilidadEsquiva(velEfectiva(player), BalanceManager.get());
    }

    /** Coste en ki de esquivar un golpe de {@code danio} HP de vanilla. */
    public static int coste(ServerPlayer player, double danio) {
        return EsquivaSystem.costeKiEsquiva(danio, KiService.conValue(player), BalanceManager.get());
    }

    /**
     * Intenta esquivar un golpe. Devuelve {@code true} si se esquiva (el llamador debe anular el daño): ya se ha gastado el ki y
     * se han lanzado la partícula, el sonido y el aviso. Devuelve {@code false} si no es esquivable, si la tirada no sale o si no
     * hay ki suficiente.
     */
    public static boolean intentar(ServerPlayer player, float danio, DamageSource source) {
        if (!player.isAlive() || player.isSpectator() || player.getAbilities().invulnerable) return false;
        if (!(danio > 0) || !esEsquivable(source)) return false;

        double roll = rollOverride != null ? rollOverride.getAsDouble() : player.getRandom().nextDouble();
        if (!EsquivaSystem.sale(probabilidad(player), roll)) return false;

        KiData ki = ProgressService.get(player).ki();
        int cost = coste(player, danio);
        if (ki.kiActual() < cost) {
            avisarSinKi(player);
            return false;
        }
        ProgressService.setKi(player, ki.withKi(ki.kiActual() - cost));
        efectos(player);
        messageSink.accept(player, Component.translatable("dragonball.combat.dodged"));
        return true;
    }

    private static void avisarSinKi(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_NO_KI_WARNING.get(player.getUUID());
        if (last != null && now >= last && now - last < NO_KI_WARNING_COOLDOWN_TICKS) return;
        LAST_NO_KI_WARNING.put(player.getUUID(), now);
        messageSink.accept(player, Component.translatable("dragonball.combat.no_ki_to_dodge"));
    }

    /** Destello blanco y azulado y un sonido corto (partículas de vanilla, enviadas desde el servidor). */
    private static void efectos(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double x = player.getX(), y = player.getY() + player.getBbHeight() * 0.5, z = player.getZ();
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 12, 0.35, 0.5, 0.35, 0.05);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 10, 0.4, 0.5, 0.4, 0.2);
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6F, 1.6F);
    }
}

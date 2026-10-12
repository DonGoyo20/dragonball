package com.dongoyo20.dragonball.game.health;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.TipoDanio;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.resources.ResourceKey;

/** Puente entre el jugador de Minecraft y las fórmulas de {@link HealthSystem}. No guarda estado propio. */
public final class HealthService {
    private HealthService() {}

    /** Valor actual de VIT del jugador (base + mejoras). */
    public static int vit(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        int upgrades = ProgressService.get(player).upgrades().get(ProgressService.attributeIndex("vit"));
        // Un guardado con más mejoras que el tope no debe romper el combate: se trata como el máximo.
        return HealthSystem.vitValue(Math.min(upgrades, p.vitMaxUpgrades()), p);
    }

    public static double vidaRealMaxima(ServerPlayer player) {
        return HealthSystem.vidaRealMaxima(vit(player), BalanceManager.get());
    }

    public static double vidaRealActual(ServerPlayer player) {
        return HealthSystem.vidaRealActual(player.getHealth(), vidaRealMaxima(player));
    }

    /** Daño de vanilla (ya reducido por armadura y efectos) convertido al daño que se debe restar. */
    public static float aplicarDanioEscalado(ServerPlayer player, float danioVanilla, TipoDanio tipo) {
        return (float) HealthSystem.danioAplicado(danioVanilla, vit(player), tipo, BalanceManager.get());
    }

    /** Daño de estado continuo: se aplica cada pocos ticks mientras dure la situación (lava, fuego, veneno…). */
    private static final Set<ResourceKey<DamageType>> CONTINUOS = Set.of(
            DamageTypes.LAVA, DamageTypes.IN_FIRE, DamageTypes.ON_FIRE, DamageTypes.HOT_FLOOR, DamageTypes.CAMPFIRE,
            DamageTypes.DROWN, DamageTypes.CACTUS, DamageTypes.SWEET_BERRY_BUSH, DamageTypes.FREEZE,
            DamageTypes.WITHER, DamageTypes.MAGIC, DamageTypes.DRAGON_BREATH, DamageTypes.DRY_OUT,
            DamageTypes.STARVE, DamageTypes.IN_WALL, DamageTypes.CRAMMING, DamageTypes.OUTSIDE_BORDER);

    /**
     * Clasifica un daño en las tres categorías de SYSTEMS.md §3.1.
     * <ul>
     *   <li><b>NO_ESCALA</b>: {@code fell_out_of_world} (vacío) y {@code generic_kill} (/kill). Son mecanismos de
     *       vanilla para <i>sacar</i> al jugador del mundo o matarlo a propósito; si escalaran, el vacío dejaría de
     *       servir como red de seguridad del mundo y /kill no mataría a un jugador con mucha VIT.</li>
     *   <li><b>ESCALA_CON_SUELO</b>: los tipos de {@link #CONTINUOS}. {@code MAGIC} cubre el veneno, pero también
     *       las pociones de daño instantáneo (comparten tipo de daño y no se pueden distinguir).</li>
     *   <li><b>ESCALA_SIN_SUELO</b>: todo lo demás (criaturas, flechas, caída, explosiones de TNT, creeper o cama…).</li>
     * </ul>
     */
    public static TipoDanio clasificar(DamageSource source) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            return TipoDanio.NO_ESCALA;
        }
        for (ResourceKey<DamageType> tipo : CONTINUOS) {
            if (source.is(tipo)) return TipoDanio.ESCALA_CON_SUELO;
        }
        return TipoDanio.ESCALA_SIN_SUELO;
    }
}

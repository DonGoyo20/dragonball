package com.dongoyo20.dragonball.game.health;

import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;

/** Puente entre el jugador de Minecraft y las fórmulas de {@link HealthSystem}. No guarda estado propio. */
public final class HealthService {
    private HealthService() {}

    /** Valor actual de VIT del jugador (base + mejoras). */
    public static int vit(ServerPlayer player) {
        int upgrades = ProgressService.get(player).upgrades().get(ProgressService.attributeIndex("vit"));
        // Un guardado con más mejoras que el tope no debe romper el combate: se trata como el máximo.
        return HealthSystem.vitValue(Math.min(upgrades, HealthSystem.VIT_MAX_UPGRADES));
    }

    public static double vidaRealMaxima(ServerPlayer player) {
        return HealthSystem.vidaRealMaxima(vit(player));
    }

    public static double vidaRealActual(ServerPlayer player) {
        return HealthSystem.vidaRealActual(player.getHealth(), vidaRealMaxima(player));
    }

    /** Daño de vanilla (ya reducido por armadura y efectos) convertido al daño que se debe restar. */
    public static float aplicarDanioEscalado(ServerPlayer player, float danioVanilla) {
        return (float) HealthSystem.danioAplicado(danioVanilla, vit(player));
    }

    /**
     * Hay "atacante" cuando el causante del daño es una criatura viva: zombi (cuerpo a cuerpo), esqueleto (la flecha
     * es la entidad directa, el esqueleto el causante), creeper (el creeper es el causante de su explosión).
     * La TNT encendida es entorno aunque la haya encendido un jugador: su entidad directa es la TNT.
     */
    public static boolean esDanioDeEntorno(DamageSource source) {
        boolean hayAtacante = source.getEntity() instanceof LivingEntity
                && !(source.getDirectEntity() instanceof PrimedTnt);
        return HealthSystem.esDanioDeEntorno(hayAtacante);
    }
}

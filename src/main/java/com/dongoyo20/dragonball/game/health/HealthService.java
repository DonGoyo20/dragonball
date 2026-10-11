package com.dongoyo20.dragonball.game.health;

import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

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

    /** Sin entidad atacante (ni directa ni causante) = daño de entorno. */
    public static boolean esDanioDeEntorno(DamageSource source) {
        return HealthSystem.esDanioDeEntorno(source.getEntity() != null || source.getDirectEntity() != null);
    }
}

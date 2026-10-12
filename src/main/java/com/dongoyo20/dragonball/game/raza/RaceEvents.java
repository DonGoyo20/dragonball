package com.dongoyo20.dragonball.game.raza;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Al entrar al mundo, si el jugador no tiene raza (o la suya ya no existe) se le asigna la raza por defecto. La raza NO se
 * reinicia al morir ni al cambiar de dimensión: es un attachment con {@code copyOnDeath} y los datos del jugador viajan con él.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class RaceEvents {
    private RaceEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RaceService.ensureRace(player);
    }
}

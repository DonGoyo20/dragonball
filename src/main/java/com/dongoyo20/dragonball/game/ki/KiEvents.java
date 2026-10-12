package com.dongoyo20.dragonball.game.ki;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Tick del recurso Ki en el servidor. */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class KiEvents {
    private KiEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && KiService.countTick(player.getUUID())) {
            KiService.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        KiService.forget(event.getEntity().getUUID());
    }
}

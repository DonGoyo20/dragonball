package com.dongoyo20.dragonball.client;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.client.hud.PlayerHud;
import com.dongoyo20.dragonball.client.screen.StatsScreen;
import com.dongoyo20.dragonball.game.network.ReleaseInputPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Entrada del jugador en el cliente: teclas de liberación, HUD y pantalla de estadísticas. */
@EventBusSubscriber(modid = DragonBall.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    /** Última dirección enviada al servidor (solo se envía al cambiar, no cada tick). */
    private static byte lastSent = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        while (KeyBindings.TOGGLE_HUD.consumeClick()) {
            PlayerHud.toggle();
        }
        while (KeyBindings.STATS.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new StatsScreen());
        }

        // Con una pantalla abierta (inventario, chat…) las teclas no cuentan: se suelta la liberación.
        byte direction = 0;
        if (mc.screen == null) {
            boolean up = KeyBindings.RELEASE_UP.isDown();
            boolean down = KeyBindings.RELEASE_DOWN.isDown();
            direction = (byte) (up == down ? 0 : (up ? 1 : -1));
        }
        if (direction != lastSent) {
            lastSent = direction;
            PacketDistributor.sendToServer(new ReleaseInputPayload(direction));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        lastSent = 0;
    }
}

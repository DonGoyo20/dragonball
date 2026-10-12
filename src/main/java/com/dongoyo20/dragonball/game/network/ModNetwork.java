package com.dongoyo20.dragonball.game.network;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.ki.KiService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registro de los dos paquetes propios del mod. */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ReleaseInputPayload.TYPE, ReleaseInputPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        KiService.setInput(player.getUUID(), payload.direction());
                    }
                }));
        registrar.playToClient(BalanceSyncPayload.TYPE, BalanceSyncPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> BalanceManager.setFromServer(payload.params())));
    }
}

package com.dongoyo20.dragonball.game.combate;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Esquiva en el servidor. Se usa {@link LivingIncomingDamageEvent} (cancelable, antes de armadura, retroceso y animación de
 * herido) y no {@code LivingDamageEvent.Pre} (no cancelable: el golpe ya habría empujado al jugador), para que esquivar sea
 * de verdad no recibir el golpe. Va antes que la reducción de VIT/DEF, que solo actúa en {@code Pre}.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class EsquivaEvents {
    private EsquivaEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && EsquivaService.intentar(player, event.getAmount(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EsquivaService.forget(event.getEntity().getUUID());
    }
}

package com.dongoyo20.dragonball.game.health;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Aplica VIT al daño recibido por los jugadores.
 *
 * <p><b>Fórmula (provisional):</b> {@code daño aplicado = daño vanilla × (8 / VIT)}. El jugador conserva 20 HP de
 * vanilla; su "vida real" es {@code hp/20 × 12,5 × VIT}. Con VIT 8 el daño es el de vanilla; con VIT 18 el mismo
 * golpe quita 8/18 del daño.
 *
 * <p><b>Deuda técnica:</b> el diseño (SYSTEMS.md §3.2) usa {@code D = DEF^0,7 × VIT^0,3}; esta versión lineal es más
 * fuerte y no depende del poder del atacante. Debe sustituirse cuando existan DEF y el poder de los enemigos.
 *
 * <p><b>Por qué {@link LivingDamageEvent.Pre} y no {@code LivingIncomingDamageEvent}:</b> Pre se dispara dentro de
 * {@code actuallyHurt}, después de la armadura, los encantamientos y las pociones de vanilla, y justo antes de restar
 * vida. Así la protección de vanilla sigue funcionando y solo se reescala el daño que de verdad iba a caer.
 * Solo se escala el daño causado por entidades; el de entorno (caída, lava, ahogamiento, /kill) queda igual.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class HealthEvents {
    private HealthEvents() {}

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (HealthService.esDanioDeEntorno(event.getSource())) return;
        float amount = event.getNewDamage();
        if (amount <= 0.0F || Float.isNaN(amount)) return;
        event.setNewDamage(HealthService.aplicarDanioEscalado(player, amount));
    }
}

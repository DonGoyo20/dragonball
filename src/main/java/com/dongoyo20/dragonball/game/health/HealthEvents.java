package com.dongoyo20.dragonball.game.health;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.TipoDanio;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Aplica VIT al daño recibido por los jugadores.
 *
 * <p><b>Fórmula (provisional):</b> {@code daño aplicado = daño vanilla × (8 / VIT)^3}. El jugador conserva 20 HP de
 * vanilla; su "vida real" es {@code hp/20 × 12,5 × VIT}. Con VIT 8 el daño es el de vanilla; con VIT 18 el mismo
 * golpe quita ~8,8 % del daño; con VIT 30, ~1,9 %.
 *
 * <p><b>Deuda técnica:</b> el diseño (SYSTEMS.md §3.2) usa {@code D = DEF^0,7 × VIT^0,3}; esta versión cúbica es mucho
 * más fuerte y no depende del poder del atacante. Debe sustituirse cuando existan DEF y el poder de los enemigos.
 *
 * <p><b>Por qué {@link LivingDamageEvent.Pre} y no {@code LivingIncomingDamageEvent}:</b> Pre se dispara dentro de
 * {@code actuallyHurt}, después de la armadura, los encantamientos y las pociones de vanilla, y justo antes de restar
 * vida. Así la protección de vanilla sigue funcionando y solo se reescala el daño que de verdad iba a caer.
 * Todo el daño escala salvo el vacío y {@code /kill}; el de estado continuo (lava, fuego, ahogamiento, veneno…) tiene un
 * suelo de 0,5 HP por aplicación. Los porqués y la lista completa están en {@link HealthService#clasificar}.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class HealthEvents {
    private HealthEvents() {}

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        TipoDanio tipo = HealthService.clasificar(event.getSource());
        if (tipo == TipoDanio.NO_ESCALA) return;
        float amount = event.getNewDamage();
        if (amount <= 0.0F || Float.isNaN(amount)) return;
        event.setNewDamage(HealthService.aplicarDanioEscalado(player, amount, tipo));
    }
}

package com.dongoyo20.dragonball.game.damage;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Aplica FUE al daño cuerpo a cuerpo que hacen los jugadores.
 *
 * <p><b>Fórmula (provisional):</b> {@code daño saliente = daño vanilla × (FUE / 8)^3}, simétrica a la de VIT. Sin tope.
 *
 * <p><b>Qué escala:</b> solo el golpe directo del jugador (tipo {@code player_attack} y el jugador es la entidad
 * directa). Si la mano está vacía (o sostiene algo que no es un arma) es un puñetazo y escala; si sostiene un arma del
 * mod (tag {@code dragonball:armas_del_mod}) también escala; las armas y herramientas de vanilla no.
 * Las <b>flechas y proyectiles</b> no escalan: su entidad directa es el proyectil, no el jugador, y el tipo de daño es
 * otro ({@code arrow}, {@code trident}…). Las técnicas de ki no escalan con FUE: escalarán con POD en su ciclo.
 * Solo afecta a jugadores: los mobs no tienen FUE.
 *
 * <p>Se usa {@link LivingDamageEvent.Pre} (como VIT): después de la armadura del defensor y justo antes de restar vida.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class FuerzaEvents {
    private FuerzaEvents() {}

    /** El golpe es del propio jugador, cuerpo a cuerpo (no un proyectil ni una explosión suya). */
    public static boolean esGolpeDirectoDeJugador(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() instanceof ServerPlayer
                && source.getDirectEntity() == source.getEntity();
    }

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (!esGolpeDirectoDeJugador(source)) return;
        ServerPlayer attacker = (ServerPlayer) source.getEntity();
        float amount = event.getNewDamage();
        if (amount <= 0.0F || Float.isNaN(amount)) return;
        event.setNewDamage(FuerzaService.danioFinal(attacker, attacker.getItemInHand(InteractionHand.MAIN_HAND), amount));
    }
}

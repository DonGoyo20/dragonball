package com.dongoyo20.dragonball.client;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.game.ki.KiService;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

/**
 * El campo de visión de vanilla crece con la velocidad de movimiento (el efecto de la poción de velocidad). El bonus de VEL
 * es un modificador de esa velocidad, así que lo "deshacemos" en el cálculo del FOV: correr con VEL alta no estira la cámara
 * más que correr en vanilla. La velocidad real no cambia.
 */
@EventBusSubscriber(modid = DragonBall.MOD_ID, value = Dist.CLIENT)
public final class VelFovFix {
    private VelFovFix() {}

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        AttributeInstance speed = event.getPlayer().getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        AttributeModifier mod = speed.getModifier(KiService.SPEED_ID);
        if (mod == null) return;
        event.setNewFovModifier((float) (event.getNewFovModifier() * KiSystem.correccionFov(speed.getValue(), event.getPlayer().getAbilities().getWalkingSpeed(), mod.amount())));
    }
}

package com.dongoyo20.dragonball.client;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.entity.ModEntities;
import com.dongoyo20.dragonball.game.entity.WildWolf;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Renderizador GeckoLib del lobo salvaje. Rutas: geo/entity/wild_wolf.geo.json, animations/entity/..., textures/entity/... */
public class WildWolfRenderer extends GeoEntityRenderer<WildWolf> {

    public WildWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "wild_wolf"), "head"));
        this.shadowRadius = 0.5F;
    }

    @EventBusSubscriber(modid = DragonBall.MOD_ID, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.WILD_WOLF.get(), WildWolfRenderer::new);
        }
    }
}

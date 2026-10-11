package com.dongoyo20.dragonball.game.entity;

import com.dongoyo20.dragonball.DragonBall;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DragonBall.MOD_ID);

    public static final Supplier<EntityType<WildWolf>> WILD_WOLF = ENTITIES.register("wild_wolf",
            () -> EntityType.Builder.of(WildWolf::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.9F)
                    .clientTrackingRange(8)
                    .build("wild_wolf"));
}

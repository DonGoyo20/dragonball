package com.dongoyo20.dragonball.game.bestiary;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class ModDataMaps {
    private ModDataMaps() {}

    /** Asocia nivel, tipo y XP a cualquier EntityType (también a los de Minecraft). Se sincroniza al cliente. */
    public static final DataMapType<EntityType<?>, BestiaryEntry> BESTIARY = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "bestiary"),
            Registries.ENTITY_TYPE,
            BestiaryEntry.CODEC
    ).synced(BestiaryEntry.CODEC, false).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(BESTIARY);
    }
}

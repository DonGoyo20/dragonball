package com.dongoyo20.dragonball.game.item;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Tags propios del mod. Los archivos están en {@code data/dragonball/tags/}. */
public final class ModTags {
    private ModTags() {}

    /**
     * Armas del mod: su daño cuerpo a cuerpo escala con FUE (las de vanilla no). Hoy el mod no tiene armas, así que el
     * tag está vacío; cada arma nueva se añade a {@code data/dragonball/tags/item/armas_del_mod.json}.
     */
    public static final TagKey<Item> ARMAS_DEL_MOD =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "armas_del_mod"));
}

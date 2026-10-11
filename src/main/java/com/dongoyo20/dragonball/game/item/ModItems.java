package com.dongoyo20.dragonball.game.item;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.entity.ModEntities;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DragonBall.MOD_ID);

    public static final DeferredItem<DeferredSpawnEggItem> WILD_WOLF_SPAWN_EGG = ITEMS.register("wild_wolf_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.WILD_WOLF, 0x6B6B6B, 0x3A3A3A, new Item.Properties()));

    @EventBusSubscriber(modid = DragonBall.MOD_ID)
    public static final class CreativeTabs {
        private CreativeTabs() {}

        @SubscribeEvent
        public static void addToTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                event.accept(WILD_WOLF_SPAWN_EGG);
            }
        }
    }
}

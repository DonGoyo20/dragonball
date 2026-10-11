package com.dongoyo20.dragonball;

import com.dongoyo20.dragonball.game.advancement.ModTriggers;
import com.dongoyo20.dragonball.game.bestiary.ModDataMaps;
import com.dongoyo20.dragonball.game.entity.ModEntities;
import com.dongoyo20.dragonball.game.item.ModItems;
import com.dongoyo20.dragonball.game.progress.ModAttachments;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Punto de entrada del mod. Los sistemas propios se registran aquí; las integraciones externas
 * (Patchouli, FTB Quests, KubeJS) NO se enlazan en código: se alimentan de datos y logros del mod.
 */
@Mod(DragonBall.MOD_ID)
public class DragonBall {
    public static final String MOD_ID = "dragonball";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DragonBall(IEventBus modEventBus) {
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModTriggers.TRIGGERS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(ModDataMaps::register);
        LOGGER.info("Dragon Ball RPG cargado");
    }
}

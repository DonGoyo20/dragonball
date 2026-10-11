package com.dongoyo20.dragonball;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** Punto de entrada del mod. Las integraciones externas se registran en sus propios paquetes. */
@Mod(DragonBall.MOD_ID)
public class DragonBall {
    public static final String MOD_ID = "dragonball";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DragonBall(IEventBus modEventBus) {
        LOGGER.info("Dragon Ball RPG {} cargado", MOD_ID);
    }
}

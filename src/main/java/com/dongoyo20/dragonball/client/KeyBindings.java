package com.dongoyo20.dragonball.client;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Teclas del mod (se pueden cambiar en Opciones → Controles). */
public final class KeyBindings {
    private KeyBindings() {}

    public static final String CATEGORY = "key.categories.dragonball";

    /** Mantener: sube la liberación (solo si el jugador está quieto). */
    public static final KeyMapping RELEASE_UP = new KeyMapping("key.dragonball.release_up", GLFW.GLFW_KEY_C, CATEGORY);
    /** Mantener: baja la liberación. */
    public static final KeyMapping RELEASE_DOWN = new KeyMapping("key.dragonball.release_down", GLFW.GLFW_KEY_X, CATEGORY);
    /** Muestra u oculta el HUD. */
    public static final KeyMapping TOGGLE_HUD = new KeyMapping("key.dragonball.toggle_hud", GLFW.GLFW_KEY_H, CATEGORY);
    /** Abre o cierra la pantalla de estadísticas. */
    public static final KeyMapping STATS = new KeyMapping("key.dragonball.stats", GLFW.GLFW_KEY_V, CATEGORY);

    @EventBusSubscriber(modid = DragonBall.MOD_ID, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(RELEASE_UP);
            event.register(RELEASE_DOWN);
            event.register(TOGGLE_HUD);
            event.register(STATS);
        }
    }
}

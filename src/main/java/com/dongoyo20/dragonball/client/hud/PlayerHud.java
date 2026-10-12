package com.dongoyo20.dragonball.client.hud;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.progress.AttributeValues;
import com.dongoyo20.dragonball.game.progress.ModAttachments;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * HUD del mod, esquina superior izquierda: barra de Vida (verde) con "HP X / Y" y barra de Ki (cyan) con "Ki X / Y".
 * Debajo, en gris, una línea con la liberación actual y su límite (sin ella no hay forma de saber qué hace la tecla C).
 * La vida mostrada es la vida real: {@code hp vanilla / 20 × vida real máxima}. Se oculta con la tecla H.
 */
public final class PlayerHud implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "player_hud");

    /** Medidas en píxeles de GUI. */
    private static final int X = 6, Y = 6, WIDTH = 110, HEIGHT = 11, GAP = 3;
    private static final int BORDER = 0xFF000000, BACKGROUND = 0xAA101010;
    private static final int HEALTH_COLOR = 0xFF3FD13F, KI_COLOR = 0xFF19D3F3, TEXT = 0xFFFFFFFF, SUBTLE = 0xFFB0B0B0;

    private static boolean visible = true;

    public static boolean isVisible() {
        return visible;
    }

    public static void toggle() {
        visible = !visible;
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!visible || mc.player == null || mc.options.hideGui) return;

        PlayerProgress progress = mc.player.getData(ModAttachments.PROGRESS);
        BalanceParams params = BalanceManager.get();

        int vit = AttributeValues.baseValue(0, progress, params);
        double maxHp = HealthSystem.vidaRealMaxima(vit, params);
        double hp = HealthSystem.vidaRealActual(Math.max(0f, Math.min(mc.player.getHealth(), 20f)), maxHp);
        double maxKi = KiSystem.reservaMaxima(AttributeValues.baseValue(4, progress, params), params);
        double ki = Math.min(progress.ki().kiActual(), maxKi);

        bar(graphics, mc, X, Y, hp / maxHp, HEALTH_COLOR, Component.translatable("hud.dragonball.hp", Math.round(hp), Math.round(maxHp)));
        bar(graphics, mc, X, Y + HEIGHT + GAP, ki / maxKi, KI_COLOR,
                Component.translatable("hud.dragonball.ki", Math.round(ki), Math.round(maxKi)));
        graphics.drawString(mc.font, Component.translatable("hud.dragonball.release",
                        Math.round(progress.ki().liberacion()), progress.ki().limite()),
                X + 1, Y + 2 * (HEIGHT + GAP) + 1, SUBTLE, true);
    }

    private static void bar(GuiGraphics graphics, Minecraft mc, int x, int y, double fraction, int color, Component label) {
        double clamped = Double.isNaN(fraction) ? 0 : Math.max(0, Math.min(1, fraction));
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BACKGROUND);
        graphics.fill(x, y, x + (int) Math.round(WIDTH * clamped), y + HEIGHT, color);
        graphics.drawString(mc.font, label, x + 3, y + 2, TEXT, true);
    }

    @EventBusSubscriber(modid = DragonBall.MOD_ID, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerLayer(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ID, new PlayerHud());
        }
    }
}

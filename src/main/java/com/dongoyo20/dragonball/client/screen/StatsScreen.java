package com.dongoyo20.dragonball.client.screen;

import com.dongoyo20.dragonball.client.KeyBindings;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.core.raza.Rasgo;
import com.dongoyo20.dragonball.game.raza.RaceService;
import net.minecraft.util.FormattedCharSequence;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.progress.AttributeValues;
import com.dongoyo20.dragonball.game.progress.ModAttachments;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Pantalla de estadísticas (tecla V; se cierra con V o Esc). Solo muestra: las mejoras se compran con
 * {@code /dragonball upgrade <atributo>}. Se actualiza sola porque lee el progreso sincronizado cada fotograma.
 */
public final class StatsScreen extends Screen {
    private static final int TEXT = 0xFFFFFFFF, SUBTLE = 0xFFB0B0B0, TITLE = 0xFFFFD34E, ROW_A = 0x55000000, ROW_B = 0x33000000;
    private static final int PANEL_WIDTH = 280, ROW_HEIGHT = 12;

    public StatsScreen() {
        super(Component.translatable("screen.dragonball.stats.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindings.STATS.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PlayerProgress progress = mc.player.getData(ModAttachments.PROGRESS);
        BalanceParams params = BalanceManager.get();

        int left = (width - PANEL_WIDTH) / 2;
        Race race = RaceService.raceOf(mc.player);
        // Centrada, pero si el contenido (con los rasgos) no cabe en una ventana pequeña, sube para que se vea el pie
        int y = Math.max(2, Math.min(height / 2 - 130, height - contentHeight(race) - 4));
        g.drawCenteredString(font, title, width / 2, y, TITLE);
        y += 16;

        long cost = progress.nextUpgradeCost();
        g.drawString(font, Component.translatable("screen.dragonball.stats.race", Component.translatable(race.displayName())), left, y, TEXT);
        y += ROW_HEIGHT;
        g.drawString(font, Component.translatable("screen.dragonball.stats.level", progress.totalLevel()), left, y, TEXT);
        y += ROW_HEIGHT;
        g.drawString(font, Component.translatable("screen.dragonball.stats.xp", progress.xp(), cost), left, y, TEXT);
        y += ROW_HEIGHT;
        g.drawString(font, Component.translatable("screen.dragonball.stats.available", affordableUpgrades(progress)), left, y, TEXT);
        y += ROW_HEIGHT;
        g.drawString(font, Component.translatable("screen.dragonball.stats.next_cost", cost), left, y, TEXT);
        y += ROW_HEIGHT + 4;

        double release = progress.ki().liberacion();
        g.drawString(font, Component.translatable("screen.dragonball.stats.release", Math.round(release), progress.ki().limite()), left, y, SUBTLE);
        y += ROW_HEIGHT + 4;

        // cabecera de la tabla
        int colBase = left + 130, colEff = left + 175, colUp = left + 235;
        g.drawString(font, Component.translatable("screen.dragonball.stats.attribute"), left + 2, y, SUBTLE);
        g.drawString(font, Component.translatable("screen.dragonball.stats.base"), colBase, y, SUBTLE);
        g.drawString(font, Component.translatable("screen.dragonball.stats.effective"), colEff, y, SUBTLE);
        g.drawString(font, Component.translatable("screen.dragonball.stats.upgrades"), colUp, y, SUBTLE);
        y += ROW_HEIGHT;

        for (int i = 0; i < ProgressService.ATTRIBUTES.length; i++) {
            g.fill(left, y - 1, left + PANEL_WIDTH, y + ROW_HEIGHT - 1, i % 2 == 0 ? ROW_A : ROW_B);
            g.drawString(font, Component.translatable("attribute.dragonball." + ProgressService.ATTRIBUTES[i]), left + 2, y + 1, TEXT);
            int base = AttributeValues.baseValue(i, progress, params, race.base(Atributo.values()[i]));
            if (base == AttributeValues.UNDEFINED) {
                g.drawString(font, Component.translatable("screen.dragonball.stats.not_implemented"), colBase, y + 1, SUBTLE);
            } else {
                g.drawString(font, Integer.toString(base), colBase, y + 1, TEXT);
                double eff = AttributeValues.effectiveValue(i, base, release, params);
                g.drawString(font, String.format("%.1f", eff), colEff, y + 1,
                        AttributeValues.scalesWithRelease(i) ? TEXT : SUBTLE);
            }
            g.drawString(font, Integer.toString(progress.upgrades().get(i)), colUp, y + 1, TEXT);
            y += ROW_HEIGHT;
        }

        // rasgos de la raza (solo informativos): nombre y descripción, con salto de línea
        y += 6;
        if (!race.traits().isEmpty()) {
            g.drawString(font, Component.translatable("screen.dragonball.stats.traits"), left, y, SUBTLE);
            y += ROW_HEIGHT;
            for (Rasgo trait : race.traits()) {
                Component line = Component.translatable(trait.displayName()).append(": ").append(Component.translatable(trait.description()));
                for (FormattedCharSequence part : font.split(line, PANEL_WIDTH - 8)) {
                    g.drawString(font, part, left + 4, y, TEXT);
                    y += font.lineHeight + 1;
                }
            }
            y += 4;
        }
        g.drawString(font, Component.translatable("screen.dragonball.stats.footer"), left, y, SUBTLE);
    }

    /** Alto aproximado del contenido, para colocarlo dentro de la ventana. */
    private int contentHeight(Race race) {
        int h = 16 + 5 * ROW_HEIGHT + 4 + ROW_HEIGHT + 4 + ROW_HEIGHT + ProgressService.ATTRIBUTES.length * ROW_HEIGHT + 6 + ROW_HEIGHT;
        if (!race.traits().isEmpty()) {
            h += ROW_HEIGHT + 4;
            for (Rasgo trait : race.traits()) {
                Component line = Component.translatable(trait.displayName()).append(": ").append(Component.translatable(trait.description()));
                h += font.split(line, PANEL_WIDTH - 8).size() * (font.lineHeight + 1);
            }
        }
        return h;
    }

    /** Cuántas mejoras seguidas podrías comprar con la XP actual (el coste sube tras cada una). */
    private static int affordableUpgrades(PlayerProgress progress) {
        long xp = progress.xp();
        int level = progress.totalLevel();
        int count = 0;
        while (count < 1000) {
            long cost = Math.round(Progression.costNextUpgrade(level + count));
            if (xp < cost) break;
            xp -= cost;
            count++;
        }
        return count;
    }
}

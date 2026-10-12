package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.game.advancement.ModTriggers;
import com.dongoyo20.dragonball.game.bestiary.BestiaryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Único punto que modifica el progreso del jugador (así el logro de nivel siempre se dispara). */
public final class ProgressService {
    private ProgressService() {}

    public static final String[] ATTRIBUTES = {"vit", "fue", "def", "vel", "ki", "pod", "con"};

    public static PlayerProgress get(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESS);
    }

    public static void set(ServerPlayer player, PlayerProgress progress) {
        player.setData(ModAttachments.PROGRESS, progress);
        ModTriggers.TOTAL_LEVEL.get().trigger(player, progress.totalLevel());
    }

    /** Cambia solo el ki (sin disparar el logro de nivel). */
    public static void setKi(ServerPlayer player, com.dongoyo20.dragonball.game.ki.KiData ki) {
        player.setData(ModAttachments.PROGRESS, get(player).withKi(ki));
    }

    /** Otorga la XP de derrotar a un enemigo del bestiario. Devuelve la XP concedida. */
    public static int awardKill(ServerPlayer player, BestiaryEntry entry, boolean fromSpawner) {
        PlayerProgress progress = get(player);
        int xp = Progression.killXp(entry.xp(), progress.totalLevel(), entry.level(), fromSpawner);
        if (xp > 0) {
            set(player, progress.addXp(xp));
            player.displayClientMessage(Component.translatable("message.dragonball.xp_gained", xp), true);
        }
        return xp;
    }

    /** Compra una mejora de atributo. Devuelve false si falta XP o el índice no es válido. */
    public static boolean upgrade(ServerPlayer player, int attributeIndex) {
        PlayerProgress next = get(player).tryUpgrade(attributeIndex);
        if (next == null) return false;
        set(player, next);
        return true;
    }

    public static int attributeIndex(String name) {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            if (ATTRIBUTES[i].equalsIgnoreCase(name)) return i;
        }
        return -1;
    }
}

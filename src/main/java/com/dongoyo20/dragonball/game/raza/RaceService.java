package com.dongoyo20.dragonball.game.raza;

import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.progress.ModAttachments;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.Collections;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Raza de un jugador y sus valores iniciales de atributo (funciona en servidor y en cliente). */
public final class RaceService {
    private RaceService() {}

    /** Id guardado en el jugador (puede ser de una raza que ya no existe). */
    public static String idOf(Player player) {
        return player.getData(ModAttachments.RACE);
    }

    /** La raza del jugador; si su id ya no existe, la raza por defecto. */
    public static Race raceOf(Player player) {
        return RaceManager.getOrDefault(idOf(player));
    }

    /** Valor inicial del atributo según la raza del jugador. */
    public static int base(Player player, Atributo attribute) {
        return raceOf(player).base(attribute);
    }

    /** Si el jugador no tiene guardada una raza válida, le pone la raza por defecto (no toca sus mejoras). */
    public static void ensureRace(ServerPlayer player) {
        if (!player.hasData(ModAttachments.RACE) || RaceManager.get(idOf(player)) == null) {
            player.setData(ModAttachments.RACE, RaceManager.DEFAULT_ID);
        }
    }

    /**
     * Cambia la raza y <b>reinicia las mejoras de atributo</b>: los valores base pasan a ser los de la nueva raza. La XP, el
     * ki y la liberación se conservan (el ki se recorta a la reserva máxima nueva).
     */
    public static void setRace(ServerPlayer player, Race race) {
        player.setData(ModAttachments.RACE, race.id());
        PlayerProgress old = ProgressService.get(player);
        PlayerProgress reset = new PlayerProgress(old.xp(), Collections.nCopies(old.upgrades().size(), 0), old.ki());
        ProgressService.set(player, reset);
        KiData ki = reset.ki();
        double max = com.dongoyo20.dragonball.game.ki.KiService.kiMax(player);
        if (ki.kiActual() > max) ProgressService.setKi(player, ki.withKi(max));
    }
}

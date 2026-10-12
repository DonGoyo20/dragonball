package com.dongoyo20.dragonball.game.command;

import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.core.raza.Rasgo;
import com.dongoyo20.dragonball.game.raza.RaceManager;
import com.dongoyo20.dragonball.game.raza.RaceService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /dragonball race info | list | set <raza>}. Cambiar de raza exige permisos de operador (reinicia las mejoras). */
public final class RaceCommand {
    private RaceCommand() {}

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("race")
                .then(Commands.literal("info").executes(ctx -> info(ctx.getSource().getPlayerOrException(), ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("set")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("race", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(RaceManager.ids(), b))
                                .executes(ctx -> set(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "race"), ctx.getSource()))));
    }

    public static int info(ServerPlayer player, CommandSourceStack source) {
        Race race = RaceService.raceOf(player);
        source.sendSuccess(() -> Component.translatable("command.dragonball.race.info",
                Component.translatable(race.displayName()), Component.translatable(race.description())), false);
        for (Rasgo trait : race.traits()) {
            source.sendSuccess(() -> Component.translatable("command.dragonball.race.trait",
                    Component.translatable(trait.displayName()), Component.translatable(trait.description())), false);
        }
        return 1;
    }

    public static int list(CommandSourceStack source) {
        Component names = Component.empty();
        boolean first = true;
        for (Race race : RaceManager.getAll()) {
            if (!first) names = names.copy().append(", ");
            names = names.copy().append(race.id() + " (").append(Component.translatable(race.displayName())).append(")");
            first = false;
        }
        Component all = names;
        source.sendSuccess(() -> Component.translatable("command.dragonball.race.list", all), false);
        return RaceManager.getAll().size();
    }

    public static int set(ServerPlayer player, String raceId, CommandSourceStack source) {
        Race race = RaceManager.get(raceId.toLowerCase());
        if (race == null) {
            source.sendFailure(Component.translatable("command.dragonball.race.unknown", raceId, String.join(", ", RaceManager.ids())));
            return 0;
        }
        RaceService.setRace(player, race);
        source.sendSuccess(() -> Component.translatable("command.dragonball.race.set", Component.translatable(race.displayName())), true);
        return 1;
    }
}

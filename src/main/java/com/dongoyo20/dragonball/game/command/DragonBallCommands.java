package com.dongoyo20.dragonball.game.command;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Comandos de progreso. /dragonball progress | upgrade &lt;atributo&gt; | givexp &lt;n&gt; (solo operadores). */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class DragonBallCommands {
    private DragonBallCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragonball")
                .then(Commands.literal("progress").executes(ctx -> showProgress(ctx.getSource().getPlayerOrException(), ctx.getSource())))
                .then(Commands.literal("upgrade")
                        .then(Commands.argument("attribute", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(ProgressService.ATTRIBUTES, b))
                                .executes(ctx -> upgrade(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "attribute"), ctx.getSource()))))
                .then(Commands.literal("givexp")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(ctx -> giveXp(ctx.getSource().getPlayerOrException(), LongArgumentType.getLong(ctx, "amount"), ctx.getSource())))));
    }

    private static int showProgress(ServerPlayer player, CommandSourceStack source) {
        PlayerProgress p = ProgressService.get(player);
        source.sendSuccess(() -> Component.translatable("command.dragonball.progress", p.totalLevel(), p.xp(), p.nextUpgradeCost(), p.upgrades().toString()), false);
        return p.totalLevel();
    }

    private static int upgrade(ServerPlayer player, String attribute, CommandSourceStack source) {
        int index = ProgressService.attributeIndex(attribute);
        if (index < 0) {
            source.sendFailure(Component.translatable("command.dragonball.unknown_attribute", attribute));
            return 0;
        }
        if (ProgressService.get(player).isAtCap(index)) {
            source.sendFailure(Component.translatable("command.dragonball.attribute_at_cap", Component.translatable("attribute.dragonball." + attribute.toLowerCase()), 99));
            return 0;
        }
        if (!ProgressService.upgrade(player, index)) {
            source.sendFailure(Component.translatable("command.dragonball.not_enough_xp", ProgressService.get(player).nextUpgradeCost()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.dragonball.upgraded", Component.translatable("attribute.dragonball." + attribute.toLowerCase()), ProgressService.get(player).totalLevel()), false);
        return 1;
    }

    private static int giveXp(ServerPlayer player, long amount, CommandSourceStack source) {
        ProgressService.set(player, ProgressService.get(player).addXp(amount));
        source.sendSuccess(() -> Component.translatable("command.dragonball.gave_xp", amount), true);
        return 1;
    }
}

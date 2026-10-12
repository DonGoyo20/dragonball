package com.dongoyo20.dragonball.game.command;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.game.combate.EsquivaService;
import com.dongoyo20.dragonball.game.raza.RaceService;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.ki.KiService;
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
                .then(Commands.literal("balance").executes(ctx -> showBalance(ctx.getSource())))
                .then(Commands.literal("ki").executes(ctx -> showKi(ctx.getSource().getPlayerOrException(), ctx.getSource())))
                .then(RaceCommand.build())
                .then(Commands.literal("esquiva").executes(ctx -> showDodge(ctx.getSource().getPlayerOrException(), ctx.getSource())))
                .then(Commands.literal("releaselimit")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("percent", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> setReleaseLimit(ctx.getSource().getPlayerOrException(),
                                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "percent"), ctx.getSource()))))
                .then(Commands.literal("givexp")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(ctx -> giveXp(ctx.getSource().getPlayerOrException(), LongArgumentType.getLong(ctx, "amount"), ctx.getSource())))));
    }

    /** Muestra el balance vigente (útil para comprobar que un datapack o un script de KubeJS se aplicó). */
    private static int showBalance(CommandSourceStack source) {
        BalanceParams b = BalanceManager.get();
        source.sendSuccess(() -> Component.translatable("command.dragonball.balance",
                b.vitBase(), b.vitGrowth(), b.hpPerVit(), b.damageExponent(), b.continuousFloor(),
                b.fueBase(), b.fueGrowth(), b.strengthExponent(), b.attributeCap()), false);
        return 1;
    }

    /** Muestra la probabilidad actual de esquivar un golpe y lo que cuesta esquivar uno de 1 HP. */
    private static int showDodge(ServerPlayer player, CommandSourceStack source) {
        double chance = EsquivaService.probabilidad(player);
        source.sendSuccess(() -> Component.translatable("command.dragonball.dodge",
                String.format("%.1f", chance * 100.0), String.format("%.1f", EsquivaService.velEfectiva(player)), EsquivaService.coste(player, 1.0)), false);
        return (int) Math.round(chance * 100.0);
    }

    private static int showKi(ServerPlayer player, CommandSourceStack source) {
        KiData ki = ProgressService.get(player).ki();
        source.sendSuccess(() -> Component.translatable("command.dragonball.ki",
                String.format("%.1f", ki.kiActual()), KiService.kiMax(player), String.format("%.0f", ki.liberacion()), ki.limite()), false);
        return 1;
    }

    /** Hasta que exista la habilidad "Desbloqueo de Potencial" (Kami-sama), el límite se cambia con este comando de pruebas. */
    private static int setReleaseLimit(ServerPlayer player, int percent, CommandSourceStack source) {
        KiService.setLimit(player, percent);
        source.sendSuccess(() -> Component.translatable("command.dragonball.release_limit", percent), true);
        return 1;
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
        if (ProgressService.get(player).isAtCap(index, RaceService.base(player, Atributo.values()[index]))) {
            source.sendFailure(Component.translatable("command.dragonball.attribute_at_cap", Component.translatable("attribute.dragonball." + attribute.toLowerCase()), BalanceManager.get().attributeCap()));
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

package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.TipoDanio;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import com.google.gson.JsonParser;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Balance como datos. Los cambios al balance vigente se hacen y se deshacen dentro de la misma llamada (sin esperar
 * ticks), para que ninguna otra prueba pueda ver un balance modificado.
 */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BalanceGameTests {
    private BalanceGameTests() {}

    private static final String TEMPLATE = "empty";

    @GameTest(template = TEMPLATE)
    public static void shippedBalanceFileEqualsTheDefaults(GameTestHelper helper) {
        // El archivo data/dragonball/balance/combat.json que trae el mod se cargó con el servidor y no cambia nada.
        helper.assertTrue(BalanceManager.get().equals(BalanceParams.DEFAULT),
                "el balance cargado debe ser el de siempre pero es " + BalanceManager.get());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void balanceJsonIsParsedAndMissingKeysKeepTheirDefaults(GameTestHelper helper) {
        BalanceParams p = BalanceManager.parse(JsonParser.parseString("{\"continuous_floor\": 0.25, \"damage_exponent\": 2}"));
        helper.assertTrue(p.continuousFloor() == 0.25 && p.damageExponent() == 2.0, "las claves presentes se aplican");
        helper.assertTrue(p.vitBase() == 8.0 && p.fueBase() == 8.0 && p.attributeCap() == 99, "las ausentes conservan su valor");
        helper.assertTrue(BalanceManager.parse(JsonParser.parseString("{}")).equals(BalanceParams.DEFAULT), "{} = valores por defecto");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void invalidBalanceFallsBackToDefaultsAsAWhole(GameTestHelper helper) {
        // Un valor inválido no deja el archivo "a medias": se descarta entero.
        List<String> errors = new java.util.ArrayList<>();
        BalanceParams badValue = BalanceManager.parse(JsonParser.parseString("{\"continuous_floor\": 0.25, \"vit_base\": 0}"), errors::add);
        BalanceParams wrongType = BalanceManager.parse(JsonParser.parseString("{\"continuous_floor\": \"mucho\"}"), errors::add);
        BalanceParams notAnObject = BalanceManager.parse(JsonParser.parseString("[1, 2, 3]"), errors::add);
        helper.assertTrue(errors.size() == 3, "cada archivo inválido debe producir un aviso (hubo " + errors.size() + ")");
        helper.assertTrue(errors.get(0).contains("vit_base"), "el aviso nombra la clave inválida: " + errors.get(0));
        helper.assertTrue(badValue.equals(BalanceParams.DEFAULT), "vit_base 0 es inválido: todo el archivo se descarta");
        helper.assertTrue(wrongType.equals(BalanceParams.DEFAULT), "un tipo equivocado se descarta");
        helper.assertTrue(notAnObject.equals(BalanceParams.DEFAULT), "un JSON que no es un objeto se descarta");
        helper.succeed();
    }

    @SuppressWarnings("removal")
    @GameTest(template = TEMPLATE)
    public static void changingTheBalanceChangesTheCombat(GameTestHelper helper) {
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
        for (int i = 0; i < 61; i++) player.tick();            // agotar la invulnerabilidad de aparición
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        ProgressService.set(player, new PlayerProgress(0, List.of(18, 0, 0, 0, 0, 0, 0)));   // VIT 26

        BalanceParams old = BalanceManager.setForTesting(BalanceManager.parse(JsonParser.parseString("{\"continuous_floor\": 0.25}")));
        try {
            player.hurt(player.damageSources().lava(), 4.0F);
        } finally {
            BalanceManager.setForTesting(old);
        }

        double dealt = 20.0 - player.getHealth();
        helper.assertTrue(Math.abs(dealt - 0.25) < 0.01, "con suelo 0,25 la lava debe quitar 0,25 y quitó " + dealt);
        helper.assertTrue(BalanceManager.get().equals(BalanceParams.DEFAULT), "el balance quedó restaurado");
        helper.assertTrue(TipoDanio.values().length == 3, "las tres categorías siguen existiendo");
        helper.succeed();
    }
}

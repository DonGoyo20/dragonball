package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** DEF (defensa) y POD con un servidor real: reducción del daño instantáneo, liberación, tope y compra. */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DefGameTests {
    private DefGameTests() {}

    private static final String TEMPLATE = "empty";

    @SuppressWarnings("removal")
    private static ServerPlayer newPlayer(GameTestHelper helper) {
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
        for (int i = 0; i < 61; i++) player.tick();
        player.setGameMode(GameType.SURVIVAL);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        return player;
    }

    /** VIT 0 mejoras (8), DEF y POD según se pida. */
    private static void setProgress(ServerPlayer player, int def, int pod, double liberacion) {
        ProgressService.set(player, new PlayerProgress(0, List.of(0, 0, def, 0, 0, pod, 0), new KiData(50, liberacion, 100)));
    }

    private static Zombie zombie(GameTestHelper helper) {
        Zombie z = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        z.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        return z;
    }

    private static void near(GameTestHelper helper, double actual, double expected, double tolerance, String what) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance, what + ": esperado " + expected + " pero fue " + actual);
    }

    @GameTest(template = TEMPLATE)
    public static void defReducesInstantDamageCubically(GameTestHelper helper) {
        ServerPlayer def8 = newPlayer(helper);
        ServerPlayer def18 = newPlayer(helper);
        setProgress(def8, 0, 0, 50);
        setProgress(def18, 10, 0, 50);                                   // DEF 18 al 50 %: ×1,25·(8/18)^3 = 0,110
        Zombie z = zombie(helper);

        def8.hurt(def8.damageSources().mobAttack(z), 4.0F);
        def18.hurt(def18.damageSources().mobAttack(z), 4.0F);

        near(helper, 20.0 - def8.getHealth(), 4.0, 0.01, "DEF 8: daño de vanilla");
        near(helper, 20.0 - def18.getHealth(), 4.0 * 1.25 * Math.pow(8.0 / 18.0, 3), 0.01, "DEF 18 (×0,110)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void vitEighteenPlusDefEighteenIsTwentyFivePercentMoreThanBefore(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        ProgressService.set(player, new PlayerProgress(0, List.of(10, 0, 10, 0, 0, 0, 0), new KiData(50, 50, 100)));   // VIT 18 y DEF 18
        Zombie z = zombie(helper);
        player.hurt(player.damageSources().mobAttack(z), 20.0F);          // 20 HP de vanilla: se mide la reducción
        near(helper, 20.0 - player.getHealth(), 20.0 * 1.25 * Math.pow(8.0 / 18.0, 6), 0.005, "VIT 18 + DEF 18 (×0,0097)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void defDependsOnTheRelease(GameTestHelper helper) {
        ServerPlayer zero = newPlayer(helper);
        ServerPlayer full = newPlayer(helper);
        setProgress(zero, 10, 0, 0);                                     // DEF 18 al 0 %: vanilla
        setProgress(full, 10, 0, 100);                                   // DEF 18 al 100 %: efectiva 36
        Zombie z = zombie(helper);

        zero.hurt(zero.damageSources().mobAttack(z), 4.0F);
        full.hurt(full.damageSources().mobAttack(z), 4.0F);

        near(helper, 20.0 - zero.getHealth(), 4.0, 0.01, "DEF al 0 % = vanilla");
        // al 100 % también VIT 8 pasa a 16 (×1/8): el resultado combina las dos reducciones
        near(helper, 20.0 - full.getHealth(), 4.0 * 1.25 * Math.pow(8.0 / 36.0, 3) * 0.125, 0.002, "DEF al 100 % (con VIT doble)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void defDoesNotTouchContinuousDamageNorTheVoid(GameTestHelper helper) {
        ServerPlayer def8 = newPlayer(helper);
        ServerPlayer def99 = newPlayer(helper);
        setProgress(def8, 0, 0, 50);
        setProgress(def99, 91, 0, 50);

        def8.hurt(def8.damageSources().fellOutOfWorld(), 4.0F);
        def99.hurt(def99.damageSources().fellOutOfWorld(), 4.0F);
        near(helper, 20.0 - def99.getHealth(), 20.0 - def8.getHealth(), 0.01, "el vacío no cambia con DEF");

        // daño continuo (lava): lo mismo con DEF 8 que con DEF 99, porque DEF no lo toca (solo VIT y su suelo)
        ServerPlayer lava8 = newPlayer(helper);
        ServerPlayer lava99 = newPlayer(helper);
        setProgress(lava8, 0, 0, 50);
        setProgress(lava99, 91, 0, 50);
        lava8.hurt(lava8.damageSources().lava(), 4.0F);
        lava99.hurt(lava99.damageSources().lava(), 4.0F);
        near(helper, lava99.getHealth(), lava8.getHealth(), 0.01, "la lava no cambia con DEF");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void defAndPodCanBeBoughtUpToTheCap(GameTestHelper helper) {
        PlayerProgress atCap = new PlayerProgress(1_000_000_000L, List.of(0, 0, 91, 0, 0, 91, 0), KiData.INITIAL);
        helper.assertTrue(atCap.isAtCap(2) && atCap.isAtCap(5), "DEF y POD tienen tope (91 mejoras = valor 99)");
        helper.assertTrue(atCap.tryUpgrade(2) == null && atCap.tryUpgrade(5) == null, "no se puede pasar del tope");

        PlayerProgress rich = new PlayerProgress(1_000_000_000L, List.of(0, 0, 0, 0, 0, 0, 0), KiData.INITIAL);
        PlayerProgress withDef = rich.tryUpgrade(2);
        PlayerProgress withPod = rich.tryUpgrade(5);
        helper.assertTrue(withDef != null && withDef.upgrades().get(2) == 1, "se compra una mejora de DEF");
        helper.assertTrue(withPod != null && withPod.upgrades().get(5) == 1, "se compra una mejora de POD");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void defAndPodBalanceComesFromDataAndIsValidated(GameTestHelper helper) {
        var parsed = com.dongoyo20.dragonball.game.balance.BalanceManager.parse(com.google.gson.JsonParser.parseString(
                "{\"defense\": {\"damage_exponent\": 2, \"defense_multiplier\": 1.5}, \"pod\": {\"pod_base\": 10}}"));
        helper.assertTrue(parsed.defense().damageExponent() == 2.0 && parsed.defense().multiplier() == 1.5 && parsed.defense().defBase() == 8.0, "claves de DEF cambiadas, el resto por defecto");
        helper.assertTrue(parsed.pod().podBase() == 10.0 && parsed.pod().podGrowth() == 1.0, "clave de POD cambiada");

        java.util.List<String> errors = new java.util.ArrayList<>();
        var bad = com.dongoyo20.dragonball.game.balance.BalanceManager.parse(
                com.google.gson.JsonParser.parseString("{\"defense\": {\"def_base\": 0}}"), errors::add);
        helper.assertTrue(bad.equals(com.dongoyo20.dragonball.core.BalanceParams.DEFAULT) && errors.size() == 1, "DEF inválida: se rechaza todo el archivo: " + errors);

        var roundTrip = com.dongoyo20.dragonball.game.balance.BalanceManager.CODEC.encodeStart(
                com.mojang.serialization.JsonOps.INSTANCE, parsed).getOrThrow();
        helper.assertTrue(com.dongoyo20.dragonball.game.balance.BalanceManager.parse(roundTrip).equals(parsed), "ida y vuelta por el codec (así viaja al cliente)");
        helper.succeed();
    }
}

package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.game.health.HealthService;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** VIT → salud, con un servidor real. Ejecutar con: ./gradlew runGameTestServer */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HealthGameTests {
    private HealthGameTests() {}

    private static final String TEMPLATE = "empty";

    @SuppressWarnings("removal")
    private static ServerPlayer newPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // El jugador simulado nace en creativo (invulnerable): se pasa a supervivencia para recibir daño.
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        // Los 60 primeros ticks el jugador es invulnerable (protección de aparición de ServerPlayer): se agotan.
        for (int i = 0; i < 61; i++) player.tick();
        // El daño de mobs escala con la dificultad de vanilla; en Normal el factor es 1.
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        return player;
    }

    private static void setVitUpgrades(ServerPlayer player, int upgrades) {
        ProgressService.set(player, new PlayerProgress(0, List.of(upgrades, 0, 0, 0, 0, 0, 0)));
    }

    private static void assertHealth(GameTestHelper helper, ServerPlayer player, double expected, String what) {
        helper.assertTrue(Math.abs(player.getHealth() - expected) < 0.01,
                what + ": vida esperada " + expected + " pero fue " + player.getHealth());
    }

    @GameTest(template = TEMPLATE)
    public static void zombieHitAtBaseVitDealsVanillaDamage(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

        player.hurt(player.damageSources().mobAttack(zombie), 3.0F);

        assertHealth(helper, player, 17.0, "VIT base");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void zombieHitWithVit18DealsLessDamage(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setVitUpgrades(player, 10); // VIT valor 18
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

        player.hurt(player.damageSources().mobAttack(zombie), 3.0F);

        assertHealth(helper, player, 20.0 - 3.0 * 8.0 / 18.0, "VIT 18");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void environmentalDamageIsNotScaled(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setVitUpgrades(player, 10);
        helper.assertTrue(HealthService.esDanioDeEntorno(player.damageSources().fall()), "caída = entorno");
        helper.assertTrue(HealthService.esDanioDeEntorno(player.damageSources().lava()), "lava = entorno");
        helper.assertTrue(HealthService.esDanioDeEntorno(player.damageSources().drown()), "ahogamiento = entorno");

        player.hurt(player.damageSources().fall(), 3.0F);

        assertHealth(helper, player, 17.0, "caída con VIT 18");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mobAttackIsNotEnvironmental(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        helper.assertTrue(!HealthService.esDanioDeEntorno(player.damageSources().mobAttack(zombie)), "zombi = no entorno");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void killCommandDamageKillsWithoutNaN(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setVitUpgrades(player, 91);

        player.kill();

        helper.assertTrue(!player.isAlive(), "/kill debe matar incluso con VIT 99");
        helper.assertTrue(!Float.isNaN(player.getHealth()), "la vida no puede ser NaN");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void raisingVitKeepsVanillaBarAndRaisesRealHealth(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        player.hurt(player.damageSources().mobAttack(zombie), 3.0F);
        double before = HealthService.vidaRealActual(player);

        setVitUpgrades(player, 10);

        assertHealth(helper, player, 17.0, "subir VIT no cambia la barra");
        helper.assertTrue(Math.abs(before - 85.0) < 0.01, "vida real inicial 85, fue " + before);
        helper.assertTrue(Math.abs(HealthService.vidaRealActual(player) - 0.85 * 225.0) < 0.01, "vida real proporcional");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void vitUpgradeIsRejectedAtTheCap(GameTestHelper helper) {
        PlayerProgress atMax = new PlayerProgress(1_000_000, List.of(HealthSystem.VIT_MAX_UPGRADES, 0, 0, 0, 0, 0, 0));
        helper.assertTrue(atMax.isAtCap(0), "VIT con 91 mejoras está en el tope");
        helper.assertTrue(atMax.tryUpgrade(0) == null, "la mejora 92 de VIT debe rechazarse aunque haya XP");
        PlayerProgress almost = new PlayerProgress(1_000_000, List.of(HealthSystem.VIT_MAX_UPGRADES - 1, 0, 0, 0, 0, 0, 0));
        helper.assertTrue(almost.tryUpgrade(0) != null, "la mejora 91 sí se permite");
        helper.assertTrue(atMax.tryUpgrade(1) != null, "otros atributos no se ven afectados por el tope de VIT");
        helper.succeed();
    }
}

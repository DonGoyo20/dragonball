package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.game.bestiary.BestiaryEntry;
import com.dongoyo20.dragonball.game.bestiary.ModDataMaps;
import com.dongoyo20.dragonball.game.entity.ModEntities;
import com.dongoyo20.dragonball.game.entity.WildWolf;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Pruebas dentro del juego: arrancan un servidor real con todos los mods cargados.
 * Ejecutar con: ./gradlew runGameTestServer
 */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DragonBallGameTests {
    private DragonBallGameTests() {}

    private static final String TEMPLATE = "empty";

    @GameTest(template = TEMPLATE)
    public static void bestiaryDataMapIsLoaded(GameTestHelper helper) {
        BestiaryEntry zombie = EntityType.ZOMBIE.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        BestiaryEntry cow = EntityType.COW.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        BestiaryEntry wolf = ModEntities.WILD_WOLF.get().builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        BestiaryEntry dragon = EntityType.ENDER_DRAGON.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);

        helper.assertTrue(zombie != null && zombie.level() == 3 && zombie.xp() == 8, "zombi: nivel 3, 8 XP");
        helper.assertTrue(cow != null && cow.kind().equals("passive") && cow.xp() == 0, "vaca: pasiva, 0 XP");
        helper.assertTrue(wolf != null && wolf.level() == 4 && wolf.xp() == 10, "lobo salvaje: nivel 4, 10 XP");
        helper.assertTrue(dragon != null && dragon.isBoss(), "dragón del End: jefe");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void killingAnEntityAwardsXp(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

        zombie.hurt(player.damageSources().playerAttack(player), 1000.0F);

        int expected = Progression.killXp(8, 1, 3, false);
        long actual = ProgressService.get(player).xp();
        helper.assertTrue(actual == expected, "XP esperada " + expected + " pero fue " + actual);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void upgradesRaiseLevelAndGrantAdvancement(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ProgressService.set(player, PlayerProgress.EMPTY.withXp(100_000));

        for (int i = 0; i < 4; i++) {
            helper.assertTrue(ProgressService.upgrade(player, 1), "mejora " + (i + 1) + " debería poder comprarse");
        }
        helper.assertTrue(ProgressService.get(player).totalLevel() == 5, "nivel total 5 tras 4 mejoras");

        AdvancementHolder level5 = helper.getLevel().getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "level_5"));
        helper.assertTrue(level5 != null, "el logro dragonball:level_5 debe existir");
        helper.assertTrue(player.getAdvancements().getOrStartProgress(level5).isDone(), "el logro level_5 debe estar concedido");

        AdvancementHolder level10 = helper.getLevel().getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "level_10"));
        helper.assertTrue(!player.getAdvancements().getOrStartProgress(level10).isDone(), "level_10 aún NO debe estar concedido");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void upgradeFailsWithoutXp(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(!ProgressService.upgrade(player, 0), "sin XP no se puede mejorar");
        helper.assertTrue(ProgressService.get(player).totalLevel() == 1, "el nivel sigue siendo 1");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wildWolfExistsAndDropsLoot(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        WildWolf wolf = helper.spawnWithNoFreeWill(ModEntities.WILD_WOLF.get(), 1, 1, 1);
        helper.assertTrue(wolf.getMaxHealth() == 12.0F, "vida máxima del lobo = 12");

        wolf.hurt(player.damageSources().playerAttack(player), 1000.0F);

        helper.assertTrue(ProgressService.get(player).xp() == Progression.killXp(10, 1, 4, false), "XP por matar al lobo");
        helper.succeedWhen(() -> helper.assertEntityPresent(EntityType.ITEM));
    }

    @GameTest(template = TEMPLATE)
    public static void spawnerMobsGiveReducedXp(GameTestHelper helper) {
        helper.assertTrue(Progression.killXp(8, 1, 3, true) == 2, "mob de generador: 25 % de la XP");
        helper.succeed();
    }
}

package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.Progression;
import com.dongoyo20.dragonball.game.bestiary.BestiaryEntry;
import com.dongoyo20.dragonball.game.bestiary.ModDataMaps;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Bestiario: mobs vanilla añadidos el 2026-10-12 (golem de hierro y otros). Ejecutar con: ./gradlew runGameTestServer */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BestiaryGameTests {
    private BestiaryGameTests() {}

    private static final String TEMPLATE = "empty";

    @SuppressWarnings("removal")
    private static ServerPlayer newPlayer(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }

    private static BestiaryEntry entry(EntityType<?> type) {
        return type.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
    }

    @GameTest(template = TEMPLATE)
    public static void killingAnIronGolemAwardsXp(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        LivingEntity golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, 1, 1, 1);
        BestiaryEntry entry = entry(EntityType.IRON_GOLEM);
        helper.assertTrue(entry != null && entry.level() == 25 && entry.xp() == 150 && entry.kind().equals("elite"),
                "golem de hierro: nivel 25, élite, 150 XP");

        golem.hurt(player.damageSources().playerAttack(player), 1000.0F);

        int expected = Progression.killXp(150, 1, 25, false);   // jugador de nivel 1 contra nivel 25 → +48 %
        long actual = ProgressService.get(player).xp();
        helper.assertTrue(!golem.isAlive(), "el golem debe morir");
        helper.assertTrue(actual == expected && expected > 150, "XP esperada " + expected + " pero fue " + actual);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void killingAVillagerGivesNoXp(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        LivingEntity villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, 1, 1, 1);
        helper.assertTrue(entry(EntityType.VILLAGER) != null && entry(EntityType.VILLAGER).xp() == 0, "aldeano: entrada con 0 XP");

        villager.hurt(player.damageSources().playerAttack(player), 1000.0F);

        helper.assertTrue(ProgressService.get(player).xp() == 0, "matar a un aldeano no da XP");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void addedVanillaMobsAreInTheBestiary(GameTestHelper helper) {
        List<EntityType<?>> withXp = List.of(EntityType.IRON_GOLEM, EntityType.WOLF, EntityType.POLAR_BEAR, EntityType.LLAMA,
                EntityType.TRADER_LLAMA, EntityType.FOX, EntityType.GOAT, EntityType.ZOMBIE_VILLAGER, EntityType.SILVERFISH,
                EntityType.ENDERMITE, EntityType.BOGGED, EntityType.VEX, EntityType.GUARDIAN, EntityType.BREEZE,
                EntityType.ELDER_GUARDIAN, EntityType.ZOGLIN, EntityType.PIGLIN_BRUTE);
        for (EntityType<?> type : withXp) {
            BestiaryEntry e = entry(type);
            helper.assertTrue(e != null && e.xp() > 0, EntityType.getKey(type) + " debe dar XP");
        }
        List<EntityType<?>> noXp = List.of(EntityType.SNOW_GOLEM, EntityType.CAT, EntityType.OCELOT, EntityType.HORSE,
                EntityType.DONKEY, EntityType.MULE, EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        for (EntityType<?> type : noXp) {
            BestiaryEntry e = entry(type);
            helper.assertTrue(e != null && e.xp() == 0, EntityType.getKey(type) + " debe figurar con 0 XP");
        }
        helper.succeed();
    }
}

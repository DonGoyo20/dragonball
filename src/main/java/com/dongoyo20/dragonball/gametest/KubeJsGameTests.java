package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.bestiary.BestiaryEntry;
import com.dongoyo20.dragonball.game.bestiary.ModDataMaps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Integración con KubeJS. Se ejecuta SOLO en la ejecución separada: ./gradlew runGameTestKubeJs
 * (espacio de nombres {@code dragonball_kubejs}). Allí el script integrations/kubejs/.../dragonball_balance_example.js
 * sobrescribe la entrada de la araña del bestiario sin recompilar Java.
 */
@GameTestHolder(DragonBall.MOD_ID + "_kubejs")
@PrefixGameTestTemplate(false)
public final class KubeJsGameTests {
    private KubeJsGameTests() {}

    @GameTest(template = "empty")
    public static void kubejsScriptOverridesBalance(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("kubejs"), "KubeJS debe estar cargado en esta ejecución");

        BestiaryEntry spider = EntityType.SPIDER.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        BestiaryEntry zombie = EntityType.ZOMBIE.builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        helper.assertTrue(spider != null && spider.xp() == 50,
                "el script de KubeJS debe fijar la araña en 50 XP, pero fue " + (spider == null ? "null" : spider.xp()));
        helper.assertTrue(zombie != null && zombie.xp() == 8, "el resto del bestiario no debe cambiar (zombi = 8 XP)");
        // Segundo canal de datos: el balance del combate. El script fija solo el suelo del daño continuo.
        var balance = com.dongoyo20.dragonball.game.balance.BalanceManager.get();
        helper.assertTrue(balance.continuousFloor() == 0.25,
                "el script de KubeJS debe fijar continuous_floor en 0,25, pero fue " + balance.continuousFloor());
        helper.assertTrue(balance.damageExponent() == 3.0 && balance.vitBase() == 8.0,
                "las claves que el script no menciona conservan su valor por defecto");
        helper.succeed();
    }
}

package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.game.damage.FuerzaEvents;
import com.dongoyo20.dragonball.game.damage.FuerzaService;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * FUE → daño cuerpo a cuerpo, con un servidor real. Ejecutar con: ./gradlew runGameTestServer
 *
 * <p>Los golpes se aplican con {@code hurt(playerAttack(jugador), daño)} y no con {@code player.attack()}: el jugador
 * simulado de GameTest no recarga la fuerza del ataque y el daño del arma saldría casi a cero. Por tanto se prueba el
 * evento y la fórmula, no el cálculo de daño de la espada de vanilla.
 */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FuerzaGameTests {
    private FuerzaGameTests() {}

    private static final String TEMPLATE = "empty";

    @SuppressWarnings("removal")
    private static ServerPlayer newPlayer(GameTestHelper helper, int fueUpgrades) {
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
        ProgressService.set(player, new PlayerProgress(0, List.of(0, fueUpgrades, 0, 0, 0, 0, 0)));
        return player;
    }

    /**
     * Zombi con 1000 de vida para medir el daño exacto sin que muera, y sin armadura: el zombi de vanilla trae 2 de
     * armadura y el evento (Pre) llega después de ella, así que reduciría ~6 % el daño medido.
     */
    private static Zombie tank(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        zombie.setHealth(1000.0F);
        return zombie;
    }

    private static void assertDamage(GameTestHelper helper, Zombie target, double expected, String what) {
        double dealt = 1000.0 - target.getHealth();
        helper.assertTrue(Math.abs(dealt - expected) < 0.05, what + ": daño esperado " + expected + " pero fue " + dealt);
    }

    @GameTest(template = TEMPLATE)
    public static void fistAtBaseFueDealsOneHp(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 0);
        Zombie zombie = tank(helper);

        zombie.hurt(player.damageSources().playerAttack(player), 1.0F);

        assertDamage(helper, zombie, 1.0, "puño con FUE 8");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fistWithFue26DealsCubicDamage(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 18);   // FUE valor 26
        Zombie zombie = tank(helper);

        zombie.hurt(player.damageSources().playerAttack(player), 1.0F);

        assertDamage(helper, zombie, Math.pow(26.0 / 8.0, 3), "puño con FUE 26"); // ≈ 34,33
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fistWithFue26KillsAVanillaZombieInOneHit(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 18);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

        zombie.hurt(player.damageSources().playerAttack(player), 1.0F);

        helper.assertTrue(!zombie.isAlive(), "un zombi de 20 HP debe morir de un puñetazo con FUE 26");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void vanillaSwordIsNotScaled(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 18);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        Zombie zombie = tank(helper);

        zombie.hurt(player.damageSources().playerAttack(player), 8.0F);

        assertDamage(helper, zombie, 8.0, "espada de diamante con FUE 26");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nonWeaponItemCountsAsFist(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 18);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
        Zombie zombie = tank(helper);

        zombie.hurt(player.damageSources().playerAttack(player), 1.0F);

        assertDamage(helper, zombie, Math.pow(26.0 / 8.0, 3), "bloque en la mano = puño");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void arrowIsNotScaledEvenWithAnEmptyHand(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper, 18);
        Zombie zombie = tank(helper);
        AbstractArrow arrow = EntityType.ARROW.create(helper.getLevel());
        DamageSource source = player.damageSources().arrow(arrow, player);
        helper.assertTrue(!FuerzaEvents.esGolpeDirectoDeJugador(source), "una flecha no es un golpe directo");

        zombie.hurt(source, 5.0F);

        assertDamage(helper, zombie, 5.0, "flecha con FUE 26");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void modWeaponScalesWithFue(GameTestHelper helper) {
        // El mod aún no tiene armas: se simula que la espada de diamante pertenece al tag con un criterio inyectado.
        ServerPlayer player = newPlayer(helper, 18);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

        float vanilla = FuerzaService.danioFinal(player, sword, 8.0F);
        float asModWeapon = FuerzaService.danioFinal(player, sword, 8.0F, s -> true);

        helper.assertTrue(vanilla == 8.0F, "sin estar en el tag: sin escalar");
        helper.assertTrue(Math.abs(asModWeapon - 8.0 * FuerzaSystem.multiplicadorFuerza(26)) < 0.01,
                "como arma del mod: escalada (fue " + asModWeapon + ")");
        helper.assertTrue(!FuerzaService.esArmaDelMod(sword), "la espada de diamante no está en dragonball:armas_del_mod");
        helper.assertTrue(!FuerzaService.esArmaDelMod(ItemStack.EMPTY), "la mano vacía no es un arma del mod");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mobAttacksAreNotAffectedByFue(GameTestHelper helper) {
        ServerPlayer victim = newPlayer(helper, 18);
        Zombie attacker = tank(helper);
        helper.assertTrue(!FuerzaEvents.esGolpeDirectoDeJugador(victim.damageSources().mobAttack(attacker)), "los mobs no tienen FUE");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fueUpgradeIsRejectedAtTheCap(GameTestHelper helper) {
        PlayerProgress atMax = new PlayerProgress(1_000_000, List.of(0, FuerzaSystem.FUE_MAX_UPGRADES, 0, 0, 0, 0, 0));
        helper.assertTrue(atMax.isAtCap(1), "FUE con 91 mejoras está en el tope");
        helper.assertTrue(atMax.tryUpgrade(1) == null, "la mejora 92 de FUE se rechaza aunque haya XP");
        helper.assertTrue(atMax.tryUpgrade(0) != null, "VIT no se ve afectado");
        helper.succeed();
    }
}

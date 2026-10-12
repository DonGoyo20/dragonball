package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.HealthSystem;
import com.dongoyo20.dragonball.core.TipoDanio;
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
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
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

        assertHealth(helper, player, 20.0 - 3.0 * Math.pow(8.0 / 18.0, 3), "VIT 18"); // ≈ 19,74
        helper.assertTrue(20.0 - player.getHealth() < 0.6, "con VIT 18 el zombi quita menos de 0,3 corazones");
        helper.succeed();
    }

    /**
     * Daño de explosión construido igual que vanilla ({@code Explosion.getDamageSource}: entidad directa y causante).
     * No se usa una explosión real porque su daño depende de la distancia y de la exposición medida contra los
     * bloques del recinto de pruebas (daba ~1 HP a 4 bloques), lo que haría la prueba frágil.
     */
    @GameTest(template = TEMPLATE)
    public static void creeperExplosionIsScaledByVit(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit18 = newPlayer(helper);
        setVitUpgrades(vit18, 10);
        net.minecraft.world.entity.monster.Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, 4, 1, 1);
        net.minecraft.world.damagesource.DamageSource blast = helper.getLevel().damageSources().explosion(creeper, creeper);
        helper.assertTrue(HealthService.clasificar(blast) == TipoDanio.ESCALA_SIN_SUELO, "el creeper escala sin suelo");

        vit8.hurt(blast, 20.25F);   // creeper a bocajarro medido en el juego
        vit18.hurt(blast, 20.25F);

        helper.assertTrue(!vit8.isAlive() || vit8.getHealth() < 0.5, "VIT 8: el creeper mata o deja al borde (vida " + vit8.getHealth() + ")");
        assertHealth(helper, vit18, 20.0 - 20.25 * Math.pow(8.0 / 18.0, 3), "VIT 18 ante el creeper"); // ≈ 18,22
        helper.assertTrue(20.0 - vit18.getHealth() < 2.0, "con VIT 18 el creeper quita menos de 1 corazón");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tntExplosionIsScaledByVit(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit26 = newPlayer(helper);
        setVitUpgrades(vit26, 18); // VIT valor 26
        net.minecraft.world.damagesource.DamageSource blast = helper.getLevel().damageSources().explosion(null, null);
        helper.assertTrue(HealthService.clasificar(blast) == TipoDanio.ESCALA_SIN_SUELO, "TNT sin dueño: escala sin suelo");

        vit8.hurt(blast, 6.0F);
        vit26.hurt(blast, 6.0F);

        assertHealth(helper, vit8, 14.0, "TNT con VIT 8");
        assertHealth(helper, vit26, 20.0 - 6.0 * Math.pow(8.0 / 26.0, 3), "TNT con VIT 26"); // ≈ 19,83
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fallOf20BlocksScalesWithVit(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit26 = newPlayer(helper);
        setVitUpgrades(vit26, 18);

        vit8.causeFallDamage(20.0F, 1.0F, vit8.damageSources().fall());   // 20 bloques = 17 HP en vanilla
        vit26.causeFallDamage(20.0F, 1.0F, vit26.damageSources().fall());

        assertHealth(helper, vit8, 3.0, "caída de 20 bloques con VIT 8");
        assertHealth(helper, vit26, 20.0 - 17.0 * Math.pow(8.0 / 26.0, 3), "caída de 20 bloques con VIT 26"); // ≈ 19,5
        helper.assertTrue(20.0 - vit26.getHealth() < 1.0, "con VIT 26 la caída quita menos de medio corazón");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lavaHasAOneHpFloor(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit26 = newPlayer(helper);
        ServerPlayer vit99 = newPlayer(helper);
        setVitUpgrades(vit26, 18);
        setVitUpgrades(vit99, 91);

        vit8.hurt(vit8.damageSources().lava(), 4.0F);     // un golpe de lava en vanilla
        vit26.hurt(vit26.damageSources().lava(), 4.0F);
        vit99.hurt(vit99.damageSources().lava(), 4.0F);

        assertHealth(helper, vit8, 16.0, "lava con VIT 8");
        assertHealth(helper, vit26, 19.5, "lava con VIT 26 (suelo de 0,5 HP)");
        assertHealth(helper, vit99, 19.5, "lava con VIT 99 (suelo de 0,5 HP)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void voidDamageIsNeverScaled(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit99 = newPlayer(helper);
        setVitUpgrades(vit99, 91);

        vit8.hurt(vit8.damageSources().fellOutOfWorld(), 4.0F);   // el vacío hace 4 HP por golpe en vanilla
        vit99.hurt(vit99.damageSources().fellOutOfWorld(), 4.0F);

        assertHealth(helper, vit8, 16.0, "vacío con VIT 8");
        assertHealth(helper, vit99, 16.0, "vacío con VIT 99 (sin escalar)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void damageSourcesAreClassifiedInThreeCategories(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        net.minecraft.world.damagesource.DamageSources d = player.damageSources();
        helper.assertTrue(HealthService.clasificar(d.mobAttack(zombie)) == TipoDanio.ESCALA_SIN_SUELO, "zombi");
        helper.assertTrue(HealthService.clasificar(d.fall()) == TipoDanio.ESCALA_SIN_SUELO, "caída");
        helper.assertTrue(HealthService.clasificar(d.explosion(null, null)) == TipoDanio.ESCALA_SIN_SUELO, "explosión");
        helper.assertTrue(HealthService.clasificar(d.lava()) == TipoDanio.ESCALA_CON_SUELO, "lava");
        helper.assertTrue(HealthService.clasificar(d.inFire()) == TipoDanio.ESCALA_CON_SUELO, "in_fire");
        helper.assertTrue(HealthService.clasificar(d.onFire()) == TipoDanio.ESCALA_CON_SUELO, "on_fire");
        helper.assertTrue(HealthService.clasificar(d.hotFloor()) == TipoDanio.ESCALA_CON_SUELO, "magma");
        helper.assertTrue(HealthService.clasificar(d.drown()) == TipoDanio.ESCALA_CON_SUELO, "ahogamiento");
        helper.assertTrue(HealthService.clasificar(d.cactus()) == TipoDanio.ESCALA_CON_SUELO, "cactus");
        helper.assertTrue(HealthService.clasificar(d.sweetBerryBush()) == TipoDanio.ESCALA_CON_SUELO, "arbusto dulce");
        helper.assertTrue(HealthService.clasificar(d.wither()) == TipoDanio.ESCALA_CON_SUELO, "wither");
        helper.assertTrue(HealthService.clasificar(d.magic()) == TipoDanio.ESCALA_CON_SUELO, "veneno (magic)");
        helper.assertTrue(HealthService.clasificar(d.fellOutOfWorld()) == TipoDanio.NO_ESCALA, "vacío");
        helper.assertTrue(HealthService.clasificar(d.genericKill()) == TipoDanio.NO_ESCALA, "/kill");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void killCommandDamageKillsAtAnyVitWithoutNaN(GameTestHelper helper) {
        ServerPlayer vit8 = newPlayer(helper);
        ServerPlayer vit99 = newPlayer(helper);
        setVitUpgrades(vit99, 91);

        vit8.kill();
        vit99.kill();

        helper.assertTrue(!vit8.isAlive() && !vit99.isAlive(), "/kill debe matar con cualquier VIT");
        helper.assertTrue(!Float.isNaN(vit8.getHealth()) && !Float.isNaN(vit99.getHealth()), "la vida no puede ser NaN");
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

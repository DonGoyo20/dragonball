package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.EsquivaSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.combate.EsquivaService;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Esquiva con un servidor real. La tirada se fija con {@code EsquivaService.setRollForTesting} para que cada prueba sea
 * determinista; una sola prueba usa la tirada aleatoria real y comprueba la frecuencia.
 */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EsquivaGameTests {
    private EsquivaGameTests() {}

    private static final String TEMPLATE = "empty";

    private static ServerPlayer newPlayer(GameTestHelper helper) {
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
        for (int i = 0; i < 61; i++) player.tick();
        player.setGameMode(GameType.SURVIVAL);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        return player;
    }

    /** VEL y CON en mejoras (valor = 8 + mejoras para VEL; CON = mejoras). Ki lleno (50), liberación al 50 %. */
    private static void setProgress(ServerPlayer player, int velUpgrades, int conUpgrades, double ki, double liberacion) {
        ProgressService.set(player, new PlayerProgress(0, List.of(0, 0, 0, velUpgrades, 0, 0, conUpgrades), new KiData(ki, liberacion, 100)));
    }

    private static Zombie zombie(GameTestHelper helper) {
        Zombie z = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        z.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        return z;
    }

    private static void near(GameTestHelper helper, double actual, double expected, double tolerance, String what) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance, what + ": esperado " + expected + " pero fue " + actual);
    }

    private static void roll(double value) {
        EsquivaService.setRollForTesting(() -> value);
    }

    private static void noMoreDodges() {
        EsquivaService.setRollForTesting(() -> GameTestSupport.NEVER_DODGE_ROLL);
    }

    private static double ki(ServerPlayer player) {
        return ProgressService.get(player).ki().kiActual();
    }

    @GameTest(template = TEMPLATE)
    public static void vel8DodgesOnlyWhenTheRollIsBelowTenPercent(GameTestHelper helper) {
        ServerPlayer lucky = newPlayer(helper);
        ServerPlayer unlucky = newPlayer(helper);
        setProgress(lucky, 0, 0, 50, 50);
        setProgress(unlucky, 0, 0, 50, 50);
        Zombie z = zombie(helper);
        near(helper, EsquivaService.probabilidad(lucky), 0.10, 1e-9, "probabilidad con VEL 8");
        try {
            roll(0.05);
            lucky.hurt(lucky.damageSources().mobAttack(z), 3.0F);
            roll(0.15);
            unlucky.hurt(unlucky.damageSources().mobAttack(z), 3.0F);
        } finally {
            noMoreDodges();
        }
        near(helper, 20.0 - lucky.getHealth(), 0.0, 1e-6, "esquivado: sin daño");
        near(helper, ki(lucky), 45.0, 1e-6, "esquivar un golpe de 3 HP cuesta 5 ki");
        near(helper, 20.0 - unlucky.getHealth(), 3.0, 0.01, "no esquivado: daño normal");
        near(helper, ki(unlucky), 50.0, 1e-6, "sin esquivar no se gasta ki");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void vel18DodgesBelowTwentySixPercent(GameTestHelper helper) {
        ServerPlayer lucky = newPlayer(helper);
        ServerPlayer unlucky = newPlayer(helper);
        setProgress(lucky, 10, 0, 50, 50);           // VEL 18 al 50 %
        setProgress(unlucky, 10, 0, 50, 50);
        Zombie z = zombie(helper);
        near(helper, EsquivaService.probabilidad(lucky), EsquivaSystem.probabilidadEsquiva(18), 1e-9, "probabilidad con VEL 18");
        try {
            roll(0.25);                                // 0,25 < 0,262: esquiva (con VEL 8 no habría esquivado)
            lucky.hurt(lucky.damageSources().mobAttack(z), 3.0F);
            roll(0.30);
            unlucky.hurt(unlucky.damageSources().mobAttack(z), 3.0F);
        } finally {
            noMoreDodges();
        }
        near(helper, 20.0 - lucky.getHealth(), 0.0, 1e-6, "VEL 18 esquiva con tirada 0,25");
        near(helper, 20.0 - unlucky.getHealth(), 3.0, 0.01, "VEL 18 no esquiva con tirada 0,30");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void realRandomRollsMatchTheChance(GameTestHelper helper) {
        ServerPlayer slow = newPlayer(helper);
        ServerPlayer fast = newPlayer(helper);
        Zombie z = zombie(helper);
        DamageSource hit = slow.damageSources().mobAttack(z);
        int n = 2000, slowHits = 0, fastHits = 0;
        try {
            EsquivaService.setRollForTesting(null);               // tirada aleatoria real
            for (int i = 0; i < n; i++) {
                setProgress(slow, 0, 0, 50, 50);                  // se rellena el ki en cada intento
                setProgress(fast, 10, 0, 50, 50);
                if (EsquivaService.intentar(slow, 3.0F, hit)) slowHits++;
                if (EsquivaService.intentar(fast, 3.0F, fast.damageSources().mobAttack(z))) fastHits++;
            }
        } finally {
            noMoreDodges();
        }
        near(helper, slowHits / (double) n, 0.10, 0.03, "frecuencia con VEL 8 (~10 %)");
        near(helper, fastHits / (double) n, 0.262, 0.04, "frecuencia con VEL 18 (~26 %)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void withoutKiYouCannotDodgeAndYouAreWarned(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 0, 0, 0, 0);
        Zombie z = zombie(helper);
        List<String> messages = new ArrayList<>();
        var old = EsquivaService.setMessageSinkForTesting((p, text) -> messages.add(((TranslatableContents) text.getContents()).getKey()));
        try {
            roll(0.0);                                   // la tirada saldría siempre
            player.hurt(player.damageSources().mobAttack(z), 3.0F);
        } finally {
            noMoreDodges();
            EsquivaService.setMessageSinkForTesting(old);
        }
        near(helper, 20.0 - player.getHealth(), 3.0, 0.01, "sin ki el golpe se recibe");
        helper.assertTrue(messages.equals(List.of("dragonball.combat.no_ki_to_dodge")), "un aviso de falta de ki: " + messages);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void notEnoughKiForABigHitStillCannotDodge(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 0, 0, 7, 50);                // 7 ki: alcanza para uno de 3 HP (5) pero no para uno de 40 HP (9)
        Zombie z = zombie(helper);
        try {
            roll(0.0);
            player.hurt(player.damageSources().mobAttack(z), 40.0F);
        } finally {
            noMoreDodges();
        }
        helper.assertTrue(player.getHealth() < 20.0F, "con 7 ki no se puede pagar la esquiva de 9");
        near(helper, ki(player), 7.0, 1e-6, "no se gasta ki si no se esquiva");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void environmentalDamageVoidAndKillAreNeverDodged(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 91, 0, 50, 50);              // VEL 99: probabilidad máxima
        Zombie z = zombie(helper);
        var s = player.damageSources();
        helper.assertTrue(!EsquivaService.esEsquivable(s.lava()), "lava");
        helper.assertTrue(!EsquivaService.esEsquivable(s.fall()), "caída");
        helper.assertTrue(!EsquivaService.esEsquivable(s.inFire()) && !EsquivaService.esEsquivable(s.onFire()), "fuego");
        helper.assertTrue(!EsquivaService.esEsquivable(s.drown()), "ahogamiento");
        helper.assertTrue(!EsquivaService.esEsquivable(s.fellOutOfWorld()), "vacío");
        helper.assertTrue(!EsquivaService.esEsquivable(s.genericKill()), "/kill");
        helper.assertTrue(!EsquivaService.esEsquivable(s.explosion(null, null)), "explosión");
        helper.assertTrue(EsquivaService.esEsquivable(s.mobAttack(z)), "golpe de mob");

        // Con la tirada siempre favorable, ni la lava, ni la caída, ni el vacío, ni /kill se esquivan: el ki queda intacto
        try {
            roll(0.0);
            player.hurt(s.lava(), 4.0F);
            near(helper, ki(player), 50.0, 1e-6, "la lava no consume ki");
            helper.assertTrue(player.getHealth() < 20.0F, "la lava hace daño");
            ServerPlayer faller = newPlayer(helper);
            setProgress(faller, 91, 0, 50, 50);
            faller.hurt(faller.damageSources().fall(), 4.0F);
            helper.assertTrue(faller.getHealth() < 20.0F && ki(faller) == 50.0, "la caída hace daño y no consume ki");
            ServerPlayer voided = newPlayer(helper);
            setProgress(voided, 91, 0, 50, 50);
            voided.hurt(voided.damageSources().fellOutOfWorld(), 4.0F);
            helper.assertTrue(voided.getHealth() < 20.0F && ki(voided) == 50.0, "el vacío hace daño y no consume ki");
            ServerPlayer killed = newPlayer(helper);
            setProgress(killed, 91, 0, 50, 50);
            killed.hurt(killed.damageSources().genericKill(), Float.MAX_VALUE);
            helper.assertTrue(!killed.isAlive() || killed.getHealth() <= 0.0F, "/kill mata aunque la tirada sea favorable");
        } finally {
            noMoreDodges();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void releaseChangesTheChance(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 10, 0, 50, 0);               // VEL 18 al 0 %: efectiva 8
        near(helper, EsquivaService.probabilidad(player), 0.10, 1e-9, "VEL 18 al 0 % = vanilla");
        setProgress(player, 10, 0, 50, 100);             // al 100 %: efectiva 36
        near(helper, EsquivaService.probabilidad(player), EsquivaSystem.probabilidadEsquiva(36), 1e-9, "VEL 18 al 100 %");
        setProgress(player, 0, 0, 50, 100);              // VEL 8 al 100 %: efectiva 16
        near(helper, EsquivaService.probabilidad(player), EsquivaSystem.probabilidadEsquiva(16), 1e-9, "VEL 8 al 100 %");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void conLowersTheKiCost(GameTestHelper helper) {
        ServerPlayer plain = newPlayer(helper);
        ServerPlayer controlled = newPlayer(helper);
        setProgress(plain, 0, 0, 50, 50);
        setProgress(controlled, 0, 60, 50, 50);          // CON 60: el coste se divide entre 2
        Zombie z = zombie(helper);
        try {
            roll(0.0);
            plain.hurt(plain.damageSources().mobAttack(z), 30.0F);
            controlled.hurt(controlled.damageSources().mobAttack(z), 30.0F);
        } finally {
            noMoreDodges();
        }
        near(helper, 50.0 - ki(plain), 8.0, 1e-6, "golpe de 30 HP con CON 0");
        near(helper, 50.0 - ki(controlled), 4.0, 1e-6, "golpe de 30 HP con CON 60");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void dodgeBalanceComesFromDataAndIsValidated(GameTestHelper helper) {
        BalanceParams parsed = BalanceManager.parse(JsonParser.parseString("{\"dodge\": {\"max_chance\": 0.5, \"ki_cost_base\": 7}}"));
        helper.assertTrue(parsed.dodge().maxChance() == 0.5 && parsed.dodge().kiCostBase() == 7.0 && parsed.dodge().baseChance() == 0.10,
                "claves de esquiva cambiadas, el resto por defecto");
        List<String> errors = new ArrayList<>();
        BalanceParams bad = BalanceManager.parse(JsonParser.parseString("{\"dodge\": {\"con_reduction\": 0}}"), errors::add);
        helper.assertTrue(bad.equals(BalanceParams.DEFAULT) && errors.size() == 1, "esquiva inválida: se rechaza todo el archivo: " + errors);
        var json = BalanceManager.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, parsed).getOrThrow();
        helper.assertTrue(BalanceManager.parse(json).equals(parsed), "ida y vuelta por el codec (así viaja al cliente)");
        helper.succeed();
    }
}

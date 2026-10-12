package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.ki.KiService;
import com.dongoyo20.dragonball.game.network.BalanceSyncPayload;
import com.dongoyo20.dragonball.game.network.ReleaseInputPayload;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Recurso Ki, liberación y estadísticas efectivas con un servidor real. Los pasos de simulación se invocan con
 * {@code KiService.advance(...)} (sin esperar ticks reales). HUD, teclas y pantalla son del cliente: no se pueden probar aquí.
 */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class KiGameTests {
    private KiGameTests() {}

    private static final String TEMPLATE = "empty";

    @SuppressWarnings("removal")
    private static ServerPlayer newPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        for (int i = 0; i < 61; i++) player.tick();                 // agotar la invulnerabilidad de aparición
        player.setGameMode(GameType.SURVIVAL);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        return player;
    }

    private static void setProgress(ServerPlayer player, List<Integer> upgrades, KiData ki) {
        ProgressService.set(player, new PlayerProgress(0, upgrades, ki));
    }

    private static List<Integer> ups(int vit, int fue, int vel, int ki, int con) {
        return List.of(vit, fue, 0, vel, ki, 0, con);
    }

    private static void near(GameTestHelper helper, double actual, double expected, double tolerance, String what) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance, what + ": esperado " + expected + " pero fue " + actual);
    }

    // ---------- guardado y sincronización

    @GameTest(template = TEMPLATE)
    public static void progressWithKiRoundTripsThroughTheSyncCodec(GameTestHelper helper) {
        PlayerProgress original = new PlayerProgress(1234, ups(3, 2, 1, 18, 4), new KiData(37.5, 20.0, 60));
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        PlayerProgress.STREAM_CODEC.encode(buf, original);
        PlayerProgress decoded = PlayerProgress.STREAM_CODEC.decode(buf);
        helper.assertTrue(decoded.equals(original), "el progreso con ki debe sobrevivir al códec de red: " + decoded);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oldSavesWithoutKiLoadWithTheInitialKi(GameTestHelper helper) {
        var old = JsonParser.parseString("{\"xp\": 5, \"upgrades\": [1, 0, 0, 0, 0, 0, 0]}");
        PlayerProgress loaded = PlayerProgress.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow();
        helper.assertTrue(loaded.xp() == 5 && loaded.upgrades().get(0) == 1, "los datos antiguos se conservan");
        helper.assertTrue(loaded.ki().equals(KiData.INITIAL), "sin campo 'ki' se usa el ki inicial");
        helper.assertTrue(KiData.INITIAL.kiActual() == 50.0 && KiData.INITIAL.liberacion() == 50.0 && KiData.INITIAL.limite() == 50,
                "ki inicial = 50, liberación 50 %, límite 50 %");
        var damaged = JsonParser.parseString("{\"ki\": {\"ki\": -5, \"release\": 900, \"limit\": 250}}");
        KiData fixed = PlayerProgress.CODEC.parse(JsonOps.INSTANCE, damaged).getOrThrow().ki();
        helper.assertTrue(fixed.kiActual() == 0.0 && fixed.limite() == 100 && fixed.liberacion() == 100.0,
                "un guardado dañado se recorta a rangos válidos: " + fixed);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void networkPayloadsRoundTrip(GameTestHelper helper) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        ReleaseInputPayload.STREAM_CODEC.encode(buf, new ReleaseInputPayload((byte) -1));
        helper.assertTrue(ReleaseInputPayload.STREAM_CODEC.decode(buf).direction() == -1, "ReleaseInputPayload");

        BalanceParams custom = BalanceManager.parse(JsonParser.parseString(
                "{\"continuous_floor\": 0.25, \"ki\": {\"cost_per_second_at_full\": 1.5}, \"release\": {\"initial_limit\": 70}}"));
        buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        BalanceSyncPayload.STREAM_CODEC.encode(buf, new BalanceSyncPayload(custom));
        helper.assertTrue(BalanceSyncPayload.STREAM_CODEC.decode(buf).params().equals(custom), "BalanceSyncPayload");
        helper.assertTrue(custom.ki().costPerSecondAtFull() == 1.5 && custom.release().initialLimit() == 70
                && custom.ki().kiFlat() == 50.0, "las claves anidadas se leen y las ausentes conservan su valor");
        helper.succeed();
    }

    // ---------- reserva, gasto, regeneración, límite

    @GameTest(template = TEMPLATE)
    public static void kiReserveFollowsTheKiAttribute(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), KiData.INITIAL);
        helper.assertTrue(KiService.kiMax(player) == 50, "KI 0 → reserva 50");
        setProgress(player, ups(0, 0, 0, 18, 0), KiData.INITIAL);
        helper.assertTrue(KiService.kiMax(player) == 230, "KI 18 → reserva 230");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void holdingTheReleaseDrainsKi(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 50));

        KiService.advance(player, 100, true, 0);

        near(helper, ProgressService.get(player).ki().kiActual(), 49.5, 1e-6, "ki tras 100 ticks al 50 %");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void kiRegeneratesOnlyBelowHalfTheRelease(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(10, 0, 50));
        KiService.advance(player, 100, true, 0);
        near(helper, ProgressService.get(player).ki().kiActual(), 15.0, 1e-6, "regeneración a 0 %");

        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(10, 50, 50));
        KiService.advance(player, 100, true, 0);
        helper.assertTrue(ProgressService.get(player).ki().kiActual() < 10.0, "al 50 % no se regenera y se gasta");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void runningOutOfKiDropsTheReleaseToZero(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(0.01, 50, 50));

        KiService.advance(player, 20, true, 0);

        KiData ki = ProgressService.get(player).ki();
        helper.assertTrue(ki.kiActual() == 0.0 && ki.liberacion() == 0.0, "sin ki la liberación baja a 0: " + ki);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void releaseRisesOnlyWhenStillAndStopsAtTheLimit(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 0, 50));

        KiService.advance(player, 100, false, 1);          // en movimiento: no sube
        near(helper, ProgressService.get(player).ki().liberacion(), 0.0, 1e-9, "en movimiento no sube");

        KiService.advance(player, 20, true, 1);            // quieto 1 s: +10 %
        near(helper, ProgressService.get(player).ki().liberacion(), 10.0, 1e-6, "quieto sube 10 %/s");

        KiService.advance(player, 400, true, 1);           // mucho tiempo: se detiene en el límite (50)
        near(helper, ProgressService.get(player).ki().liberacion(), 50.0, 1e-6, "no supera el límite inicial");

        KiService.setLimit(player, 100);
        KiService.advance(player, 400, true, 1);           // con el límite desbloqueado llega al 100 %
        near(helper, ProgressService.get(player).ki().liberacion(), 100.0, 1e-6, "con el límite en 100 llega al 100 %");

        KiService.advance(player, 20, false, -1);          // bajar: -20 %/s, sin importar el movimiento
        near(helper, ProgressService.get(player).ki().liberacion(), 80.0, 1e-6, "bajar -20 %/s");

        KiService.setLimit(player, 30);                    // si el límite baja, la liberación se recorta
        helper.assertTrue(ProgressService.get(player).ki().liberacion() == 30.0, "el límite recorta la liberación");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void thePlayerTickEventDrivesTheKiLoop(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 50));

        for (int i = 0; i < 25; i++) player.doTick();     // ServerPlayer.doTick() llama a Player.tick(): dispara PlayerTickEvent.Post (5 pasos de 5 ticks)

        near(helper, ProgressService.get(player).ki().kiActual(), 50.0 - 25 * 0.005, 1e-6, "ki tras 25 ticks reales al 50 %");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void clientInputIsClampedOnTheServer(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 0, 50));
        KiService.setInput(player.getUUID(), 99);          // un cliente tramposo no sube más rápido: solo cuenta el signo
        KiService.advance(player, 20, true, Integer.compare(99, 0));
        near(helper, ProgressService.get(player).ki().liberacion(), 10.0, 1e-6, "una entrada enorme equivale a 1");
        KiService.forget(player.getUUID());
        helper.succeed();
    }

    // ---------- estadísticas efectivas en combate y movimiento

    @GameTest(template = TEMPLATE)
    public static void releaseChangesTheDamageTaken(GameTestHelper helper) {
        ServerPlayer low = newPlayer(helper);
        ServerPlayer mid = newPlayer(helper);
        ServerPlayer high = newPlayer(helper);
        setProgress(low, ups(0, 0, 0, 0, 0), new KiData(50, 0, 100));
        setProgress(mid, ups(0, 0, 0, 0, 0), new KiData(50, 50, 100));
        setProgress(high, ups(0, 0, 0, 0, 0), new KiData(50, 100, 100));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

        low.hurt(low.damageSources().mobAttack(zombie), 1.0F);     // liberación 0 %: vanilla (VIT efectiva 8), sin castigo
        mid.hurt(mid.damageSources().mobAttack(zombie), 1.0F);     // 50 %: los atributos del mod (VIT 8) → ×1
        high.hurt(high.damageSources().mobAttack(zombie), 1.0F);   // 100 %: VIT efectiva 16 → ×1/8

        near(helper, 20.0 - low.getHealth(), 1.0, 0.01, "0 % de liberación (vanilla, sin castigo)");
        near(helper, 20.0 - mid.getHealth(), 1.0, 0.01, "50 % de liberación");
        near(helper, 20.0 - high.getHealth(), 0.125, 0.01, "100 % de liberación");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void releaseChangesTheMeleeDamageDealt(GameTestHelper helper) {
        ServerPlayer none = newPlayer(helper);
        ServerPlayer mid = newPlayer(helper);
        ServerPlayer high = newPlayer(helper);
        setProgress(none, ups(0, 0, 0, 0, 0), new KiData(50, 0, 100));
        setProgress(mid, ups(0, 0, 0, 0, 0), new KiData(50, 50, 100));
        setProgress(high, ups(0, 0, 0, 0, 0), new KiData(50, 100, 100));
        Zombie z = tank(helper);
        Zombie a = tank(helper);
        Zombie b = tank(helper);

        z.hurt(none.damageSources().playerAttack(none), 1.0F);
        a.hurt(mid.damageSources().playerAttack(mid), 1.0F);
        b.hurt(high.damageSources().playerAttack(high), 1.0F);

        near(helper, 1000.0 - z.getHealth(), 1.0, 0.05, "puño al 0 % (vanilla ×1, sin castigo)");
        near(helper, 1000.0 - a.getHealth(), 1.0, 0.05, "puño al 50 %");
        near(helper, 1000.0 - b.getHealth(), FuerzaSystem.danioSalienteEfectivo(1.0, 16, BalanceParams.DEFAULT), 0.05, "puño al 100 % (×8)");
        helper.succeed();
    }

    private static Zombie tank(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        zombie.setHealth(1000.0F);
        return zombie;
    }

    @GameTest(template = TEMPLATE)
    public static void maxHealthAndMaxKiDoNotChangeWithTheRelease(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(5, 0, 0, 7, 0), new KiData(50, 0, 100));
        double hp0 = com.dongoyo20.dragonball.game.health.HealthService.vidaRealMaxima(player);
        int ki0 = KiService.kiMax(player);
        setProgress(player, ups(5, 0, 0, 7, 0), new KiData(50, 100, 100));
        helper.assertTrue(com.dongoyo20.dragonball.game.health.HealthService.vidaRealMaxima(player) == hp0, "la vida máxima es fija");
        helper.assertTrue(KiService.kiMax(player) == ki0 && ki0 == 120, "el ki máximo es fijo (50 + 10 × 7)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void velAffectsMovementSpeedAndJump(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 100));
        KiService.advance(player, 1, true, 0);
        near(helper, KiService.speedModifierAmount(player), 0.0, 1e-9, "al 50 % no hay modificador de velocidad");
        setProgress(player, ups(0, 0, 10, 0, 0), new KiData(50, 0, 100));   // VEL 18 al 0 %
        KiService.advance(player, 1, true, 0);
        near(helper, KiService.speedModifierAmount(player), 0.0, 1e-9, "al 0 % la velocidad es la de vanilla (no lento)");
        near(helper, KiService.jumpModifierAmount(player), 0.0, 1e-9, "al 0 % el salto es el de vanilla");
        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 100));
        KiService.advance(player, 1, true, 0);
        near(helper, KiService.jumpModifierAmount(player), 0.0, 1e-9, "al 50 % no hay modificador de salto");

        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 100, 100));
        KiService.advance(player, 1, true, 0);
        near(helper, KiService.speedModifierAmount(player), Math.sqrt(2.0) - 1.0, 1e-3, "al 100 %: velocidad ×√2");
        near(helper, KiService.jumpModifierAmount(player), Math.pow(2.0, 0.25) - 1.0, 1e-3, "al 100 %: salto ×2^0,25");
        near(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.1 * Math.sqrt(2.0), 0.002, "el atributo real del jugador cambia");

        setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 100));
        KiService.advance(player, 1, true, 0);
        near(helper, KiService.speedModifierAmount(player), 0.0, 1e-9, "al volver al 50 % se quita el modificador");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void kiUpgradeIsRejectedAtTheCapAndOtherAttributesHaveCaps(GameTestHelper helper) {
        BalanceParams p = BalanceParams.DEFAULT;
        PlayerProgress max = new PlayerProgress(10_000_000, List.of(0, 0, 0, p.velMaxUpgrades(), p.kiMaxUpgrades(), 0, p.conMaxUpgrades()));
        helper.assertTrue(max.isAtCap(3) && max.isAtCap(4) && max.isAtCap(6), "VEL, KI y CON en el tope");
        helper.assertTrue(max.tryUpgrade(3) == null && max.tryUpgrade(4) == null && max.tryUpgrade(6) == null, "no se puede pasar del tope");
        helper.assertTrue(max.tryUpgrade(0) != null && max.tryUpgrade(2) != null, "los demás no se ven afectados");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void upgradingPreservesTheKiState(GameTestHelper helper) {
        PlayerProgress before = new PlayerProgress(1000, ups(0, 0, 0, 0, 0), new KiData(33.0, 20.0, 80));
        PlayerProgress after = before.tryUpgrade(0);
        helper.assertTrue(after != null && after.ki().equals(before.ki()), "comprar una mejora no reinicia el ki");
        helper.assertTrue(before.addXp(5).ki().equals(before.ki()) && before.withXp(3).ki().equals(before.ki()), "la XP tampoco");
        helper.succeed();
    }

    // ---------- sin ki (sin castigo) y avisos

    @GameTest(template = TEMPLATE)
    public static void withoutKiTheCharacterIsVanillaWithoutPunishment(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, ups(10, 10, 0, 0, 0), new KiData(0.0, 0.0, 50));   // VIT 18 y FUE 18, sin ki
        Zombie attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        Zombie victim = tank(helper);

        player.hurt(player.damageSources().mobAttack(attacker), 1.0F);             // vanilla (×1), no ×4 ni ×0,088
        victim.hurt(player.damageSources().playerAttack(player), 1.0F);            // vanilla (×1), no ×0,25 ni ×11

        near(helper, 20.0 - player.getHealth(), 1.0, 0.01, "daño recibido sin ki = vanilla");
        near(helper, 1000.0 - victim.getHealth(), 1.0, 0.05, "daño hecho sin ki = vanilla");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void outOfKiWarnsOnceAndLoweringReleaseYourselfDoesNot(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        List<String> messages = new java.util.ArrayList<>();
        var old = KiService.setMessageSinkForTesting((p, text) -> messages.add(((net.minecraft.network.chat.contents.TranslatableContents) text.getContents()).getKey()));
        try {
            setProgress(player, ups(0, 0, 0, 0, 0), new KiData(0.01, 50, 50));
            KiService.advance(player, 20, true, 0);                               // el ki se agota: la liberación baja a 0
            helper.assertTrue(messages.equals(List.of("message.dragonball.no_ki")), "un único aviso 'Sin Ki': " + messages);

            KiService.advance(player, 20, true, 0);                               // ya está a 0: no repite el aviso
            helper.assertTrue(messages.size() == 1, "no se repite estando ya a 0");

            messages.clear();
            setProgress(player, ups(0, 0, 0, 0, 0), new KiData(40, 30, 50));
            KiService.advance(player, 100, true, -1);                             // el jugador la baja a 0 con X: sin aviso de falta de ki
            helper.assertTrue(ProgressService.get(player).ki().liberacion() == 0.0 && messages.isEmpty(),
                    "bajarla uno mismo no es quedarse sin ki: " + messages);
        } finally {
            KiService.setMessageSinkForTesting(old);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void standStillWarningAppearsAtMostOnceEveryTwoSeconds(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        List<String> messages = new java.util.ArrayList<>();
        var old = KiService.setMessageSinkForTesting((p, text) -> messages.add(((net.minecraft.network.chat.contents.TranslatableContents) text.getContents()).getKey()));
        try {
            KiService.forget(player.getUUID());
            // el aviso en sí: instantes 0, 10, 39 (dentro de los 40 ticks) no repiten; 40 y 80 sí
            int sent = 0;
            for (long t : new long[] {0, 10, 39, 40, 41, 79, 80}) {
                if (KiService.warnMustStandStill(player, t)) sent++;
            }
            helper.assertTrue(sent == 3 && messages.size() == 3, "3 avisos en 80 ticks (0, 40 y 80), hubo " + sent);
            helper.assertTrue(messages.stream().allMatch("message.dragonball.must_stand_still"::equals), "es el aviso de estar quieto");

            // por el camino real: intentar subir moviéndose avisa; quieto no; en el límite no
            KiService.forget(player.getUUID());
            messages.clear();
            setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 10, 50));
            KiService.advance(player, 5, false, 1);
            KiService.advance(player, 5, false, 1);                               // mismo instante de juego: no se repite
            helper.assertTrue(messages.size() == 1, "moviéndose y pulsando C avisa una sola vez: " + messages);
            messages.clear();
            KiService.forget(player.getUUID());
            KiService.advance(player, 5, true, 1);
            KiService.advance(player, 5, false, 0);
            helper.assertTrue(messages.isEmpty(), "quieto o sin pulsar C no avisa: " + messages);
            setProgress(player, ups(0, 0, 0, 0, 0), new KiData(50, 50, 50));     // ya en el límite: subir no tendría efecto
            KiService.advance(player, 5, false, 1);
            helper.assertTrue(messages.isEmpty(), "en el límite no avisa: " + messages);
        } finally {
            KiService.setMessageSinkForTesting(old);
            KiService.forget(player.getUUID());
        }
        helper.succeed();
    }
}

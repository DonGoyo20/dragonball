package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.game.combate.EsquivaService;
import com.dongoyo20.dragonball.game.damage.FuerzaService;
import com.dongoyo20.dragonball.game.health.HealthService;
import com.dongoyo20.dragonball.game.ki.KiData;
import com.dongoyo20.dragonball.game.ki.KiService;
import com.dongoyo20.dragonball.game.network.RaceSyncPayload;
import com.dongoyo20.dragonball.game.progress.AttributeValues;
import com.dongoyo20.dragonball.game.progress.ModAttachments;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import com.dongoyo20.dragonball.game.raza.RaceManager;
import com.dongoyo20.dragonball.game.raza.RaceService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Razas como datos con un servidor real: carga, comando, attachment, reinicio de stats y efecto sobre el combate. */
@GameTestHolder(DragonBall.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RazaGameTests {
    private RazaGameTests() {}

    private static final String TEMPLATE = "empty";

    private static ServerPlayer newPlayer(GameTestHelper helper) {
        ServerPlayer player = GameTestSupport.mockPlayer(helper);
        for (int i = 0; i < 61; i++) player.tick();
        player.setGameMode(GameType.SURVIVAL);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        return player;
    }

    private static void setProgress(ServerPlayer player, long xp, List<Integer> upgrades, double lib) {
        ProgressService.set(player, new PlayerProgress(xp, upgrades, new KiData(50, lib, 100)));
    }

    private static List<Integer> noUpgrades() {
        return List.of(0, 0, 0, 0, 0, 0, 0);
    }

    private static void near(GameTestHelper helper, double actual, double expected, double tolerance, String what) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance, what + ": esperado " + expected + " pero fue " + actual);
    }

    private static Zombie tank(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        zombie.setHealth(1000.0F);
        return zombie;
    }

    /** Origen de comandos que recoge los mensajes que se le mandan. */
    private static CommandSourceStack capturing(ServerPlayer player, int permission, List<Component> messages) {
        CommandSource sink = new CommandSource() {
            @Override public void sendSystemMessage(Component message) { messages.add(message); }
            @Override public boolean acceptsSuccess() { return true; }
            @Override public boolean acceptsFailure() { return true; }
            @Override public boolean shouldInformAdmins() { return false; }
        };
        return new CommandSourceStack(sink, Vec3.ZERO, Vec2.ZERO, player.serverLevel(), permission, "test", Component.literal("test"),
                player.getServer(), player);
    }

    /** ¿El mensaje (o alguno de sus hijos) es la traducción con esa clave? Los fallos de comando llegan envueltos. */
    private static boolean hasKey(Component c, String key) {
        if (c.getContents() instanceof TranslatableContents t && t.getKey().equals(key)) return true;
        return c.getSiblings().stream().anyMatch(s -> hasKey(s, key));
    }

    // ---------- carga de datos

    @GameTest(template = TEMPLATE)
    public static void managerLoadsBothRacesFromData(GameTestHelper helper) {
        helper.assertTrue(RaceManager.ids().containsAll(List.of("terricola", "saiyajin")) && RaceManager.getAll().size() == 2,
                "las dos razas cargadas desde data/dragonball/races: " + RaceManager.ids());
        Race t = RaceManager.get("terricola"), s = RaceManager.get("saiyajin");
        int[] terr = {8, 8, 8, 8, 0, 8, 0};
        int[] sai = {9, 10, 9, 10, 0, 8, 0};
        for (Atributo a : Atributo.values()) {
            helper.assertTrue(t.base(a) == terr[a.index()], "Terrícola " + a + " = " + t.base(a));
            helper.assertTrue(s.base(a) == sai[a.index()], "Saiyajin " + a + " = " + s.base(a));
        }
        helper.assertTrue(RaceManager.getDefault().id().equals("terricola"), "la raza por defecto es terricola");
        helper.assertTrue(t.traits().stream().map(r -> r.id()).toList().equals(List.of("adaptable", "sin_cola")), "rasgos del Terrícola");
        helper.assertTrue(s.traits().stream().map(r -> r.id()).toList().equals(List.of("zenkai", "cola", "apetito_saiyajin")), "rasgos del Saiyajin");
        helper.assertTrue(RaceManager.get("nope") == null && RaceManager.getOrDefault("nope").id().equals("terricola"), "id desconocido");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void managerRejectsInvalidFilesAndKeepsTheDefaultRace(GameTestHelper helper) {
        BalanceParams p = BalanceParams.DEFAULT;
        String ok = "{\"id\":\"%s\",\"display_name\":\"n\",\"description\":\"d\",\"base_stats\":{\"vit\":8,\"fue\":8,\"def\":8,\"vel\":8,\"ki\":0,\"pod\":8,\"con\":0}}";
        Map<String, JsonElement> files = new TreeMap<>();
        files.put("buena", JsonParser.parseString(ok.formatted("buena")));
        files.put("MalId", JsonParser.parseString(ok.formatted("MalId")));                                   // id con mayúsculas
        files.put("distinta", JsonParser.parseString(ok.formatted("otra")));                                  // id ≠ nombre de archivo
        files.put("sinstat", JsonParser.parseString("{\"id\":\"sinstat\",\"display_name\":\"n\",\"description\":\"d\",\"base_stats\":{\"vit\":8}}"));
        files.put("raro", JsonParser.parseString("{\"id\":\"raro\",\"display_name\":\"n\",\"description\":\"d\",\"base_stats\":{\"vit\":8,\"fue\":8,\"def\":8,\"vel\":8,\"ki\":0,\"pod\":8,\"con\":0,\"magia\":5}}"));
        files.put("debil", JsonParser.parseString(ok.formatted("debil").replace("\"vit\":8", "\"vit\":7")));  // VIT < vanilla
        files.put("negativa", JsonParser.parseString(ok.formatted("negativa").replace("\"ki\":0", "\"ki\":-1")));
        files.put("basura", JsonParser.parseString("[1, 2, 3]"));
        List<String> errors = new ArrayList<>();
        Map<String, Race> result = RaceManager.parse(files, p, errors::add);
        helper.assertTrue(result.keySet().equals(java.util.Set.of("buena", "terricola")),
                "solo la válida y la copia integrada de terricola: " + result.keySet());
        helper.assertTrue(errors.size() == 8, "7 archivos inválidos + aviso de falta de terricola: " + errors);
        helper.assertTrue(result.get("terricola") == RaceManager.TERRICOLA_FALLBACK, "se usa la copia integrada");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void codecRoundTripAndNetworkSync(GameTestHelper helper) {
        for (Race r : RaceManager.getAll()) {
            JsonElement json = RaceManager.CODEC.encodeStart(JsonOps.INSTANCE, r).getOrThrow();
            helper.assertTrue(RaceManager.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().equals(r), "ida y vuelta por JSON: " + r.id());
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        RaceSyncPayload.STREAM_CODEC.encode(buf, new RaceSyncPayload(List.copyOf(RaceManager.getAll())));
        RaceSyncPayload decoded = RaceSyncPayload.STREAM_CODEC.decode(buf);
        helper.assertTrue(decoded.races().equals(List.copyOf(RaceManager.getAll())), "las razas viajan al cliente intactas");
        // el cliente las adopta
        var old = RaceManager.snapshot();
        try {
            RaceManager.setFromServer(decoded.races());
            helper.assertTrue(RaceManager.get("saiyajin").base(Atributo.FUE) == 10, "el cliente adopta las razas del servidor");
        } finally {
            RaceManager.setForTesting(old);
        }
        helper.succeed();
    }

    // ---------- el jugador

    @GameTest(template = TEMPLATE)
    public static void aNewPlayerIsTerricola(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        helper.assertTrue(RaceService.idOf(player).equals("terricola"), "raza inicial: " + RaceService.idOf(player));
        RaceService.ensureRace(player);
        helper.assertTrue(player.hasData(ModAttachments.RACE) && RaceService.raceOf(player).id().equals("terricola"), "ensureRace deja terricola guardada");
        for (Atributo a : Atributo.values()) {
            helper.assertTrue(AttributeValues.baseValue(a.index(), PlayerProgress.EMPTY, BalanceParams.DEFAULT, RaceService.base(player, a))
                    == AttributeValues.baseValue(a.index(), PlayerProgress.EMPTY, BalanceParams.DEFAULT), "stats de un jugador existente sin cambios: " + a);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anUnknownSavedRaceFallsBackToTheDefault(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        player.setData(ModAttachments.RACE, "borrada");
        helper.assertTrue(RaceService.raceOf(player).id().equals("terricola"), "raza desconocida = la por defecto al calcular");
        RaceService.ensureRace(player);
        helper.assertTrue(RaceService.idOf(player).equals("terricola"), "ensureRace la reemplaza");
        player.setData(ModAttachments.RACE, "saiyajin");
        RaceService.ensureRace(player);
        helper.assertTrue(RaceService.idOf(player).equals("saiyajin"), "una raza válida no se toca");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void raceSetCommandChangesTheRaceAndTheBaseStats(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 500, List.of(3, 2, 0, 1, 0, 0, 0), 50);       // un jugador con mejoras compradas
        List<Component> messages = new ArrayList<>();
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 2, messages), "dragonball race set saiyajin");

        helper.assertTrue(RaceService.idOf(player).equals("saiyajin"), "la raza cambió a saiyajin");
        PlayerProgress after = ProgressService.get(player);
        helper.assertTrue(after.upgrades().stream().allMatch(u -> u == 0), "las mejoras se reiniciaron: " + after.upgrades());
        helper.assertTrue(after.xp() == 500, "la XP se conserva");
        int[] sai = {9, 10, 9, 10, 0, 8, 0};
        for (Atributo a : Atributo.values()) {
            helper.assertTrue(AttributeValues.baseValue(a.index(), after, BalanceParams.DEFAULT, RaceService.base(player, a)) == sai[a.index()],
                    "valor base de " + a + " = el de la raza");
        }
        helper.assertTrue(FuerzaService.fue(player) == 10 && HealthService.vit(player) == 9 && HealthService.def(player) == 9
                && KiService.velValue(player) == 10 && KiService.kiValue(player) == 0 && KiService.conValue(player) == 0, "los servicios usan la raza");
        helper.assertTrue(messages.stream().anyMatch(m -> hasKey(m, "command.dragonball.race.set")), "mensaje de confirmación: " + messages);

        // y de vuelta a terricola
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 2, new ArrayList<>()), "dragonball race set terricola");
        helper.assertTrue(RaceService.idOf(player).equals("terricola") && FuerzaService.fue(player) == 8, "de vuelta a Terrícola: FUE 8");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void raceSetNeedsPermissionAndAKnownRace(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        List<Component> messages = new ArrayList<>();
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 0, messages), "dragonball race set saiyajin");
        helper.assertTrue(RaceService.idOf(player).equals("terricola"), "sin permisos no cambia la raza");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 2, messages), "dragonball race set namekiano");
        helper.assertTrue(RaceService.idOf(player).equals("terricola"), "raza desconocida: no cambia");
        helper.assertTrue(messages.stream().anyMatch(m -> hasKey(m, "command.dragonball.race.unknown")), "mensaje de raza desconocida: " + messages);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void raceInfoAndListShowTheData(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        List<Component> info = new ArrayList<>();
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 0, info), "dragonball race info");
        helper.assertTrue(info.size() == 3 && hasKey(info.get(0), "command.dragonball.race.info")
                && info.subList(1, 3).stream().allMatch(m -> hasKey(m, "command.dragonball.race.trait")), "info Terrícola: raza + 2 rasgos: " + info);
        RaceService.setRace(player, RaceManager.get("saiyajin"));
        List<Component> info2 = new ArrayList<>();
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 0, info2), "dragonball race info");
        helper.assertTrue(info2.size() == 4, "info Saiyajin: raza + 3 rasgos: " + info2);
        List<Component> list = new ArrayList<>();
        helper.getLevel().getServer().getCommands().performPrefixedCommand(capturing(player, 0, list), "dragonball race list");
        helper.assertTrue(list.size() == 1 && hasKey(list.get(0), "command.dragonball.race.list"), "list: " + list);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void changingRaceResetsTheBaseStatsButUpgradesStayEqualForEveryRace(GameTestHelper helper) {
        ServerPlayer player = newPlayer(helper);
        setProgress(player, 100_000, noUpgrades(), 50);
        RaceService.setRace(player, RaceManager.get("saiyajin"));
        int fueIndex = ProgressService.attributeIndex("fue");
        long cost = ProgressService.get(player).nextUpgradeCost();
        helper.assertTrue(ProgressService.upgrade(player, fueIndex), "se compra una mejora de FUE");
        helper.assertTrue(FuerzaService.fue(player) == 11, "FUE 10 + 1 mejora = 11 (+1 por mejora, como en todas las razas)");
        helper.assertTrue(ProgressService.get(player).xp() == 100_000 - cost, "el coste es el de siempre");
        RaceService.setRace(player, RaceManager.get("terricola"));
        helper.assertTrue(FuerzaService.fue(player) == 8 && ProgressService.get(player).upgrades().get(fueIndex) == 0, "cambiar reinicia");
        long sameCost = ProgressService.get(player).nextUpgradeCost();
        helper.assertTrue(sameCost == cost, "mismo coste para la primera mejora de cualquier raza");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theCapIsOnTheValueSoSaiyansHaveFewerFueUpgrades(GameTestHelper helper) {
        ServerPlayer saiyan = newPlayer(helper);
        RaceService.setRace(saiyan, RaceManager.get("saiyajin"));
        setProgress(saiyan, 1_000_000_000L, List.of(0, 88, 0, 0, 0, 0, 0), 50);
        helper.assertTrue(ProgressService.upgrade(saiyan, 1) && FuerzaService.fue(saiyan) == 99, "la mejora 89 de FUE lleva a 99");
        helper.assertTrue(!ProgressService.upgrade(saiyan, 1) && ProgressService.get(saiyan).upgrades().get(1) == 89, "la 90 se rechaza (tope 99)");
        helper.assertTrue(ProgressService.get(saiyan).isAtCap(1, 10) && !ProgressService.get(saiyan).isAtCap(0, 9), "tope por raza");

        ServerPlayer earthling = newPlayer(helper);
        setProgress(earthling, 1_000_000_000L, List.of(0, 90, 0, 0, 0, 0, 0), 50);
        helper.assertTrue(ProgressService.upgrade(earthling, 1) && FuerzaService.fue(earthling) == 99, "Terrícola llega a 99 con 91 mejoras");
        helper.assertTrue(!ProgressService.upgrade(earthling, 1), "y no pasa de ahí");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void releaseIsAppliedOnTopOfTheRaceBase(GameTestHelper helper) {
        ServerPlayer saiyan = newPlayer(helper);
        RaceService.setRace(saiyan, RaceManager.get("saiyajin"));
        double[] saiyanFue = new double[3];
        double[] libs = {0, 50, 100};
        for (int i = 0; i < 3; i++) {
            setProgress(saiyan, 0, noUpgrades(), libs[i]);
            saiyanFue[i] = FuerzaService.fueEfectiva(saiyan);
        }
        near(helper, saiyanFue[0], 8.0, 1e-9, "Saiyajin FUE al 0 % (vanilla)");
        near(helper, saiyanFue[1], 10.0, 1e-9, "Saiyajin FUE al 50 %");
        near(helper, saiyanFue[2], 20.0, 1e-9, "Saiyajin FUE al 100 %");

        ServerPlayer earthling = newPlayer(helper);
        double[] earthFue = new double[3];
        for (int i = 0; i < 3; i++) {
            setProgress(earthling, 0, noUpgrades(), libs[i]);
            earthFue[i] = FuerzaService.fueEfectiva(earthling);
        }
        near(helper, earthFue[0], 8.0, 1e-9, "Terrícola FUE al 0 %");
        near(helper, earthFue[1], 8.0, 1e-9, "Terrícola FUE al 50 %");
        near(helper, earthFue[2], 16.0, 1e-9, "Terrícola FUE al 100 % (como siempre)");

        // la esquiva también usa la VEL de la raza (Saiyajin VEL 10 al 50 %)
        setProgress(saiyan, 0, noUpgrades(), 50);
        near(helper, EsquivaService.velEfectiva(saiyan), 10.0, 1e-9, "Saiyajin VEL al 50 %");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void saiyanFistHitsHarderThanVanillaAtFiftyPercent(GameTestHelper helper) {
        ServerPlayer saiyan = newPlayer(helper);
        RaceService.setRace(saiyan, RaceManager.get("saiyajin"));
        setProgress(saiyan, 0, noUpgrades(), 50);
        Zombie z = tank(helper);
        z.hurt(saiyan.damageSources().playerAttack(saiyan), 1.0F);
        near(helper, 1000.0 - z.getHealth(), Math.pow(10.0 / 8.0, 3), 0.05, "puño de un Saiyajin al 50 % (×1,95)");

        ServerPlayer zero = newPlayer(helper);
        RaceService.setRace(zero, RaceManager.get("saiyajin"));
        setProgress(zero, 0, noUpgrades(), 0);
        Zombie z0 = tank(helper);
        z0.hurt(zero.damageSources().playerAttack(zero), 1.0F);
        near(helper, 1000.0 - z0.getHealth(), 1.0, 0.05, "al 0 % es vanilla");

        ServerPlayer full = newPlayer(helper);
        RaceService.setRace(full, RaceManager.get("saiyajin"));
        setProgress(full, 0, noUpgrades(), 100);
        Zombie z1 = tank(helper);
        z1.hurt(full.damageSources().playerAttack(full), 1.0F);
        near(helper, 1000.0 - z1.getHealth(), Math.pow(20.0 / 8.0, 3), 0.1, "al 100 % (FUE 20: ×15,6)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void saiyanTakesLessDamageThanAnEarthlingAtFiftyPercent(GameTestHelper helper) {
        ServerPlayer saiyan = newPlayer(helper);
        RaceService.setRace(saiyan, RaceManager.get("saiyajin"));
        setProgress(saiyan, 0, noUpgrades(), 50);                         // VIT 9 y DEF 9
        ServerPlayer earthling = newPlayer(helper);
        setProgress(earthling, 0, noUpgrades(), 50);                      // VIT 8 y DEF 8
        Zombie z = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        z.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        saiyan.hurt(saiyan.damageSources().mobAttack(z), 4.0F);
        earthling.hurt(earthling.damageSources().mobAttack(z), 4.0F);
        double vit = Math.pow(8.0 / 9.0, 3);
        near(helper, 20.0 - saiyan.getHealth(), 4.0 * vit * Math.min(1.0, 1.25 * vit), 0.01, "Saiyajin con VIT 9 y DEF 9");
        near(helper, 20.0 - earthling.getHealth(), 4.0, 0.01, "Terrícola: daño de vanilla");
        helper.succeed();
    }
}

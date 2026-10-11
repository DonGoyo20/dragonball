package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.DragonBall;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Integración con FTB Quests. Se ejecuta SOLO en: ./gradlew runGameTestFtbQuests -PwithFtbQuests
 * No enlaza clases de FTB Quests: comprueba que el mod está cargado y que los archivos de misiones (datos nuestros)
 * apuntan a logros y entidades que existen de verdad en el juego. La carga de las misiones por parte de FTB se
 * verifica con su línea de registro (tools/verify_integrations.sh).
 */
@GameTestHolder(DragonBall.MOD_ID + "_ftb")
@PrefixGameTestTemplate(false)
public final class FtbQuestsGameTests {
    private FtbQuestsGameTests() {}

    private static final Pattern ADVANCEMENT = Pattern.compile("advancement:\\s*\"([^\"]+)\"");
    private static final Pattern ENTITY = Pattern.compile("entity:\\s*\"([^\"]+)\"");

    @GameTest(template = "empty")
    public static void ftbQuestsIsLoaded(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("ftbquests"), "FTB Quests debe estar cargado en esta ejecución");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void questFilesReferenceExistingAdvancementsAndEntities(GameTestHelper helper) throws IOException {
        Path chapters = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests/chapters");
        helper.assertTrue(Files.isDirectory(chapters), "debe existir la carpeta de capítulos: " + chapters);

        List<String> advancements = new ArrayList<>();
        List<String> entities = new ArrayList<>();
        try (var files = Files.list(chapters)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".snbt")).toList()) {
                String text = Files.readString(file);
                collect(ADVANCEMENT, text, advancements);
                collect(ENTITY, text, entities);
            }
        }
        helper.assertTrue(!advancements.isEmpty(), "las misiones de prueba deben referenciar al menos un logro");
        helper.assertTrue(!entities.isEmpty(), "las misiones de prueba deben referenciar al menos una entidad");

        var server = helper.getLevel().getServer();
        for (String id : advancements) {
            helper.assertTrue(server.getAdvancements().get(ResourceLocation.parse(id)) != null,
                    "el logro " + id + " de una misión no existe en el juego");
        }
        for (String id : entities) {
            helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(id)),
                    "la entidad " + id + " de una misión no existe en el juego");
        }
        helper.succeed();
    }

    private static void collect(Pattern pattern, String text, List<String> into) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            into.add(m.group(1));
        }
    }
}

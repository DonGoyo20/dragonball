package com.dongoyo20.dragonball.game.balance;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

/**
 * Balance del combate como DATOS: {@code data/dragonball/balance/combat.json}. Se recarga con {@code /reload} y se puede
 * sobrescribir con un datapack o con KubeJS (ver integrations/kubejs) sin recompilar. Los números por defecto son los de
 * {@link BalanceParams#DEFAULT}; una clave ausente conserva su valor por defecto, así que un datapack puede cambiar
 * solo una.
 *
 * <p>Si el archivo está mal formado o tiene valores inválidos NO se rompe la carga ni se queda a medias: se registra el
 * error y se usan los valores por defecto (nunca se adopta un archivo parcialmente válido).
 *
 * <p>Los cálculos de combate son del servidor, así que solo el servidor necesita estos valores. Cuando exista un HUD que
 * muestre la vida real habrá que sincronizarlos al cliente.
 */
public final class BalanceManager {
    private BalanceManager() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation COMBAT_FILE = ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "combat");

    private static final BalanceParams D = BalanceParams.DEFAULT;

    /** Cada clave es opcional y vale su valor por defecto; un valor inválido hace fallar todo el archivo. */
    static final Codec<BalanceParams> CODEC = RecordCodecBuilder.<BalanceParams>create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("vit_base", D.vitBase()).forGetter(BalanceParams::vitBase),
            Codec.DOUBLE.optionalFieldOf("vit_growth", D.vitGrowth()).forGetter(BalanceParams::vitGrowth),
            Codec.DOUBLE.optionalFieldOf("hp_per_vit", D.hpPerVit()).forGetter(BalanceParams::hpPerVit),
            Codec.DOUBLE.optionalFieldOf("damage_exponent", D.damageExponent()).forGetter(BalanceParams::damageExponent),
            Codec.DOUBLE.optionalFieldOf("continuous_floor", D.continuousFloor()).forGetter(BalanceParams::continuousFloor),
            Codec.DOUBLE.optionalFieldOf("fue_base", D.fueBase()).forGetter(BalanceParams::fueBase),
            Codec.DOUBLE.optionalFieldOf("fue_growth", D.fueGrowth()).forGetter(BalanceParams::fueGrowth),
            Codec.DOUBLE.optionalFieldOf("strength_exponent", D.strengthExponent()).forGetter(BalanceParams::strengthExponent),
            Codec.INT.optionalFieldOf("attribute_cap", D.attributeCap()).forGetter(BalanceParams::attributeCap)
    ).apply(i, BalanceParams::new)).validate(p -> {
        String problem = p.problem();
        return problem == null ? DataResult.success(p) : DataResult.error(() -> problem);
    });

    private static volatile BalanceParams current = BalanceParams.DEFAULT;

    /** Parámetros vigentes (los de siempre hasta que se cargue un archivo de datos válido). */
    public static BalanceParams get() {
        return current;
    }

    /** Solo para pruebas: fija los parámetros y devuelve los anteriores para restaurarlos. */
    public static BalanceParams setForTesting(BalanceParams params) {
        BalanceParams old = current;
        current = params;
        return old;
    }

    /** Interpreta un JSON de balance; ante cualquier error devuelve los valores por defecto y lo registra en el log. */
    public static BalanceParams parse(JsonElement json) {
        return parse(json, error -> LOGGER.error("Balance inválido en {}: {}. Se usan los valores por defecto.",
                COMBAT_FILE, error));
    }

    /** Igual, pero el aviso de error va a {@code onError} (las pruebas lo recogen en vez de ensuciar el log). */
    public static BalanceParams parse(JsonElement json, Consumer<String> onError) {
        DataResult<BalanceParams> result = CODEC.parse(JsonOps.INSTANCE, json);
        return result.resultOrPartial(onError).orElse(BalanceParams.DEFAULT);
    }

    /** Lee {@code data/<espacio>/balance/*.json}; solo se usa {@code dragonball:combat}. */
    static final class Reloader extends SimpleJsonResourceReloadListener {
        Reloader() {
            super(new Gson(), "balance");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            JsonElement json = files.get(COMBAT_FILE);
            if (json == null) {
                LOGGER.warn("No existe {}: se usan los valores de balance por defecto.", COMBAT_FILE);
                current = BalanceParams.DEFAULT;
            } else {
                current = parse(json);
                LOGGER.info("Balance de combate cargado: {}", current);
            }
        }
    }

    @EventBusSubscriber(modid = DragonBall.MOD_ID)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new Reloader());
        }
    }
}

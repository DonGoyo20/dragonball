package com.dongoyo20.dragonball.game.balance;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.KiParams;
import com.dongoyo20.dragonball.core.ReleaseParams;
import com.dongoyo20.dragonball.game.network.BalanceSyncPayload;
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
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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

    private static final KiParams KD = KiParams.DEFAULT;
    private static final ReleaseParams RD = ReleaseParams.DEFAULT;

    static final Codec<KiParams> KI_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("ki_base", KD.kiBase()).forGetter(KiParams::kiBase),
            Codec.DOUBLE.optionalFieldOf("ki_growth", KD.kiGrowth()).forGetter(KiParams::kiGrowth),
            Codec.DOUBLE.optionalFieldOf("ki_flat", KD.kiFlat()).forGetter(KiParams::kiFlat),
            Codec.DOUBLE.optionalFieldOf("ki_per_point", KD.kiPerPoint()).forGetter(KiParams::kiPerPoint),
            Codec.DOUBLE.optionalFieldOf("con_base", KD.conBase()).forGetter(KiParams::conBase),
            Codec.DOUBLE.optionalFieldOf("con_growth", KD.conGrowth()).forGetter(KiParams::conGrowth),
            Codec.DOUBLE.optionalFieldOf("con_scale", KD.conScale()).forGetter(KiParams::conScale),
            Codec.DOUBLE.optionalFieldOf("cost_per_second_at_full", KD.costPerSecondAtFull()).forGetter(KiParams::costPerSecondAtFull),
            Codec.DOUBLE.optionalFieldOf("regen_percent_per_second", KD.regenPercentPerSecond()).forGetter(KiParams::regenPercentPerSecond),
            Codec.DOUBLE.optionalFieldOf("regen_below_percent", KD.regenBelowPercent()).forGetter(KiParams::regenBelowPercent)
    ).apply(i, KiParams::new));

    static final Codec<ReleaseParams> RELEASE_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("initial_limit", RD.initialLimit()).forGetter(ReleaseParams::initialLimit),
            Codec.DOUBLE.optionalFieldOf("reference_percent", RD.referencePercent()).forGetter(ReleaseParams::referencePercent),
            Codec.DOUBLE.optionalFieldOf("max_damage_taken_factor", RD.maxDamageTakenFactor()).forGetter(ReleaseParams::maxDamageTakenFactor),
            Codec.DOUBLE.optionalFieldOf("min_damage_dealt_factor", RD.minDamageDealtFactor()).forGetter(ReleaseParams::minDamageDealtFactor),
            Codec.DOUBLE.optionalFieldOf("ramp_up_per_second", RD.rampUpPerSecond()).forGetter(ReleaseParams::rampUpPerSecond),
            Codec.DOUBLE.optionalFieldOf("ramp_down_per_second", RD.rampDownPerSecond()).forGetter(ReleaseParams::rampDownPerSecond),
            Codec.DOUBLE.optionalFieldOf("vel_base", RD.velBase()).forGetter(ReleaseParams::velBase),
            Codec.DOUBLE.optionalFieldOf("vel_growth", RD.velGrowth()).forGetter(ReleaseParams::velGrowth),
            Codec.DOUBLE.optionalFieldOf("speed_exponent", RD.speedExponent()).forGetter(ReleaseParams::speedExponent),
            Codec.DOUBLE.optionalFieldOf("speed_max", RD.speedMax()).forGetter(ReleaseParams::speedMax),
            Codec.DOUBLE.optionalFieldOf("jump_exponent", RD.jumpExponent()).forGetter(ReleaseParams::jumpExponent),
            Codec.DOUBLE.optionalFieldOf("jump_max", RD.jumpMax()).forGetter(ReleaseParams::jumpMax)
    ).apply(i, ReleaseParams::new));

    /** Cada clave es opcional y vale su valor por defecto; un valor inválido hace fallar todo el archivo. */
    public static final Codec<BalanceParams> CODEC = RecordCodecBuilder.<BalanceParams>create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("vit_base", D.vitBase()).forGetter(BalanceParams::vitBase),
            Codec.DOUBLE.optionalFieldOf("vit_growth", D.vitGrowth()).forGetter(BalanceParams::vitGrowth),
            Codec.DOUBLE.optionalFieldOf("hp_per_vit", D.hpPerVit()).forGetter(BalanceParams::hpPerVit),
            Codec.DOUBLE.optionalFieldOf("damage_exponent", D.damageExponent()).forGetter(BalanceParams::damageExponent),
            Codec.DOUBLE.optionalFieldOf("continuous_floor", D.continuousFloor()).forGetter(BalanceParams::continuousFloor),
            Codec.DOUBLE.optionalFieldOf("fue_base", D.fueBase()).forGetter(BalanceParams::fueBase),
            Codec.DOUBLE.optionalFieldOf("fue_growth", D.fueGrowth()).forGetter(BalanceParams::fueGrowth),
            Codec.DOUBLE.optionalFieldOf("strength_exponent", D.strengthExponent()).forGetter(BalanceParams::strengthExponent),
            Codec.INT.optionalFieldOf("attribute_cap", D.attributeCap()).forGetter(BalanceParams::attributeCap),
            KI_CODEC.optionalFieldOf("ki", KD).forGetter(BalanceParams::ki),
            RELEASE_CODEC.optionalFieldOf("release", RD).forGetter(BalanceParams::release)
    ).apply(i, BalanceParams::new)).validate(p -> {
        String problem = p.problem();
        return problem == null ? DataResult.success(p) : DataResult.error(() -> problem);
    });

    private static volatile BalanceParams current = BalanceParams.DEFAULT;

    /** Parámetros vigentes (los de siempre hasta que se cargue un archivo de datos válido). */
    public static BalanceParams get() {
        return current;
    }

    /** El cliente adopta los parámetros que le manda el servidor (los necesita para mostrar máximos correctos). */
    public static void setFromServer(BalanceParams params) {
        current = params;
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

        /** Al entrar y tras cada /reload: el cliente recibe el balance vigente (si su conexión conoce el paquete). */
        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            event.getRelevantPlayers().forEach(player -> {
                if (player.connection.hasChannel(BalanceSyncPayload.TYPE)) {
                    PacketDistributor.sendToPlayer(player, new BalanceSyncPayload(get()));
                }
            });
        }

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new Reloader());
        }
    }
}

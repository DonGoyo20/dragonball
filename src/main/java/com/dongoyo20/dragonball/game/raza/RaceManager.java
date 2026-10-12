package com.dongoyo20.dragonball.game.raza;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.core.raza.Rasgo;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.network.RaceSyncPayload;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
 * Las razas como DATOS: {@code data/<espacio>/races/<id>.json}. Se recargan con {@code /reload} (junto con el balance) y se
 * pueden añadir o cambiar con un datapack. Un archivo mal formado o inválido se rechaza <b>entero</b> con un mensaje en el log
 * y las demás razas siguen cargando. La raza por defecto, {@code terricola}, siempre existe: si su archivo falta o es
 * inválido se usa la copia integrada (idéntica a la de los datos).
 *
 * <p>Los textos del JSON son claves de traducción. El servidor manda las razas a los clientes (el HUD y la pantalla de
 * estadísticas necesitan los valores iniciales).
 */
public final class RaceManager {
    private RaceManager() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_ID = "terricola";

    // ---------- Codec

    static final Codec<Atributo> ATRIBUTO_CODEC = Codec.STRING.comapFlatMap(
            s -> Atributo.fromId(s).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "atributo desconocido: '" + s + "'")),
            Atributo::id);

    static final Codec<Rasgo> RASGO_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(Rasgo::id),
            Codec.STRING.fieldOf("display_name").forGetter(Rasgo::displayName),
            Codec.STRING.fieldOf("description").forGetter(Rasgo::description)
    ).apply(i, Rasgo::new));

    /** Un valor inválido (id, falta algún atributo, negativo…) hace fallar todo el archivo. */
    public static final Codec<Race> CODEC = RecordCodecBuilder.<Race>create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(Race::id),
            Codec.STRING.fieldOf("display_name").forGetter(Race::displayName),
            Codec.STRING.fieldOf("description").forGetter(Race::description),
            Codec.unboundedMap(ATRIBUTO_CODEC, Codec.INT).fieldOf("base_stats").forGetter(Race::baseStats),
            RASGO_CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(Race::traits)
    ).apply(i, Race::new)).validate(r -> {
        String problem = r.structuralProblem();
        return problem == null ? DataResult.success(r) : DataResult.error(() -> problem);
    });

    public static final Codec<List<Race>> LIST_CODEC = CODEC.listOf();

    // ---------- razas integradas (copia de seguridad idéntica a los archivos de datos)

    public static final Race TERRICOLA_FALLBACK = new Race(DEFAULT_ID, "race.dragonball.terricola", "race.dragonball.terricola.description",
            stats(8, 8, 8, 8, 0, 8, 0),
            List.of(new Rasgo("adaptable", "trait.dragonball.adaptable", "trait.dragonball.adaptable.description"),
                    new Rasgo("sin_cola", "trait.dragonball.sin_cola", "trait.dragonball.sin_cola.description")));

    private static Map<Atributo, Integer> stats(int vit, int fue, int def, int vel, int ki, int pod, int con) {
        Map<Atributo, Integer> m = new EnumMap<>(Atributo.class);
        m.put(Atributo.VIT, vit);
        m.put(Atributo.FUE, fue);
        m.put(Atributo.DEF, def);
        m.put(Atributo.VEL, vel);
        m.put(Atributo.KI, ki);
        m.put(Atributo.POD, pod);
        m.put(Atributo.CON, con);
        return m;
    }

    private static volatile Map<String, Race> current = Map.of(DEFAULT_ID, TERRICOLA_FALLBACK);

    // ---------- consulta

    /** La raza con ese id, o {@code null} si no existe. */
    public static Race get(String id) {
        return id == null ? null : current.get(id);
    }

    /** La raza con ese id; si no existe, la raza por defecto. */
    public static Race getOrDefault(String id) {
        Race r = get(id);
        return r != null ? r : getDefault();
    }

    /** Todas las razas, ordenadas por id. */
    public static Collection<Race> getAll() {
        return current.values();
    }

    public static Collection<String> ids() {
        return current.keySet();
    }

    /** {@code terricola}; siempre existe. */
    public static Race getDefault() {
        Race r = current.get(DEFAULT_ID);
        return r != null ? r : TERRICOLA_FALLBACK;
    }

    /** El cliente adopta las razas que le manda el servidor. */
    public static void setFromServer(List<Race> races) {
        current = build(races);
    }

    /** Solo para pruebas: fija las razas y devuelve las anteriores para restaurarlas. */
    public static Map<String, Race> setForTesting(Map<String, Race> races) {
        Map<String, Race> old = current;
        current = races;
        return old;
    }

    /** Solo para pruebas: las razas vigentes tal cual. */
    public static Map<String, Race> snapshot() {
        return current;
    }

    private static Map<String, Race> build(Collection<Race> races) {
        Map<String, Race> map = new TreeMap<>();
        for (Race r : races) map.put(r.id(), r);
        map.putIfAbsent(DEFAULT_ID, TERRICOLA_FALLBACK);
        return Map.copyOf(map);
    }

    // ---------- carga

    /**
     * Interpreta los archivos {@code races/*.json}. {@code files} va de id (el nombre del archivo) a JSON. Los inválidos se
     * descartan con un aviso a {@code onError}; si falta {@code terricola} se añade la copia integrada.
     */
    public static Map<String, Race> parse(Map<String, JsonElement> files, BalanceParams balance, Consumer<String> onError) {
        List<Race> valid = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : new TreeMap<>(files).entrySet()) {
            String fileId = entry.getKey();
            DataResult<Race> result = CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            // Solo un resultado completo vale: un resultado parcial (p. ej. con un atributo desconocido) rechaza todo el archivo.
            Race race = result.result().orElse(null);
            if (race == null) {
                onError.accept("raza '" + fileId + "': " + result.error().map(e -> e.message()).orElse("formato inválido"));
                continue;
            }
            if (!race.id().equals(fileId)) {
                onError.accept("raza '" + fileId + "': el campo id ('" + race.id() + "') debe coincidir con el nombre del archivo");
                continue;
            }
            String problem = race.problem(balance);
            if (problem != null) {
                onError.accept("raza '" + fileId + "': " + problem);
                continue;
            }
            valid.add(race);
        }
        if (valid.stream().noneMatch(r -> r.id().equals(DEFAULT_ID))) {
            onError.accept("falta la raza por defecto '" + DEFAULT_ID + "': se usa la copia integrada");
        }
        return build(valid);
    }

    /** Lee {@code data/<espacio>/races/*.json}. */
    static final class Reloader extends SimpleJsonResourceReloadListener {
        Reloader() {
            super(new Gson(), "races");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, JsonElement> byId = new TreeMap<>();
            files.forEach((location, json) -> byId.put(location.getPath(), json));
            current = parse(byId, BalanceManager.get(), error -> LOGGER.error("Raza inválida: {}", error));
            LOGGER.info("Razas cargadas: {}", current.keySet());
        }
    }

    @EventBusSubscriber(modid = DragonBall.MOD_ID)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new Reloader());
        }

        /** Al entrar y tras cada /reload: el cliente recibe las razas vigentes (si su conexión conoce el paquete). */
        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            event.getRelevantPlayers().forEach(player -> {
                if (player.connection.hasChannel(RaceSyncPayload.TYPE)) {
                    PacketDistributor.sendToPlayer(player, new RaceSyncPayload(List.copyOf(getAll())));
                }
            });
        }
    }
}

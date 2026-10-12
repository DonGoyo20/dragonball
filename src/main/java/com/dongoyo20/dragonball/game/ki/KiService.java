package com.dongoyo20.dragonball.game.ki;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.progress.PlayerProgress;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * Puente entre el jugador de Minecraft y {@link KiSystem}. El servidor es la autoridad: el cliente solo envía qué tecla
 * mantiene (subir/bajar liberación) y el servidor decide si el jugador está quieto, cuánto sube y cuánto ki se gasta.
 *
 * <p>Para no enviar un paquete de sincronización por tick, el ki se actualiza cada {@link #STEP_TICKS} ticks aplicando de
 * golpe los efectos de esos ticks, y solo se escribe en el progreso (y por tanto se sincroniza) si algo cambió.
 */
public final class KiService {
    private KiService() {}

    /** Cada cuántos ticks se actualiza el ki. */
    public static final int STEP_TICKS = 5;
    private static final double STILL_DISTANCE = 0.05;
    private static final double EPSILON = 1e-6;

    public static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "vel_speed");
    private static final ResourceLocation JUMP_ID = ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "vel_jump");

    /** Tecla que mantiene cada jugador: 1 sube, -1 baja, 0 nada. Solo en memoria del servidor. */
    private static final Map<UUID, Integer> INPUT = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> LAST_POS = new ConcurrentHashMap<>();
    /** Ticks acumulados desde el último paso (contador propio: no depende de {@code tickCount}). */
    private static final Map<UUID, Integer> TICKS = new ConcurrentHashMap<>();

    // ---------- avisos en la barra de acción

    /** Mínimo de ticks entre dos avisos de "debes estar quieto" (2 segundos). */
    public static final int STILL_WARNING_COOLDOWN_TICKS = 40;
    private static final Map<UUID, Long> LAST_STILL_WARNING = new ConcurrentHashMap<>();

    /** Dónde van los avisos (la barra de acción del jugador); las pruebas lo sustituyen para recogerlos. */
    private static BiConsumer<ServerPlayer, Component> messageSink = (player, text) -> player.displayClientMessage(text, true);

    /** Solo para pruebas: cambia el destino de los avisos y devuelve el anterior. */
    public static BiConsumer<ServerPlayer, Component> setMessageSinkForTesting(BiConsumer<ServerPlayer, Component> sink) {
        BiConsumer<ServerPlayer, Component> old = messageSink;
        messageSink = sink;
        return old;
    }

    /**
     * Avisa "Debes estar quieto para liberar Ki" como máximo una vez cada {@link #STILL_WARNING_COOLDOWN_TICKS} ticks.
     * Devuelve si el aviso se envió.
     */
    public static boolean warnMustStandStill(ServerPlayer player, long gameTime) {
        Long last = LAST_STILL_WARNING.get(player.getUUID());
        if (last != null && gameTime - last < STILL_WARNING_COOLDOWN_TICKS && gameTime >= last) return false;
        LAST_STILL_WARNING.put(player.getUUID(), gameTime);
        messageSink.accept(player, Component.translatable("message.dragonball.must_stand_still"));
        return true;
    }

    // ---------- entrada del jugador

    /** Se llama desde el paquete del cliente; cualquier valor fuera de [-1, 1] se recorta (nunca se confía en el cliente). */
    public static void setInput(UUID player, int direction) {
        INPUT.put(player, Integer.compare(direction, 0));
    }

    public static void forget(UUID player) {
        INPUT.remove(player);
        LAST_POS.remove(player);
        TICKS.remove(player);
        LAST_STILL_WARNING.remove(player);
    }

    /** Cuenta un tick del jugador; devuelve true cada {@link #STEP_TICKS} ticks (entonces toca llamar a {@link #tick}). */
    public static boolean countTick(UUID player) {
        int next = TICKS.merge(player, 1, Integer::sum);
        if (next < STEP_TICKS) return false;
        TICKS.put(player, 0);
        return true;
    }

    // ---------- valores de atributos del jugador (siempre bajo el tope, aunque el guardado lo supere)

    private static int upgrades(ServerPlayer player, String attribute) {
        return ProgressService.get(player).upgrades().get(ProgressService.attributeIndex(attribute));
    }

    public static int kiValue(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        return KiSystem.kiValue(Math.min(upgrades(player, "ki"), p.kiMaxUpgrades()), p);
    }

    public static int conValue(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        return KiSystem.conValue(Math.min(upgrades(player, "con"), p.conMaxUpgrades()), p);
    }

    public static int velValue(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        return KiSystem.velValue(Math.min(upgrades(player, "vel"), p.velMaxUpgrades()), p);
    }

    public static int kiMax(ServerPlayer player) {
        return KiSystem.reservaMaxima(kiValue(player), BalanceManager.get());
    }

    /** Liberación actual del jugador, en %. */
    public static double liberacion(ServerPlayer player) {
        return ProgressService.get(player).ki().liberacion();
    }

    // ---------- simulación

    /** Llamado cada {@link #STEP_TICKS} ticks desde el evento de tick del jugador. */
    public static void tick(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return;
        UUID id = player.getUUID();
        Vec3 now = player.position();
        Vec3 before = LAST_POS.put(id, now);
        boolean moved = before != null && before.distanceToSqr(now) > STILL_DISTANCE * STILL_DISTANCE;
        boolean still = !moved && !player.swinging;
        advance(player, STEP_TICKS, still, INPUT.getOrDefault(id, 0));
    }

    /**
     * Avanza {@code ticks} ticks: primero sube o baja la liberación (subir solo si está quieto), luego gasta y regenera ki,
     * y por último aplica VEL al movimiento. Público para poder probarlo sin esperar ticks reales.
     */
    public static void advance(ServerPlayer player, int ticks, boolean still, int direction) {
        BalanceParams p = BalanceManager.get();
        KiData ki = ProgressService.get(player).ki();

        // Intenta subir pero se está moviendo (y aún hay margen hasta el límite): avisa, como mucho cada 2 s
        if (direction > 0 && !still && ki.liberacion() < ki.limite()) {
            warnMustStandStill(player, player.level().getGameTime());
        }

        double release = KiSystem.siguientePorcentaje(ki.liberacion(), ki.limite(), direction, still, ticks, p);
        KiSystem.Estado state = KiSystem.avanzar(new KiSystem.Estado(ki.kiActual(), release), kiValue(player), conValue(player), ticks, p);
        KiData next = KiData.sane(state.ki(), state.liberacion(), ki.limite());

        // La liberación llegó a 0 porque se acabó el ki (no porque el jugador la bajara con X): aviso informativo, sin castigo
        if (ki.liberacion() > 0.0 && next.liberacion() <= 0.0 && next.kiActual() <= 0.0) {
            messageSink.accept(player, Component.translatable("message.dragonball.no_ki"));
        }

        if (Math.abs(next.kiActual() - ki.kiActual()) > EPSILON || Math.abs(next.liberacion() - ki.liberacion()) > EPSILON) {
            ProgressService.setKi(player, next);
        }
        applyMovement(player, next, p);
    }

    /**
     * VEL efectiva → velocidad de movimiento y salto, con modificadores transitorios (no se guardan en el jugador).
     * <b>Solo mientras el jugador corre</b> (doble W / Ctrl): caminando, parado o en el aire sin correr el jugador se mueve
     * y salta como en vanilla, sea cual sea su VEL.
     */
    private static void applyMovement(ServerPlayer player, KiData ki, BalanceParams p) {
        int vel = velValue(player);
        boolean sprinting = player.isSprinting();
        setModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, sprinting ? KiSystem.factorVelocidad(vel, ki.liberacion(), p) - 1.0 : 0.0);
        setModifier(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP_ID, sprinting ? KiSystem.factorSalto(vel, ki.liberacion(), p) - 1.0 : 0.0);
    }

    /** Reaplica solo el movimiento (barato): se llama cada tick para que correr/dejar de correr se note al instante. */
    public static void updateMovement(ServerPlayer player) {
        applyMovement(player, ProgressService.get(player).ki(), BalanceManager.get());
    }

    private static void setModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null) return;
        AttributeModifier current = attribute.getModifier(id);
        if (Math.abs(amount) < 1e-4) {
            if (current != null) attribute.removeModifier(id);
            return;
        }
        if (current == null || Math.abs(current.amount() - amount) > 1e-6) {
            attribute.removeModifier(id);
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Lee el modificador de VEL aplicado al movimiento (para pruebas): 0 si no hay. */
    public static double speedModifierAmount(ServerPlayer player) {
        AttributeModifier m = player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(SPEED_ID);
        return m == null ? 0.0 : m.amount();
    }

    public static double jumpModifierAmount(ServerPlayer player) {
        AttributeModifier m = player.getAttribute(Attributes.JUMP_STRENGTH).getModifier(JUMP_ID);
        return m == null ? 0.0 : m.amount();
    }

    /** Fija el límite de liberación (comando de pruebas hasta que exista la habilidad "Desbloqueo de Potencial"). */
    public static void setLimit(ServerPlayer player, int limit) {
        PlayerProgress progress = ProgressService.get(player);
        ProgressService.setKi(player, progress.ki().withLimite(limit));
    }
}

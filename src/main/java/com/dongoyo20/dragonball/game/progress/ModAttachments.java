package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.DragonBall;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DragonBall.MOD_ID);

    /** Progreso del jugador: se guarda con el mundo y se conserva al morir (en el Otro Mundo no se pierde nada). */
    public static final Supplier<AttachmentType<PlayerProgress>> PROGRESS = ATTACHMENTS.register("progress",
            () -> AttachmentType.builder(() -> PlayerProgress.EMPTY)
                    .serialize(PlayerProgress.CODEC)
                    // Solo al propio jugador (el HUD y la pantalla de estadísticas son suyos): evita enviar cada cambio de ki a los vecinos.
                    // Y solo si su conexión conoce el paquete de sincronización: los jugadores simulados de las pruebas automáticas
                    // no han negociado canales y NeoForge lanzaría una excepción al intentar enviarles datos.
                    .sync((holder, to) -> holder == to && to.connection.hasChannel(SyncAttachmentsPayload.TYPE), PlayerProgress.STREAM_CODEC)
                    .copyOnDeath()
                    .build());
}

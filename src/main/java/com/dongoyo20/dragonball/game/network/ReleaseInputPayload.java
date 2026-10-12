package com.dongoyo20.dragonball.game.network;

import com.dongoyo20.dragonball.DragonBall;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente → servidor: qué tecla de liberación mantiene el jugador (1 sube, -1 baja, 0 ninguna). Solo se envía al cambiar. */
public record ReleaseInputPayload(byte direction) implements CustomPacketPayload {
    public static final Type<ReleaseInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "release_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseInputPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, ReleaseInputPayload::direction, ReleaseInputPayload::new);

    @Override
    public Type<ReleaseInputPayload> type() {
        return TYPE;
    }
}

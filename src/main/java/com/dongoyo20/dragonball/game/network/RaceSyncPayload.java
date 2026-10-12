package com.dongoyo20.dragonball.game.network;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.raza.Race;
import com.dongoyo20.dragonball.game.raza.RaceManager;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor → cliente: las razas vigentes (el HUD y la pantalla de estadísticas necesitan sus valores iniciales y rasgos). */
public record RaceSyncPayload(List<Race> races) implements CustomPacketPayload {
    public static final Type<RaceSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "race_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RaceSyncPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodec(RaceManager.LIST_CODEC).map(RaceSyncPayload::new, RaceSyncPayload::races).cast();

    @Override
    public Type<RaceSyncPayload> type() {
        return TYPE;
    }
}

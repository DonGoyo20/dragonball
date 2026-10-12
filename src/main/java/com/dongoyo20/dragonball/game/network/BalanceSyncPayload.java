package com.dongoyo20.dragonball.game.network;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Servidor → cliente: el balance vigente. El combate se calcula en el servidor, pero el HUD y la pantalla de estadísticas
 * necesitan los mismos números para mostrar máximos correctos aunque un datapack los haya cambiado.
 */
public record BalanceSyncPayload(BalanceParams params) implements CustomPacketPayload {
    public static final Type<BalanceSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DragonBall.MOD_ID, "balance_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BalanceSyncPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodec(BalanceManager.CODEC).map(BalanceSyncPayload::new, BalanceSyncPayload::params).cast();

    @Override
    public Type<BalanceSyncPayload> type() {
        return TYPE;
    }
}

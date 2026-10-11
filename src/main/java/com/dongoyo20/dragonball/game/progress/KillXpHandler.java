package com.dongoyo20.dragonball.game.progress;

import com.dongoyo20.dragonball.DragonBall;
import com.dongoyo20.dragonball.game.bestiary.BestiaryEntry;
import com.dongoyo20.dragonball.game.bestiary.ModDataMaps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Concede XP cuando un jugador derrota a una entidad que figura en el bestiario. */
@EventBusSubscriber(modid = DragonBall.MOD_ID)
public final class KillXpHandler {
    private KillXpHandler() {}

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        BestiaryEntry entry = dead.getType().builtInRegistryHolder().getData(ModDataMaps.BESTIARY);
        if (entry == null) return;

        boolean fromSpawner = dead instanceof Mob mob && mob.getSpawnType() == MobSpawnType.SPAWNER;
        ProgressService.awardKill(player, entry, fromSpawner);
    }
}

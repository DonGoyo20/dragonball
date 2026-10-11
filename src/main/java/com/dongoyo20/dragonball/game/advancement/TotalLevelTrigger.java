package com.dongoyo20.dragonball.game.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

/**
 * Criterio de logro "alcanzar el nivel total N". Es el adaptador mínimo para que las misiones
 * (FTB Quests, mediante su tarea de logro) puedan depender de nuestro progreso sin enlazar código de FTB.
 */
public class TotalLevelTrigger extends SimpleCriterionTrigger<TotalLevelTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int totalLevel) {
        trigger(player, instance -> instance.matches(totalLevel));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, int minLevel)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.intRange(1, 1000).fieldOf("min_level").forGetter(TriggerInstance::minLevel)
        ).apply(i, TriggerInstance::new));

        public boolean matches(int totalLevel) {
            return totalLevel >= minLevel;
        }
    }
}

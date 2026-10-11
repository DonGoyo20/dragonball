package com.dongoyo20.dragonball.game.advancement;

import com.dongoyo20.dragonball.DragonBall;
import java.util.function.Supplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTriggers {
    private ModTriggers() {}

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, DragonBall.MOD_ID);

    public static final Supplier<TotalLevelTrigger> TOTAL_LEVEL = TRIGGERS.register("total_level", TotalLevelTrigger::new);
}

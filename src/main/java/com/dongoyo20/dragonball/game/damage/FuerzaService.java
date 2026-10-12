package com.dongoyo20.dragonball.game.damage;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.dongoyo20.dragonball.core.raza.Atributo;
import com.dongoyo20.dragonball.core.raza.Atributos;
import com.dongoyo20.dragonball.game.raza.RaceService;
import com.dongoyo20.dragonball.core.KiSystem;
import com.dongoyo20.dragonball.core.DanioCuerpo;
import com.dongoyo20.dragonball.core.FuerzaSystem;
import com.dongoyo20.dragonball.game.balance.BalanceManager;
import com.dongoyo20.dragonball.game.item.ModTags;
import com.dongoyo20.dragonball.game.ki.KiService;
import com.dongoyo20.dragonball.game.progress.ProgressService;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Puente entre el jugador de Minecraft y las fórmulas de {@link FuerzaSystem}. No guarda estado propio. */
public final class FuerzaService {
    private FuerzaService() {}

    /** Valor actual de FUE del jugador (base + mejoras). */
    public static int fue(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        int upgrades = ProgressService.get(player).upgrades().get(ProgressService.attributeIndex("fue"));
        // Un guardado con más mejoras que el tope no debe romper el combate: se trata como el máximo.
        return Atributos.valorRecortado(Atributo.FUE, RaceService.base(player, Atributo.FUE), upgrades, p);
    }

    /** FUE efectiva según la liberación (vanilla 8 a 0 %, la del mod a 50 %, el doble a 100 %). */
    public static double fueEfectiva(ServerPlayer player) {
        BalanceParams p = BalanceManager.get();
        return KiSystem.statEfectiva(fue(player), p.fueBase(), KiService.liberacion(player), p);
    }

    /** ¿Es un arma del mod? Hoy el tag está vacío (el mod no tiene armas). */
    public static boolean esArmaDelMod(ItemStack stack) {
        return stack.is(ModTags.ARMAS_DEL_MOD);
    }

    /** ¿El ítem añade daño de ataque (espadas, hachas, picos…)? Se lee de sus modificadores de atributos. */
    public static boolean tieneDanioDeAtaque(ItemStack stack) {
        return stack.getAttributeModifiers().modifiers().stream()
                .anyMatch(e -> e.attribute().is(Attributes.ATTACK_DAMAGE.unwrapKey().orElseThrow()));
    }

    public static DanioCuerpo clasificar(ItemStack stack) {
        return clasificar(stack, FuerzaService::esArmaDelMod);
    }

    /** Variante con el criterio de "arma del mod" inyectable (para probar sin necesidad de armas reales). */
    public static DanioCuerpo clasificar(ItemStack stack, Predicate<ItemStack> esDelMod) {
        return DanioCuerpo.clasificar(stack.isEmpty(), esDelMod.test(stack), tieneDanioDeAtaque(stack));
    }

    /** Daño final del golpe: escalado con FUE si el arma lo permite, o el de vanilla si no. */
    public static float danioFinal(ServerPlayer player, ItemStack arma, float danioVanilla, Predicate<ItemStack> esDelMod) {
        if (!clasificar(arma, esDelMod).escalaConFuerza()) return danioVanilla;
        return (float) FuerzaSystem.danioSalienteLiberado(danioVanilla, fue(player), KiService.liberacion(player), BalanceManager.get());
    }

    public static float danioFinal(ServerPlayer player, ItemStack arma, float danioVanilla) {
        return danioFinal(player, arma, danioVanilla, FuerzaService::esArmaDelMod);
    }
}

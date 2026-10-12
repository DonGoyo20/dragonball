package com.dongoyo20.dragonball.core.raza;

import com.dongoyo20.dragonball.core.BalanceParams;
import java.util.Optional;

/** Los siete atributos, en el orden de SYSTEMS.md (el mismo de {@code ProgressService.ATTRIBUTES}). */
public enum Atributo {
    VIT("vit"), FUE("fue"), DEF("def"), VEL("vel"), KI("ki"), POD("pod"), CON("con");

    private final String id;

    Atributo(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public int index() {
        return ordinal();
    }

    public static Optional<Atributo> fromId(String id) {
        for (Atributo a : values()) if (a.id.equals(id)) return Optional.of(a);
        return Optional.empty();
    }

    /** Cuánto sube el atributo por mejora (igual para todas las razas). */
    public double growth(BalanceParams p) {
        return switch (this) {
            case VIT -> p.vitGrowth();
            case FUE -> p.fueGrowth();
            case DEF -> p.defense().defGrowth();
            case VEL -> p.release().velGrowth();
            case KI -> p.ki().kiGrowth();
            case POD -> p.pod().podGrowth();
            case CON -> p.ki().conGrowth();
        };
    }

    /**
     * Valor "de referencia" del atributo en el balance: el 8 vanilla de VIT, FUE, DEF y VEL (lo que usan las fórmulas de
     * daño y la función a trozos de la liberación) y el valor inicial por defecto de KI, POD y CON.
     */
    public double reference(BalanceParams p) {
        return switch (this) {
            case VIT -> p.vitBase();
            case FUE -> p.fueBase();
            case DEF -> p.defense().defBase();
            case VEL -> p.release().velBase();
            case KI -> p.ki().kiBase();
            case POD -> p.pod().podBase();
            case CON -> p.ki().conBase();
        };
    }
}

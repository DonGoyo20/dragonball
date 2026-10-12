package com.dongoyo20.dragonball.core.raza;

import com.dongoyo20.dragonball.core.BalanceParams;

/**
 * Valor de un atributo a partir del valor inicial de la raza y de las mejoras compradas:
 * {@code valor = base_raza + crecimiento × mejoras}, con tope en {@code attribute_cap} (99). Como el tope es del <i>valor</i>,
 * una raza que empieza más arriba tiene menos mejoras disponibles (FUE 10 → 89 mejoras; FUE 8 → 91).
 */
public final class Atributos {
    private Atributos() {}

    /** Mejoras que caben bajo el tope partiendo de {@code base}. */
    public static int maxMejoras(Atributo a, int base, BalanceParams p) {
        return Math.max(0, (int) Math.floor((p.attributeCap() - base) / a.growth(p) + 1e-9));
    }

    /** Valor tras {@code mejoras} mejoras; error si la cuenta es negativa o pasa del tope. */
    public static int valor(Atributo a, int base, int mejoras, BalanceParams p) {
        int max = maxMejoras(a, base, p);
        if (mejoras < 0 || mejoras > max) {
            throw new IllegalArgumentException("mejoras de " + a.id().toUpperCase() + " fuera de [0, " + max + "]: " + mejoras);
        }
        return (int) Math.round(base + a.growth(p) * mejoras);
    }

    /** Igual que {@link #valor}, pero recorta las mejoras al máximo (un guardado con más mejoras que el tope no debe romper el combate). */
    public static int valorRecortado(Atributo a, int base, int mejoras, BalanceParams p) {
        return valor(a, base, Math.max(0, Math.min(mejoras, maxMejoras(a, base, p))), p);
    }

    /** ¿Se puede comprar otra mejora sin pasar del tope? */
    public static boolean puedeMejorar(Atributo a, int base, int mejoras, BalanceParams p) {
        return mejoras >= 0 && mejoras < maxMejoras(a, base, p);
    }

    /** Valor inicial por defecto de un atributo (el de la raza Terrícola con el balance por defecto). */
    public static int baseDefecto(Atributo a, BalanceParams p) {
        return (int) Math.round(a.reference(p));
    }
}

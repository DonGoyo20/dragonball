package com.dongoyo20.dragonball.core;

/** Cómo trata el sistema de vida cada tipo de daño (decisión del usuario, 2026-10-12; SYSTEMS.md §3.1). */
public enum TipoDanio {
    /** Daño puntual (criaturas, caída, explosiones…): {@code daño × (8/VIT)^3}. */
    ESCALA_SIN_SUELO,
    /** Daño de estado continuo (lava, fuego, ahogamiento, veneno…): la cúbica, con un suelo de 1 HP por aplicación. */
    ESCALA_CON_SUELO,
    /** Vacío y /kill: se aplica tal cual. */
    NO_ESCALA
}

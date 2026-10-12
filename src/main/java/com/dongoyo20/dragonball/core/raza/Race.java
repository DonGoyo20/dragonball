package com.dongoyo20.dragonball.core.raza;

import com.dongoyo20.dragonball.core.BalanceParams;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Una raza, como datos puros (Fase 3a). Define los valores iniciales de los siete atributos; las mejoras con XP son iguales
 * para todas las razas (mismo coste, mismo tope de 99 en el valor del atributo).
 *
 * <p>Los textos ({@code displayName}, {@code description} y los de los rasgos) son <b>claves de traducción</b>, no texto: así
 * se traducen a los 8 idiomas. El Codec y la carga están en {@code game/raza/RaceManager} (el {@code core/} no depende de
 * Minecraft ni de sus bibliotecas de datos).
 *
 * @param id        identificador (por ejemplo {@code saiyajin}); coincide con el nombre del archivo JSON
 * @param baseStats valor inicial de cada uno de los siete atributos
 * @param traits    rasgos (solo informativos en este ciclo)
 */
public record Race(String id, String displayName, String description, Map<Atributo, Integer> baseStats, List<Rasgo> traits) {

    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_]*");

    /** Copias inmutables: una raza no cambia después de crearla. */
    public Race {
        Map<Atributo, Integer> copy = new EnumMap<>(Atributo.class);
        if (baseStats != null) copy.putAll(baseStats);
        baseStats = Collections.unmodifiableMap(copy);
        traits = traits == null ? List.of() : List.copyOf(traits);
    }

    public static boolean isValidId(String id) {
        return id != null && ID.matcher(id).matches();
    }

    /** Valor inicial del atributo (0 si la raza no lo define: solo ocurre en razas inválidas). */
    public int base(Atributo attribute) {
        return baseStats.getOrDefault(attribute, 0);
    }

    /**
     * Problemas que no dependen del balance: id, textos, los siete atributos presentes y no negativos, rasgos válidos y sin
     * repetir. {@code null} si no hay ninguno.
     */
    public String structuralProblem() {
        if (!isValidId(id)) return "id de raza inválido: '" + id + "' (solo minúsculas, números y _, empezando por letra)";
        if (displayName == null || displayName.isBlank()) return "la raza '" + id + "' no tiene nombre";
        if (description == null || description.isBlank()) return "la raza '" + id + "' no tiene descripción";
        for (Atributo a : Atributo.values()) {
            Integer v = baseStats.get(a);
            if (v == null) return "la raza '" + id + "' no define el atributo " + a.id();
            if (v < 0) return "la raza '" + id + "' tiene " + a.id() + " negativo: " + v;
        }
        Set<String> seen = new HashSet<>();
        for (Rasgo t : traits) {
            if (t == null) return "la raza '" + id + "' tiene un rasgo vacío";
            String problem = t.problem();
            if (problem != null) return "raza '" + id + "': " + problem;
            if (!seen.add(t.id())) return "la raza '" + id + "' repite el rasgo '" + t.id() + "'";
        }
        return null;
    }

    /**
     * Problemas contra el balance: VIT, FUE, DEF y VEL no pueden empezar por debajo del valor vanilla (las fórmulas de daño y
     * la liberación parten de él) y todo atributo debe poder comprarse al menos una vez sin pasar del tope.
     */
    public String problem(BalanceParams p) {
        String structural = structuralProblem();
        if (structural != null) return structural;
        for (Atributo a : Atributo.values()) {
            int v = base(a);
            boolean vanillaFloor = a == Atributo.VIT || a == Atributo.FUE || a == Atributo.DEF || a == Atributo.VEL;
            if (vanillaFloor && v < a.reference(p)) {
                return "la raza '" + id + "' tiene " + a.id() + " " + v + " por debajo del valor vanilla " + (int) a.reference(p);
            }
            if (v + a.growth(p) > p.attributeCap()) {
                return "la raza '" + id + "' tiene " + a.id() + " " + v + ": no deja margen para mejoras bajo el tope " + p.attributeCap();
            }
        }
        return null;
    }
}

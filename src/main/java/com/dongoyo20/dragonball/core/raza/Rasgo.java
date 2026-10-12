package com.dongoyo20.dragonball.core.raza;

/**
 * Rasgo de una raza. De momento es <b>solo informativo</b>: su efecto se implementará en ciclos posteriores.
 *
 * @param id          identificador estable (minúsculas, números y _)
 * @param displayName clave de traducción del nombre
 * @param description clave de traducción de la descripción
 */
public record Rasgo(String id, String displayName, String description) {

    /** {@code null} si es válido; si no, una frase con el primer problema. */
    public String problem() {
        if (!Race.isValidId(id)) return "id de rasgo inválido: '" + id + "'";
        if (displayName == null || displayName.isBlank()) return "el rasgo '" + id + "' no tiene nombre";
        if (description == null || description.isBlank()) return "el rasgo '" + id + "' no tiene descripción";
        return null;
    }
}

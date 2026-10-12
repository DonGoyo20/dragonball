package com.dongoyo20.dragonball.game.ki;

import com.dongoyo20.dragonball.core.BalanceParams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Estado del recurso Ki de un jugador (se guarda dentro de {@code PlayerProgress}).
 *
 * @param kiActual    ki disponible
 * @param liberacion  porcentaje de liberación actual (0–100)
 * @param limite      límite de liberación (la habilidad "Desbloqueo de Potencial" de Kami-sama lo subirá +5 % por nivel)
 */
public record KiData(double kiActual, double liberacion, int limite) {

    /** Un personaje nuevo: reserva llena (con KI 0 son 50), liberación = límite inicial. */
    public static final KiData INITIAL = new KiData(
            BalanceParams.DEFAULT.ki().kiFlat() + BalanceParams.DEFAULT.ki().kiPerPoint() * BalanceParams.DEFAULT.ki().kiBase(),
            BalanceParams.DEFAULT.release().initialLimit(), (int) BalanceParams.DEFAULT.release().initialLimit());

    /** Campos opcionales con valores por defecto, y siempre dentro de rango aunque el guardado esté dañado. */
    public static final Codec<KiData> CODEC = RecordCodecBuilder.<KiData>create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("ki", INITIAL.kiActual()).forGetter(KiData::kiActual),
            Codec.DOUBLE.optionalFieldOf("release", INITIAL.liberacion()).forGetter(KiData::liberacion),
            Codec.INT.optionalFieldOf("limit", INITIAL.limite()).forGetter(KiData::limite)
    ).apply(i, KiData::sane));

    /** Recorta los valores a rangos válidos (NaN y negativos pasan a 0; la liberación no supera el límite). */
    public static KiData sane(double ki, double release, int limit) {
        int safeLimit = Math.max(0, Math.min(100, limit));
        double safeKi = Double.isNaN(ki) ? 0.0 : Math.max(0.0, ki);
        double safeRelease = Double.isNaN(release) ? 0.0 : Math.max(0.0, Math.min(Math.min(100.0, release), safeLimit));
        return new KiData(safeKi, safeRelease, safeLimit);
    }

    public KiData withKi(double newKi) {
        return sane(newKi, liberacion, limite);
    }

    public KiData withLiberacion(double newRelease) {
        return sane(kiActual, newRelease, limite);
    }

    public KiData withLimite(int newLimit) {
        return sane(kiActual, liberacion, newLimit);
    }
}

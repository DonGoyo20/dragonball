package com.dongoyo20.dragonball.game.bestiary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Datos de un enemigo en el bestiario (mapa de datos {@code dragonball:bestiary}).
 * La fuente de diseño es design_data/beta/bestiary.json; el mapa se genera con tools/simulator/export_datamap.py.
 */
public record BestiaryEntry(int level, String kind, String archetype, int xp, int groupSize) {

    public static final Codec<BestiaryEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, 1000).fieldOf("level").forGetter(BestiaryEntry::level),
            Codec.STRING.fieldOf("kind").forGetter(BestiaryEntry::kind),
            Codec.STRING.fieldOf("archetype").forGetter(BestiaryEntry::archetype),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("xp").forGetter(BestiaryEntry::xp),
            Codec.INT.optionalFieldOf("group_size", 1).forGetter(BestiaryEntry::groupSize)
    ).apply(i, BestiaryEntry::new));

    public boolean isBoss() {
        return kind.equals("boss") || kind.equals("story") || kind.equals("secret");
    }
}

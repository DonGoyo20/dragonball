package com.dongoyo20.dragonball.gametest;

import com.dongoyo20.dragonball.game.combate.EsquivaService;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/** Ayudas comunes de las pruebas de juego. */
final class GameTestSupport {
    private GameTestSupport() {}

    /** Tirada de esquiva que nunca sale (la probabilidad máxima es 0,60): las pruebas de daño no deben ser aleatorias. */
    static final double NEVER_DODGE_ROLL = 0.999;

    /** Jugador simulado; además fija la esquiva en "nunca" para que los golpes de las pruebas sean deterministas. */
    @SuppressWarnings("removal")
    static ServerPlayer mockPlayer(GameTestHelper helper) {
        EsquivaService.setRollForTesting(() -> NEVER_DODGE_ROLL);
        return helper.makeMockServerPlayerInLevel();
    }
}

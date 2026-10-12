# Dragon Ball RPG (mod para Minecraft 1.21.1 / NeoForge 21.1.259)

Estado: prototipo técnico (progresión por XP y mejoras, bestiario por datos, un enemigo con GeckoLib). Mira `PROJECT_STATUS.md` e `INTEGRATIONS.md`. El diseño está en los demás `.md`.

## Requisitos
Java 21. La primera compilación descarga dependencias (necesita red).

## Comandos
```
./gradlew build                 # compila y pasa las pruebas JUnit (5)
./gradlew runClient             # juego con el mod (+ Patchouli)
./gradlew runGameTestServer     # 6 pruebas en juego (núcleo)
tools/verify_integrations.sh    # núcleo + FTB Quests + KubeJS, con comprobación de logs
./gradlew runClient -PwithFtbQuests -PwithKubeJs   # con FTB Quests y KubeJS (copia solos los datos de misiones y scripts a run/)
```
En el juego: `/dragonball progress | upgrade <atributo> | givexp <n> | balance | ki | releaselimit <0-100>`; huevo de aparición del Lobo salvaje en creativo; la guía de Patchouli sale con la receta `guide_book`.

## Demo con pantalla virtual (sin monitor)
`Xvfb :99 -screen 0 1280x720x24 &` y `DISPLAY=:99 LIBGL_ALWAYS_SOFTWARE=1 ./gradlew runClientDemo` entran a un mundo de pruebas con un lobo delante.

## Teclas (se cambian en Opciones → Controles)
`C` (mantener, quieto) sube la liberación de poder · `X` (mantener) la baja · `H` muestra/oculta el HUD (vida y ki) · `V` abre/cierra la pantalla de estadísticas. Hasta que exista la habilidad que sube el límite de liberación (50 % al inicio), `/dragonball releaselimit 100` (operadores) permite probar el 100 %.

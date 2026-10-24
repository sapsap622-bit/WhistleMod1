# Whistles (NeoForge 1.21.1)

Mod de silbatos con habilidades especiales. Por ahora trae uno:

## The Frequency Whistle
- Se usa como el cuerno de cabra (misma animación, placeholder) y suena **5 segundos obligatorios**: soltar el click, cambiar de slot o abrir el inventario no lo cancela.
- Al empezar hay un efecto de cámara (temblor + golpe de FOV) para todos los jugadores cercanos.
- Cada segundo (1 s, 2 s, 3 s y 4 s) sale una **shockwave en el suelo** de 8 bloques de radio. Cada shockwave suma **3 s de Stasis** a todo ser vivo dentro del radio (acumulativo).
  Quien se quede dentro todo el rato termina el canal con ~8 s de Stasis restante.
- Cooldown de **42,5 s**, que empieza cuando termina el canal.
- Stasis: los mobs quedan congelados (sin IA, sin gravedad, sin atacar); los jugadores no pueden moverse, saltar, atacar, romper ni interactuar.
- El jugador que sopla no se congela a sí mismo.

## Ajustes
Todos los números están en `src/main/java/com/whistles/WhistleConfig.java`
(duración, intervalo de shockwaves, radio, Stasis por shockwave, cooldown, si afecta al que sopla).

## Compilar
Requiere Java 21.

    ./gradlew build        # el .jar queda en build/libs/
    ./gradlew runClient    # para probarlo en un cliente de desarrollo

Placeholders: la textura del ítem usa `minecraft:item/goat_horn` (ver `models/item/frequency_whistle.json`)
y el sonido es el primer cuerno de cabra. El icono del efecto Stasis es un PNG simple de 18x18.

package world.gen.habitats

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class Habitat(
    val mapGlyph: Glyph,
) {



}

@Serializable
object Blank : Habitat(
    Glyph.BLANK
)

@Serializable
object Arctic : Habitat(
    Glyph.MAP_HABITAT_ARCTIC
)

@Serializable
object AlpineA : Habitat(
    Glyph.MAP_HABITAT_COLD_A
)

@Serializable
object AlpineB : Habitat(
    Glyph.MAP_HABITAT_COLD_B
)

@Serializable
object TemperateA : Habitat(
    Glyph.MAP_HABITAT_TEMP_A
)

@Serializable
object TemperateB : Habitat(
    Glyph.MAP_HABITAT_TEMP_B
)

@Serializable
object TropicalA: Habitat(
    Glyph.MAP_HABITAT_HOT_A
)

@Serializable
object TropicalB : Habitat(
    Glyph.MAP_HABITAT_HOT_B
)

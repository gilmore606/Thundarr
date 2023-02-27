package world.gen.habitats

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.terrains.Terrain

@Serializable
sealed class Habitat(
    val mapGlyph: Glyph,
) {

    open fun forestWallType(): Terrain.Type = Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL

}

@Serializable
object Blank : Habitat(
    Glyph.BLANK
)

@Serializable
object Arctic : Habitat(
    Glyph.MAP_HABITAT_ARCTIC
) {
    override fun forestWallType() = Terrain.Type.TERRAIN_PINE_FORESTWALL
}

@Serializable
object AlpineA : Habitat(
    Glyph.MAP_HABITAT_COLD_A
) {
    override fun forestWallType() = Terrain.Type.TERRAIN_PINE_FORESTWALL
}

@Serializable
object AlpineB : Habitat(
    Glyph.MAP_HABITAT_COLD_B
) {
    override fun forestWallType() = Terrain.Type.TERRAIN_PINE_FORESTWALL
}

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
) {
    override fun forestWallType() = Terrain.Type.TERRAIN_TROPICAL_FORESTWALL
}

@Serializable
object TropicalB : Habitat(
    Glyph.MAP_HABITAT_HOT_B
) {
    override fun forestWallType() = Terrain.Type.TERRAIN_TROPICAL_FORESTWALL
}

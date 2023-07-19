package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Glacier : Biome(
    Glyph.MAP_GLACIER,
    Terrain.Type.TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "glacier"
    override fun maxWeatherRank() = 4
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun bareTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun trailSideTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun outcroppingChance() = 0.2f
    override fun xpValue() = 3
    override fun temperatureBase() = -15
}

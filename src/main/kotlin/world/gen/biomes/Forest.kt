package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Forest : Biome(
    Glyph.MAP_FOREST,
    Terrain.Type.TERRAIN_GRASS
) {
    private const val cabinChance = 0.2f

    override fun defaultTitle(habitat: Habitat) = habitat.forestName()
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun cabinChance() = 0.15f
    override fun plantDensity() = 1.4f
    override fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (fertilityAt(x, y) > 0.6f) Terrain.Type.TERRAIN_SWAMP else Terrain.Type.TERRAIN_UNDERGROWTH
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 1f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) * 1.5f
    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = NoisePatches.get("plantsBasic", x, y)
        val ef = NoisePatches.get("extraForest", x, y)
        if (fert > 0.7f || ef > 0.2f) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (ef > 0.01f) {
            return Terrain.Type.TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }

}

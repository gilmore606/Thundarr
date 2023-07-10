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
object ForestHill : Biome(
    Glyph.MAP_FORESTHILL,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.forestHillName()
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun cabinChance() = 0.1f
    override fun cavesChance() = 0.4f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.1f
    override fun plantDensity() = 0.7f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun villageWallType() = if (Dice.flip()) Terrain.Type.TERRAIN_BRICKWALL else Terrain.Type.TERRAIN_WOODWALL
    override fun villageFloorType() = if (Dice.chance(0.7f)) Terrain.Type.TERRAIN_WOODFLOOR else Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 2f
    override fun xpValue() = 4
    override fun temperatureAmplitude() = 0.7f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) -
            (NoisePatches.get("mountainShapes", x, y) * 0.7f + NoisePatches.get("extraForest", x, y) * 3f).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        if (NoisePatches.get("extraForest", x, y) > 0.2f) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        }
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.3f) return Terrain.Type.TERRAIN_DIRT
        else return Terrain.Type.TERRAIN_GRASS
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        carto.fringeTerrain(
            Terrain.Type.TERRAIN_CAVEWALL,
            Terrain.Type.TERRAIN_ROCKS, 0.6f,
            Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        )
        carto.fringeTerrain(
            Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL,
            Terrain.Type.TERRAIN_UNDERGROWTH, 0.6f,
            Terrain.Type.TERRAIN_CAVEWALL
        )
        carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_ROCKS, Terrain.Type.TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_UNDERGROWTH, Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL)
    }
}

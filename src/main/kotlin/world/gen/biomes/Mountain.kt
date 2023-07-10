package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Mountain : Biome(
    Glyph.MAP_MOUNTAIN,
    Terrain.Type.TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "mountains"
    override fun ambientSoundDay() = Speaker.Ambience.MOUNTAIN
    override fun ambientSoundNight() = Speaker.Ambience.MOUNTAIN
    override fun cabinChance() = 0.05f
    override fun cavesChance() = 0.8f
    override fun pondChance() = 0.01f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun wallsBlockTrails() = true
    override fun plantDensity() = 0.5f
    override fun villageWallType() = Terrain.Type.TERRAIN_BRICKWALL
    override fun villageFloorType() = if (Dice.chance(0.1f)) Terrain.Type.TERRAIN_DIRT else
        if (Dice.flip()) Terrain.Type.TERRAIN_STONEFLOOR else Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 3f
    override fun edgeDistanceThreatFactor() = 3f
    override fun xpValue() = 5
    override fun temperatureBase() = -8
    override fun temperatureAmplitude() = 1.2f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y).toFloat()
        if (v > 0.53f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (NoisePatches.get("ruinMicro",x,y) > NoisePatches.get("metaVariance", x / 10, y / 10) * 2.5f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.2f) return Terrain.Type.TERRAIN_DIRT
        else if (Dice.chance(1f - v * 2f)) return Terrain.Type.TERRAIN_DIRT
        else return Terrain.Type.TERRAIN_ROCKS
    }

    override fun riverBankTerrain(x: Int, y: Int) = if (Dice.flip()) Terrain.Type.TERRAIN_GRASS else Terrain.Type.TERRAIN_DIRT

}

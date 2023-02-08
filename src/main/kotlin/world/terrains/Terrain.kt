package world.terrains

import actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.Scoot
import render.sparks.Spark
import render.tilesets.Glyph
import util.LightColor
import util.XY
import world.Entity
import world.level.Level

sealed class Terrain(
    val type: Type,
    private val glyph: Glyph,
    private val walkable: Boolean,
    private val flyable: Boolean,
    private val opaque: Boolean,
    val canGrowPlants: Boolean,
    val dataType: Type = type  // should match TerrainData.forType
) : Entity {

    companion object {
        fun get(type: Type) = when (type) {
            Type.BLANK -> Blank
            Type.TEMP1 -> Blank
            Type.TEMP2 -> Blank
            Type.TEMP3 -> Blank
            Type.TERRAIN_BRICKWALL -> BrickWall
            Type.TERRAIN_CAVEWALL -> CaveWall
            Type.TERRAIN_WOODWALL -> WoodWall
            Type.TERRAIN_TEMPERATE_FORESTWALL -> TemperateForestWall
            Type.TERRAIN_PINE_FORESTWALL -> PineForestWall
            Type.TERRAIN_CAVEFLOOR -> CaveFloor
            Type.TERRAIN_STONEFLOOR -> StoneFloor
            Type.TERRAIN_WOODFLOOR -> WoodFloor
            Type.TERRAIN_DIRT -> Dirt
            Type.TERRAIN_ROCKS -> Rocks
            Type.TERRAIN_CAVE_ROCKS -> CaveRocks
            Type.TERRAIN_GRASS -> Grass
            Type.TERRAIN_UNDERGROWTH -> Undergrowth
            Type.TERRAIN_HARDPAN -> Hardpan
            Type.TERRAIN_SWAMP -> Swamp
            Type.TERRAIN_BEACH -> Beach
            Type.TERRAIN_SAND -> Sand
            Type.TERRAIN_SHALLOW_WATER -> ShallowWater
            Type.TERRAIN_DEEP_WATER -> DeepWater
            Type.GENERIC_WATER -> ScratchWater
            Type.TERRAIN_PORTAL_DOOR -> PortalDoor
            Type.TERRAIN_PORTAL_CAVE -> PortalCave
            Type.TERRAIN_PAVEMENT -> Pavement
            Type.TERRAIN_HIGHWAY_H -> HighwayH
            Type.TERRAIN_HIGHWAY_V -> HighwayV
            Type.TERRAIN_LAVA -> Lava
            Type.TERRAIN_CHASM -> Chasm
            Type.TERRAIN_RUBBLE -> Rubble
            else -> throw RuntimeException("tried to get(terrainType) for un-instantiatable type $type !")
        }
        fun getTiled(type: Int): Terrain.Type? = when (type) {
            27 -> null
            24,25 -> Type.TERRAIN_DIRT
            1 -> Type.TERRAIN_STONEFLOOR
            3 -> Type.TERRAIN_BRICKWALL
            28 -> Type.TERRAIN_PORTAL_DOOR
            8 -> Type.TERRAIN_GRASS
            else -> null
        }
    }

    enum class Type {
        BLANK,
        TEMP1,
        TEMP2,
        TEMP3,
        GENERIC_WALL,
        GENERIC_FLOOR,
        GENERIC_WATER,
        GENERIC_HIGHWAY,
        TERRAIN_BRICKWALL,
        TERRAIN_CAVEWALL,
        TERRAIN_WOODWALL,
        TERRAIN_TEMPERATE_FORESTWALL,
        TERRAIN_PINE_FORESTWALL,
        TERRAIN_STONEFLOOR,
        TERRAIN_CAVEFLOOR,
        TERRAIN_WOODFLOOR,
        TERRAIN_DIRT,
        TERRAIN_ROCKS,
        TERRAIN_CAVE_ROCKS,
        TERRAIN_GRASS,
        TERRAIN_UNDERGROWTH,
        TERRAIN_HARDPAN,
        TERRAIN_SWAMP,
        TERRAIN_BEACH,
        TERRAIN_SAND,
        TERRAIN_SHALLOW_WATER,
        TERRAIN_DEEP_WATER,
        TERRAIN_PORTAL_DOOR,
        TERRAIN_PORTAL_CAVE,
        TERRAIN_PAVEMENT,
        TERRAIN_HIGHWAY_H,
        TERRAIN_HIGHWAY_V,
        TERRAIN_LAVA,
        TERRAIN_CHASM,
        TERRAIN_RUBBLE,
    }

    override fun glyph() = this.glyph
    override fun description() = ""
    override fun glyphBatch() = Screen.terrainBatch
    override fun uiBatch() = Screen.uiTerrainBatch
    override fun level() = null
    override fun xy() = null

    open fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                              doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                       vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean)->Unit) { }

    open fun isWalkable() = this.walkable
    open fun isOpaque() = this.opaque
    open fun moveSpeed(actor: Actor) = 1f
    open fun stepSound(actor: Actor): Speaker.SFX? = null
    open fun stepSpark(actor: Actor, dir: XY): Spark? = Scoot(dir)
    open fun fertilityBonus() = 0f
    open fun glowColor(): LightColor? = null

    open fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) { }

    open fun pruneVerticalOrphans() = false

    open fun debugData(data: TerrainData?): String { return "none" }
}

object Blank : Terrain(Type.BLANK, Glyph.BLANK, true, true, false, false) {
    override fun name() = "BLANK"
}

@Serializable
sealed class TerrainData(
    val forType: Terrain.Type
)

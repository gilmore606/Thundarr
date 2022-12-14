package world.terrains

import actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.modals.Modal
import ui.panels.Console
import util.LightColor
import util.XY
import world.level.Level

sealed class Terrain(
    val type: Type,
    private val glyph: Glyph,
    private val walkable: Boolean,
    private val flyable: Boolean,
    private val opaque: Boolean,
    val canGrowPlants: Boolean,
    val dataType: Type = type  // should match TerrainData.forType
) {

    companion object {
        fun get(type: Type) = when (type) {
            Type.BLANK -> Blank
            Type.TERRAIN_BRICKWALL -> BrickWall
            Type.TERRAIN_CAVEWALL -> CaveWall
            Type.TERRAIN_FORESTWALL -> ForestWall
            Type.TERRAIN_CAVEFLOOR -> CaveFloor
            Type.TERRAIN_STONEFLOOR -> StoneFloor
            Type.TERRAIN_DIRT -> Dirt
            Type.TERRAIN_ROCKS -> Rocks
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
            Type.TERRAIN_PAVEMENT -> Pavement
            Type.TERRAIN_HIGHWAY_H -> HighwayH
            Type.TERRAIN_HIGHWAY_V -> HighwayV
            Type.TERRAIN_LAVA -> Lava
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
        GENERIC_WALL,
        GENERIC_FLOOR,
        GENERIC_WATER,
        GENERIC_HIGHWAY,
        TERRAIN_BRICKWALL,
        TERRAIN_CAVEWALL,
        TERRAIN_FORESTWALL,
        TERRAIN_STONEFLOOR,
        TERRAIN_CAVEFLOOR,
        TERRAIN_DIRT,
        TERRAIN_ROCKS,
        TERRAIN_GRASS,
        TERRAIN_UNDERGROWTH,
        TERRAIN_HARDPAN,
        TERRAIN_SWAMP,
        TERRAIN_BEACH,
        TERRAIN_SAND,
        TERRAIN_SHALLOW_WATER,
        TERRAIN_DEEP_WATER,
        TERRAIN_PORTAL_DOOR,
        TERRAIN_PAVEMENT,
        TERRAIN_HIGHWAY_H,
        TERRAIN_HIGHWAY_V,
        TERRAIN_LAVA
    }

    open fun glyph() = this.glyph

    open fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                              doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                       vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean)->Unit) { }

    open fun isWalkable() = this.walkable
    open fun isOpaque() = this.opaque
    open fun moveSpeed(actor: Actor) = 1f
    open fun stepSound(actor: Actor): Speaker.SFX? = null

    open fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) { }

    open fun debugData(data: TerrainData?): String { return "none" }
}

object Blank : Terrain(Type.BLANK, Glyph.BLANK, true, true, false, false)

@Serializable
sealed class TerrainData(
    val forType: Terrain.Type
)

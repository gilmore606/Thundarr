package world.terrains

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import render.Screen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.panels.Console
import util.LightColor
import util.XY
import world.Level

sealed class Terrain(
    val type: Type,
    private val glyph: Glyph,
    private val walkable: Boolean,
    private val flyable: Boolean,
    private val opaque: Boolean,
    val dataType: Type = type  // should match TerrainData.forType
) {

    companion object {
        fun get(type: Type) = when (type) {
            Type.TERRAIN_BRICKWALL -> BrickWall
            Type.TERRAIN_STONEFLOOR -> StoneFloor
            Type.TERRAIN_DIRT -> Dirt
            Type.TERRAIN_GRASS -> Grass
            Type.TERRAIN_WATER -> Water
            Type.TERRAIN_PORTAL_DOOR -> PortalDoor
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
        GENERIC_WALL,
        GENERIC_FLOOR,
        TERRAIN_BRICKWALL,
        TERRAIN_STONEFLOOR,
        TERRAIN_DIRT,
        TERRAIN_GRASS,
        TERRAIN_WATER,
        TERRAIN_PORTAL_DOOR,
    }

    open fun glyph() = this.glyph

    open fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                              doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                       vis: Float, glyph: Glyph, light: LightColor)->Unit) { }

    open fun isWalkable() = this.walkable

    open fun isOpaque() = this.opaque

    open fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) { }

    open fun debugData(data: TerrainData?): String { return "none" }
}

@Serializable
sealed class TerrainData(
    val forType: Terrain.Type
)


object PortalDoor : Terrain(
    Type.TERRAIN_PORTAL_DOOR,
    Glyph.PORTAL_DOOR,
    false,
    false,
    true
) {
    @Serializable class Data(
        val enterMsg: String,
        val levelId: String,
        val xy: XY? = null // only for doors to world
    ) : TerrainData(Type.TERRAIN_PORTAL_DOOR)

    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        if (data == null) throw RuntimeException("portalDoor had null terrain data!")
        val terrainData = data as Data
        Screen.addModal(ConfirmModal(
            terrainData.enterMsg.split('\n'), "Travel", "Cancel"
        ) { yes ->
            if (yes) {
                if (terrainData.levelId == "world") {
                    App.enterWorldFromLevel(terrainData.xy ?: throw RuntimeException("Door to world with no XY dest!"))
                } else {
                    App.enterLevelFromWorld(terrainData.levelId)
                }
            } else {
                Console.say("You reconsider and step away.")
            }
        })
    }
}

object Water : Terrain(
    Type.TERRAIN_WATER,
    Glyph.WATER,
    false,
    true,
    false
) {

}

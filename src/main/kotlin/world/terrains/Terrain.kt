package world.terrains

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import render.GameScreen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.panels.Console
import util.XY

sealed class Terrain(
    private val glyph: Glyph,
    private val walkable: Boolean,
    private val flyable: Boolean,
    private val opaque: Boolean,
) {

    companion object {
        fun get(type: Type) = when (type) {
            Type.TERRAIN_BRICKWALL -> BrickWall
            Type.TERRAIN_STONEFLOOR -> StoneFloor
            Type.TERRAIN_DIRT -> Dirt
            Type.TERRAIN_GRASS -> Grass
            Type.TERRAIN_WATER -> Water
            Type.TERRAIN_PORTAL_DOOR -> PortalDoor
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
        TERRAIN_BRICKWALL,
        TERRAIN_STONEFLOOR,
        TERRAIN_DIRT,
        TERRAIN_GRASS,
        TERRAIN_WATER,
        TERRAIN_PORTAL_DOOR,
    }

    open fun glyph() = this.glyph

    open fun isWalkable() = this.walkable

    open fun isOpaque() = this.opaque

    open fun onBump(actor: Actor, data: String) { }
}


object StoneFloor : Terrain(
    Glyph.STONE_FLOOR,
    true,
    true,
    false
)

object BrickWall : Terrain(
    Glyph.BRICK_WALL,
    false,
    false,
    true,
){
    override fun onBump(actor: Actor, data: String) {
        if (actor is Player) Console.say("You bump into a brick wall.")
    }
}

object Dirt : Terrain(
    Glyph.DIRT,
    true,
    true,
    false
)

object Grass : Terrain(
    Glyph.GRASS,
    true,
    true,
    false
)

object PortalDoor : Terrain(
    Glyph.PORTAL_DOOR,
    false,
    false,
    true
) {
    @Serializable class Data(
        val enterMsg: String,
        val levelId: String,
        val xy: XY? = null // only for doors to world
    )

    override fun onBump(actor: Actor, data: String) {
        val terrainData = Json.decodeFromString<Data>(data)
        GameScreen.addModal(ConfirmModal(
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
    Glyph.WATER,
    false,
    true,
    false
) {

}

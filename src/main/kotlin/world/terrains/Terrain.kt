package world.terrains

import actors.Actor
import actors.Player
import render.GameScreen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.panels.ConsolePanel

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
        TERRAIN_PORTAL_DOOR,
    }

    open fun glyph() = this.glyph

    open fun isWalkable() = this.walkable

    open fun isOpaque() = this.opaque

    open fun onBump(actor: Actor) { }
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
    override fun onBump(actor: Actor) {
        if (actor is Player) ConsolePanel.say("You bump into a brick wall.")
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
    override fun onBump(actor: Actor) {
        GameScreen.addModal(ConfirmModal(
            listOf("Enter the abandoned building?"), "Enter", "Cancel"
        ) { yes ->

        })
    }
}

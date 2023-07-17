package world.terrains

import actors.actors.Actor
import actors.actors.Player
import actors.statuses.Dazed
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import util.Dice
import util.LightColor
import world.level.Level

sealed class Wall(
    type: Type,
    glyph: Glyph,
    val damageToBreak: Float = 1f,
    val overrideOpaque: Boolean = true,
) : Terrain(type, glyph, false, overrideOpaque, false, dataType = Type.GENERIC_WALL) {

    @Serializable class Data(
        var damage: Float = 0f
    ) : TerrainData(Type.GENERIC_WALL)

    override fun isWalkableBy(actor: Actor) = false
    open fun bumpMsg() = "You bump into solid rock."
    open fun isDiggable() = true
    open fun digResult(): Thing? = null
    open fun digToFloorTerrain(): Terrain.Type = Terrain.Type.TERRAIN_CAVEFLOOR

    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        actor.level()?.also { level ->
            val weapon = actor.meleeWeapon()
            if (isDiggable() && weapon.canDig(type)) {
                Speaker.world(Speaker.SFX.DIG, source = actor.xy)
                level.addSpark(Smoke().at(actor.xy.x, actor.xy.y))
                if (actor is Player && !actor.dangerMode) {
                    Console.say("Ow!  That almost made you mad enough to dig through it with your ${weapon.name()}.")
                } else {
                    val terrainData = data?.let { it as Data } ?: Data()
                    terrainData.damage += 1f
                    if (terrainData.damage < damageToBreak) {
                        level.setTerrainData(x, y, terrainData)
                        Console.sayAct("You tunnel through solid rock.", "%Dn digs furiously into the stone!", actor)
                    } else {
                        actor.level()?.also { level ->
                            level.setTerrain(x, y, digToFloorTerrain(), roofed = true)
                            digResult()?.also { it.moveTo(level, actor.xy.x, actor.xy.y) }
                        }
                    }
                }
            } else {
                if (actor is Player) {
                    Console.say(bumpMsg())
                    if (actor.dangerMode) actor.addStatus(Dazed())
                }
            }
        }
    }

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean)->Unit
    ) {
        level.getTerrainData(x, y)?.let { it as Data }?.also {
            if (it.damage > 0f) {
                doQuad(x.toDouble(), y.toDouble(), (x+1).toDouble(), (y+1).toDouble(),
                    0f, 0f, 1f, 1f, vis, Glyph.WALL_DAMAGE, light, false)
            }
        }
    }
}

object BrickWall : Wall(Type.TERRAIN_BRICKWALL, Glyph.BRICK_WALL, 3f) {
    override fun name() = "brick wall"
    override fun bumpMsg() = "You bump into a brick wall."
    override fun digResult() = Brick()
    override fun digToFloorTerrain() = Terrain.Type.TERRAIN_STONEFLOOR
    override fun trailsOverwrite() = false
}

object MetalWall : Wall(Type.TERRAIN_METALWALL, Glyph.METAL_WALL, 10f) {
    override fun name() = "metal wall"
    override fun bumpMsg() = "You bump into a smooth metal wall."
    override fun isDiggable() = false
    override fun trailsOverwrite() = false
}

object CaveWall : Wall(Type.TERRAIN_BRICKWALL, Glyph.CLIFF_WALL, 4f) {
    override fun name() = "rock face"
    override fun bumpMsg() = "You bump into a rock face."
    override fun digResult() = if (Dice.chance(0.4f)) Boulder() else if (Dice.chance(0.1f)) Rock() else null
    override fun digToFloorTerrain() = Type.TERRAIN_CAVEFLOOR
}

object WoodWall : Wall(Type.TERRAIN_WOODWALL, Glyph.WOOD_WALL, 2f) {
    override fun name() = "wooden wall"
    override fun bumpMsg() = "You bump into a wooden wall."
    override fun digResult() = Log()
    override fun digToFloorTerrain() = Type.TERRAIN_DIRT
    override fun trailsOverwrite() = false
}

object WindowWall : Wall(Type.TERRAIN_WINDOWWALL, Glyph.WINDOW, 1f, overrideOpaque = false) {
    override fun name() = "window"
    override fun bumpMsg() = "You can't fit through the window."
    override fun isDiggable() = false
    override fun trailsOverwrite() = false
}


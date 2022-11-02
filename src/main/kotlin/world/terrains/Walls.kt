package world.terrains

import actors.Actor
import actors.Player
import actors.statuses.Dazed
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import things.Brick
import things.Thing
import ui.panels.Console
import util.Dice
import util.LightColor
import world.Level

sealed class Wall(
    type: Type,
    glyph: Glyph,
    val damageToBreak: Float = 1f
) : Terrain(type, glyph, false, false, true, dataType = Type.GENERIC_WALL) {

    @Serializable class Data(
        var damage: Float = 0f
    ) : TerrainData(Type.GENERIC_WALL)

    open fun bumpMsg() = "You bump into solid rock."
    open fun digResult(): Thing? = null

    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        actor.level()?.also { level ->
            val weapon = actor.weapon()
            if (weapon?.canDig(type) == true) {
                level.addSpark(Smoke().at(actor.xy.x, actor.xy.y))
                if (actor is Player && !actor.willAggro) {
                    Console.say("Ow!  That almost made you mad enough to dig through it with your ${weapon.name()}.")
                } else {
                    val terrainData = data?.let { it as Data } ?: Data()
                    terrainData.damage += 1f
                    if (terrainData.damage < damageToBreak) {
                        level.setTerrainData(x, y, terrainData)
                        Console.sayAct("You tunnel through solid rock.", "%Dn digs furiously into the stone!", actor)
                    } else {
                        Console.sayAct("You break through!", "%Dn digs furiously into the stone!", actor)
                        actor.level()?.also { level ->
                            level.setTerrain(x, y, Type.TERRAIN_STONEFLOOR, roofed = true)
                            digResult()?.also { it.moveTo(level, actor.xy.x, actor.xy.y) }
                        }
                    }
                }
            } else {
                if (actor is Player) Console.say(bumpMsg())
                actor.addStatus(Dazed())
            }
        }
    }

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor)->Unit
    ) {
        level.getTerrainData(x, y)?.let { it as Data }?.also {
            if (it.damage > 0f) {
                doQuad(x.toDouble(), y.toDouble(), (x+1).toDouble(), (y+1).toDouble(),
                    0f, 0f, 1f, 1f, vis, Glyph.WALL_DAMAGE, light)
            }
        }
    }
}

object BrickWall : Wall(Type.TERRAIN_BRICKWALL, Glyph.BRICK_WALL, 3f) {
    override fun bumpMsg() = "You bump into a brick wall."
    override fun digResult() = if (Dice.chance(0.3f)) Brick() else null
}
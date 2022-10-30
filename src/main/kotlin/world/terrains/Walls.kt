package world.terrains

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console

sealed class Wall(
    type: Type,
    glyph: Glyph,
    val damageToBreak: Float = 1f
) : Terrain(type, glyph, false, false, true, dataType = Type.GENERIC_WALL) {

    @Serializable class Data(
        var damage: Float = 0f
    ) : TerrainData(Type.GENERIC_WALL)

    open fun bumpMsg() = "You bump into solid rock."

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
                        actor.level()?.setTerrain(x, y, Type.TERRAIN_STONEFLOOR, roofed = true)
                    }
                }
            } else {
                if (actor is Player) Console.say(bumpMsg())
            }
        }
    }
}

object BrickWall : Wall(Type.TERRAIN_BRICKWALL, Glyph.BRICK_WALL, 3f) {
    override fun bumpMsg() = "You bump into a brick wall."
}

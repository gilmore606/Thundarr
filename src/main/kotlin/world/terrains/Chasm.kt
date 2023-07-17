package world.terrains

import actors.actors.Actor
import actors.actors.Player
import render.tilesets.Glyph
import ui.panels.Console

object Chasm : Terrain(Type.TERRAIN_CHASM, Glyph.CHASM, true, false, false) {

    override fun name() = "chasm"
    override fun isWalkableBy(actor: Actor) = actor.canFly()
    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        if (actor is Player) {
            Console.say("A deep chasm yawns before you.  You think twice about jumping in.")
        }
    }
}

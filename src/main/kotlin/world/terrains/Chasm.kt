package world.terrains

import actors.Actor
import actors.Player
import render.tilesets.Glyph
import ui.panels.Console

object Chasm : Terrain(Type.TERRAIN_CHASM, Glyph.CHASM, false, true, false, false) {

    override fun name() = "chasm"
    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        if (actor is Player) {
            Console.say("A deep chasm yawns before you.  You think twice about jumping in.")
        }
    }
}

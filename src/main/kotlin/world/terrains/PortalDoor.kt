package world.terrains

import actors.Actor
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.modals.Modal
import ui.panels.Console
import util.XY
import world.Chunk


object PortalDoor : Terrain(
    Type.TERRAIN_PORTAL_DOOR,
    Glyph.PORTAL_DOOR,
    false,
    false,
    true
) {
    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        actor.level?.exitAt(x, y)?.also { exitRecord ->
            Screen.addModal(ConfirmModal(
                exitRecord.enterMsg.split('\n'), "Travel", "Cancel", position = Modal.Position.CENTER_LOW
            ) { yes ->
                if (yes) {
                    if (exitRecord.type == Chunk.ExitType.WORLD) {
                        App.enterWorldFromLevel(exitRecord.worldDest)
                    } else {
                        App.enterLevelFromWorld(exitRecord.buildingFirstLevelId)
                    }
                } else {
                    Console.say("You reconsider and step away.")
                }
            })
        } ?: throw RuntimeException("No exit record found for portalDoor at $x $y !")
    }
}

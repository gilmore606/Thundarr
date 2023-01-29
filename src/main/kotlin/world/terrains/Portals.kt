package world.terrains

import actors.Actor
import actors.Player
import render.Screen
import render.tilesets.Glyph
import ui.modals.ConfirmModal
import ui.modals.Modal
import ui.panels.Console
import world.Chunk

sealed class Portal(
    type: Terrain.Type,
    glyph: Glyph
) : Terrain(type, glyph, false, false, true, false) {
    override fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) {
        actor.level?.exitAt(x, y)?.also { exitRecord ->
            val oldLevel = actor.level!!
            Screen.addModal(ConfirmModal(
                exitRecord.enterMsg.split('\n'), "Travel", "Cancel", position = Modal.Position.CENTER_LOW
            ) { yes ->
                if (yes) {
                    oldLevel.onPlayerExited()
                    if (exitRecord.type == Chunk.ExitType.WORLD) {
                        if (actor is Player) {
                            App.enterWorldFromLevel(exitRecord.worldDest)
                        } else {
                            // TODO: let NPCs use portal doors
                        }
                    } else {
                        if (actor is Player) {
                            App.enterLevelFromWorld(exitRecord.buildingFirstLevelId)
                        } else {
                            // TODO
                        }
                    }
                } else {
                    Console.say("You reconsider and step away.")
                }
            })
        } ?: throw RuntimeException("No exit record found for portal at $x $y !")
    }
}

object PortalDoor : Portal(
    Type.TERRAIN_PORTAL_DOOR,
    Glyph.PORTAL_DOOR
) {
    override fun name() = "door"
}

object PortalCave : Portal(
    Type.TERRAIN_PORTAL_CAVE,
    Glyph.PORTAL_CAVE
) {
    override fun name() = "cave mouth"
}

package things

import actors.Actor
import actors.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel
import util.LightColor

@Serializable
class Thing(
    val glyph: Glyph,
    val isOpaque: Boolean = false,
    val isBlocking: Boolean = false
): LightSource {
    var light: LightColor? = null

    fun glyph() = glyph

    fun onWalkedOnBy(actor: Actor) {
        if (actor is Player) {
            ConsolePanel.say("You hack through the underbrush.")
        }
    }

    override fun light() = light
}

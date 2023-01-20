package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console


@Serializable
sealed class Scenery : Thing() {
    override fun isPortable() = false
    override fun isBlocking() = false

    override fun onWalkedOnBy(actor: Actor) {
        if (actor is Player) { Console.say(walkOnMsg()) }
    }
    abstract fun walkOnMsg(): String

    override fun description() = "Just another bit of the twisted beauty of the wasteland."
}

@Serializable
class HighwaySign(
    val text: String
) : Scenery() {
    override fun glyph() = Glyph.HIGHWAY_SIGN
    override fun name() = "highway sign"
    override fun isOpaque() = false
    override fun walkOnMsg() = "\"$text\""
    override fun description() = "An ancient faded highway information sign."
}

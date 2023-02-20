package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import ui.panels.Console


@Serializable
sealed class Scenery : Thing() {
    override fun isPortable() = false
    override fun isBlocking() = false

    override fun onWalkedOnBy(actor: Actor) {
        if (actor is Player) { Console.say(walkOnMsg()) }
    }
    open fun walkOnMsg() = ""

    override fun description() = "Just another bit of the twisted beauty of the wasteland."
}

@Serializable
class HighwaySign(
    val text: String
) : Scenery() {
    override val tag = Tag.THING_HIGHWAYSIGN
    override fun glyph() = Glyph.HIGHWAY_SIGN
    override fun name() = "highway sign"
    override fun isOpaque() = false
    override fun walkOnMsg() = "\"$text\""
    override fun description() = "An ancient faded highway information sign."
}

@Serializable
class Boulder : Scenery() {
    override val tag = Tag.THING_BOULDER
    override fun glyph() = Glyph.BOULDER
    override fun name() = "boulder"
    override fun isOpaque() = false
    override fun description() = "A large rock."
}

package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice
import util.Madlib


@Serializable
sealed class Scenery : Thing() {
    override fun isPortable() = false
    override fun announceOnWalk() = false
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
    override val tag = Tag.HIGHWAY_SIGN
    override fun glyph() = Glyph.HIGHWAY_SIGN
    override fun name() = "highway sign"
    override fun isOpaque() = false
    override fun walkOnMsg() = "\"$text\"."
    override fun description() = "An ancient faded highway information sign.  \"$text\"."
}

@Serializable
class TrailSign(
    val text: String
) : Scenery() {
    override val tag = Tag.TRAIL_SIGN
    override fun glyph() = Glyph.TRAIL_SIGN
    override fun name() = "trail sign"
    override fun isOpaque() = false
    override fun walkOnMsg() = "\"$text\"."
    override fun description() = "A handmade wooden trail marker. \"$text\"."
}

@Serializable
class Boulder : Scenery() {
    override val tag = Tag.BOULDER
    override fun glyph() = Glyph.BOULDER
    override fun name() = "boulder"
    override fun isOpaque() = false
    override fun description() = "A large rock."
}

@Serializable
class Gravestone(
    val text: String = Madlib.graveName() + " -- " + Dice.range(1994, 2007).toString() + " -- " + Madlib.epitaph()
) : Scenery() {
    override val tag = Tag.GRAVESTONE
    override fun glyph() = Glyph.GRAVESTONE
    override fun name() = "gravestone"
    override fun isOpaque() = false
    override fun description() = "A lichen-covered tombstone.  '$text'"
}

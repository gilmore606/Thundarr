package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel


@Serializable
sealed class Scenery : Thing() {
    override fun isPortable() = false
    override fun isBlocking() = false

    override fun onWalkedOnBy(actor: Actor) {
        if (actor is Player) { ConsolePanel.say(walkOnMsg()) }
    }
    abstract fun walkOnMsg(): String
}


@Serializable
class OakTree : Scenery() {
    override fun glyph() = Glyph.TREE
    override fun name() = "oak tree"
    override val kind = Kind.OAK_TREE
    override fun isOpaque() = true
    override fun walkOnMsg() = "You hack through the dense trees."
}

@Serializable
class PineTree : Scenery() {
    override fun glyph() = Glyph.PINE_TREE
    override fun name() = "pine tree"
    override val kind = Kind.PINE_TREE
    override fun isOpaque() = true
    override fun walkOnMsg() = "You trudge through the bracken and pinecones."
}

@Serializable
class PalmTree : Scenery() {
    override fun glyph() = Glyph.PALM_TREE
    override fun name() = "palm tree"
    override val kind = Kind.PALM_TREE
    override fun isOpaque() = true
    override fun walkOnMsg() = "You hack through the thick jungle growth."
}

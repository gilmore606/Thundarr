package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.sparks.Smoke
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

    override fun description() = "Just another bit of the hideous natural beauty of the wasteland."
}

@Serializable
sealed class Tree : Scenery() {
    override fun isOpaque() = true
    override fun walkOnMsg() = "You hack through the dense trees."

    override fun uses() = mutableSetOf<Use>().apply {
        add(Use("chop down " + name(), 3.0f,
            canDo = { it.weapon() is Axe },
            toDo = { actor, level ->
                Log().moveTo(level, actor.xy.x, actor.xy.y)
                level.addSpark(Smoke().at(actor.xy.x, actor.xy.y))
                this@Tree.moveTo(null)
                Console.sayAct("%Dd comes crashing down!", "%Dn chops down %id.", actor, this@Tree)
            }))
    }
}

@Serializable
class OakTree : Tree() {
    override fun glyph() = Glyph.TREE
    override fun name() = "oak tree"
    override fun walkOnMsg() = "You hack through the dense trees."
}

@Serializable
class PineTree : Tree() {
    override fun glyph() = Glyph.PINE_TREE
    override fun name() = "pine tree"
    override fun walkOnMsg() = "You trudge through the bracken and pinecones."
}

@Serializable
class PalmTree : Tree() {
    override fun glyph() = Glyph.PALM_TREE
    override fun name() = "palm tree"
    override fun walkOnMsg() = "You hack through the thick jungle growth."
}

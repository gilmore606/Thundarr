package things

import actors.Actor
import actors.Player
import actors.stats.skills.Dodge
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice


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
    override fun isOpaque() = false
    override fun walkOnMsg() = if (Dice.chance(0.3f)) treeMsg() else ""
    open fun treeMsg() = "You hack through the dense trees."
    override fun moveSpeedPast(actor: Actor): Float? = 1.4f

    override fun uses() = mapOf(
        UseTag.DESTROY to Use("chop down " + name(), 3.0f,
            canDo = { actor,x,y,targ -> actor.meleeWeapon() is Axe && isNextTo(actor) },
            toDo = { actor, level, x, y ->
                val logVictim = level.actorAt(x,y)
                repeat(Dice.oneTo(3)) {
                    logVictim?.also { victim ->
                        if (Dodge.resolve(victim, 0f) < 0f) {
                            Console.sayAct("Ow!  The falling branches hit you!", "The falling timber collides with %dn!", victim)
                            victim.receiveDamage(Dice.float(1f, 5f))
                        }
                    }
                    Log().moveTo(level, x, y)
                }
                level.addSpark(Smoke().at(x, y))
                this@Tree.moveTo(null)
                Speaker.world(Speaker.SFX.TREEFALL, source = actor.xy)
                Console.sayAct("%Dd comes crashing down!", "%Dn chops down %id.", actor, this@Tree)
            })
    )

    override fun flammability() = 0.4f
    override fun onBurn(delta: Float): Float {
        if (Dice.chance(delta * 0.002f)) {
            moveTo(null)
            return 0f
        } else {
            return 4f * delta
        }
    }
}

@Serializable
class OakTree : Tree() {
    override fun glyph() = Glyph.TREE
    override fun name() = "oak tree"
    override fun treeMsg() = "You hack through the dense trees."
}

@Serializable
class PineTree : Tree() {
    override fun glyph() = Glyph.PINE_TREE
    override fun name() = "pine tree"
    override fun treeMsg() = "You trudge through the bracken and pinecones."
}

@Serializable
class PalmTree : Tree() {
    override fun glyph() = Glyph.PALM_TREE
    override fun name() = "palm tree"
    override fun treeMsg() = "You hack through the thick jungle growth."
}

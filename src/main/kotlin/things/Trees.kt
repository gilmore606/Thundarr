package things

import actors.Actor
import actors.stats.skills.Dodge
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice


@Serializable
sealed class Tree : Plant() {
    override fun description() = "A tree."
    override fun moveSpeedPast(actor: Actor): Float? = 1.4f
    open fun chopProduct(): Thing? = Log()
    open fun chopProductAmount() = Dice.oneTo(3)
    override fun uses() = mapOf(
        UseTag.DESTROY to Use("chop down " + name(), 3.0f,
            canDo = { actor,x,y,targ -> actor.meleeWeapon() is Axe && isNextTo(actor) },
            toDo = { actor, level, x, y ->
                val logVictim = level.actorAt(x,y)
                repeat(chopProductAmount()) {
                    chopProduct()?.also { log ->
                        logVictim?.also { victim ->
                            if (Dodge.resolve(victim, 0f) < 0f) {
                                Console.sayAct("Ow!  The falling branches hit you!", "The falling timber collides with %dn!", victim)
                                victim.receiveDamage(Dice.float(1f, 5f))
                            }
                        }
                        log.moveTo(level, x, y)
                    }
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
    override fun glyph() = Glyph.OAK_TREE
    override fun name() = "oak tree"
}

@Serializable
class TeakTree : Tree() {
    override fun glyph() = Glyph.OAK_TREE
    override fun hue() = 0.3f
    override fun name() = "teak tree"
}

@Serializable
class MapleTree : Tree() {
    override fun glyph() = Glyph.MAPLE_TREE
    override fun name() = "maple tree"
}

@Serializable
class BirchTree : Tree() {
    override fun glyph() = Glyph.BIRCH_TREE
    override fun name() = "birch tree"
}

@Serializable
class AppleTree : Tree() {
    override fun glyph() = Glyph.FRUIT_TREE
    override fun name() = "apple tree"
    override fun bearsFruit() = true
    override fun spawnFruitChance() = 0.7f
    override fun fruit() = Apple()
}

@Serializable
class PearTree: Tree() {
    override fun glyph() = Glyph.FRUIT_TREE
    override fun hue() = 0.7f
    override fun name() = "pear tree"
    override fun bearsFruit() = true
    override fun spawnFruitChance() = 0.4f
    override fun fruit() = Pear()
}

@Serializable
class PineTree : Tree() {
    override fun glyph() = Glyph.PINE_TREE
    override fun name() = "pine tree"
}

@Serializable
class SpruceTree : Tree() {
    override fun glyph() = Glyph.PINE_TREE
    override fun hue() = 0.8f
    override fun name() = "spruce tree"
}

@Serializable
class PalmTree : Tree() {
    override fun glyph() = Glyph.PALM_TREE
    override fun name() = "palm tree"
}

@Serializable
class CoconutTree : Tree() {
    override fun glyph() = Glyph.PALM_TREE
    override fun hue() = 0.3f
    override fun name() = "coconut tree"
}

@Serializable
class DeadTree : Tree() {
    override fun glyph() = Glyph.DEAD_TREE
    override fun name() = "dead tree"
    override fun description() = "The dessicated skeleton of a once-mighty tree."
}

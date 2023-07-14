package things

import actors.Actor
import actors.stats.skills.Dodge
import actors.stats.skills.Survive
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
            canDo = { actor,x,y,targ -> actor.meleeWeapon().canChopTrees() && isNextTo(actor) },
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
    override val tag = Tag.OAKTREE
    override fun glyph() = Glyph.OAK_TREE
    override fun name() = "oak tree"
}

@Serializable
class TeakTree : Tree() {
    override val tag = Tag.TEAKTREE
    override fun glyph() = Glyph.OAK_TREE
    override fun hue() = 0.3f
    override fun name() = "teak tree"
}

@Serializable
class MapleTree : Tree() {
    override val tag = Tag.MAPLETREE
    override fun glyph() = Glyph.MAPLE_TREE
    override fun name() = "maple tree"
}

@Serializable
class BirchTree : Tree() {
    override val tag = Tag.BIRCHTREE
    override fun glyph() = Glyph.BIRCH_TREE
    override fun name() = "birch tree"
}

@Serializable
class AppleTree : Tree() {
    override val tag = Tag.APPLETREE
    override fun glyph() = Glyph.FRUIT_TREE
    override fun name() = "apple tree"
    override fun bearsFruit() = true
    override fun spawnFruitChance() = 0.7f
    override fun fruit() = Apple()
    override fun fruitTag() = Tag.APPLE
}

@Serializable
class PearTree: Tree() {
    override val tag = Tag.PEARTREE
    override fun glyph() = Glyph.FRUIT_TREE
    override fun hue() = 0.7f
    override fun name() = "pear tree"
    override fun bearsFruit() = true
    override fun spawnFruitChance() = 0.4f
    override fun fruit() = Pear()
    override fun fruitTag() = Tag.PEAR
}

@Serializable
class PineTree : Tree() {
    override val tag = Tag.PINETREE
    override fun glyph() = Glyph.PINE_TREE
    override fun name() = "pine tree"
}

@Serializable
class SpruceTree : Tree() {
    override val tag = Tag.SPRUCETREE
    override fun glyph() = Glyph.PINE_TREE
    override fun hue() = 0.8f
    override fun name() = "spruce tree"
}

@Serializable
class PalmTree : Tree() {
    override val tag = Tag.PALMTREE
    override fun glyph() = Glyph.PALM_TREE
    override fun name() = "palm tree"
}

@Serializable
class CoconutTree : Tree() {
    override val tag = Tag.COCONUTTREE
    override fun glyph() = Glyph.PALM_TREE
    override fun hue() = 0.3f
    override fun name() = "coconut tree"
}

@Serializable
class DeadTree : Tree() {
    override val tag = Tag.DEADTREE
    override fun glyph() = Glyph.DEAD_TREE
    override fun name() = "dead tree"
    override fun description() = "The dessicated skeleton of a once-mighty tree."
    override fun uses() = mapOf(
        UseTag.TRANSFORM to Use("pull a log from ${name()}", 3f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y ->
                scavengeLog(actor)
            })
    )
    private fun scavengeLog(actor: Actor) {
        if (Survive.resolve(actor, 0f) >= 0) {
            Console.sayAct("You find a log and break it off of the dead tree trunk.", "%Dn pulls a log from %it.", actor, this)
            Log().moveTo(actor)
        } else {
            Console.sayAct("You fail to find a usable log from the dead tree.", "%Dn pokes at %it.", actor, this)
        }
        if (Dice.chance(0.3f)) {
            this.moveTo(null)
        }
    }
}

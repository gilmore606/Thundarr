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
sealed class Plant : Scenery() {
    override fun isOpaque() = false
    override fun walkOnMsg() = ""
    override fun description() = "A plant."
}

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


@Serializable
sealed class Bush : Plant() {
}

@Serializable
class ThornBush : Bush() {
    override fun glyph() = Glyph.BUSH_1
    override fun name() = "thorn bush"
}

@Serializable
class SageBush : Bush() {
    override fun glyph() = Glyph.BUSH_2
    override fun name() = "sage bush"
}

@Serializable
class BerryBush : Bush() {
    override fun glyph() = Glyph.BUSH_1_FRUIT
    override fun name() = "berry bush"
}

@Serializable
class HoneypodBush: Bush() {
    override fun glyph() = Glyph.BUSH_2_FRUIT
    override fun name() = "honeypod bush"
}


@Serializable
class Wildflowers : Plant() {
    override fun glyph() = Glyph.FLOWERS
    override fun name() = "flowers"
}

@Serializable
class Poppies : Plant() {
    override fun glyph() = Glyph.FLOWERS
    override fun hue() = 0.73f
    override fun name() = "poppy flowers"
}

@Serializable
class Deathflower : Plant() {
    override fun glyph() = Glyph.HANGFLOWER
    override fun name() = "death flower"
}

@Serializable
class Dreamflower : Plant() {
    override fun glyph() = Glyph.HANGFLOWER
    override fun hue() = 0.4f
    override fun name() = "dream flower"
}

@Serializable
class Sunflower : Plant() {
    override fun glyph() = Glyph.SUNFLOWER
    override fun name() = "sunflower"
}

@Serializable
class Lightflower : Plant() {
    override fun glyph() = Glyph.SUNFLOWER
    override fun hue() = 0.7f
    override fun name() = "lightflower"
}

@Serializable
class Saguaro : Tree() {
    override fun glyph() = Glyph.CACTUS_BIG
    override fun name() = "saguaro cactus"
}

@Serializable
class Cholla : Plant() {
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun name() = "cholla cactus"
}

@Serializable
class Prickpear : Plant() {
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun hue() = 0.3f
    override fun name() = "prickpear cactus"
}

@Serializable
class BalmMoss : Plant() {
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun name() = "balm moss"
}

@Serializable
class LaceMoss : Plant() {
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun hue() = 0.8f
    override fun name() = "lace moss"
}

@Serializable
class WizardcapMushroom : Plant() {
    override fun glyph() = Glyph.MUSHROOM
    override fun name() = "wizardcap mushroom"
}

@Serializable
class SpeckledMushroom : Plant() {
    override fun glyph() = Glyph.TOADSTOOLS
    override fun name() = "speckled mushrooms"
}

@Serializable
class BloodcapMushroom : Plant() {
    override fun glyph() = Glyph.TOADSTOOLS
    override fun hue() = 0.5f
    override fun name() = "bloodcap mushrooms"
}

@Serializable
class Foolsleaf : Plant() {
    override fun glyph() = Glyph.SUCCULENT
    override fun name() = "foolsleaf"
}

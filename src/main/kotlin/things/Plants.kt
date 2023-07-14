package things

import actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice
import util.hasOneWhere

@Serializable
sealed class Plant : Scenery(), Temporal {
    override fun isOpaque() = false
    override fun walkOnMsg() = ""
    override fun description() = "A plant."

    private var nextFruitTime = App.gameTime.time.toLong() + nextFruitTurns()
    override fun temporalDone() = !bearsFruit()
    override fun advanceTime(delta: Float) {
        if (bearsFruit() && App.gameTime.time > nextFruitTime) {
            bearFruit()
        }
    }

    open fun bearsFruit() = false
    open fun fruitTemperatureMin() = 60
    open fun spawnFruitChance() = 0.2f
    open fun fruit(): Thing? = null
    open fun fruitTag(): Thing.Tag? = null
    open fun nextFruitTurns() = Dice.range(2000, 4000).toLong()
    open fun bearFruit() {
        holder?.also {
            if (it.temperature() >= fruitTemperatureMin()) {
                if (!hasFruit()) {
                    fruit()?.also { fruit ->
                        fruit.moveTo(holder)
                    }
                }
                nextFruitTime = App.gameTime.time.toLong() + nextFruitTurns()
            }
        }
    }

    open fun hasFruit(): Boolean =
        holder?.let {
            (it.contents().hasOneWhere { it.tag == fruitTag() })
        } ?: true

    override fun onSpawn() {
        if (bearsFruit() && Dice.chance(spawnFruitChance())) {
            bearFruit()
        }
    }
}


@Serializable
sealed class Bush : Plant() {
    override fun uses() = mapOf(
        UseTag.TRANSFORM to Use("pull a stick from ${name()}", 3f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y ->
                scavengeStick(actor)
            })
    )

    private fun scavengeStick(actor: Actor) {
        if (Survive.resolve(actor, 0f) >= 0) {
            Console.sayAct("You tear a sturdy stick out of the bush.", "%Dn rips up a bush.", actor)
            Stick().moveTo(actor)
        } else {
            Console.sayAct("You rip up the bush, but fail to find a decent stick.", "%Dn rips up a bush.", actor)
        }
        this.moveTo(null)
    }
}

@Serializable
class ThornBush : Bush() {
    override val tag = Tag.THORNBUSH
    override fun glyph() = Glyph.BUSH_2
    override fun name() = "thorn bush"
}

@Serializable
class SageBush : Bush() {
    override val tag = Tag.SAGEBUSH
    override fun glyph() = Glyph.BUSH_1
    override fun name() = "sage bush"
}

@Serializable
class BerryBush : Bush() {
    override val tag = Tag.BERRYBUSH
    override fun glyph() = Glyph.BUSH_1_FRUIT
    override fun name() = "berry bush"
}

@Serializable
class HoneypodBush: Bush() {
    override val tag = Tag.HONEYPODBUSH
    override fun glyph() = Glyph.BUSH_2_FRUIT
    override fun name() = "honeypod bush"
}


@Serializable
class Wildflowers : Plant() {
    override val tag = Tag.WILDFLOWERS
    override fun glyph() = Glyph.FLOWERS
    override fun name() = "flowers"
}

@Serializable
class BlueBells : Plant() {
    override val tag = Tag.BLUEBELLS
    override fun glyph() = Glyph.FLOWERS
    override fun hue() = 0.33f
    override fun name() = "bluebell flowers"
}

@Serializable
class Poppies : Plant() {
    override val tag = Tag.POPPIES
    override fun glyph() = Glyph.FLOWERS
    override fun hue() = 1.8f
    override fun name() = "poppies"
}

@Serializable
class Dandylions : Plant() {
    override val tag = Tag.DANDYLIONS
    override fun glyph() = Glyph.FLOWERS
    override fun hue() = -1.8f
    override fun name() = "dandylions"
}

@Serializable
class Deathflower : Plant() {
    override val tag = Tag.DEATHFLOWER
    override fun glyph() = Glyph.HANGFLOWER
    override fun name() = "death flower"
}

@Serializable
class Dreamflower : Plant() {
    override val tag = Tag.DREAMFLOWER
    override fun glyph() = Glyph.HANGFLOWER
    override fun hue() = 0.4f
    override fun name() = "dream flower"
}

@Serializable
class Sunflower : Plant() {
    override val tag = Tag.SUNFLOWER
    override fun glyph() = Glyph.SUNFLOWER
    override fun name() = "sunflower"
}

@Serializable
class Lightflower : Plant() {
    override val tag = Tag.LIGHTFLOWER
    override fun glyph() = Glyph.SUNFLOWER
    override fun hue() = 0.7f
    override fun name() = "lightflower"
}

@Serializable
class Saguaro : Tree() {
    override val tag = Tag.SAGUARO
    override fun glyph() = Glyph.CACTUS_BIG
    override fun name() = "saguaro cactus"
}

@Serializable
class Cholla : Plant() {
    override val tag = Tag.CHOLLA
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun name() = "cholla cactus"
}

@Serializable
class Prickpear : Plant() {
    override val tag = Tag.PRICKPEAR
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun hue() = 0.3f
    override fun name() = "prickpear cactus"
}

@Serializable
class BalmMoss : Plant() {
    override val tag = Tag.BALMMOSS
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun name() = "balm moss"
}

@Serializable
class LaceMoss : Plant() {
    override val tag = Tag.LACEMOSS
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun hue() = 0.8f
    override fun name() = "lace moss"
}

@Serializable
class Foolsleaf : Plant() {
    override val tag = Tag.FOOLSLEAF
    override fun glyph() = Glyph.SUCCULENT
    override fun name() = "foolsleaf"
}

@Serializable
sealed class Mycelium : Plant() {
    override fun glyph() = Glyph.BLANK
    override fun isIntangible() = true
    override fun name() = ""
    override fun bearsFruit() = true
}

@Serializable
class WizardcapMycelium : Mycelium() {
    override val tag = Tag.WIZARDCAP_MYCELIUM
    override fun fruit() = WizardcapMushroom()
    override fun fruitTag() = Tag.WIZARDCAP_MUSHROOM
}

@Serializable
class SpeckledMycelium : Mycelium() {
    override val tag = Tag.SPECKLED_MYCELIUM
    override fun fruit() = SpeckledMushroom()
    override fun fruitTag() = Tag.SPECKLED_MUSHROOM
}

@Serializable
class BloodcapMycelium : Mycelium() {
    override val tag = Tag.BLOODCAP_MYCELIUM
    override fun fruit() = BloodcapMushroom()
    override fun fruitTag() = Tag.BLOODCAP_MUSHROOM
}

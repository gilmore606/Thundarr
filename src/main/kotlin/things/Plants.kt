package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

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
    open fun nextFruitTurns() = Dice.range(2000, 4000).toLong()
    open fun bearFruit() {
        holder?.also {
            if (it.temperature() >= fruitTemperatureMin()) {
                fruit()?.also { fruit ->
                    fruit.moveTo(holder)
                }
                nextFruitTime = App.gameTime.time.toLong() + nextFruitTurns()
            }
        }
    }

    override fun onSpawn() {
        if (bearsFruit() && Dice.chance(spawnFruitChance())) {
            bearFruit()
        }
    }
}


@Serializable
sealed class Bush : Plant() {
}

@Serializable
class ThornBush : Bush() {
    override val tag = Tag.THING_THORNBUSH
    override fun glyph() = Glyph.BUSH_2
    override fun name() = "thorn bush"
}

@Serializable
class SageBush : Bush() {
    override val tag = Tag.THING_SAGEBUSH
    override fun glyph() = Glyph.BUSH_1
    override fun name() = "sage bush"
}

@Serializable
class BerryBush : Bush() {
    override val tag = Tag.THING_BERRYBUSH
    override fun glyph() = Glyph.BUSH_1_FRUIT
    override fun name() = "berry bush"
}

@Serializable
class HoneypodBush: Bush() {
    override val tag = Tag.THING_HONEYPODBUSH
    override fun glyph() = Glyph.BUSH_2_FRUIT
    override fun name() = "honeypod bush"
}


@Serializable
class Wildflowers : Plant() {
    override val tag = Tag.THING_WILDFLOWERS
    override fun glyph() = Glyph.FLOWERS
    override fun name() = "flowers"
}

@Serializable
class Poppies : Plant() {
    override val tag = Tag.THING_POPPIES
    override fun glyph() = Glyph.FLOWERS
    override fun hue() = 0.73f
    override fun name() = "poppy flowers"
}

@Serializable
class Deathflower : Plant() {
    override val tag = Tag.THING_DEATHFLOWER
    override fun glyph() = Glyph.HANGFLOWER
    override fun name() = "death flower"
}

@Serializable
class Dreamflower : Plant() {
    override val tag = Tag.THING_DREAMFLOWER
    override fun glyph() = Glyph.HANGFLOWER
    override fun hue() = 0.4f
    override fun name() = "dream flower"
}

@Serializable
class Sunflower : Plant() {
    override val tag = Tag.THING_SUNFLOWER
    override fun glyph() = Glyph.SUNFLOWER
    override fun name() = "sunflower"
}

@Serializable
class Lightflower : Plant() {
    override val tag = Tag.THING_LIGHTFLOWER
    override fun glyph() = Glyph.SUNFLOWER
    override fun hue() = 0.7f
    override fun name() = "lightflower"
}

@Serializable
class Saguaro : Tree() {
    override val tag = Tag.THING_SAGUARO
    override fun glyph() = Glyph.CACTUS_BIG
    override fun name() = "saguaro cactus"
}

@Serializable
class Cholla : Plant() {
    override val tag = Tag.THING_CHOLLA
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun name() = "cholla cactus"
}

@Serializable
class Prickpear : Plant() {
    override val tag = Tag.THING_PRICKPEAR
    override fun glyph() = Glyph.CACTUS_SMALL
    override fun hue() = 0.3f
    override fun name() = "prickpear cactus"
}

@Serializable
class BalmMoss : Plant() {
    override val tag = Tag.THING_BALMMOSS
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun name() = "balm moss"
}

@Serializable
class LaceMoss : Plant() {
    override val tag = Tag.THING_LACEMOSS
    override fun glyph() = Glyph.HERB_PLANT_1
    override fun hue() = 0.8f
    override fun name() = "lace moss"
}

@Serializable
class WizardcapMushroom : Plant() {
    override val tag = Tag.THING_WIZARDCAPMUSHROOM
    override fun glyph() = Glyph.MUSHROOM
    override fun name() = "wizardcap mushroom"
}

@Serializable
class SpeckledMushroom : Plant() {
    override val tag = Tag.THING_SPECKLEDMUSHROOM
    override fun glyph() = Glyph.TOADSTOOLS
    override fun name() = "speckled mushrooms"
}

@Serializable
class BloodcapMushroom : Plant() {
    override val tag = Tag.THING_BLOODCAPMUSHROOM
    override fun glyph() = Glyph.TOADSTOOLS
    override fun hue() = 0.5f
    override fun name() = "bloodcap mushrooms"
}

@Serializable
class Foolsleaf : Plant() {
    override val tag = Tag.THING_FOOLSLEAF
    override fun glyph() = Glyph.SUCCULENT
    override fun name() = "foolsleaf"
}

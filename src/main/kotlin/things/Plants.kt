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
            holder?.also {
                if (it.temperature() >= fruitTemperatureMin()) {
                    bearFruit()
                }
            }
        }
    }

    open fun bearsFruit() = false
    open fun fruitTemperatureMin() = 60
    open fun spawnFruitChance() = 0.2f
    open fun fruit(): Thing? = null
    open fun nextFruitTurns() = Dice.range(2000, 4000).toLong()
    open fun bearFruit() {
        fruit()?.also { fruit ->
            fruit.moveTo(holder)
        }
        nextFruitTime = App.gameTime.time.toLong() + nextFruitTurns()
    }

    override fun onSpawn() {
        holder?.also {
            if (bearsFruit() && it.temperature() >= fruitTemperatureMin()) {
                if (Dice.chance(spawnFruitChance())) {
                    bearFruit()
                }
            }
        }
    }
}


@Serializable
sealed class Bush : Plant() {
}

@Serializable
class ThornBush : Bush() {
    override fun glyph() = Glyph.BUSH_2
    override fun name() = "thorn bush"
}

@Serializable
class SageBush : Bush() {
    override fun glyph() = Glyph.BUSH_1
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

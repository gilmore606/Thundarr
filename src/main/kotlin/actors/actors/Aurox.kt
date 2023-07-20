package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.Hide
import things.RawMeat
import util.Dice

@Serializable
class Aurox : NPC() {
    override fun glyph() = Glyph.CATTLE
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun name() = "aurox"
    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."
    override fun onSpawn() {
        initStats(14, 8, 5, 9, 8, 0, 0)
    }
    override fun skinArmor() = 1.5f

    override fun idleState() = IdleHerd(
        0.4f, 10, true,
        20.0f,
        6.0f,
    )

    override fun onDeath(corpse: Container?) {
        corpse?.also { RawMeat().moveTo(it) }
        if (Dice.chance(0.5f)) {
            corpse?.also { Hide().moveTo(it) }
        }
    }
}

@Serializable
class MuskOx : NPC() {
    override fun glyph() = Glyph.CATTLE
    override fun hue() = 4.3f
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun name() = "muskox"
    override fun description() = "Predictably, it smells awful."
    override fun onSpawn() {
        initStats(15, 9, 5, 10, 9, 1, 0)
    }
    override fun skinArmor() = 3f

    override fun idleState() = IdleHerd(
        0.4f, 15, true,
        20.0f,
        6.0f,
    )

    override fun onDeath(corpse: Container?) {
        corpse?.also { RawMeat().moveTo(it) }
        if (Dice.chance(0.8f)) {
            corpse?.also { Hide().moveTo(it) }
        }
    }
}

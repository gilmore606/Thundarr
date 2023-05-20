package actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.RawMeat
import util.Dice

@Serializable
sealed class GenericVoltelope : NPC() {
    override fun glyph() = Glyph.VOLTELOPE
    override fun name() = "voltelope"
    override fun shadowWidth() = 1.7f
    override fun isHuman() = false
    override fun onSpawn() {
        Strength.set(this, 7f)
        Speed.set(this, 13f)
        Brains.set(this, 7f)
    }

    override fun idleState() = IdleHerd(
        0.6f, 15, true,
        21.0f,
        6.0f,
    )

    open fun meatChance() = 1.0f
    override fun onDeath(corpse: Container?) {
        corpse?.also {
            if (Dice.chance(meatChance())) {
                RawMeat().moveTo(it)
            }
        }
    }
}

@Serializable
class Voltelope : GenericVoltelope() {
    override fun name() = "voltelope"
    override fun description() = "A large ruminant beast with two branching yellow horns."
}

@Serializable
class VoltelopeFawn : GenericVoltelope() {
    override fun name() = "voltelope fawn"
    override fun description() = "A small juvenile ruminant beast with two small yellow horns."
    override fun meatChance() = 0.6f
    override fun onSpawn() {
        Strength.set(this, 5f)
        Speed.set(this, 11f)
    }
}

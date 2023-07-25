package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.RawMeat
import things.Thing
import util.Dice

@Serializable
sealed class GenericVoltelope : NPC() {
    override fun glyph() = Glyph.VOLTELOPE
    override fun name() = "voltelope"
    override fun shadowWidth() = 1.7f
    override fun onSpawn() {
        initStats(8, 14, 6, 13, 8, 1, 2)
    }

    override fun idleState() = IdleHerd(
        0.6f, 15, true,
        21.0f,
        6.0f,
    )
    override fun unarmedWeapons() = setOf(hooves, horns)
    open fun meatChance() = 1.0f
    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        if (Dice.chance(meatChance())) add(RawMeat())
    }
}

@Serializable
class Voltelope : GenericVoltelope() {
    override fun name() = "voltelope"
    override fun description() = "A large ruminant beast with two branching yellow horns."
    override fun skinArmor() = 1f
    override fun unarmedDamage() = 3f
}

@Serializable
class VoltelopeFawn : GenericVoltelope() {
    override fun name() = "voltelope fawn"
    override fun description() = "A small juvenile ruminant beast with two small yellow horns."
    override fun meatChance() = 0.5f
    override fun onSpawn() {
        super.onSpawn()
        Dodge.set(this, 1)
        Speed.set(this, 12f)
    }
    override fun unarmedDamage() = 2f
}

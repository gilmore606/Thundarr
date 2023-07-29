package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.RawMeat
import things.Thing
import util.Dice

@Serializable
sealed class GenericTusker : NPC() {
    override fun glyph() = Glyph.PIG
    override fun shadowWidth() = 1.6f
    override fun shadowXOffset() = 0.2f
    override fun description() = "A large, stout porcine animal covered in wiry bristles, with a protruding snout."
    override fun onSpawn() {
        initStats(10, 9, 5, 8, 8, 1, 1)
    }
    override fun unarmedWeapons() = setOf(hooves, teeth)
    override fun skinArmor() = 1.0f
    open fun meatChance() = 1f

    override fun idleState() = IdleHerd(
        0.5f, 6, true,
        21.0f,
        7.0f,
    )

    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        if (Dice.chance(meatChance())) add(RawMeat())
    }
}

@Serializable
class Tusker : GenericTusker() {
    override val tag = Tag.TUSKER
    override fun name() = "tusker"
    override fun hpMax() = 22f
    override fun unarmedDamage() = 5f
}

@Serializable
class Tusklet : GenericTusker() {
    override val tag = Tag.TUSKLET
    override fun name() = "tusklet"
    override fun description() = "A juvenile pig creature covered in wiry bristles."
    override fun hpMax() = 14f
    override fun onSpawn() {
        super.onSpawn()
        Strength.set(this, 8f)
        Speed.set(this, 7f)
    }
    override fun unarmedDamage() = 3.5f
    override fun skinArmor() = 0f
    override fun meatChance() = 0.5f
}

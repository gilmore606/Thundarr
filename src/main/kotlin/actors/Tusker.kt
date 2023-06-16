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
sealed class GenericTusker : NPC() {
    override fun glyph() = Glyph.PIG
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun description() = "A large, stout porcine animal covered in wiry bristles, with a protruding snout."
    override fun onSpawn() {
        Strength.set(this, 10f)
        Speed.set(this, 9f)
        Brains.set(this, 5f)
    }
    override fun armorTotal() = 1.0f

    override fun idleState() = IdleHerd(
        0.5f, 6, true,
        21.0f,
        7.0f,
    )

    override fun onDeath(corpse: Container?) {
        corpse?.also {
            if (Dice.chance(0.7f)) {
                RawMeat().moveTo(it)
            }
        }
    }
}

@Serializable
class Tusker : GenericTusker() {
    override fun name() = "tusker"
}

@Serializable
class Tusklet : GenericTusker() {
    override fun name() = "tusklet"
    override fun description() = "A juvenile pig creature covered in wiry bristles."
    override fun onSpawn() {
        Strength.set(this, 8f)
        Speed.set(this, 9f)
        Brains.set(this, 4f)
    }
    override fun armorTotal() = 0f
    override fun onDeath(corpse: Container?) {
        corpse?.also {
            if (Dice.chance(0.4f)) {
                RawMeat().moveTo(it)
            }
        }
    }
}

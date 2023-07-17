package actors.actors

import actors.states.IdleDen
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.Hide
import things.RawMeat

@Serializable
class Grizzler : NPC() {
    override fun glyph() = Glyph.DEMONDOG
    override fun name() = "grizzler"
    override fun description() = "A large furry brown predator with glittering yellow eyes and large claws."
    override fun onSpawn() {
        Strength.set(this, 18f)
        Speed.set(this, 11f)
        Brains.set(this, 7f)
    }
    override fun skinArmor() = 2f

    override fun idleState() = IdleDen(
        0.4f, 12, true,
        22.0f,
        5.0f
    )

    override fun onDeath(corpse: Container?) {
        corpse?.also {
            RawMeat().moveTo(it)
            Hide().moveTo(it)
        }
    }
}

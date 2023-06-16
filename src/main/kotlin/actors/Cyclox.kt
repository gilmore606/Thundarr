package actors

import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.Hide
import things.RawMeat

@Serializable
class Cyclox : NPC() {
    override fun glyph() = Glyph.CYCLOX
    override fun name() = "cyclox"
    override fun description() = "A large spheroid ruminant with wrinkly gray skin, and one giant eye."
    override fun onSpawn() {
        Strength.set(this, 15f)
        Speed.set(this, 9f)
        Brains.set(this, 7f)
    }
    override fun armorTotal() = 3f

    override fun idleState() = IdleWander(0.5f)

    override fun onDeath(corpse: Container?) {
        corpse?.also {
            RawMeat().moveTo(it)
            Hide().moveTo(it)
        }
    }
}

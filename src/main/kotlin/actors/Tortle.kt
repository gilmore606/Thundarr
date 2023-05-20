package actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Tortle : NPC() {
    override fun glyph() = Glyph.TORTLE
    override fun shadowWidth() = 1.4f
    override fun name() = "tortle"
    override fun description() = "A large tortoise with a large raised head and sharp horns."
    override fun isHuman() = false
    override fun onSpawn() {
        Strength.set(this, 12f)
        Speed.set(this, 6f)
        Brains.set(this, 8f)
    }
    override fun armorTotal() = 3.5f

    override fun idleState() = IdleHerd(
        0.3f, 8, true,
        19.0f,
        6.0f
    )
}

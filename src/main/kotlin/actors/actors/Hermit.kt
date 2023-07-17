package actors.actors

import actors.states.IdleDen
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Hermit : NPC() {
    override fun glyph() = Glyph.PEASANT_WHITE_DARK
    override fun name() = "hermit"
    override fun description() = "A wizened old human in tattered rags."
    override fun isHuman() = true
    override fun onSpawn() {
        Strength.set(this, 9f)
        Speed.set(this, 10f)
        Brains.set(this, 12f)
    }

    override fun armorTotal() = 0f
    override fun idleState() = IdleDen(
        0.5f, 16, false,
        22.0f, 5.0f
    )
}

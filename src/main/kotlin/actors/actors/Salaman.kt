package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.ScalyHide

@Serializable
class Salaman : NPC() {
    override fun glyph() = Glyph.TAILMANDER
    override fun shadowWidth() = 1.4f
    override fun name() = "salaman"
    override fun description() = "A man-sized red salamander.  A curiously human face bobs at the end of its long neck."
    override fun canSwimShallow() = true
    override fun onSpawn() {
        Strength.set(this, 10f)
        Speed.set(this, 10f)
        Brains.set(this, 7f)
    }
    override fun skinArmor() = 1.5f

    override fun idleState() = IdleHerd(
        0.3f, 8, true,
        21.0f,
        7.0f
    )

    override fun onDeath(corpse: Container?) {
        corpse?.also { ScalyHide().moveTo(it) }
    }
}

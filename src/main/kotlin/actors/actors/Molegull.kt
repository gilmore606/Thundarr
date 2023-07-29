package actors.actors

import actors.animations.Jump
import actors.animations.Slide
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.XY

@Serializable
class Molegull : NPC() {
    override val tag = Tag.MOLEGULL
    override fun glyph() = Glyph.BLUE_BIRD
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun name() = "molegull"
    override fun description() = "An angry bird covered in fine blue fur."
    override fun hpMax() = 4f
    override fun onSpawn() {
        initStats(11, 9, 4, 10, 6, 1, 1)
    }

    override fun unarmedWeapon() = beak
    override fun unarmedDamage() = 3f

    override fun visualRange() = 10f
    override fun idleState() = IdleWander(0.6f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}

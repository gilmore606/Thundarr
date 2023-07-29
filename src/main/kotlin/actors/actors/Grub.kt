package actors.actors

import actors.abilities.Leap
import actors.animations.Jump
import actors.animations.Slide
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.XY

@Serializable
class Grub : NPC() {
    override val tag = Tag.GRUB
    override fun glyph() = Glyph.GRUB
    override fun shadowWidth() = 1.6f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun name() = "grub"
    override fun description() = "A huge yellowish insect grub."
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }
    override fun unarmedWeapon() = teeth
    override fun unarmedDamage() = 3f

    override fun visualRange() = 6f
    override fun idleState() = IdleWander(0.4f)
}

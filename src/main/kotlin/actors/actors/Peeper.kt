package actors.actors

import actors.animations.Slide
import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.GooGore
import render.tilesets.Glyph
import util.XY
import world.stains.Goo

@Serializable
class Peeper : NPC() {

    override val tag = Tag.PEEPER
    override fun glyph() = Glyph.FLOATING_EYE
    override fun name() = "peeper"
    override fun description() = "A floating eyeball.  Disgusting.  You wonder who's looking through it."
    override fun hpMax() = 3f
    override fun onSpawn() {
        initStats(4, 8, 6, 13, 4, 0, 1)
    }

    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun stepSpark(dir: XY) = null
    override fun stepSound(dir: XY) = null
    override fun animOffsetY() = super.animOffsetY() - Screen.sinBob * 0.06f
    override fun corpse() = null
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()

    override fun idleState() = IdleWander(0.6f)

}

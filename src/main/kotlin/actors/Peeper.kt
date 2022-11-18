package actors

import actors.actions.Action
import actors.actions.Wait
import actors.animations.Slide
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.GooGore
import render.tilesets.Glyph
import util.Dice
import util.XY
import world.stains.Goo

@Serializable
class Peeper : NPC() {

    override fun glyph() = Glyph.FLOATING_EYE
    override fun name() = "peeper"
    override fun description() = "A floating eyeball.  Disgusting.  You wonder who's looking through it."
    override fun onSpawn() {
        hpMax = 6f
        hp = 6f
        Strength.set(this, 4f)
        Speed.set(this, 8f)
        Brains.set(this, 6f)
        Dodge.set(this, 2f)
    }

    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun stepSpark(dir: XY) = null
    override fun stepSound(dir: XY) = null
    override fun animOffsetY() = super.animOffsetY() - Screen.sinBob * 0.06f
    override fun corpse() = null
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()

}

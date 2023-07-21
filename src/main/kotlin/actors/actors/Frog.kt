package actors.actors

import actors.animations.Jump
import actors.states.IdleWander
import render.tilesets.Glyph
import things.Clothing
import util.XY

class Frog : NPC() {

    override fun glyph() = Glyph.FROG
    override fun shadowWidth() = 1.2f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "frog"
    override fun description() = "A big slimy frog."
    override fun onSpawn() {
        hpMax = 6f
        initStats(9, 9, 4, 8, 6, 1, 0)
    }

    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
}

package actors.actors

import actors.animations.Slide
import actors.states.IdleWander
import render.tilesets.Glyph
import things.Clothing
import util.XY

class Beetle : NPC() {

    override fun glyph() = Glyph.PINCER_BEETLE
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.2f
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun name() = "pincer beetle"
    override fun description() = "A huge shiny purple beetle, with large pinching jaws."
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 3, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = mandibles
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.SHELL
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
}

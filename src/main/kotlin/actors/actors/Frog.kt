package actors.actors

import actors.abilities.Leap
import actors.animations.Jump
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.XY

@Serializable
class Frog : NPC() {

    override fun glyph() = Glyph.FROG
    override fun shadowWidth() = 1.2f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "frog"
    override fun description() = "A big slimy frog."
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }
    override fun canSwimShallow() = true

    override fun unarmedWeapon() = horns
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
    override fun initialAbilities() = setOf(Leap(4))
}

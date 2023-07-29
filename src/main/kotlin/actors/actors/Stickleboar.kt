package actors.actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.RawMeat

@Serializable
class Stickleboar : NPC() {
    override val tag = Tag.STICKLEBOAR
    override fun glyph() = Glyph.PORCUPINE
    override fun shadowWidth() = 1.3f
    override fun name() = "stickleboar"
    override fun description() = "A wild boar, its back covered in stiff spines."
    override fun canSwimShallow() = true
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(12, 12, 6, 13, 10, 4, 2)
    }
    override fun corpseMeats() = setOf(RawMeat())
    override fun unarmedWeapon() = hooves
    override fun unarmedDamage() = 4f
    override fun skinArmor() = 2f

    override fun visualRange() = 8f
    override fun canSeeInDark() = true
    override fun idleState() = IdleWander(0.4f)
}
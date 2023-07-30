package actors.actors

import actors.abilities.Steal
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.Hide

@Serializable
class Hyenaman : NPC() {
    override val tag = Tag.HYENAMAN
    override fun glyph() = Glyph.HYENAMAN
    override fun name() = "hyenaman"
    override fun description() = "A yellow-furred canine-faced humanoid with beady red eyes, clad in raw hides."
    override fun corpseMeats() = setOf(Hide())
    override fun makeAbilities() = setOf(Steal())
    override fun isSentient() = true
    override fun xpValue() = 25
    override fun hpMax() = 9f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = spear
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 10f
    override fun aggroRange() = 7f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}

package actors.actors

import actors.abilities.Regenerate
import actors.bodyparts.Bodypart
import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.Container
import things.RawMeat
import things.ScalyHide

@Serializable
class Salaman : NPC() {
    override val tag = Tag.SALAMAN
    override fun glyph() = Glyph.TAILMANDER
    override fun shadowWidth() = 1.4f
    override fun name() = "salaman"
    override fun description() = "A man-sized red salamander.  A curiously human face bobs at the end of its long neck."
    override fun makeBody() = Bodypart.quadruped()
    override fun makeAbilities() = setOf(Regenerate())
    override fun corpseMeats() = setOf(RawMeat(), ScalyHide())
    override fun canSwimShallow() = true
    override fun xpValue() = 40
    override fun hpMax() = 14f
    override fun onSpawn() {
        initStats(10, 10, 7, 9, 9, 1, 0)
    }
    override fun unarmedWeapons() = setOf(claws, teeth)
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.SCALES
    override fun skinArmor() = 1.5f

    override fun idleState() = IdleHerd(
        0.3f, 8, true,
        21.0f,
        7.0f
    )
}

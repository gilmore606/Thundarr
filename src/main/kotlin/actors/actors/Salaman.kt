package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.Container
import things.ScalyHide

@Serializable
class Salaman : NPC() {
    override fun glyph() = Glyph.TAILMANDER
    override fun shadowWidth() = 1.4f
    override fun name() = "salaman"
    override fun description() = "A man-sized red salamander.  A curiously human face bobs at the end of its long neck."
    override fun canSwimShallow() = true
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

    override fun corpseMeats() = setOf(ScalyHide())
}

package actors.actors

import actors.states.IdleDen
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*

@Serializable
class Grizzler : NPC() {
    override fun glyph() = Glyph.DEMONDOG
    override fun name() = "grizzler"
    override fun description() = "A large furry brown predator with glittering yellow eyes and large claws."
    override fun onSpawn() {
        initStats(17, 12, 7, 12, 12, 3, 1)
    }

    override fun unarmedWeapons() = setOf(teeth, claws)
    override fun unarmedDamage() = 8f
    override fun skinArmorMaterial() = Clothing.Material.FUR
    override fun skinArmor() = 2f

    override fun idleState() = IdleDen(
        0.4f, 12, true,
        22.0f,
        5.0f
    )

    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        add(RawMeat())
        add(FurHide())
    }
}

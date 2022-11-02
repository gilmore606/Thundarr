package things

import actors.stats.Stat
import actors.stats.skills.Dig
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.terrains.Terrain

@Serializable
sealed class Weapon : Gear() {
    override val slot = Slot.WEAPON
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."

    open fun skill(): Stat = Fight
    open fun canDig(terrainType: Terrain.Type): Boolean = false

}

@Serializable
class Axe : Weapon() {
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override fun description() = "A woodsman's axe.  Looks like it could chop more than wood.  I'm talking about flesh here."
}

@Serializable
class Pickaxe : Weapon() {
    override fun glyph() = Glyph.AXE
    override fun hue() = 5.2f
    override fun name() = "pickaxe"
    override fun description() = "A miner's pickaxe.  Looks like it could pick more than flesh.  I'm talking about stone here."
    override fun skill() = Dig
    override fun canDig(terrainType: Terrain.Type) = Terrain.get(terrainType).dataType == Terrain.Type.GENERIC_WALL
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Dig.tag] = 1f
    }
}

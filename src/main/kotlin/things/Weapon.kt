package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import world.terrains.Terrain

@Serializable
sealed class Weapon : Gear() {
    override val slot = Slot.WEAPON
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."

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
    override fun name() = "pickaxe"
    override fun description() = "A miner's pickaxe.  Looks like it could pick more than flesh.  I'm talking about stone here."
    override fun canDig(terrainType: Terrain.Type) = Terrain.get(terrainType).dataType == Terrain.Type.GENERIC_WALL
}

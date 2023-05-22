package actors

import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import render.tilesets.Glyph
import things.Container
import things.Hide
import things.RawMeat

class Grizzler : NPC() {
    override fun glyph() = Glyph.DEMONDOG
    override fun name() = "grizzler"
    override fun description() = "A large furry brown predator with glittering yellow eyes and large claws."
    override fun isHuman() = false
    override fun onSpawn() {
        Strength.set(this, 18f)
        Speed.set(this, 11f)
        Brains.set(this, 7f)
    }
    override fun armorTotal() = 2f

    override fun onDeath(corpse: Container?) {
        corpse?.also {
            RawMeat().moveTo(it)
            Hide().moveTo(it)
        }
    }
}

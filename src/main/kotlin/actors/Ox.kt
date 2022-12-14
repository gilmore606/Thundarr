package actors

import actors.actions.Action
import actors.actions.Bark
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Container
import things.RawMeat
import util.Dice

@Serializable
class Ox : NPC() {
    override fun glyph() = Glyph.CATTLE
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun name() = "ox"
    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."
    override fun isHuman() = false
    override fun onSpawn() {
        Strength.set(this, 14f)
        Speed.set(this, 8f)
        Brains.set(this, 5f)
    }
    override fun armorTotal() = 2.5f

    override fun onDeath(corpse: Container?) {
        corpse?.also { RawMeat().moveTo(it) }
    }
}

@Serializable
class MuskOx : NPC() {
    override fun glyph() = Glyph.CATTLE
    override fun hue() = 4.3f
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun name() = "musk ox"
    override fun description() = "Predictably, it smells awful."
    override fun isHuman() = false
    override fun onSpawn() {
        Strength.set(this, 15f)
        Speed.set(this, 10f)
        Brains.set(this, 6f)
    }

    override fun onDeath(corpse: Container?) {
        corpse?.also { RawMeat().moveTo(it) }
    }
}

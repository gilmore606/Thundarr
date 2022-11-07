package actors

import actors.actions.Action
import actors.actions.Bark
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Corpse
import things.Meat
import ui.input.Keyboard
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

    override fun pickAction(): Action {
        if (awareness == Awareness.AWARE && Dice.chance(0.1f)) {
            wander()?.also { return it }
        }
        if (Dice.chance(0.16f)) {
            entitiesSeen { it is Ox || it is MuskOx }.keys.randomOrNull()?.also { ox ->
                stepToward(ox)?.also { return it }
            }
        }
        if (Dice.chance(0.1f)) {
            return Bark(Speaker.SFX.MOO)
        }
        return super.pickAction()
    }

    override fun onDeath(corpse: Corpse) {
        Meat().moveTo(corpse)
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

    override fun pickAction(): Action {
        if (awareness == Awareness.AWARE && Dice.chance(0.5f)) {
            wander()?.also { return it }
        }
        if (Dice.chance(0.1f)) {
            return Bark(Speaker.SFX.MOO)
        }
        return super.pickAction()
    }

    override fun onDeath(corpse: Corpse) {
        Meat().moveTo(corpse)
    }
}

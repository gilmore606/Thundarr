package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Corpse
import things.Meat
import ui.input.Keyboard
import util.Dice

@Serializable
class Ox : NPC() {
    override fun glyph() = Glyph.CATTLE

    override fun name() = "ox"

    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."

    override fun pickAction(): Action {
        if (awareness == Awareness.AWARE && Dice.chance(0.5f)) {
            wander()?.also { return it }
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

    override fun name() = "musk ox"

    override fun description() = "Predictably, it smells awful."

    override fun pickAction(): Action {
        if (awareness == Awareness.AWARE && Dice.chance(0.5f)) {
            wander()?.also { return it }
        }
        return super.pickAction()
    }

    override fun onDeath(corpse: Corpse) {
        Meat().moveTo(corpse)
    }
}

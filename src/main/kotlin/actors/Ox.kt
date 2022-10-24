package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
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
}

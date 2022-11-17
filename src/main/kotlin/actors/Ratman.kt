package actors

import actors.actions.Action
import actors.actions.Wait
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Teeth
import util.Dice

@Serializable
class Ratman : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATMAN
    override fun name() = "rat-man"
    override fun description() = "A man-sized hunched gray rodent, its eyes glittering with hatred."
    override fun onSpawn() {
        hpMax = 15
        hp = 15
        Strength.set(this, 10f)
        Speed.set(this, 9f)
        Brains.set(this, 7f)
        Fight.set(this, 2f)
    }
    override fun meleeWeapon() = weapon

    override fun pickAction(): Action {
        if (Dice.chance(0.4f)) {
            wander()?.also { return it }
        }
        return Wait(1f)
    }
}

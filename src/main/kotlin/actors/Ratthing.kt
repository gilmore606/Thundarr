package actors

import actors.actions.Action
import actors.actions.Wait
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import render.tilesets.Glyph
import things.Teeth
import util.Dice

class Ratthing : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATTHING
    override fun name() = "rat-thing"
    override fun description() = "A dog-sized shaggy gray rodent.  Its red eyes are full of hatred."
    override fun onSpawn() {
        Strength.set(this, 8f)
        Speed.set(this, 8f)
        Brains.set(this, 3f)
        Dodge.set(this, 1f)
        Fight.set(this, 1f)
    }
    override fun meleeWeapon() = weapon

    override fun pickAction(): Action {
        if (Dice.chance(0.4f)) {
            wander()?.also { return it }
        }
        return Wait(1f)
    }
}

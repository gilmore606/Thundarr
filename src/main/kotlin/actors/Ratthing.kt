package actors

import actors.states.Attacking
import actors.states.Fleeing
import actors.states.State
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Teeth

@Serializable
class Ratthing : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATTHING
    override fun name() = "rat-thing"
    override fun description() = "A dog-sized shaggy gray rodent.  Its red eyes are full of hatred."
    override fun enterStateMsg(newState: State): String? = when (newState) {
        is Attacking -> "%Dn scurries toward you, baring its teeth!"
        else -> super.enterStateMsg(newState)
    }
    override fun onSpawn() {
        hpMax = 10f
        hp = 10f
        Strength.set(this, 8f)
        Speed.set(this, 8f)
        Brains.set(this, 3f)
        Dodge.set(this, 1f)
        Fight.set(this, 1f)
    }
    override fun meleeWeapon() = weapon
    override fun corpse() = null
    override fun talkSound(actor: Actor) = Speaker.SFX.RAT

    override fun isHostile() = true

}

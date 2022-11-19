package actors

import actors.actions.Action
import actors.actions.Wait
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Teeth
import util.Dice

@Serializable
class Ratlord : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATLORD
    override fun name() = "rat-lord"
    override fun description() = "A man-sized hunched gray rodent wearing bronze armor.  It has an air of arrogant authority."
    override fun onSpawn() {
        hpMax = 25f
        hp = 25f
        Strength.set(this, 11f)
        Speed.set(this, 10f)
        Brains.set(this, 9f)
        Fight.set(this, 3f)
    }
    override fun meleeWeapon() = weapon
    override fun talkSound(actor: Actor) = Speaker.SFX.RAT

}

package actors.actors

import actors.states.IdleInRoom
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Thrall(
    val wizardName: String
) : NPC() {
    override fun glyph() = Glyph.THRALL
    override fun name() = "thrall"
    override fun description() = "A pitiful human, enslaved by magic bonds to $wizardName.  ${gender().pp.capitalize()} eyes beg you for rescue."
    override fun isHuman() = true
    override fun onSpawn() {
        hpMax = 10f
        hp = 10f
        Strength.set(this, 7f)
        Speed.set(this, 9f)
        Brains.set(this, 10f)
    }

    override fun commentLines() = listOf(
        "We slave all day for that wizard.  And for what?",
        "Your bonds are gone!  You've got to get out of here, before they see!",
        "Are you escaping?  Take me with you...",
        "Where are your magical bonds?  How did you get free?",
        "If you get out of this place, come back for us.",
        "The wizard is so cruel to us.  How can we go on this way?"
    )
    override fun talkSound(actor: Actor) = Speaker.SFX.VOICE_MALEHIGH
    override fun meetPlayerMsg() = this.dnamec() + " says, \"" + commentLines().random() + "\""

    override fun idleState() = IdleInRoom()
}

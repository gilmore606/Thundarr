package actors

import actors.actions.Action
import actors.actions.Wait
import actors.states.Idle
import actors.states.IdleInRoom
import actors.states.Looting
import actors.states.Seeking
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Cheese
import things.Teeth
import things.Thing
import util.Dice

@Serializable
class Ratman(
    val wizardName: String
) : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATMAN
    override fun name() = "rat-man"
    override fun description() = "A man-sized hunched gray rodent. Its eyes glitter with hatred."
    override fun onSpawn() {
        hpMax = 15f
        hp = 15f
        Strength.set(this, 10f)
        Speed.set(this, 9f)
        Brains.set(this, 7f)
        Fight.set(this, 2f)
    }
    override fun meleeWeapon() = weapon
    override fun isHostile() = true
    override fun idleState() = IdleInRoom()

    override fun converseLines() = listOf(
        "Ttthhhee rebelliouthh one!  Killl him!",
        "You dare to defy $wizardName?  Die!",
        "The ethhcaped thhlave!  He itthh here!",
        "The penalty for ethcape ith death!"
    )
    override fun talkSound(actor: Actor) = Speaker.SFX.RAT
    override fun meetPlayerMsg() = this.dnamec() + " says, \"" + converseLines().random() + "\""
    fun findCheeseLines() = listOf(
        "Arrgg cheethe!  I can't rethithht!",
        "What?!  Cheethe rationth!",
        "Unclaimed cheethe!  I'll take that."
    )


    override fun considerState() {
        if (state !is Looting) {
            val cheeses = entitiesSeen { it is Cheese }
            if (cheeses.isNotEmpty()) {
                val cheese = cheeses.keys.random()
                pushState(Looting(cheese.xy()!!, (cheese as Thing).thingTag()))
                say(findCheeseLines().random())
            }
        }
    }
}

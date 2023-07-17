package actors.actors

import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Entity

@Serializable
class Wolfman : NPC() {

    override fun glyph() = Glyph.WOLFMAN
    override fun name() = "wolfman"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A snarling wolf, on two legs...no, a man with...Demon dogs!!"
    override fun onSpawn() {
        Strength.set(this, 12f)
        Speed.set(this, 12f)
        Brains.set(this, 6f)
        Fight.set(this, 4f)
    }
}

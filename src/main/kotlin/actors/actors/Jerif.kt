package actors.actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Jerif : NPC() {
    override fun name() = "jerif"
    override fun glyph() = Glyph.JERIF
    override fun shadowWidth() = 1.7f
    override fun description() = "A tall long-necked equine ruminant."
    override fun idleState() = IdleWander(0.35f)
}

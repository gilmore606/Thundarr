package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class Clothing : Gear() {
    override fun equipSelfMsg() = "You put on your %d."
    override fun unequipSelfMsg() = "You take off your %d."
    override fun equipOtherMsg() = "%Dn puts on %id."
    override fun unequipOtherMsg() = "%Dn takes off %p %d."
    override fun weight() = 1f

    open fun armor() = 0f
}

@Serializable
class HardHat : Clothing() {
    override fun glyph() = Glyph.HARD_HAT
    override fun name() = "hard hat"
    override fun description() = "A dented yellow plastic helmet labelled 'BEN LLI  ONSTRU TION'."
    override val slot = Slot.HEAD
    override fun weight() = 0.5f
    override fun armor() = 1f
}

@Serializable
class HornetHelmet : Clothing() {
    override fun glyph() = Glyph.HORNED_HAT
    override fun name() = "horned helmet"
    override fun description() = "A fur-lined metal skullcap affixed with two large curved ox horns."
    override val slot = Slot.HEAD
    override fun weight() = 1.8f
    override fun armor() = 2f
}

@Serializable
class RiotHelmet : Clothing() {
    override fun glyph() = Glyph.HELMET
    override fun name() = "riot helmet"
    override fun description() = "A shiny blue-black helmet made of some high tech material."
    override val slot = Slot.HEAD
    override fun weight() = 0.7f
    override fun armor() = 4f
}
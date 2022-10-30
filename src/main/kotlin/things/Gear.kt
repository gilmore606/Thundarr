package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.aOrAn

@Serializable
sealed class Gear : Portable() {

    enum class Slot(val duration: Float, val title: String, val where: String, val verb: String, val unverb: String) {
        WEAPON(0.5f, "primary", "as weapon", "wield", "put away"),
        SECONDARY(0.5f, "secondary", "as secondary", "ready", "unready"),
        HEAD(0.6f, "head", "on head", "wear", "remove"),
        TORSO(1.5f, "torso", "on torso", "wear", "remove"),
        LEGS(2.0f, "legs", "on legs", "wear", "remove"),
        FEET(2.0f, "feet", "on feet", "wear", "remove")
    }

    companion object {
        val slots = listOf(Slot.WEAPON, Slot.SECONDARY, Slot.HEAD, Slot.TORSO, Slot.LEGS, Slot.FEET)
    }

    var equipped = false
    abstract val slot: Slot

    open fun equipSelfMsg() = "You put on your %d."
    open fun equipOtherMsg() = "%Dn puts on %p %d."
    open fun unequipSelfMsg() = "You take off your %d."
    open fun unequipOtherMsg() = "%Dn takes off %p %d."

    override fun listName() = if (equipped) super.listName() + " (equipped)" else super.listName()

    override fun onMoveTo(from: ThingHolder?, to: ThingHolder?) {
        this.equipped = false
        if (from is Actor && from != to && from.gear[slot] == this) {
            from.gear[slot] = null
        }
    }

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        if (equipped && holder is Actor) {
            holder.gear[slot] = this
        }
    }

    override fun uses(): Set<Use> {
        val uses = mutableSetOf<Use>()
        val current = App.player.equippedOn(slot)
        if (current == this) {
            uses.add(
                Use(slot.unverb + " " + this.name(), 0f,
                    canDo = { actor -> this in actor.contents },
                    toDo = { actor, level ->
                        actor.unequipGear(this)
                    })
            )
        } else if (current != null) {
            uses.add(
                Use(current.slot.unverb + " " + current.name() + " and " + slot.verb + " " + name(), 0f,
                    canDo = { actor -> this in actor.contents },
                    toDo = { actor, level ->
                        actor.equipGear(this)
                    })
            )
        } else {
            uses.add(
                Use(slot.verb + " " + this.name() + " " + slot.where, 0f,
                    canDo = { actor -> this in actor.contents },
                    toDo = { actor, level ->
                        actor.equipGear(this)
                    }
                )
            )
        }
        return uses
    }
}

@Serializable
sealed class Weapon : Gear() {
    override val slot = Slot.WEAPON
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."
}

@Serializable
class Axe : Weapon() {
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override fun description() = "A woodsman's axe.  Looks like it could chop more than wood.  I'm talking about living creatures here."
    override val kind = Kind.AXE
}

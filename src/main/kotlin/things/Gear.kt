package things

import actors.Actor
import actors.stats.Stat
import actors.statuses.StatEffector
import kotlinx.serialization.Serializable
import util.toEnglishList

@Serializable
sealed class Gear : Portable(), StatEffector {

    enum class Slot(val duration: Float, val title: String, val where: String, val verb: String, val unverb: String) {
        MELEE(0.5f, "primary", "as weapon", "wield", "put away"),
        RANGED(0.5f, "secondary", "as secondary", "ready", "unready"),
        HEAD(0.6f, "head", "on head", "wear", "remove"),
        NECK(1.0f, "neck", "around neck", "wear", "remove"),
        HANDS(1.5f, "hands", "on hands", "wear", "remove"),
        TORSO(1.5f, "torso", "on torso", "wear", "remove"),
        LEGS(2.0f, "legs", "on legs", "wear", "remove"),
        FEET(2.0f, "feet", "on feet", "wear", "remove")
    }

    companion object {
        val slots = listOf(Slot.MELEE, Slot.RANGED, Slot.HEAD, Slot.NECK, Slot.HANDS, Slot.TORSO, Slot.LEGS, Slot.FEET)
    }

    var equipped = false
    var known = false
    abstract val slot: Slot

    open fun equipSelfMsg() = "You put on your %d."
    open fun equipOtherMsg() = "%Dn puts on %p %d."
open fun unequipSelfMsg() = "You take off your %d."
    open fun unequipOtherMsg() = "%Dn takes off %p %d."

    override fun listTag() = if (equipped) "(equipped)" else ""

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

    open fun onEquip(actor: Actor) { known = true }
    open fun onUnequip(actor: Actor) { }

    override fun toolbarName() = (if (equipped) slot.unverb else slot.verb) + " " + this.name()
    override fun toolbarUseTag() = if (equipped) UseTag.UNEQUIP else UseTag.EQUIP

    override fun uses() = mapOf(
        UseTag.EQUIP to Use(slot.verb + " " + name(), slot.duration,
            canDo = { actor -> !this.equipped && this in actor.contents },
            toDo = { actor, level -> actor.equipGear(this) }),
        UseTag.UNEQUIP to Use(slot.unverb + " " + name(), slot.duration,
            canDo = { actor -> this.equipped && this in actor.contents },
            toDo = { actor, level -> actor.unequipGear(this) })
    )

    override fun statEffects() = defaultStatEffects
    protected val defaultStatEffects = mapOf<Stat.Tag, Float>()

    override fun examineInfo(): String {
        if (!known) return super.examineInfo()
        val effects = statEffects()
        if (effects.isEmpty()) return super.examineInfo()
        val helps = ArrayList<String>()
        val hurts = ArrayList<String>()
        effects.forEach { (tag, value) ->
            if (value > 0f) helps.add(Stat.get(tag).verb()) else hurts.add(Stat.get(tag).verb())
        }
        var info = slot.verb.capitalize() + "ing this seems to "
        if (hurts.isNotEmpty()) {
            info += "interfere with " + hurts.toEnglishList(false)
            if (helps.isNotEmpty()) info += " but "
        }
        if (helps.isNotEmpty()) {
            info += "help with " + helps.toEnglishList(false)
        }
        info += "."
        return info
    }
}

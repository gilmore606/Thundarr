package things

import actors.actors.Actor
import actors.stats.Stat
import actors.statuses.StatEffector
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.toEnglishList

@Serializable
sealed class Gear : Portable(), StatEffector {

    enum class Slot(val duration: Float, val title: String, val where: String,
                    val verb: String, val unverb: String, val equippedTag: String,
                    val drawOrder: Int, val coldProtection: Float, val heatProtection: Float, val weatherProtection: Float) {
        MELEE(0.5f, "melee", "as melee", "wield", "put away", "ready",
            1, 0f, 0f, 0f),
        RANGED(0.5f, "ranged", "as ranged", "ready", "unready", "ready",
            2, 0f, 0f, 0f),
        HEAD(0.6f, "head", "on head", "wear", "remove", "worn",
            3, 0.3f, 0.3f, 0.4f),
        NECK(1.0f, "neck", "around neck", "wear", "remove", "worn",
            4, 0.3f, 0f, 0f),
        HANDS(1.5f, "hands", "on hands", "wear", "remove", "worn",
            5, 0.3f, 0f, 0f),
        TORSO(1.5f, "torso", "on torso", "wear", "remove", "worn",
            6, 0.3f, 0.2f, 0.5f),
        CLOAK(1.0f, "cloak", "around body", "wear", "remove", "worn",
            0, 0.5f, 0.6f, 0.8f),
        LEGS(2.0f, "legs", "on legs", "wear", "remove", "worn",
            7, 0.3f, 0f, 0.3f),
        FEET(2.0f, "feet", "on feet", "wear", "remove", "worn",
            8, 0.3f, 0f, 0.3f),
    }

    class GlyphTransform(
        val glyph: Glyph,
        val x: Float = 0f,
        val y: Float = 0f,
        val rotate: Boolean = false
    )

    companion object {
        val slots = listOf(Slot.MELEE, Slot.RANGED, Slot.HEAD, Slot.NECK, Slot.HANDS, Slot.TORSO, Slot.CLOAK, Slot.LEGS, Slot.FEET)
    }

    var equipped = false
    var known = false
    abstract val slot: Slot

    open fun equipSelfMsg() = "You put on your %d."
    open fun equipOtherMsg() = "%Dn puts on %p %d."
    open fun unequipSelfMsg() = "You take off your %d."
    open fun unequipOtherMsg() = "%Dn takes off %p %d."

    open fun glyphTransform(): GlyphTransform? = null

    override fun listTag() = if (equipped) "(${slot.equippedTag})" else ""

    override fun onDropping(actor: Actor, dest: ThingHolder) {
        this.equipped = false
        if (actor.gear[slot] == this) {
            actor.setGearSlot(slot, null)
        }
        this.onUnequip(actor)
    }

    override fun onMoveTo(from: ThingHolder?, to: ThingHolder?) {
        this.equipped = false
        if (from is Actor && from != to && from.gear[slot] == this) {
            from.setGearSlot(slot, null)
        }
    }

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        if (equipped && holder is Actor) {
            holder.setGearSlot(slot, this)
        }
    }

    open fun onEquip(actor: Actor) { known = true }
    open fun onUnequip(actor: Actor) { }

    override fun toolbarName() = (if (equipped) slot.unverb else slot.verb) + " " + this.name()
    override fun toolbarUseTag() = if (equipped) UseTag.UNEQUIP else UseTag.EQUIP

    override fun uses() = super.uses().apply {
        this[UseTag.EQUIP] = Use(slot.verb + " " + name(), slot.duration,
            canDo = { actor, x, y, targ -> !targ && !this@Gear.equipped && isHeldBy(actor) },
            toDo = { actor, level, x, y -> actor.equipGear(this@Gear) })
        this[UseTag.UNEQUIP] = Use(slot.unverb + " " + name(), slot.duration,
            canDo = { actor, x, y, targ -> !targ && this@Gear.equipped && isHeldBy(actor) },
            toDo = { actor, level, x, y -> actor.unequipGear(this@Gear) })
    }

    override fun statEffects() = defaultStatEffects
    protected val defaultStatEffects = mapOf<Stat.Tag, Float>()

    override fun category() = Category.GEAR

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

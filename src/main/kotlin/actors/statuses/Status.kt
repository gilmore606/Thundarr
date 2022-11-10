package actors.statuses

import actors.Actor
import actors.Player
import actors.actions.*
import actors.actions.processes.WalkTo
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import ui.panels.Console
import util.Dice
import util.toEnglishList

@Serializable
sealed class Status : StatEffector {

    enum class TagColor { NORMAL, GOOD, GREAT, BAD, FATAL }
    companion object {
        val tagColors = mapOf(
            TagColor.NORMAL to Color(0.6f, 0.6f, 0.6f, 1f),
            TagColor.GOOD to Color(0f, 0.9f, 0f, 1f),
            TagColor.GREAT to Color(0.2f, 0.2f, 1f, 1f),
            TagColor.BAD to Color(1f, 1f, 0f, 1f),
            TagColor.FATAL to Color(1f, 0f, 0f, 1f),
        )
    }

    var done = false

    abstract val tag: Tag
    enum class Tag {
        WIRED, DAZED, BURDENED, ENCUMBERED, SATIATED, HUNGRY, STARVING,
        STUNNED, ASLEEP, BANDAGED, SICK
    }

    abstract fun description(): String
    open fun panelTag(): String = ""
    open fun panelTagColor(): Color = tagColors[TagColor.NORMAL]!!
    open fun statusGlyph(actor: Actor): Glyph? = null
    open fun proneGlyph(): Boolean = false

    override fun statEffects() = defaultStatEffects
    protected val defaultStatEffects = mapOf<Stat.Tag, Float>()

    open fun onAddStack(actor: Actor, added: Status) { }

    open fun advanceTime(actor: Actor, delta: Float) { }

    open fun onAdd(actor: Actor) {
        Console.sayAct(onAddMsg(), onAddOtherMsg(), actor)
    }
    open fun onAddMsg() = ""
    open fun onAddOtherMsg() = ""

    open fun onRemove(actor: Actor) {
        Console.sayAct(onRemoveMsg(), onRemoveOtherMsg(), actor)
    }
    open fun onRemoveMsg() = ""
    open fun onRemoveOtherMsg() = ""

    open fun preventedAction(action: Action, actor: Actor) = false

    open fun panelInfo(): String {
        val effects = statEffects()
        if (effects.isEmpty()) return ""
        var info = ""
        val helps = ArrayList<String>()
        val hurts = ArrayList<String>()
        effects.forEach { (tag, value) ->
            if (value > 0f) helps.add(Stat.get(tag).name) else hurts.add(Stat.get(tag).name)
        }
        if (helps.isNotEmpty()) {
            info += "Boosts " + helps.toEnglishList(false)
        }
        if (hurts.isNotEmpty()) {
            info += (if (info.isEmpty()) "Reduces " else ", reduces ") + hurts.toEnglishList(false)
        }
        info += "."
        return info
    }
}

@Serializable
class Encumbered() : Status() {
    override val tag = Tag.ENCUMBERED
    override fun name() = "encumbered"
    override fun description() = "All your stuff is weighing you down, making you slow."
    override fun panelTag() = "enc"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Speed.tag to -2f,
        Dodge.tag to -2f
    )
}

@Serializable
class Burdened() : Status() {
    override val tag = Tag.BURDENED
    override fun name() = "burdened"
    override fun description() = "You're carrying as much as you can, making you very slow."
    override fun panelTag() = "burd"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Speed.tag to -4f,
        Dodge.tag to -3f
    )
}

@Serializable
class Satiated() : Status() {
    override val tag = Tag.SATIATED
    override fun name() = "satiated"
    override fun description() = "Couldn't eat another bite, really."
    override fun panelTag() = "sat"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun onAddMsg() = "Oof, you're full."
    override fun statEffects() = mapOf(
        Speed.tag to -1f
    )

    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Use && action.useTag == Thing.UseTag.CONSUME) {
            if (actor is Player) Console.say("You feel too full to eat or drink anything.")
            return true
        }
        return false
    }
}

@Serializable
class Hungry() : Status() {
    override val tag = Tag.HUNGRY
    override fun name() = "hungry"
    override fun description() = "Lack of calories is making you weak and foggy."
    override fun panelTag() = "hung"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -1f,
        Speed.tag to -1f
    )
}

@Serializable
class Starving() : Status() {
    override val tag = Tag.STARVING
    override fun name() = "starving"
    override fun description() = "Lack of calories is taking its toll.  You'll die soon without food."
    override fun panelTag() = "starv"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Strength.tag to -2f,
        Brains.tag to -2f,
        Speed.tag to -2f
    )
    override fun advanceTime(actor: Actor, delta: Float) {
        if (Dice.chance(0.02f * delta)) {
            Console.say("You're starving to death!")
            actor.receiveDamage(1f, internal = true)
        }
    }
}

@Serializable
class Asleep() : Status() {
    override val tag = Tag.ASLEEP
    override fun name() = "asleep"
    override fun description() = "Sleeping gives your wounds a chance to heal and your spirit to renew."
    override fun panelTag() = "zzz"
    override fun panelTagColor() = tagColors[TagColor.NORMAL]!!
    override fun statusGlyph(actor: Actor) = Glyph.SLEEP_ICON
    override fun proneGlyph() = true
    override fun statEffects() = mapOf(
        Speed.tag to -10f
    )
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action !is Sleep) {
            if (actor is Player) Console.say("You can't do anything in your sleep.")
            return true
        }
        return false
    }
}

@Serializable
sealed class TimeStatus : Status() {
    private var addTime = 0.0
    private var turnsLeft = 0f

    open fun duration() = 8f
    open fun maxDuration() = duration()  // override to enable re-up dosing

    override fun onAdd(actor: Actor) {
        addTime = App.time
        turnsLeft = duration()
        super.onAdd(actor)
    }

    override fun advanceTime(actor: Actor, delta: Float) {
        if (App.time > (addTime + turnsLeft)) {
            done = true
        }
    }

    override fun onAddStack(actor: Actor, added: Status) {
        turnsLeft += (added as TimeStatus).duration()
        Console.sayAct(onStackMsg(), onStackOtherMsg(), actor)
    }
    open fun onStackMsg() = ""
    open fun onStackOtherMsg() = ""
}

@Serializable
class Bandaged(val quality: Float) : TimeStatus() {
    override val tag = Tag.BANDAGED
    override fun name() = "bandaged"
    override fun description() = "Your wounds are bound and cleaned to assist healing."
    override fun panelTag() = "band"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun duration() = 500f + quality * 4f
    override fun maxDuration() = 800f
    override fun advanceTime(actor: Actor, delta: Float) {
        super.advanceTime(actor, delta)

    }
}

@Serializable
class Sick(): TimeStatus() {
    override val tag = Tag.SICK
    override fun name() = "sick"
    override fun description() = "You're too nauseous to eat."
    override fun onAddMsg() = "Ugh, you must have eaten something bad.  You feel sick."
    override fun onRemoveMsg() = "Your stomach feels better.  You could probably eat something."
    override fun panelTag() = "sick"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -1f
    )
    override fun duration() = Dice.float(40f, 80f)
    override fun maxDuration() = 300f
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Use && action.useTag == Thing.UseTag.CONSUME) {
            if (actor is Player) Console.say("You feel too nauseous to eat or drink anything.")
            return true
        }
        return false
    }
}

@Serializable
class Stunned() : TimeStatus() {
    override val tag = Tag.STUNNED
    override fun name() = "stunned"
    override fun description() = "You're too stunned to move or attack."
    override fun onAddMsg() = "You're stunned!"
    override fun onAddOtherMsg() = "%Dn looks stunned!"
    override fun panelTag() = "stun"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Speed.tag to -4f,
        Dodge.tag to -8f
    )
    override fun duration() = 1f
    override fun maxDuration() = 2f
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Move || action is WalkTo || action is Attack || action is Throw) {
            if (actor is Player) Console.say("You're too stunned to " + action.name() + "!")
            return true
        }
        return false
    }
}

@Serializable
class Wired : TimeStatus() {
    override val tag = Tag.WIRED
    override fun name() = "wired"
    override fun description() = "Stimulant drugs drive your reflexes to extreme speed."
    override fun panelTag() = "wire"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun onAddMsg() = "Your skin vibrates and your pupils dilate.  You feel speedy."
    override fun onAddOtherMsg() = "%Dn's movements speed up."
    override fun onRemoveMsg() = "You feel your nerves relax and slow back down."
    override fun onRemoveOtherMsg() = "%Dn's movements slow down to normal."
    override fun onStackMsg() = "Ahh...that should keep the party going."
    override fun statEffects() = mapOf(
        Speed.tag to 4f,
        Fight.tag to 1f
    )
    override fun duration() = 10f
    override fun maxDuration() = 20f
}

@Serializable
class Dazed : TimeStatus() {
    override val tag = Tag.DAZED
    override fun name() = "dazed"
    override fun description() = "A minor concussion makes it hard to think or move for a short time."
    override fun panelTag() = "daze"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun onAddMsg() = "You stagger, dazed."
    override fun onAddOtherMsg()  = "%Dn staggers!"
    override fun onRemoveMsg() = "You shake out of your daze."
    override fun onRemoveOtherMsg() = "%Dn shakes out of %p daze."
    override fun onStackMsg() = "Whooaaa!"
    override fun statEffects() = mapOf(
        Speed.tag to -2f,
        Brains.tag to -4f
    )
    override fun duration() = 3f
    override fun maxDuration() = 5f
}

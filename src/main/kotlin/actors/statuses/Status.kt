package actors.statuses

import actors.actors.Actor
import actors.actors.NPC
import actors.actors.Player
import actors.actions.*
import actors.actions.WalkTo
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
import world.journal.GameTime
import java.lang.Float.min

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
        BURDENED, ENCUMBERED,
        WET, COLD, FREEZING, HOT, HEATSTROKE,
        SATIATED, HUNGRY, STARVING,
        WIRED, DAZED, BLEEDING, STUNNED, ASLEEP, BANDAGED, SICK, AFRAID
    }

    abstract fun description(): String
    open fun panelTag(): String = ""
    open fun panelTagColor(): Color = tagColors[TagColor.NORMAL]!!
    open fun statusGlyph(actor: Actor): Glyph? = null
    open fun proneGlyph(): Boolean = false

    override fun statEffects() = defaultStatEffects
    protected val defaultStatEffects = mapOf<Stat.Tag, Float>()

    open fun preventVision() = false
    open fun preventActiveDefense() = false
    open fun comfort() = 0f
    open fun calorieBurnMod() = 1f // 1f = unchanged

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

    open fun considerState(npc: NPC) { }

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
sealed class TimeStatus : Status() {
    protected var addTime = 0.0
    var turnsLeft = 0f

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

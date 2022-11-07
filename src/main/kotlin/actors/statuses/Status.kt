package actors.statuses

import actors.Actor
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable
import ui.panels.Console
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
    enum class Tag { WIRED, DAZED, BURDENEND, ENCUMBERED }

    abstract fun description(): String
    open fun panelTag(): String = ""
    open fun panelTagColor(): Color = tagColors[TagColor.NORMAL]!!

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
    override val tag = Tag.BURDENEND
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

package actors.statuses

import actors.Actor
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.skills.Fight
import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable
import ui.panels.Console

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
    enum class Tag { WIRED }

    open fun panelTag(): String = ""
    open fun panelTagColor(): Color = tagColors[TagColor.NORMAL]!!

    override fun statEffects() = mapOf<Stat.Tag, Float>()

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
    override fun panelTag() = "wired"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun onAddMsg() = "Your skin vibrates and your pupils dilate.  You feel speedy."
    override fun onRemoveMsg() = "You feel your nerves relax and slow back down."
    override fun onStackMsg() = "Ahh...that should keep the party going."
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Speed.tag] = 4f
        this[Fight.tag] = 1f
    }
    override fun duration() = 10f
    override fun maxDuration() = 20f
}

package actors.statuses

import actors.actions.Action
import actors.actions.Sleep
import actors.actors.Actor
import actors.actors.Player
import actors.stats.Speed
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console

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
    override fun preventVision() = true
    override fun preventActiveDefense() = true
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action !is Sleep) {
            if (actor is Player) Console.say("You can't do anything in your sleep.")
            return true
        }
        return false
    }
}

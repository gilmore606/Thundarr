package actors.actions

import actors.Actor
import actors.Player
import things.Gear
import ui.panels.ConsolePanel
import world.Level

class Unequip(
    private val gear: Gear
) : Action(gear.slot.duration) {

    override fun execute(actor: Actor, level: Level) {
        if (gear.holder == actor) {
            gear.equipped = false
            actor.gear[gear.slot] = null
            if (actor is Player) {
                ConsolePanel.say(gear.unequipSelfMsg())
            } else {
                ConsolePanel.announce(level, gear.holder!!.xy.x, gear.holder!!.xy.y, ConsolePanel.Reach.VISUAL, gear.unequipOtherMsg(actor))
            }
        }
    }

}

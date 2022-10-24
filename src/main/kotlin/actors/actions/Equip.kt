package actors.actions

import actors.Actor
import actors.Player
import things.Gear
import ui.panels.ConsolePanel
import world.Level

class Equip(
    private val gear: Gear
) : Action(gear.slot.duration) {

    override fun execute(actor: Actor, level: Level) {
        if (gear.holder == actor) {
            gear.equipped = true
            actor.gear[gear.slot] = gear
            if (actor is Player) {
                ConsolePanel.say(gear.equipSelfMsg())
            } else {
                ConsolePanel.announce(level, gear.holder!!.xy.x, gear.holder!!.xy.y, ConsolePanel.Reach.VISUAL, gear.equipOtherMsg(actor))
            }
        }
    }

}

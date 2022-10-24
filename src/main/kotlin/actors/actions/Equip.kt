package actors.actions

import actors.Actor
import actors.Player
import things.Gear
import ui.panels.Console
import world.Level

class Equip(
    private val gear: Gear
) : Action(gear.slot.duration) {

    override fun execute(actor: Actor, level: Level) {
        if (gear.holder == actor) {
            gear.equipped = true
            actor.gear[gear.slot] = gear
            Console.sayAct(gear.equipSelfMsg(), gear.equipOtherMsg(), actor, gear)
        }
    }

}

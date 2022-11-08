package actors.actions

import actors.Actor
import things.Gear
import ui.panels.Console
import world.level.Level

class Unequip(
    private val gear: Gear
) : Action(gear.slot.duration) {
    override fun name() = "unequip gear"

    override fun execute(actor: Actor, level: Level) {
        if (gear.holder == actor) {
            gear.equipped = false
            actor.gear[gear.slot] = null
            actor.onUnequip(gear)
            gear.onUnequip(actor)
            Console.sayAct(gear.unequipSelfMsg(), gear.unequipOtherMsg(), actor, gear)
        }
    }

}

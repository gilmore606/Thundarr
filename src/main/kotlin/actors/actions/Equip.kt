package actors.actions

import actors.Actor
import things.Gear
import ui.panels.Console
import world.level.Level

class Equip(
    private val gear: Gear
) : Action(gear.slot.duration) {
    override fun name() = "equip gear"

    override fun canQueueFor(actor: Actor): Boolean {
        if (gear.holder == actor) {
            val currentGear = actor.equippedOn(gear.slot)
            if (currentGear != null && currentGear != gear) {
                actor.queue(Unequip(currentGear))
            }
            return currentGear != gear
        }
        return false
    }

    override fun execute(actor: Actor, level: Level) {
        gear.equipped = true
        actor.gear[gear.slot] = gear
        actor.onEquip(gear)
        gear.onEquip(actor)
        Console.sayAct(gear.equipSelfMsg(), gear.equipOtherMsg(), actor, gear)
    }

}

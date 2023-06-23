package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import things.Gear
import things.Thing
import ui.panels.Console
import world.level.Level

@Serializable
class Equip(
    private val gearKey: Thing.Key
) : Action(2f) {
    override fun name() = "equip gear"

    override fun canQueueFor(actor: Actor): Boolean {
        gearKey.getThing(actor.level!!)?.also { gear ->
            if (gear is Gear) {
                if (gear.holder == actor) {
                    val currentGear = actor.equippedOn(gear.slot)
                    if (currentGear != null && currentGear != gear) {
                        actor.queue(Unequip(currentGear.getKey()))
                    }
                    return currentGear != gear
                }
                return false
            }
        }
        return false
    }

    override fun execute(actor: Actor, level: Level) {
        gearKey.getThing(actor.level!!)?.also { gear ->
            if (gear is Gear) {
                gear.equipped = true
                actor.setGearSlot(gear.slot, gear)
                actor.onEquip(gear)
                gear.onEquip(actor)
                Console.sayAct(gear.equipSelfMsg(), gear.equipOtherMsg(), actor, gear)
            }
        }
    }

}

package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import things.Gear
import things.Thing
import ui.panels.Console
import world.level.Level

@Serializable
class Unequip(
    private val gearKey: Thing.Key
) : Action(2f) {
    override fun name() = "unequip gear"

    override fun execute(actor: Actor, level: Level) {
        gearKey.getThing(level)?.also { gear ->
            if (gear is Gear) {
                if (gear.holder == actor) {
                    gear.equipped = false
                    actor.setGearSlot(gear.slot, null)
                    actor.onUnequip(gear)
                    gear.onUnequip(actor)
                    Console.sayAct(gear.unequipSelfMsg(), gear.unequipOtherMsg(), actor, gear)
                }
            }
        }
    }

}

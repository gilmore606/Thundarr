package things

import actors.Actor
import actors.Player
import actors.stats.skills.Medic
import actors.statuses.Bandaged
import actors.statuses.Status
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.toEnglish

@Serializable
sealed class Medkit : Portable() {
    open fun useSelfMsg() = "You bind your wounds with %di."
    open fun useOtherMsg() = "%Dn binds %p wounds with %ii."
    open fun useSelfOtherMsg() = "You bind %dd's wounds with %di."
    open fun useOtherSelfMsg() = "%Dn binds your wounds with %p %i."
    open fun useOtherOtherMsg() = "%Dn binds %dd's wounds with %p %i."
    open fun junkMsg() = "That was the last of your %n."
    open fun treatmentsLeft() = 0

    override fun category() = Category.TOOL

    override fun uses() = mapOf(
        UseTag.USE to Use("self-treat with " + name(), 3f,
            canDo = { actor,x,y,targ ->
                !targ && isHeldBy(actor) && actor.hp < actor.hpMax && !actor.hasStatus(Status.Tag.BANDAGED)
                    },
            toDo = { actor, level, x, y ->
                doTreatment(actor, actor)
            }
        ),
        // TODO: USE_ON to treat other, but how to select/target?
    )

    override fun spawnContainers() = mutableListOf(
        Tag.THING_TRUNK, Tag.THING_BONEPILE, Tag.THING_WRECKEDCAR, Tag.THING_TABLE
    )

    private fun doTreatment(healer: Actor, target: Actor) {
        if (healer == target) Console.sayAct(useSelfMsg(), useOtherMsg(), healer, target, this)
        else Console.sayAct(useSelfOtherMsg(), if (target is Player) useOtherSelfMsg() else useOtherOtherMsg(), healer, target, this)
        val result = Medic.resolve(healer, 2f - (target.hpMax - target.hp) * 0.3f)
        if (result < 0f) {
            if (healer is Player) Console.say("You don't think you did much good.")
        } else {
            if (healer is Player) Console.say("That should do it.")
            target.addStatus(Bandaged(result))
            target.receiveAssistance(healer)
            onTreatment(healer, target)
            junkIfEmpty(healer, target)
        }
    }

    open fun onTreatment(healer: Actor, target: Actor) { }

    private fun junkIfEmpty(healer: Actor, target: Actor) {
        if (treatmentsLeft() < 1) {
            Console.sayAct(junkMsg(), "", this)
            moveTo(null)
        }
    }

    override fun examineInfo(): String {
        return "It's got " + treatmentsLeft().toEnglish() + " treatments left."
    }
}

@Serializable
class Bandages : Medkit() {
    override val tag = Tag.THING_BANDAGES
    override fun glyph() = Glyph.CLOTH_ROLL
    override fun name() = "bandage"
    override fun description() = "A roll of thick cloth suitable for binding wounds."
    override fun weight() = 0.2f
    override fun flammability() = 1f
    private var bandages = 5
    override fun treatmentsLeft() = bandages
    override fun onTreatment(healer: Actor, target: Actor) { bandages-- }
}

@Serializable
class FirstAidKit : Medkit() {
    override val tag = Tag.THING_FIRSTAIDKIT
    override fun glyph() = Glyph.MEDPACK
    override fun name() = "first-aid kit"
    override fun description() = "A box of bandages and topical antibiotics for effective wound care."
    override fun weight() = 0.7f
    override fun flammability() = 0.7f
    private var bandages = 8
    override fun treatmentsLeft() = bandages
    override fun onTreatment(healer: Actor, target: Actor) { bandages-- }
}

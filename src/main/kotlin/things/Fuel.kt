package things

import actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.groupByTag
import util.log
import util.turnsToRoughTime

@Serializable
sealed class Fuel : Portable() {

    override fun flammability() = 0.7f
    open fun fuelPerTurn() = 1f
    var fuel = 50f

    override fun onBurn(delta: Float): Float {
        val burn = fuelPerTurn() * delta
        fuel -= burn
        if (fuel < 0f) {
            moveTo(null)
            return 0f
        }
        return burn
    }

}

@Serializable
class Log : Fuel() {
    var spawned = false
    init {
        if (!spawned) {
            spawned = true
            fuel = 100f
        }
    }
    override fun name() = "log"
    override fun description() = "Big, heavy, wood.  Better than bad.  Good."
    override fun glyph() = Glyph.LOG
    override fun weight() = 3f
    override fun uses() = mapOf(
        UseTag.TRANSFORM to Use("build campfire from ${name()} here", 8.0f,
            canDo = { actor,x,y,targ ->
                isHeldBy(actor) && Survive.getBase(actor) > 0f
            },
            toDo = { actor, level, x, y ->
                Campfire().apply {
                    log.info("moving campfire to $level $x $y")
                    moveTo(level, x, y)
                    feedWith(this@Log, actor)
                }
            }
        )
    )
}

@Serializable
class Campfire : Fuel() {
    override fun name() = "campfire"
    override fun description() = "Logs expertly stacked for controlled burning."
    override fun examineInfo() = "The stack of wood looks like it'll burn for " + (fuel / 2f).turnsToRoughTime() + "."
    override fun glyph() = Glyph.CAMPFIRE
    override fun isPortable() = false
    override fun fuelPerTurn() = 2f
    override fun weight() = fuel / 60f

    fun feedWith(log: Fuel, actor: Actor) {
        val bonus = Survive.bonus(actor) + Survive.resolve(actor, 0f) * 0.25f
        fuel += log.fuel * (2f + bonus * 2f)
        Console.sayAct("You feed %ii to %dd.", "%Dn feeds %ii to %dd.", actor, this, log)
        log.moveTo(null)
    }

    override fun uses(): Map<UseTag, Use> {
        val uses = mutableMapOf<UseTag, Use>()
        var useTagNum = 0
        App.player.contents.groupByTag().forEach { group ->
            if (group[0] is Fuel) {
                val fuel = group[0] as Fuel
                uses[Use.enumeratedTag(useTagNum)] = Use("feed ${name()} with ${fuel.name()}", 1f,
                    canDo = { actor, x, y, targ ->
                        isNextTo(actor)
                    },
                    toDo = { actor, level, x, y, ->
                        feedWith(fuel, actor)
                    })
                useTagNum++
            }
        }
        return uses
    }
}

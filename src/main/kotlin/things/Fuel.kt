package things

import actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.groupByTag
import util.log
import util.turnsToRoughTime
import world.Entity

@Serializable
sealed interface Fuel {

    fun fuelPerTurn() = 1f
    var fuel: Float
    fun moveTo(to: ThingHolder?)
    fun name(): String

    fun onBurn(delta: Float): Float {
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
class Log : Portable(), Fuel {
    override var fuel = 100f
    override fun flammability() = 0.7f
    override fun name() = "log"
    override fun description() = "Big, heavy, wood.  Better than bad.  Good."
    override fun glyph() = Glyph.LOG
    override fun weight() = 3f
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }

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

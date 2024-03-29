package things

import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.log

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
sealed class FuelBlock : Portable(), Fuel {
    override fun flammability() = 0.7f
    override fun weight() = 3f
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }

    override fun uses() = super.uses().apply {
        this[UseTag.TRANSFORM] = Use("build campfire from ${name()} here", 8.0f,
            canDo = { actor, x, y, targ ->
                isHeldBy(actor) && Survive.getBase(actor) > 0f
            },
            toDo = { actor, level, x, y ->
                Campfire().apply {
                    moveTo(level, x, y)
                    feedWith(this@FuelBlock, actor)
                }
            }
        )
    }
}

@Serializable
class Log() : FuelBlock() {
    override val tag = Tag.LOG
    override fun name() = "log"
    override fun description() = "Big, heavy, wood.  Better than bad.  Good."
    override fun glyph() = Glyph.LOG
    override var fuel = 160f
}

@Serializable
class Board() : FuelBlock() {
    override val tag = Tag.BOARD
    override fun name() = "board"
    override fun description() = "A length of 2x4 knotty pine."
    override fun glyph() = Glyph.BOARD
    override fun hue() = 0.1f
    override var fuel = 80f
}

package world.stains

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.DIRECTIONS
import util.Dice
import util.LightColor
import util.log
import world.CellContainer
import world.level.Level
import java.lang.Float.min
import kotlin.random.Random

@Serializable
class Fire : Stain(), LightSource {

    companion object {
        val maxSize = 4f
        val color = LightColor(0.4f, 0.15f, 0f)
        var flicker = 1f
        fun onRender(delta: Float) {
            flicker = Random.nextFloat() * 0.25f + 0.75f
        }
    }

    override fun glyph() = Glyph.BLANK
    override fun name() = "fire"
    override fun stackType() = Type.FIRE
    override fun lifespan() = 1.0

    override fun light() = color
    override fun flicker() = flicker

    var size = 1f

    override fun onRestore(holder: CellContainer) {
        super.onRestore(holder)
        holder.level?.dirtyLights?.set(this, holder.xy)
    }

    override fun onAdd(level: Level, x: Int, y: Int) {
        holder?.level?.addLightSource(holder!!.xy.x, holder!!.xy.y, this)
    }

    override fun advanceTime(delta: Float) {
        if (done) return
        holder?.also { holder ->

            val rain = holder.level?.let { if (!it.isRoofedAt(holder.xy.x, holder.xy.y)) it.weather.rain() else 0f } ?: 0f
            if (rain > 0f && Dice.chance(rain * 0.2f)) {
                expire()
                return
            }
            var needToBurn = size * delta * (1f + (holder.level?.weather?.rain() ?: 0f))

            // burn fuel
            val fuels = holder.contents.filter { it.flammability() > 0f } as MutableList<Thing>
            while (fuels.isNotEmpty() && needToBurn > 0f) {
                val fuel = fuels.random()
                needToBurn -= fuel.onBurn(delta)
                fuels.remove(fuel)
            }

            // get smaller or bigger
            if (needToBurn > 0f) {
                size -= 1f
                if (size < 1f) {
                    expire()
                    return
                }
            } else if (needToBurn < 0f) {
                size = min(maxSize, size + 1f)
            }

            // maybe spread nearby
            holder.level?.also { level ->
                if (size > 1f && Dice.chance(size * 0.05f)) {
                    DIRECTIONS.forEach { dir ->
                        val lx = holder.xy.x + dir.x
                        val ly = holder.xy.y + dir.y
                        var spreadChance = 0f
                        level.thingsAt(lx, ly).forEach {
                            spreadChance += it.flammability()
                        }
                        if (Dice.chance(spreadChance * 0.5f)) {
                            level.addStain(Fire(), lx, ly)
                        }
                    }
                }
            }
        }
    }

    override fun onExpire() {
        holder?.level?.addStain(Scorch(), holder!!.xy.x, holder!!.xy.y)
        holder?.level?.removeLightSource(this)
    }
}

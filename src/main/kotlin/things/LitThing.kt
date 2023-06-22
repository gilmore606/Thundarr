package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice
import util.LightColor
import util.hasOneWhere
import world.CellContainer
import java.lang.Float.max
import kotlin.random.Random


interface LightSource {
    fun light(): LightColor?
    fun flicker(): Float = 1f
}

@Serializable
sealed class LitThing : Portable(), LightSource {
    abstract val lightColor: LightColor
    var active: Boolean = false

    open fun extinguishSelfMsg() = "Your %d sputters and dies."
    open fun extinguishOtherMsg() = "%Dn's %d goes out."
    open fun extinguishMsg() = "%Dn goes out."
    open fun lightMsg() = "%Dn turns on."
    open fun lightSelfMsg() = "Your %d turns on."
    open fun lightOtherMsg() = "%Dn's %d turns on."

    override fun listTag() = if (active) "(lit)" else ""

    override fun category() = Category.TOOL

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        if (light() != null && holder is CellContainer) {
            holder.level?.dirtyLights?.set(this, holder.xy)
        }
    }

    override fun light(): LightColor? = lightColor

    fun becomeLit() {
        active = true
        holder?.also { holder ->
            holder.xy().also { xy ->
                holder.level?.addLightSource(xy.x, xy.y,
                    if (holder is Actor) holder else this
                )
                if (holder is Actor) {
                    Console.sayAct(lightSelfMsg(), lightOtherMsg(), holder, this)
                } else {
                    Console.sayAct("", lightMsg(), this, this)
                }
            }
        }
    }

    protected fun becomeDark() {
        active = false
        holder?.also { holder ->
            holder.level?.also { level ->
                holder.xy().also { xy ->
                    level.removeLightSource(if (holder is Actor) holder else this )
                    if (holder is Actor) {
                        Console.sayAct(extinguishSelfMsg(), extinguishOtherMsg(), holder, this)
                        holder.light()?.also { level.addLightSource(xy.x, xy.y, holder) }
                    } else {
                        Console.sayAct("", extinguishMsg(), this, this)
                    }
                }
            }
        }
    }

    fun reproject() {
        holder?.also { holder ->
            holder.level?.also { level ->
                holder.xy().also { xy ->
                    level.removeLightSource(if (holder is Actor) holder else this )
                    level.addLightSource(xy.x, xy.y, if (holder is Actor) holder else this)
                }
            }
        }
    }
}

@Serializable
class Lightbulb : LitThing() {
    override val tag = Tag.THING_LIGHTBULB
    override fun glyph() = Glyph.LIGHTBULB
    override fun name() = "lightbulb"
    override fun description() = "A light bulb with no obvious power source.  Why is this even here?"
    override val lightColor = LightColor(0.7f, 0.6f, 0.3f)
    override fun onCreate() { active = true }
}

@Serializable
class CeilingLight : LitThing(), Smashable {
    override val tag = Tag.THING_CEILINGLIGHT
    var broken = false
    override fun glyph() = Glyph.CEILING_LIGHT
    override fun name() = "ceiling light"
    override fun description() = "An electric light panel installed in the ceiling behind a plastic grate."
    override fun isPortable() = false
    override val lightColor = LightColor(0f, 0f, 0f)
    fun withColor(r: Float, g: Float, b: Float): CeilingLight {
        lightColor.r = r
        lightColor.g = g
        lightColor.b = b
        return this
    }
    override fun sturdiness() = -1f
    override fun isSmashable() = !broken
    override fun onSmashSuccess() {
        becomeDark()
        broken = true
    }
    override fun examineInfo() = when {
        (!active && broken) -> "It's dark.  It looks broken, but could probably be repaired."
        (!active) -> "It's dark."
        else -> "It's turned on."
    }
}

@Serializable
class Candlestick : LitThing() {
    override val tag = Tag.THING_CANDLESTICK
    var lit = false
    override fun glyph() = if (lit) Glyph.CANDLESTICK_ON else Glyph.CANDLESTICK_OFF
    override fun name() = "candlestick"
    override fun description() = "An iron candlestick holding three thick beeswax candles."
    override fun isPortable() = false
    override val lightColor = LightColor(0.6f, 0.5f, 0.3f)
    override fun light() = if (lit) lightColor else null

    override fun uses() = mapOf(
        UseTag.SWITCH_ON to Use("light " + name(), 0.5f,
            canDo = { actor,x,y,targ -> !lit && isNextTo(actor) },
            toDo = { actor,level,x,y ->
                lit = true
                level.addLightSource(x, y, this)
                Console.sayAct("You light %dd.", "%Dn lights %dd.", actor, this)
            }),
        UseTag.SWITCH_OFF to Use("extinguish " + name(), 0.5f,
            canDo = { actor,x,y,targ -> lit && isNextTo(actor) },
            toDo = { actor,level,x,y ->
                Console.sayAct("You snuff out %dd.", "%Dn snuffs out %dd.", actor, this)
                lit = false
                level.removeLightSource(this)
            }
        )
    )

}

@Serializable
class Glowstone : LitThing() {
    override val tag = Tag.THING_GLOWSTONE
    override fun glyph() = Glyph.GLOWING_CRYSTAL
    override fun name() = "glowstone"
    override fun description() = "A softly glowing quartz-like crystal formation."
    override fun isPortable() = false
    override fun onCreate() { active = true }
    override val lightColor = LightColor(0f, 0f, 0f)
    fun withColor(r: Float, g: Float, b: Float): Glowstone {
        lightColor.r = r
        lightColor.g = g
        lightColor.b = b
        return this
    }
}

@Serializable
class Torch : LitThing(), Temporal {
    override val tag = Tag.THING_TORCH
    private var fuel = 2000f
    override fun glyph() = if (active) Glyph.TORCH_LIT else Glyph.TORCH
    override fun name() = "torch"
    override fun description() = "A branch dipped in pitch tar."
    override fun extinguishMsg() = "%Dn sputters and dies."
    override fun extinguishSelfMsg() = "Your %d sputters and dies."
    override fun extinguishOtherMsg() = "%Dn's %d sputters and dies."
    override val lightColor = LightColor(0.6f, 0.5f, 0.2f)

    private val smokeChance = 1.1f

    override fun onCreate() { active = false }

    override fun light() = if (active) lightColor else null

    private var flicker = 1f
    override fun flicker() = flicker
    override fun onRender(delta: Float) {
        if (active && Screen.timeMs % 5 == 1L) {
            flicker = Random.nextFloat() * 0.12f + 0.88f
            if (Dice.chance(delta * smokeChance * 5f)) {
                xy().also { xy -> level()?.addSpark(Smoke().at(xy.x, xy.y)) }
            }
        }
    }

    override fun uses() = mapOf(
        UseTag.SWITCH to Use("light " + name(), 0.5f,
                canDo = { actor,x,y,targ -> !targ && !active && (isHeldBy(actor) || isNextTo(actor)) &&
                        actor.contents.hasOneWhere { it is Lighter }},
                toDo = { actor, level, x, y ->
                    active = true
                    level.addLightSource(x, y, (if (holder == actor) actor else this@Torch) as LightSource)
                    level.linkTemporal(this@Torch)
                }))

    override fun advanceTime(delta: Float) {
        if (fuel >= 0f) {
            fuel -= delta
            if (fuel < 200f) {
                lightColor.r = max(0.15f, lightColor.r - delta * 0.0015f)
                lightColor.g = max(0f, lightColor.g - delta * 0.0035f)
                lightColor.b = max(0f, lightColor.b - delta * 0.0025f)
                if (fuel < 0f) {
                    becomeDark()
                    moveTo(null)
                } else {
                    reproject()
                }
            }
        }
    }
}

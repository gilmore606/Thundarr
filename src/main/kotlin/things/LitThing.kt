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

    override fun toString() = "${name()}(light=${light()})(active=$active)"

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
        reproject()
    }

    override fun light(): LightColor? = if (active) lightColor else null

    fun becomeLit() {
        active = true
        holder?.also { holder ->
            holder.xy().also { xy ->
                reproject()
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
                    reproject()
                    if (holder is Actor) {
                        Console.sayAct(extinguishSelfMsg(), extinguishOtherMsg(), holder, this)
                    } else {
                        Console.sayAct("", extinguishMsg(), this, this)
                    }
                }
            }
        }
    }

    override fun onSpawn() {
        super.onSpawn()
        reproject()
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
class CeilingLight : LitThing(), Smashable {
    override val tag = Tag.CEILING_LIGHT
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
sealed class SwitchablePlacedLight : LitThing() {
    override fun isPortable() = false
    override fun isAlwaysVisible() = true
    override fun glyph() = if (active) glyphLit() else glyphDark()
    abstract fun glyphLit(): Glyph
    abstract fun glyphDark(): Glyph
    open fun leaveLit() = false

    override fun uses() = mapOf(
        UseTag.SWITCH_ON to Use("light " + name(), 0.5f,
            canDo = { actor,x,y,targ -> !active && isNextTo(actor) },
            toDo = { actor,level,x,y ->
                active = true
                level.addLightSource(x, y, this)
                Console.sayAct("You light %dd.", "%Dn lights %dd.", actor, this)
            }),
        UseTag.SWITCH_OFF to Use("extinguish " + name(), 0.5f,
            canDo = { actor,x,y,targ -> active && isNextTo(actor) && !leaveLit() },
            toDo = { actor,level,x,y ->
                Console.sayAct("You snuff out %dd.", "%Dn snuffs out %dd.", actor, this)
                active = false
                level.removeLightSource(this)
            }
        )
    )
}

@Serializable
class Candlestick : SwitchablePlacedLight() {
    override val tag = Tag.CANDLESTICK
    override fun glyphLit() = Glyph.CANDLESTICK_ON
    override fun glyphDark() = Glyph.CANDLESTICK_OFF
    override fun name() = "candlestick"
    override fun description() = "An iron candlestick holding three thick beeswax candles."
    override val lightColor = LightColor(0.6f, 0.5f, 0.3f)
}

@Serializable
class Lamppost : SwitchablePlacedLight() {
    override val tag = Tag.LAMPPOST
    override fun glyphLit() = Glyph.LAMPPOST_ON
    override fun glyphDark() = Glyph.LAMPPOST_OFF
    override fun leaveLit() = true
    override fun name() = "lamppost"
    override fun description() = "A wrought iron lamppost."
    override val lightColor = LightColor(0.4f, 0.6f, 0f)
    override fun onCreate() {
        super.onCreate()
        active = true
    }
}

@Serializable
class Glowstone : LitThing() {
    override val tag = Tag.GLOWSTONE
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
sealed class SwitchablePortableLight : LitThing() {
    override fun isPortable() = true
    override fun glyph() = if (active) glyphLit() else glyphDark()
    abstract fun glyphLit(): Glyph
    abstract fun glyphDark(): Glyph

    override fun toolbarName() = "turn ${name()} " + if (active) "off" else "on"
    override fun toolbarUseTag() = UseTag.SWITCH

    override fun uses() = mapOf(
        UseTag.SWITCH to Use(toolbarName(), 0.25f,
            canDo =  { actor,x,y,targ -> isHeldBy(actor) || isNextTo(actor) },
            toDo = { actor,level,x,y ->
                if (active) {
                    Console.sayAct("You turn off your %d.", "%Dn turns off %dd.", actor, this)
                    active = false
                    level.removeLightSource(this)
                } else {
                    active = true
                    level.addLightSource(x, y, this)
                    Console.sayAct("You turn on your %d.", "%Dn turns on %id.", actor, this)
                }
            }),
    )
}

@Serializable
class Flashlight : SwitchablePortableLight() {
    override val tag = Tag.FLASHLIGHT
    override fun glyphLit() = Glyph.FLASHLIGHT
    override fun glyphDark() = Glyph.FLASHLIGHT
    override fun name() = "flashlight"
    override fun description() = "A metal tube with a bulb on one end.  You can't imagine how it works."
    override val lightColor = LightColor(0.4f, 0.5f, 0.5f)
}

@Serializable
class Lantern : SwitchablePortableLight() {
    override val tag = Tag.LANTERN
    override fun glyphLit() = Glyph.LANTERN
    override fun glyphDark() = Glyph.LANTERN
    override fun name() = "lantern"
    override fun description() = "A brass lantern."
    override val lightColor = LightColor(0.5f, 0.4f, 0.4f)
}

@Serializable
class Torch : LitThing(), Temporal {
    override val tag = Tag.TORCH
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

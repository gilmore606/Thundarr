package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice
import util.LightColor
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
    var active: Boolean = true

    override fun listName() = if (active) super.listName() + " (lit)" else super.listName()

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        if (light() != null && holder is CellContainer) {
            holder.level?.dirtyLights?.set(this, holder.xy)
        }
    }

    override fun light(): LightColor? = lightColor

    protected fun becomeLit() {
        active = true
        holder?.also { holder ->
            holder.xy()?.also { xy ->
                holder.level?.addLightSource(xy.x, xy.y,
                    if (holder is Actor) holder else this
                )
            }
        }
    }

    protected fun becomeDark() {
        active = false
        holder?.also { holder ->
            holder.level?.also { level ->
                holder.xy()?.also { xy ->
                    level.removeLightSource(if (holder is Actor) holder else this )
                    if (holder is Actor) {
                        Console.sayAct("Your torch sputters and dies.", "%Dn's torch goes out.", holder)
                        holder.light()?.also { level.addLightSource(xy.x, xy.y, holder) }
                    } else {
                        Console.sayAct("", "The torch sputters and dies.", this)
                    }
                }
            }
        }
    }

    protected fun reproject() {
        holder?.also { holder ->
            holder.level?.also { level ->
                holder.xy()?.also { xy ->
                    level.removeLightSource(if (holder is Actor) holder else this )
                    level.addLightSource(xy.x, xy.y, if (holder is Actor) holder else this)
                }
            }
        }
    }
}

@Serializable
class Lightbulb : LitThing() {
    override fun glyph() = Glyph.LIGHTBULB
    override fun name() = "lightbulb"
    override fun description() = "A light bulb with no obvious power source.  Why is this even here?"
    override val lightColor = LightColor(0.7f, 0.6f, 0.3f)
}

@Serializable
class Sunsword : LitThing() {
    private var spawned = false
    init {
        if (!spawned) {
            spawned = true
            active = false
        }
    }
    override fun glyph() = if (active) Glyph.HILT_LIT else Glyph.HILT
    override fun name() = "sunsword"
    override fun description() = "The legendary Sunsword holds the power of sunlight.  Weirdly effective against robots."
    override val lightColor = LightColor(0.1f, 0.25f, 0.3f)

    override fun light() = if (active) lightColor else null

    override fun uses() = setOf(
        Use("switch " + (if (active) "off" else "on"), 0.2f,
            canDo = { actor ->
                this in actor.contents
            },
            toDo = { actor, level ->
                if (active) {
                    becomeDark()
                    Console.sayAct("The shimmering blade vanishes.", "%Dn's sunsword turns off.", actor)
                } else {
                    becomeLit()
                    Console.sayAct("A shimmering blade emerges from the sunsword's hilt.", "%Dn's sunsword turns on.", actor)
                }
            })
    )
}

@Serializable
class Torch : LitThing(), Temporal {
    private var fuel = 50f
    override fun glyph() = if (active) Glyph.TORCH_LIT else Glyph.TORCH
    override fun name() = "torch"
    override fun description() = "A branch dipped in pitch tar."
    override val lightColor = LightColor(0.6f, 0.5f, 0.2f)

    private val smokeChance = 1.1f

    private var spawned = false
    init {
        if (!spawned) {
            spawned = true
            active = false
        }
    }

    override fun light() = if (active) lightColor else null

    private var flicker = 1f
    override fun flicker() = flicker
    override fun onRender(delta: Float) {
        if (active && System.currentTimeMillis() % 5 == 1L) {
            flicker = Random.nextFloat() * 0.12f + 0.88f
            if (Dice.chance(delta * smokeChance * 5f)) {
                xy()?.also { xy -> level()?.addSpark(Smoke().at(xy.x, xy.y)) }
            }
        }
    }

    override fun uses() = mutableSetOf<Use>().apply {
        if (!active) add(Use("light " + name(), 0.5f,
                canDo = { true },
                toDo = { actor, level ->
                    active = true
                    level.addLightSource(actor.xy.x, actor.xy.y, (if (holder == actor) actor else this@Torch) as LightSource)
                    level.linkTemporal(this@Torch)
                }))
        }

    override fun advanceTime(delta: Float) {
        if (fuel >= 0f) {
            fuel -= delta
            if (fuel < 16f) {
                lightColor.r = max(0.2f, lightColor.r - delta * 0.05f)
                lightColor.g = max(0f, lightColor.g - delta * 0.08f)
                lightColor.b = max(0f, lightColor.b - delta * 0.07f)
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

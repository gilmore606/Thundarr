package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel
import util.LightColor
import world.CellContainer
import java.lang.Float.max


interface LightSource {
    fun light(): LightColor?
}

@Serializable
sealed class LitThing : Portable(), LightSource {
    abstract val lightColor: LightColor
    var active = true

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
            holder.level?.addLightSource(holder.xy.x, holder.xy.y,
                if (holder is Actor) holder else this
            )
        }
    }

    protected fun becomeDark() {
        active = false
        holder?.also { holder ->
            holder.level?.also { level ->
                level.removeLightSource(if (holder is Actor) holder else this )
                if (holder is Actor) {
                    ConsolePanel.announce(level, holder.xy.x, holder.xy.y, ConsolePanel.Reach.VISUAL,
                        (if (holder is Player) "Your " else "Some guy's ") + " torch goes out.")
                    holder.light()?.also { level.addLightSource(holder.xy.x, holder.xy.y, holder) }
                } else {
                    ConsolePanel.announce(level, holder.xy.x, holder.xy.y, ConsolePanel.Reach.VISUAL,
                    "The torch burns out.")
                }
            }
        }
    }

    protected fun reproject() {
        holder?.also { holder ->
            holder.level?.also { level ->
                level.removeLightSource(if (holder is Actor) holder else this )
                level.addLightSource(holder.xy.x, holder.xy.y, if (holder is Actor) holder else this)
            }
        }
    }
}

@Serializable
class Lightbulb : LitThing() {
    override fun glyph() = Glyph.LIGHTBULB
    override fun name() = "lightbulb"
    override val kind = Kind.LIGHTBULB
    override val lightColor = LightColor(0.7f, 0.6f, 0.3f)

}

@Serializable
class Sunsword : LitThing() {
    override fun glyph() = if (active) Glyph.HILT_LIT else Glyph.HILT
    override fun name() = "sunsword"
    override val kind = Kind.SUNSWORD
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
                    if (actor is Player) ConsolePanel.say("The shimmering blade vanishes.")
                } else {
                    becomeLit()
                    if (actor is Player) ConsolePanel.say("A shimmering blade emerges from the sunsword's hilt.")
                }
            })
    )
}

@Serializable
class Torch : LitThing(), Temporal {
    private var fuel = 50f
    override fun glyph() = if (active) Glyph.TORCH_LIT else Glyph.TORCH
    override fun name() = "torch"
    override val kind = Kind.TORCH
    override val lightColor = LightColor(0.6f, 0.5f, 0.2f)

    init { active = false }

    override fun light() = if (active) lightColor else null

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

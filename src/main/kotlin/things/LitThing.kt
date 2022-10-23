package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel
import util.LightColor
import world.Level


interface LightSource {
    fun light(): LightColor?
}

@Serializable
sealed class LitThing : Portable(), LightSource {
    abstract val lightColor: LightColor
    var active = true

    override fun light(): LightColor? = lightColor

    protected fun becomeLit(actor: Actor, level: Level) {
        active = true
        level.addLightSource(actor.xy.x, actor.xy.y, actor)
    }

    protected fun becomeDark(actor: Actor, level: Level) {
        active = false
        level.removeLightSource(actor)
        actor.light()?.also { level.addLightSource(actor.xy.x, actor.xy.y, actor) }
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
                    becomeDark(actor, level)
                    if (actor is Player) ConsolePanel.say("The shimmering blade vanishes.")
                } else {
                    becomeLit(actor, level)
                    if (actor is Player) ConsolePanel.say("A shimmering blade emerges from the sunsword's hilt.")
                }
            })
    )
}

@Serializable
class Torch : LitThing() {
    var lit = false
    override fun glyph() = if (lit) Glyph.TORCH_LIT else Glyph.TORCH
    override fun name() = "torch"
    override val kind = Kind.TORCH
    override val lightColor = LightColor(0.6f, 0.5f, 0.2f)

    override fun light() = if (lit) lightColor else null

    override fun uses() = mutableSetOf<Use>().apply {
        if (!lit) add(Use("light " + name(), 0.5f,
                canDo = { true },
                toDo = { actor, level ->
                    lit = true
                    level.addLightSource(actor.xy.x, actor.xy.y, actor)
                }))
        }
}

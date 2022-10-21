package things

import actors.Actor
import actors.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel
import util.LightColor

@Serializable
sealed class Thing {
    abstract fun glyph(): Glyph
    abstract fun isOpaque(): Boolean
    abstract fun isBlocking(): Boolean
    abstract fun isPortable(): Boolean
    abstract fun name(): String

    open fun onWalkedOnBy(actor: Actor) { }
}

@Serializable
sealed class Portable : Thing() {
    override fun isOpaque() = false
    override fun isBlocking() = false
    override fun isPortable() = true
}

interface LightSource {
    fun light(): LightColor?
}

@Serializable
sealed class LitThing : Portable(), LightSource {
    abstract val lightColor: LightColor

    override fun light(): LightColor? = lightColor
}

@Serializable
class Lightbulb : LitThing() {
    override fun glyph() = Glyph.LIGHTBULB
    override fun name() = "lightbulb"
    override val lightColor = LightColor(1f, 1f, 1f)
}

@Serializable
sealed class Obstacle : Thing() {
    override fun isBlocking() = true
    override fun isPortable() = false
}

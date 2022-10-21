package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor

@Serializable
sealed class Thing {
    abstract fun glyph(): Glyph
    abstract fun isOpaque(): Boolean
    abstract fun isBlocking(): Boolean
    abstract fun isPortable(): Boolean
    abstract fun name(): String
    abstract val kind: Kind

    open fun onWalkedOnBy(actor: Actor) { }

    enum class Kind {
        LIGHTBULB,
        APPLE,
        AXE,
        ENERGY_DRINK,
        PINE_TREE,
        OAK_TREE,
        PALM_TREE,
    }
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
    override val kind = Kind.LIGHTBULB
    override val lightColor = LightColor(1f, 1f, 1f)
}

@Serializable
sealed class Obstacle : Thing() {
    override fun isBlocking() = true
    override fun isPortable() = false
}

@Serializable
class Axe : Portable() {
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override val kind = Kind.AXE
}

@Serializable
class EnergyDrink: Portable() {
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "Monster energy drink"
    override val kind = Kind.ENERGY_DRINK
}

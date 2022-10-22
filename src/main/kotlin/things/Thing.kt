package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.GameScreen
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


    enum class Kind {
        LIGHTBULB,
        APPLE,
        AXE,
        SUNSWORD,
        ENERGY_DRINK,
        PINE_TREE,
        OAK_TREE,
        PALM_TREE,
    }

    open fun onWalkedOnBy(actor: Actor) { }

    fun moveTo(from: ThingHolder, to: ThingHolder) {
        from.remove(this)
        to.add(this)
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
    override val lightColor = LightColor(0.7f, 0.6f, 0.3f)
}

@Serializable
class Sunsword : LitThing() {
    var active = true
    override fun glyph() = Glyph.HILT
    override fun name() = "sunsword"
    override val kind = Kind.SUNSWORD
    override val lightColor = LightColor(0.1f, 0.2f, 0.3f)
    override fun light() = if (active) lightColor else GameScreen.fullDark
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

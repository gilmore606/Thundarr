package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor

@Serializable
class Stick : GenericTorch(), Fuel {
    override val tag = Tag.STICK
    override fun glyphDark() = Glyph.STICK
    override fun name() = "stick"
    override fun description() = "A long wooden stick.  You could burn it, or make something out of it."
    override var fuel = 40f
    override fun torchFuelMax() = 900f
    override val lightColor = LightColor(0.4f, 0.2f, 0.1f)
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }
}

@Serializable
class Torch : GenericTorch() {
    override val tag = Tag.TORCH
    override fun glyphDark() = Glyph.TORCH
    override fun torchFuelMax() = 2500f
    override val lightColor = LightColor(0.6f, 0.5f, 0.2f)
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
class Candle : GenericTorch() {
    override val tag = Tag.CANDLE
    override fun glyphLit() = Glyph.CANDLE_ON
    override fun glyphDark() = Glyph.CANDLE_OFF
    override fun name() = "candle"
    override fun description() = "A beeswax candle."
    override fun isAlwaysVisible() = true
    override fun torchFuelMax() = 6000f
    override fun smokeChance() = 0.1f
    override val lightColor = LightColor(0.5f, 0.5f, 0.3f)
}

package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Lighter : Portable() {
    override val tag = Tag.LIGHTER
    override fun name() = "lighter"
    override fun description() = "A brass cigarette lighter.  Handy for starting fires."
    override fun glyph() = Glyph.LIGHTER
    override fun category() = Category.TOOL
    override fun weight() = 0.02f
    override fun canLightFires() = true
    override fun toolbarName() = "light fire nearby"
    override fun toolbarUseTag() = UseTag.USE

}

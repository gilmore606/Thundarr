package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import ui.modals.DirectionModal
import ui.panels.Console
import util.DIRECTIONS
import util.NO_DIRECTION
import util.XY
import util.hasOneWhere
import world.level.Level
import world.stains.Fire

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

package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor
import util.hasOneWhere
import util.safeForEach
import java.lang.Integer.max

@Serializable
class Table : Container(), Smashable, LightSource {

    override fun isPortable() = false
    override fun isBlocking() = true
    override fun isOpaque() = false

    override val tag = Tag.THING_TABLE
    override fun name() = "table"
    override fun description() = "A lacquered wooden table."
    override fun glyph() = Glyph.TABLE
    override fun openVerb() = "look on"
    override fun preposition() = "on"
    override fun isEmptyMsg() = "There's nothing on " + dname() + "."

    override fun sturdiness() = 3f
    override fun smashDebris() = Board()
    override fun flammability() = 0.6f

    override fun drawExtraGlyphs(toDraw: (Glyph, Float, Float, Float) -> Unit) {
        val contents = contents()
        for (n in max(0, contents.size - 3) until contents.size) {
            val thing = contents[n]
            toDraw(thing.glyph(), thing.hue(), -0.3f + n * 0.2f, -0.4f)
        }
    }

    override fun onMoveTo(from: ThingHolder?, to: ThingHolder?) {
        super.onMoveTo(from, to)
        if (to == null) {
            emptySelf(from)
        }
    }

    private fun emptySelf(dest: ThingHolder?) {
        contents().safeForEach { thing ->
            thing.moveTo(dest)
        }
    }

    override fun light(): LightColor? {
        var light: LightColor? = null
        contents.forEach { thing ->
            if (thing is LightSource) {
                val thingLight = thing.light()
                if (thingLight != null) {
                    if (light == null) light = LightColor(0f,0f,0f)
                    light!!.r += thingLight.r
                    light!!.g += thingLight.g
                    light!!.b += thingLight.b
                }
            }
        }
        return light
    }

    override fun onAdd(thing: Thing) {
        if (thing is LightSource) {
            level?.addLightSource(xy()!!.x, xy()!!.y, this)
        }
    }

    override fun onRemove(thing: Thing) {
        if (thing is LightSource) {
            if (!this.contents.hasOneWhere { it is LightSource }) {
                level?.removeLightSource(this)
            }
        }
    }
}

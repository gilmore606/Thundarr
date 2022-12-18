package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.tilesets.Glyph
import util.*
import world.terrains.Terrain
import java.lang.Double.max
import java.lang.Double.min

class PerlinModal : Modal(300, 600, "perlin test") {

    class Sample(
        var scale: Float = 0.01f,
        var octaves: Int = 2,
        var persistence: Float = 0.5f,
    ) {
        fun value(x: Int, y: Int) = Simplex.octave(x, y, octaves, persistence, scale)
    }

    val samples = ArrayList<Sample>().apply {
        add(Sample())
    }
    var sampleCursor = 0

    private val lightCache = LightColor(0f,0f,0f)

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun onAdd() {
        App.DEBUG_PERLIN = this
        super.onAdd()
    }

    override fun onDismiss() {
        super.onDismiss()
        App.DEBUG_PERLIN = null
    }

    override fun onKeyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.I -> { App.player.debugMove(NORTH) }
            Input.Keys.J -> { App.player.debugMove(WEST) }
            Input.Keys.K -> { App.player.debugMove(SOUTH) }
            Input.Keys.L -> { App.player.debugMove(EAST) }

            Input.Keys.Q -> { samples[sampleCursor].scale *= 1.1f }
            Input.Keys.W -> { samples[sampleCursor].scale /= 1.1f }
            Input.Keys.A -> { samples[sampleCursor].octaves = kotlin.math.min(samples[sampleCursor].octaves + 1, 8) }
            Input.Keys.S -> { samples[sampleCursor].octaves = kotlin.math.max(1, samples[sampleCursor].octaves - 1) }
            Input.Keys.Z -> { samples[sampleCursor].persistence = kotlin.math.min(samples[sampleCursor].persistence + 0.1f, 1f) }
            Input.Keys.X -> { samples[sampleCursor].persistence = kotlin.math.max(0f, samples[sampleCursor].persistence - 0.1f) }

            Input.Keys.F11 -> dismiss()
        }
    }

    fun forEachCellToRender(doTile: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit) {
        for (x in App.level.pov.x - Screen.renderTilesWide / 2 until App.level.pov.x + Screen.renderTilesWide / 2) {
            for (y in App.level.pov.y - Screen.renderTilesHigh / 2 until App.level.pov.y + Screen.renderTilesHigh / 2) {
                val level = noise(x, y)
                lightCache.r = level
                lightCache.g = level
                lightCache.b = level
                doTile(
                    x, y, 1f, Glyph.BEACH, lightCache
                )
            }
        }
    }

    fun noise(x: Int, y: Int): Float {
        var noise = 0.0
        noise = samples[0].value(x, y)
        return (max(0.0, min (1.0, noise))).toFloat()
    }
}

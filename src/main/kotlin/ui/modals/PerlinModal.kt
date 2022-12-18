package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.tilesets.Glyph
import ui.modals.widgets.Slider
import ui.modals.widgets.Widget
import util.*
import world.terrains.Terrain
import java.lang.Double.max
import java.lang.Double.min

class PerlinModal : WidgetModal(600, 1000, "nOisE LAb") {

    val padding = 18

    class Sample(
        var scale: Float = 0.01f,
        var octaves: Int = 2,
        var persistence: Float = 0.5f,
        var amplitude: Float = 1.0f,
        var min: Double = 0.0,
        var max: Double = 1.0
    ) {
        fun value(x: Int, y: Int): Double {
            var v = min(max, max(0.0, Simplex.octave(x, y, octaves, persistence, scale) - min) / (1.0 - min)) * amplitude
            return v
        }
    }

    val samples = ArrayList<Sample>().apply {
        add(Sample())
        add(Sample())
    }
    var sampleCursor = 0

    private val lightCache = LightColor(0f,0f,0f)

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    init {
        add(Slider(
            "A scale", 0.007, 0.0001, 0.04,
            this, padding, 60, width - (padding * 2), 60
        ) { samples[0].scale = it.toFloat() })
        add(Slider(
            "A octaves", 1.0, 1.0, 8.0,
            this, padding, 120, width - (padding * 2), 60
        ) { samples[0].octaves = it.toInt() })
        add(Slider(
            "A octPersist", 0.5, 0.0, 1.0,
            this, padding, 180, width - (padding * 2), 60
        ) { samples[0].persistence = it.toFloat() })
        add(Slider(
            "A amp", 1.0, 0.0, 2.0,
            this, padding, 240, width - (padding * 2), 60
        ) { samples[0].amplitude = it.toFloat() })
        add(Slider(
            "A min", 0.0, 0.0, 1.0,
            this, padding, 300, width - (padding * 2), 60
        ) { samples[0].min = it })
        add(Slider(
            "A max", 1.0, 0.0, 1.0,
            this, padding, 360, width - (padding * 2), 60
        ) { samples[0].max = it })

        add(Slider(
            "B scale", 0.007, 0.0001, 0.04,
            this, padding, 560, width - (padding * 2), 60
        ) { samples[1].scale = it.toFloat() })
        add(Slider(
            "B octaves", 1.0, 1.0, 8.0,
            this, padding, 620, width - (padding * 2), 60
        ) { samples[1].octaves = it.toInt() })
        add(Slider(
            "B octPersist", 0.5, 0.0, 1.0,
            this, padding, 680, width - (padding * 2), 60
        ) { samples[1].persistence = it.toFloat() })
        add(Slider(
            "B amp", 1.0, 0.0, 2.0,
            this, padding, 740, width - (padding * 2), 60
        ) { samples[1].amplitude = it.toFloat() })
        add(Slider(
            "B min", 0.0, 0.0, 1.0,
            this, padding, 800, width - (padding * 2), 60
        ) { samples[1].min = it })
        add(Slider(
            "B max", 1.0, 0.0, 1.0,
            this, padding, 860, width - (padding * 2), 60
        ) { samples[1].max = it })
    }

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
        for (x in App.level.pov.x - Screen.renderTilesWide until App.level.pov.x + Screen.renderTilesWide) {
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
        noise = samples[0].value(x, y) * samples[1].value(x, y)
        return (max(0.0, min (1.0, noise))).toFloat()
    }
}

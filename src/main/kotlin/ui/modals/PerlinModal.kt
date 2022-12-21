package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.tilesets.Glyph
import ui.modals.widgets.ButtonStrip
import ui.modals.widgets.NoisePicker
import ui.modals.widgets.Slider
import ui.panels.Console
import util.*
import world.gen.NoiseMode
import world.gen.NoisePatches
import world.level.CHUNK_SIZE

class PerlinModal : WidgetModal(410, 750, "nOisE LAb"), ContextMenu.ParentModal {

    val padding = 18
    var patchName = NoisePatches.patches.keys.first()
    var patch = NoisePatches.patches[patchName] ?: throw RuntimeException("No such patch $patchName !")

    private val lightCache = LightColor(0f,0f,0f)

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    private val modeNames = ArrayList<String>().apply {
        NoiseMode.values().forEach { add(it.label) }
    }
    private val modes = ArrayList<NoiseMode>().apply {
        NoiseMode.values().forEach { add(it) }
    }

    init {
        buildUI()
    }

    fun buildUI() {
        widgets.clear()

        val sliderWidth = width - (padding * 2)
        add(Slider(
            "A scale", patch.samples.first.scale.toDouble(), 0.0001, 0.04, this, padding, 70, sliderWidth, 35
        ) { patch.samples.first.scale = it.toFloat() })
        add(Slider(
            "A octaves", patch.samples.first.octaves.toDouble(), 1.0, 8.0, this, padding, 105, sliderWidth, 35
        ) { patch.samples.first.octaves = it.toInt() })
        add(Slider(
            "A persist", patch.samples.first.persistence.toDouble(), 0.0, 1.0, this, padding, 140, sliderWidth, 35
        ) { patch.samples.first.persistence = it.toFloat() })
        add(Slider(
            "A amp", patch.samples.first.amplitude.toDouble(), 0.0, 2.0, this, padding, 175, sliderWidth, 35
        ) { patch.samples.first.amplitude = it.toFloat() })
        add(Slider(
            "A min", patch.samples.first.min.toDouble(), 0.0, 1.0, this, padding, 210, sliderWidth, 35
        ) { patch.samples.first.min = it })
        add(Slider(
            "A max", patch.samples.first.max.toDouble(), 0.0, 1.0, this, padding, 245, sliderWidth, 35
        ) { patch.samples.first.max = it })

        add(ButtonStrip(
            modeNames, modes.indexOf(patch.mode), this, padding, 290, sliderWidth, 50
        ) { patch.mode = modes[it] })

        add(Slider(
            "B scale", patch.samples.second.scale.toDouble(), 0.0001, 0.04, this, padding, 340, sliderWidth, 35
        ) { patch.samples.second.scale = it.toFloat() })
        add(Slider(
            "B octaves", patch.samples.second.octaves.toDouble(), 1.0, 8.0, this, padding, 375, sliderWidth, 35
        ) { patch.samples.second.octaves = it.toInt() })
        add(Slider(
            "B persist", patch.samples.second.persistence.toDouble(), 0.0, 1.0, this, padding, 410, sliderWidth, 35
        ) { patch.samples.second.persistence = it.toFloat() })
        add(Slider(
            "B amp", patch.samples.second.amplitude.toDouble(), 0.0, 2.0, this, padding, 445, sliderWidth, 35
        ) { patch.samples.second.amplitude = it.toFloat() })
        add(Slider(
            "B min", patch.samples.second.min.toDouble(), 0.0, 1.0, this, padding, 480, sliderWidth, 35
        ) { patch.samples.second.min = it })
        add(Slider(
            "B max", patch.samples.second.max.toDouble(), 0.0, 1.0, this, padding, 515, sliderWidth, 35
        ) { patch.samples.second.max = it })

        add(Slider(
            "OUT quant", patch.quantize.toDouble(), 0.0, 8.0, this, padding, 570, sliderWidth, 35
        ) { patch.quantize = it.toInt() })
        add(Slider(
            "OUT amp", patch.amp.toDouble(), 0.1, 3.0, this, padding, 605, sliderWidth, 35
        ) { patch.amp = it })
        add(Slider(
            "OUT min", patch.min.toDouble(), 0.0, 1.0, this, padding, 640, sliderWidth, 35
        ) { patch.min = it })

        add(NoisePicker(
            patchName, this, padding, 700, sliderWidth, 50
        ) { loadPatch(it) })
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

            Input.Keys.ESCAPE -> dismiss()
            Input.Keys.F11 -> dismiss()
        }
    }

    fun forEachCellToRender(doTile: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit) {
        for (x in App.level.pov.x - Screen.renderTilesWide until App.level.pov.x + Screen.renderTilesWide) {
            for (y in App.level.pov.y - Screen.renderTilesHigh / 2 until App.level.pov.y + Screen.renderTilesHigh / 2) {
                var level = noise(x, y)
                var glyph = Glyph.BEACH
                if (x % CHUNK_SIZE == 0 || y % CHUNK_SIZE == 0) {
                    glyph = Glyph.DEEP_WATER
                    level = kotlin.math.max(level, 0.1f)
                }
                lightCache.r = level
                lightCache.g = level
                lightCache.b = level
                doTile(
                    x, y, 1f, glyph, lightCache
                )
            }
        }
    }

    fun loadPatch(name: String) {
        patchName = name
        patch = NoisePatches.patches[name] ?: throw RuntimeException("No such noise patch $name !")
        buildUI()
        Console.say("Loaded noise patch $name .")
    }

    override fun childSucceeded() { }
    private fun noise(x: Int, y: Int) = patch.value(x, y).toFloat()
}

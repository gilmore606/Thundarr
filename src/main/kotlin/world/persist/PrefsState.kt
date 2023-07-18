package world.persist

import kotlinx.serialization.Serializable
import ui.input.Keydef
import util.XY

@Serializable
data class PrefsState(
    val zoomIndex: Double,
    val windowSize: XY,
    val fullscreen: Boolean,
    val worldZoom: Double,
    val cameraSpeed: Double,
    val cameraAccel: Double,
    val cameraMenuShift: Double,
    val uiHue: Double,
    val volumeMaster: Double,
    val volumeWorld: Double,
    val volumeMusic: Double,
    val volumeUI: Double,
    val binds: Map<Int, Keydef>,
)

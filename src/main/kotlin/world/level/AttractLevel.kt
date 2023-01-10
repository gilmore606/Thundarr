package world.level

import audio.Speaker
import java.lang.Float
import java.lang.Integer.max
import java.lang.Integer.min

class AttractLevel() : EnclosedLevel("attract") {

    companion object {
        val dimension = 120
    }
    val povBufferX = 60
    val povBufferY = 40

    override fun timeScale() = 5f

    override fun setPov(x: Int, y: Int) {
        val cx = max(povBufferX, min(x, dimension - povBufferX))
        val cy = max(povBufferY, min(y, dimension - povBufferY))
        super.setPov(cx, cy)
    }

    override fun updateVisibility() {
        for (cx in 0 until width) {
            for (cy in 0 until height) {
                chunk?.setTileVisibility(cx, cy, true)
            }
        }
    }

    override fun onPlayerEntered() {
        Speaker.clearMusic()
        Speaker.requestSong(Speaker.Song.ATTRACT)
        Speaker.clearAmbience()
        Speaker.requestAmbience(Speaker.Ambience.OUTDOORDAY)
        Speaker.requestAmbience(Speaker.Ambience.OUTDOORNIGHT)
        Speaker.requestAmbience(Speaker.Ambience.RAINLIGHT)
        Speaker.requestAmbience(Speaker.Ambience.RAINHEAVY)
    }

    override fun updateAmbientSound() {
        val outdoors = Float.max(0f, 1f - (distanceFromOutdoors * 0.06f))
        val day = Float.max(0f, (ambientLight.brightness() - 0.5f) * 2f)
        val night = 1f - day
        val rain1 = Float.min(1f, App.weather.rain() * 3f)
        val rain2 = Float.max(0f, (App.weather.rain() - 0.5f) * 2f)
        Speaker.adjustAmbience(Speaker.Ambience.OUTDOORDAY, outdoors * day)
        Speaker.adjustAmbience(Speaker.Ambience.OUTDOORNIGHT, outdoors * night)
        Speaker.adjustAmbience(Speaker.Ambience.RAINLIGHT, outdoors * rain1)
        Speaker.adjustAmbience(Speaker.Ambience.RAINHEAVY, outdoors * rain2)
    }

}

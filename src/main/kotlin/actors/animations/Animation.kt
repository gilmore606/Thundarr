package actors.animations

import render.Screen
import java.lang.Float.max
import java.lang.Float.min


abstract class Animation(
    val durationMs: Long,
) {
    var done = false
    var startMs: Long = 0L

    open fun offsetX(): Float = 0f
    open fun offsetY(): Float = 0f
    open fun shadowOffsetX(): Float = 0f
    open fun shadowOffsetY(): Float = 0f

    open fun onStart() {
        startMs = Screen.timeMs
    }

    fun onRender(delta: Float) {
        if (Screen.timeMs - startMs > durationMs) {
            done = true
        } else {
            doOnRender(delta)
        }
    }

    open fun doOnRender(delta: Float) { }

    fun progress() = min(1f, (Screen.timeMs - startMs).toFloat() / durationMs.toFloat())

}

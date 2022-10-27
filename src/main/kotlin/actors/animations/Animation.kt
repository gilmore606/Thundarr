package actors.animations


abstract class Animation(
    val durationMs: Long,
) {
    var done = false
    var startMs: Long = 0L

    open fun offsetX(): Float = 0f
    open fun offsetY(): Float = 0f

    open fun onStart() {
        startMs = System.currentTimeMillis()
    }

    fun onRender(delta: Float) {
        if (System.currentTimeMillis() - startMs > durationMs) {
            done = true
        } else {
            doOnRender(delta)
        }
    }

    open fun doOnRender(delta: Float) { }

    fun progress() = (System.currentTimeMillis() - startMs).toFloat() / durationMs.toFloat()

}

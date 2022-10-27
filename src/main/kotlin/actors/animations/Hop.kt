package actors.animations

class Hop : Animation(200) {

    var oy = 0f
    val reach = 0.2f

    override fun doOnRender(delta: Float) {
        var p = progress()
        if (p <= 0.5f) {
            p /= 0.5f
            oy = -1f * p * reach
        } else {
            p = (p - 0.5f) / 0.5f
            oy = -1f * (1f - p) * reach
        }
    }
    override fun offsetY() = oy

}

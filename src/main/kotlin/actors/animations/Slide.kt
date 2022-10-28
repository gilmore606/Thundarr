package actors.animations

import util.XY

class Slide(
    val dir: XY
) : Animation(90) {

    override fun offsetX() = (-1f + progress()) * dir.x
    override fun offsetY() = (-1f + progress()) * dir.y

}

package input

import ktx.app.KtxInputAdapter
import render.GameScreen

object MouseProcessor : KtxInputAdapter {
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        GameScreen.mouseMovedTo(screenX, screenY)
        return super.mouseMoved(screenX, screenY)
    }
}

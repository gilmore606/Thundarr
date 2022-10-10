package ui.input

import ktx.app.KtxInputAdapter
import render.GameScreen

object MouseProcessor : KtxInputAdapter {
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        GameScreen.mouseMovedTo(screenX, screenY)
        return super.mouseMoved(screenX, screenY)
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        GameScreen.mouseScrolled(amountY)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (GameScreen.mouseClicked(screenX, screenY)) return true
        return super.touchDown(screenX, screenY, pointer, button)
    }
}

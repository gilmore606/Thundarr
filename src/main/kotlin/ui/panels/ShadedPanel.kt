package ui.panels

import render.Screen
import render.tilesets.Glyph

open class ShadedPanel: Panel() {

    override fun drawText() { }

    override fun drawBackground() {
        Screen.uiBatch.addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            Screen.uiBatch.getTextureIndex(Glyph.PANEL_SHADOW))
    }
}

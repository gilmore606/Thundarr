package ui.panels

import render.tilesets.Glyph

open class ShadedPanel: Panel() {

    override fun drawText() { }

    override fun drawBackground() {
        boxBatch.addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            boxBatch.getTextureIndex(Glyph.PANEL_SHADOW))
    }
}

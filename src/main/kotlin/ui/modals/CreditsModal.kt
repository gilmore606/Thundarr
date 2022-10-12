package ui.modals

import render.tilesets.Glyph

class CreditsModal : Modal(450, 180, "THUNDARR the BARBARIAN") {

    override fun drawBackground() {
        super.drawBackground()
        boxBatch.addPixelQuad(x + 24 - 1, y + 56 - 1, x + 24 + 64 + 1, y + 56 + 64 + 1,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
        boxBatch.addPixelQuad(x + 24, y + 56, x + 24 + 64, y + 56 + 64,
            boxBatch.getTextureIndex(Glyph.LOGO_MOON))
        boxBatch.addPixelQuad(x + width - (24 + 64 + 1), y + 56 - 1, x + 1 + width - 24, y + 1 + 56 + 64,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
        boxBatch.addPixelQuad(x + width - (24 + 64), y + 56, x + width - 24, y + 56 + 64,
            boxBatch.getTextureIndex(Glyph.LOGO_OOKLA))
    }

    override fun drawText() {
        super.drawText()
        drawString("a homofagge production", 132, 70)
    }

    override fun mouseClicked(screenX: Int, screenY: Int) {
        dismiss()
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }
}

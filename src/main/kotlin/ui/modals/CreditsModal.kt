package ui.modals

import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse

class CreditsModal : Modal(450, 180, "ThUNdARR  the  BARBARIAN", Position.CENTER_LOW) {

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return
        Screen.uiBatch.addPixelQuad(x + 24 - 1, y + 56 - 1, x + 24 + 64 + 1, y + 56 + 64 + 1,
            Screen.uiBatch.getTextureIndex(Glyph.BOX_SHADOW))
        Screen.uiBatch.addPixelQuad(x + 24, y + 56, x + 24 + 64, y + 56 + 64,
            Screen.uiBatch.getTextureIndex(Glyph.LOGO_MOON))
        Screen.uiBatch.addPixelQuad(x + width - (24 + 64 + 1), y + 56 - 1, x + 1 + width - 24, y + 1 + 56 + 64,
            Screen.uiBatch.getTextureIndex(Glyph.BOX_SHADOW))
        Screen.uiBatch.addPixelQuad(x + width - (24 + 64), y + 56, x + width - 24, y + 56 + 64,
            Screen.uiBatch.getTextureIndex(Glyph.LOGO_OOKLA))
    }

    override fun drawModalText() {
        drawString("a dlfsystems production", 132, 70)
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        dismiss()
    }
}

package render.batches

import render.Screen

class UIBatch : QuadBatch(Screen.uiTileSet) {
    override val uiScale = true
}

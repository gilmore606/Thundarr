package render.tilesets

import com.badlogic.gdx.graphics.Texture
import world.Level
import render.tileholders.TileHolder
import util.log


class TileSet(
    textureFileName: String,
    val tilesPerRow: Int,
    val tilesPerColumn: Int,
) {
    var tileHolders: HashMap<Glyph, TileHolder> = HashMap()
    var tileRowStride = 0.0
    var tileColumnStride = 0.0

    val texture: Texture

    init {
        texture = Texture(textureFileName)
        log.info("Loaded texture $textureFileName (${texture.width} x ${texture.height})")
        tileRowStride = (1.0 / tilesPerRow)
        tileColumnStride = (1.0 / tilesPerColumn)
    }

    fun setTile(glyph: Glyph, holder: TileHolder) {
        tileHolders[glyph] = holder
    }

    fun getIndex(
        glyph: Glyph,
        level: Level? = null,
        x: Int = 0,
        y: Int = 0
    ) = tileHolders[glyph]?.getTextureIndex(level, x, y) ?: 0

    fun dispose() {
        texture.dispose()
    }
}

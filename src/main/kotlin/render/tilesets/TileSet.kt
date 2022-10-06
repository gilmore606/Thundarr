package render.tilesets

import com.badlogic.gdx.graphics.Texture
import mu.KotlinLogging
import world.Level
import render.tileholders.TileHolder
import util.Tile
import util.log


class TileSet(
    textureFileName: String,
    val tilesPerRow: Int,
    val tilesPerColumn: Int,
) {
    var tileHolders: HashMap<Tile, TileHolder> = HashMap()
    var tileRowStride = 0.0f
    var tileColumnStride = 0.0f

    val texture: Texture

    init {
        texture = Texture(textureFileName)
        log.info("Loaded texture $textureFileName (${texture.width} x ${texture.height})")
        tileRowStride = (1.0f / tilesPerRow)
        tileColumnStride = (1.0f / tilesPerColumn)
    }

    fun setTile(tile: Tile, holder: TileHolder) {
        tileHolders[tile] = holder
    }

    fun getIndex(
        tile: Tile,
        level: Level? = null,
        x: Int = 0,
        y: Int = 0
    ) = tileHolders[tile]?.getTextureIndex(level, x, y) ?: 0

    fun dispose() {
        texture.dispose()
    }
}

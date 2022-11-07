package render.tilesets

import com.badlogic.gdx.graphics.Texture
import render.tileholders.SimpleTile
import world.level.Level
import render.tileholders.TileHolder
import util.log


class TileSet(
    spriteSheet: SpriteSheets.Sheet
) {
    var tileHolders: HashMap<Glyph, TileHolder> = HashMap()

    val tilesPerRow = spriteSheet.tilesPerRow
    val tilesPerColumn = spriteSheet.tilesPerColumn
    val tileRowStride = (1.0 / tilesPerRow)
    val tileColumnStride = (1.0 / tilesPerColumn)
    val texture: Texture = SpriteSheets.sheets[spriteSheet]!!

    fun setTile(glyph: Glyph, holder: TileHolder) {
        tileHolders[glyph] = holder
    }

    fun hasGlyph(glyph: Glyph) = tileHolders.contains(glyph)

    inline fun getIndex(
        glyph: Glyph,
        level: Level? = null,
        x: Int = 0,
        y: Int = 0
    ) = tileHolders[glyph]?.getTextureIndex(level, x, y) ?: 0

    fun dispose() {
        // nothing to do anymore
    }

    // Return all texture indices a QuadBatch can safely cache on startup (because they're SimpleTile or other unchanging tile).
    fun getCache(): Map<Glyph,Int> {
        val cache = mutableMapOf<Glyph,Int>()
        tileHolders.forEach { glyph, holder ->
            if (holder is SimpleTile) {
                cache[glyph] = holder.getTextureIndex(null, 0, 0)
            }
        }
        return cache
    }
}

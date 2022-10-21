package render.tileholders

import render.tilesets.TileSet
import world.Level

open class AnimatedTile(
    set: TileSet
): TileHolder(set) {

    private val frames = ArrayList<Pair<Int, Int>>()

    var frameMs = 350

    fun add(tx: Int, ty: Int) {
        frames.add(Pair(tx,ty))
    }

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        level?.also { level ->
            val frame = (System.currentTimeMillis() / frameMs + level.getRandom(x,y)).toInt() % frames.size
            return indexFromCoords(frames[frame].first, frames[frame].second)
        }
        return 0
    }

}

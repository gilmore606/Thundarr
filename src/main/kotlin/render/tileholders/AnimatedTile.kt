package render.tileholders

import render.Screen
import render.tilesets.TileSet
import world.level.Level
import kotlin.math.abs

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
            val frame = abs((Screen.timeMs / frameMs + level.getRandom(x,y)).toInt()) % frames.size
            return indexFromCoords(frames[frame].first, frames[frame].second)
        }
        return 0
    }

}

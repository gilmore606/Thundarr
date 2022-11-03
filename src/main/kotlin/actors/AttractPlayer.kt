package actors

import actors.actions.Action
import actors.actions.Move
import render.tilesets.Glyph
import util.DIRECTIONS
import util.XY

class AttractPlayer : Player() {

    override fun glyph() = Glyph.MOK

    override fun defaultAction(): Action? {
        return wander()
    }

    fun wander(): Action? {
        val dirs = mutableListOf<XY>()
        level?.also { level ->
            DIRECTIONS.forEach { dir ->
                if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y)) {
                    dirs.add(dir)
                }
            }
        }
        if (dirs.isNotEmpty()) {
            val dir = dirs.random()
            return Move(dir)
        }
        return null
    }
}

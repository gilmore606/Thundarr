package actors

import actors.actions.Action
import actors.actions.Move
import render.tilesets.Glyph
import util.DIRECTIONS
import util.Dice
import util.XY
import kotlin.random.Random

class AttractPlayer : Player() {

    var lastActionMs = System.currentTimeMillis()

    override fun glyph() = Glyph.MOK

    override fun nextAction() = super.nextAction() ?: defaultAction()
    override fun canAct() = true

    override fun defaultAction(): Action? {
        if (System.currentTimeMillis() - lastActionMs > 1000L) {
            lastActionMs = System.currentTimeMillis() - Random.nextLong(800L)
            return wander()
        }
        return null
    }

    val lastDirs = mutableListOf<XY>()

    fun wander(): Action? {
        val dirs = mutableListOf<XY>()
        level?.also { level ->
            lastDirs.forEach { dir ->
                if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y)) {
                    dirs.add(dir)
                }
            }
            if (lastDirs.isEmpty() || Dice.chance(0.2f)) {
                DIRECTIONS.forEach { dir ->
                    if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y)) {
                        dirs.add(dir)
                    }
                }
            }
        }
        if (dirs.isNotEmpty()) {
            val dir = dirs.random()
            lastDirs.add(dir)
            if (lastDirs.size > 3) lastDirs.removeFirst()
            return Move(dir)
        }
        return null
    }
}

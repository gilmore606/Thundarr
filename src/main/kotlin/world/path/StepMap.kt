package world.path

import actors.Actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import util.DIRECTIONS
import util.Rect
import util.XY
import util.log
import world.Entity
import world.level.Level

open class StepMap {

    val subscribers = mutableSetOf<Entity>()
    var range: Float = 1f
    var width = (range * 2f).toInt()
    var height = width
    var offsetX = 0
    var offsetY = 0 // offset to level coords
    var outOfDate = true
    var done = false
    var level: Level? = null

    protected var scratch = Array(1) { IntArray(1) { -1 } }
    var map = Array(1) { IntArray(1) { -1 } }

    fun addSubscriber(subscriber: Entity, range: Float) {
        if (subscriber !in subscribers) {
            subscribers.add(subscriber)
            done = false
        }
        if (range > this.range) {
            changeRange(range)
            done = false
        }
    }

    fun removeSubscriber(subscriber: Entity) {
        subscribers.remove(subscriber)
        if (subscribers.isEmpty()) { done = true }
    }

    open fun dispose() {
        scratch = Array(1) { IntArray(1) { 0 } }
        map = Array(1) { IntArray(1) { 0 } }
    }

    fun changeRange(newRange: Float) {
        val nextRange = java.lang.Float.min(Pather.maxRange, newRange)
        if (nextRange != range) {
            range = nextRange
            log.debug("map $map changing range to $range")
            scratch = Array((range * 2f).toInt() + 2) { IntArray((range * 2f).toInt() + 2) { -1 } }
            map = Array((range * 2f).toInt() + 2) { IntArray((range * 2f).toInt() + 2) { -1 } }
            width = (range * 2f).toInt()
            height = width
            outOfDate = true
        }
    }

    open fun onActorMove(actor: Actor) { }

    open fun nextStep(from: XY, to: Entity): XY? = null
    open fun nextStep(from: Entity, to: XY): XY? = null
    open fun nextStep(from: Entity, to: Rect): XY? = null

    open fun canReach(to: Entity) = false
    open fun canReach(to: Rect) = false

    protected fun clearScratch() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                scratch[x][y] = -1
            }
        }
    }

    protected fun promoteScratch() {
        KtxAsync.launch {
            val old = map
            map = scratch
            scratch = old
        }
    }

    private suspend fun waitForActorLock(level: Level) {
        while (level.director.actorsLocked) {
            log.info("...stepMap waiting for actors lock...")
            delay(0L)
        }
    }
    private suspend fun waitForCellLock(level: Level, x: Int, y: Int) {
        while (level.hasCellContainerAt(x, y)?.locked == true) {
            log.info("...stepMap waiting for cell lock...")
            delay(0L)
        }
    }

    open suspend fun update() {
        level?.also { level ->
            val walker = subscribers.firstOrNull()?.let { if (it is Actor) it else null }
            var dirty = true
            var step = 0
            while (dirty) {
                dirty = false
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        if (scratch[x][y] == step) {
                            DIRECTIONS.forEach { dir ->
                                val tx = x + dir.x
                                val ty = y + dir.y
                                if (tx in 0 until width && ty in 0 until height) {
                                    if (scratch[tx][ty] < 0) {
                                        //waitForActorLock(level) don't need this since actors don't block walkable
                                        waitForCellLock(level, tx, ty)
                                        if (level.isPathableBy(walker ?: App.player, x + offsetX + dir.x, y + offsetY + dir.y)) {
                                            scratch[tx][ty] = step + 1
                                            dirty = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                step++
            }
            // Buffer swap
            promoteScratch()
            outOfDate = false
        }
    }
}

package world.path

import actors.Actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import util.*
import world.Entity
import world.level.Level
import java.lang.Float.min

// This should be DijkstraMap but who wants to type that guy's name all the time?

class StepMap() {

    val subscribers = mutableSetOf<Entity>()
    var targetEntity: Entity? = null
    var range: Float = 1f
    var width = (range * 2f).toInt()
    var height = width
    var offsetX = 0
    var offsetY = 0 // offset to level coords

    var outOfDate = true
    var done = false

    private var scratch = Array(1) { IntArray(1) { -1 } }
    var map = Array(1) { IntArray(1) { -1 } }

    fun addSubscriber(subscriber: Entity, range: Float) {
        subscribers.add(subscriber)
        done = false
        if (range > this.range) {
            changeRange(range)
        }
    }

    suspend fun removeSubscriber(subscriber: Entity) {
        subscribers.remove(subscriber)
        if (subscribers.isEmpty()) { done = true }
    }

    suspend fun dispose() {
        targetEntity = null
        scratch = Array(1) { IntArray(1) { 0 } }
        map = Array(1) { IntArray(1) { 0 } }
    }

    fun changeRange(newRange: Float) {
        val nextRange = min(Pather.maxRange, newRange)
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

    fun setTargetToEntity(newTarget: Entity) {
        targetEntity = newTarget
        val centerX = width / 2 + 1
        val centerY = height / 2 + 1
        offsetX = targetEntity!!.xy()!!.x - centerX
        offsetY = targetEntity!!.xy()!!.y - centerY
        outOfDate = true
    }

    fun onActorMove(actor: Actor) {
        if (actor == this.targetEntity) outOfDate = true
    }

    override fun toString() = "StepMap(target=${targetEntity?.name()})"

    suspend fun update() {
        targetEntity?.level()?.also { level ->
            clearScratch()
            var step = 0
            val centerX = width / 2 + 1
            val centerY = height / 2 + 1
            scratch[centerX][centerY] = 0
            offsetX = targetEntity!!.xy()!!.x - centerX
            offsetY = targetEntity!!.xy()!!.y - centerY
            var dirty = true
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
                                        if (level.isPathableBy(targetEntity, x + offsetX + dir.x, y + offsetY + dir.y)) {
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
        }
        outOfDate = false
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

    private fun clearScratch() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                scratch[x][y] = -1
            }
        }
    }

    private fun promoteScratch() {
        KtxAsync.launch {
            val old = map
            map = scratch
            scratch = old
        }
    }

    fun nextStep(from: XY, to: Entity): XY? {
        if (to == targetEntity) {
            val lx = from.x - offsetX
            val ly = from.y - offsetY
            if (lx in 0 until width && ly in 0 until height) {
                val nextstep = map[lx][ly] - 1
                if (nextstep < 0) return null
                var step: XY? = null
                DIRECTIONS.from(lx, ly) { tx, ty, _ ->
                    if (tx in 0 until width && ty in 0 until height) {
                        if (map[tx][ty] == nextstep) {
                            step = XY(tx + offsetX, ty + offsetY)
                        }
                    }
                }
                step?.also { return it }
            }
        }
        return null
    }

    fun nextStep(from: Entity, to: XY): XY? {
        if (from == targetEntity) {
            val lx = to.x - offsetX
            val ly = to.y - offsetY
            log.info("path $lx $ly")
            if (lx in 0 until width && ly in 0 until height) {
                log.info("pathing to $to")
                val feet = XY(lx, ly)
                var step = map[feet.x][feet.y] -1
                var foundDir: XY? = null

                while (step >= 0) {
                    foundDir = null
                    DIRECTIONS.from(feet.x, feet.y) { dx, dy, dir ->
                        if (dx in 0 until width && dy in 0 until height) {
                            if (foundDir == null && map[dx][dy] == step) {
                                feet.x = dx
                                feet.y = dy
                                foundDir = dir
                            }
                        }
                    }
                    if (foundDir == null) { return null }
                    step = map[feet.x][feet.y] - 1
                }
                if (foundDir == null) { return null }
                return XY(-(foundDir!!.x), -(foundDir!!.y))
            }
        }
        return null
    }

    fun canReach(to: Entity) = to == targetEntity

    fun debugStepAt(x: Int, y: Int): Int {
        val lx = x - offsetX
        val ly = y - offsetY
        if (lx in 0 until width && ly in 0 until height) {
            return map[lx][ly]
        }
        return 0
    }
}

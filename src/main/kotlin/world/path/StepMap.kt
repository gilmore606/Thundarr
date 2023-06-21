package world.path

import App
import actors.Actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import util.*
import world.Entity
import world.level.Level

@Serializable
abstract class StepMap {

    @Transient var walker: Actor? = null
    var walkerID: String = ""
    var range: Int = 1
    var width = 2
    var height = 2
    var offsetX = 0
    var offsetY = 0 // offset to level coords

    var dirty = true  // Do we need an update?
    var expired = false  // Can we be disposed of?

    var scratch = Array(width) { IntArray(height) { -1 } }
    var map = Array(width) { IntArray(height) { -1 } }

    open fun init(walker: Actor, range: Int) {
        this.walker = walker
        this.walkerID = walker.id
        this.range = range
        this.width = range * 2
        this.height = range * 2
        scratch = Array(width) { IntArray(height) { -1 } }
        map = Array(width) { IntArray(height) { -1 } }
        onActorMove(walker)
    }

    open fun dispose() {
        scratch = Array(1) { IntArray(1) { 0 } }
        map = Array(1) { IntArray(1) { 0 } }
    }

    open fun onActorMove(actor: Actor) {
        if (actor.id == walkerID) {
            dirty = true
            offsetX = actor.xy.x - width / 2
            offsetY = actor.xy.y - height / 2
        }
    }

    // Write step 0 into target cells on scratch
    protected open fun printTarget() { }

    open fun nextStep(from: XY, to: Entity): XY? = null
    open fun nextStep(from: Actor, to: XY): XY? = null
    open fun nextStep(from: Actor, to: Rect): XY? = null
    open fun nextStep(from: Actor, to: Actor): XY? = null

    protected fun getNextStep(fromX: Int, fromY: Int): XY? {
        val lx = fromX - offsetX
        val ly = fromY - offsetY
        if (lx in 0 until width && ly in 0 until height) {
            val nextstep = map[lx][ly] - 1
            if (nextstep < 0) return null
            var steps = mutableSetOf<XY>()
            DIRECTIONS.from(lx, ly) { tx, ty, dir ->
                if (tx in 0 until width && ty in 0 until height) {
                    if (map[tx][ty] == nextstep) {
                        steps.add(dir)
                    }
                }
            }
            if (steps.isNotEmpty()) return steps.random()
        }
        return null
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

    inline fun writeTargetCell(x: Int, y: Int) {
        val ix = x - offsetX
        val iy = y - offsetY
        if ((ix in 0..width - 1) && (iy in 0..height - 1)) {
            scratch[ix][iy] = 0
        }
    }

    suspend fun update() {
        if (walker == null) {
            walker = App.level.director.getActor(walkerID)
        }
        walker?.level?.also { level ->
            clearScratch()
            printTarget()
            var step = 0
            var notDone = true
            while (notDone) {
                notDone = false
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
                                        if (level.isPathableBy(walker!!, x + offsetX + dir.x, y + offsetY + dir.y)) {
                                            scratch[tx][ty] = step + 1
                                            notDone = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                step++
            }
            promoteScratch()
            dirty = false
        }
    }
}

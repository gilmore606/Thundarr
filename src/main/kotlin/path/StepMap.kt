package path

import App
import actors.actors.Actor
import actors.actors.NPC
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import util.*
import world.Entity
import world.level.Level

@Serializable
sealed class StepMap {

    companion object {
        const val expireAge = 8.0
    }

    var walkerID: String = ""
    var range: Int = 1
    var width = 2
    var height = 2
    var offset = XY(0,0) // offset to level coords

    var lastConsultTime: Double = App.gameTime.time

    // We need this because the child data classes' copy method can't see the non-data sealed parent class
    // Like most terrible things I've done, this is for serialization
    protected fun prepareCopy(copy: StepMap) = copy.also {
        it.walkerID = walkerID
        it.range = range
        it.width = width
        it.height = height
        it.offset.setTo(offset)
        it.lastConsultTime = lastConsultTime
    }

    @Transient var walker: Actor? = null
    @Transient var dirty = true  // Do we need an update?
    @Transient protected var expired = false  // Can we be disposed of?
    @Transient var updating = false

    @Transient var scratch = Array(width) { IntArray(height) { -1 } }
    @Transient var map = Array(width) { IntArray(height) { -1 } }

    abstract fun getClone(): StepMap

    fun isActive() = !expired
    fun needsUpdated() = !expired && dirty && !updating
    fun expire() {
        expired = true
    }

    open fun initialize(walker: Actor, range: Int) {
        this.walker = walker
        this.walkerID = walker.id
        this.range = range
        this.width = range * 2
        this.height = range * 2
        scratch = Array(width) { IntArray(height) { -1 } }
        map = Array(width) { IntArray(height) { -1 } }
        lastConsultTime = App.gameTime.time
        onActorMove(walker)
    }

    open fun dispose() {
        scratch = Array(1) { IntArray(1) { 0 } }
        map = Array(1) { IntArray(1) { 0 } }
    }

    open fun onRestore(forActor: Actor) {
        this.walker = forActor
        this.walkerID = forActor.id
        this.dirty = true
        this.expired = false
        scratch = Array(width) { IntArray(height) { -1 } }
        map = Array(width) { IntArray(height) { -1 } }
    }

    open fun onActorMove(actor: Actor) {
        if (actor.id == walkerID) {
            dirty = true
            offset.x = actor.xy.x - width / 2
            offset.y = actor.xy.y - height / 2
        }
    }

    // Write step 0 into target cells on scratch
    protected open fun printTarget() { }

    open fun nextStep(from: XY, to: Entity): XY? = null
    open fun nextStep(from: Actor, to: XY): XY? = null
    open fun nextStep(from: Actor, to: Rect): XY? = null
    open fun nextStep(from: Actor, to: Actor): XY? = null
    open fun nextStepAwayFrom(from: Actor, to: Actor): XY? = null

    protected fun getNextStep(walker: Actor, fromX: Int, fromY: Int): XY? {
        if (expired) return null
        lastConsultTime = App.gameTime.time
        val lx = fromX - offset.x
        val ly = fromY - offset.y
        var passNeighborStep: XY? = null
        if (lx in 0 until width && ly in 0 until height) {
            val nextstep = map[lx][ly] - 1
            if (nextstep < 0) return null
            val steps = mutableSetOf<XY>()
            val altSteps = mutableSetOf<XY>()
            DIRECTIONS.from(lx, ly) { tx, ty, dir ->
                if (tx in 0 until width && ty in 0 until height) {
                    if (map[tx][ty] == nextstep) {
                        if (walker.level?.isWalkableFrom(walker, fromX, fromY, dir) == true) steps.add(dir)
                        else if (walker.level?.actorAt(fromX+dir.x, fromY+dir.y) is NPC) passNeighborStep = dir
                    } else if (map[tx][ty] == nextstep - 1) {
                        if (walker.level?.isWalkableFrom(walker, fromX, fromY, dir) == true) altSteps.add(dir)
                    }
                }
            }
            if (steps.isNotEmpty()) return steps.random()
            if (altSteps.isNotEmpty()) return altSteps.random()
        }
        return passNeighborStep
    }

    protected fun getNextStepAway(walker: Actor, fromX: Int, fromY: Int): XY? {
        if (expired) return null
        lastConsultTime = App.gameTime.time
        val lx = fromX - offset.x
        val ly = fromY - offset.y
        if (lx in 0 until width && ly in 0 until height) {
            val nextstep = map[lx][ly] + 1
            val steps = mutableSetOf<XY>()
            val altSteps = mutableSetOf<XY>()
            DIRECTIONS.from(lx, ly) { tx, ty, dir ->
                if (tx in 0 until width && ty in 0 until height) {
                    if (map[tx][ty] == nextstep) {
                        if (walker.level?.isWalkableFrom(walker, fromX, fromY, dir) == true) steps.add(dir)
                    } else if (map[tx][ty] == nextstep - 1) {
                        if (walker.level?.isWalkableFrom(walker, fromX, fromY, dir) == true) altSteps.add(dir)
                    }
                }
            }
            if (steps.isNotEmpty()) return steps.random()
            if (altSteps.isNotEmpty()) return altSteps.random()
        }
        return null
    }

    private fun clearScratch() {
        forXY(0,0, width-1,height-1) { x,y ->
            scratch[x][y] = -1
        }
    }

    private fun promoteScratch() {
        KtxAsync.launch {
            val old = map
            map = scratch
            scratch = old
        }
    }

    private suspend fun waitForCellLock(level: Level, x: Int, y: Int) {
        while (level.hasCellContainerAt(x, y)?.locked == true) {
            log.info("...stepMap waiting for cell lock...")
            delay(0L)
        }
    }

    inline fun writeTargetCell(x: Int, y: Int) {
        val ix = x - offset.x
        val iy = y - offset.y
        if ((ix in 0 until width) && (iy in 0 until height)) {
            scratch[ix][iy] = 0
        }
    }

    suspend fun updateAge() {
        if (App.gameTime.time - lastConsultTime > expireAge) {
            //log.info("expiring $this (not consulted in $expireAge turns)")
            expire()
        }
    }

    suspend fun update() {
        if (walker == null) {
            walker = App.level.director.getActor(walkerID)
        }
        if (!dirty) return
        if (expired) return
        val level = walker?.level ?: run { return }
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
                                    waitForCellLock(level, tx, ty)
                                    if (level.isPathableBy(walker!!, x + offset.x + dir.x, y + offset.y + dir.y)) {
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
        KtxAsync.launch { updating = false }
    }
}

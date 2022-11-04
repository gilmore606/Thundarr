package world.path

import actors.Actor
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import util.*
import world.Entity
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
    var inSurgery = false
    var done = false

    private var scratch = Array(1) { Array(1) { -1 } }
    var map = Array(1) { Array(1) { -1 } }

    suspend fun addSubscriber(subscriber: Entity, range: Float) {
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
        scratch = Array(1) { Array(1) { 0 } }
        map = Array(1) { Array(1) { 0 } }
    }

    suspend fun changeRange(newRange: Float) {
        val nextRange = min(Pather.maxRange, newRange)
        if (nextRange != range) {
            range = nextRange
            log.debug("map $map changing range to $range")
            scratch = Array((range * 2f).toInt() + 1) { Array((range * 2f).toInt() + 1) { -1 } }
            map = Array((range * 2f).toInt() + 1) { Array((range * 2f).toInt() + 1) { -1 } }
            width = (range * 2f).toInt()
            height = width
            outOfDate = true
        }
    }

    suspend fun setTargetToEntity(newTarget: Entity) {
        targetEntity = newTarget
        outOfDate = true
    }

    fun onActorMove(actor: Actor) {
        if (actor == this.targetEntity) {
            Pather.coroutineScope.launch { outOfDate = true }
        }
    }

    override fun toString() = "StepMap(target=${targetEntity?.name()})"

    suspend fun update() {
        targetEntity?.level()?.also { level ->
            log.debug("updating map $this")
            inSurgery = true
            clearScratch()
            var step = 0
            val centerX = width / 2
            val centerY = height / 2
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
                                    if (scratch[x + dir.x][y + dir.y] < 0) {
                                        if (level.isWalkableAt(x + dir.x + offsetX, y + dir.y + offsetY)) {
                                            scratch[x + dir.x][y + dir.y] = step + 1
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
            promoteScratch()
        }
        outOfDate = false
        inSurgery = false
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
                CARDINALS.forEach { dir ->
                    if (map[lx+dir.x][ly+dir.y] == nextstep) {
                        return XY(lx+dir.x+offsetX, ly+dir.y+offsetY)
                    }
                }
                DIAGONALS.forEach { dir ->
                    if (map[lx+dir.x][ly+dir.y] == nextstep) {
                        return XY(lx+dir.x+offsetX, ly+dir.y+offsetY)
                    }
                }
            }
        }
        return null
    }

    fun nextStep(from: Entity, to: XY): XY? {
        if (from == targetEntity) {
            val lx = to.x - offsetX
            val ly = to.y - offsetY
            if (lx in 0 until width && ly in 0 until height) {
                val feet = XY(lx, ly)
                var step = map[feet.x][feet.y]
                var found = false
                while (step > 0) {
                    step--
                    CARDINALS.forEach { dir ->
                        if (!found && map[lx+dir.x][ly+dir.y] == step) {
                            feet.x = lx + dir.x
                            feet.y = ly + dir.y
                            found = true
                        }
                    }
                    if (!found) DIAGONALS.forEach { dir ->
                        if (!found && map[lx+dir.x][ly+dir.y] == step) {
                            feet.x = lx + dir.x
                            feet.y = ly + dir.y
                            found = true
                        }
                    }
                    if (!found) {
                        return null // lost our way??
                    } else if (step == 1) {
                        feet.x += offsetX
                        feet.y += offsetY
                        return feet
                    }
                }
            }
        }
        return null
    }

    fun canReach(to: Entity) = to == targetEntity
}

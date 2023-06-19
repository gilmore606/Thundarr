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

class EntityStepMap(): StepMap() {

    var targetEntity: Entity? = null

    override fun dispose() {
        targetEntity = null
        super.dispose()
    }

    fun setTargetToEntity(newTarget: Entity) {
        targetEntity = newTarget
        val centerX = width / 2 + 1
        val centerY = height / 2 + 1
        offsetX = targetEntity!!.xy()!!.x - centerX
        offsetY = targetEntity!!.xy()!!.y - centerY
        outOfDate = true
    }

    override fun onActorMove(actor: Actor) {
        if (actor == this.targetEntity) outOfDate = true
    }

    override fun toString() = "EntityStepMap(target=${targetEntity?.name()})"

    override suspend fun update() {
        targetEntity?.level()?.also { level ->
            val centerX = width / 2 + 1
            val centerY = height / 2 + 1
            clearScratch()
            scratch[centerX][centerY] = 0
            offsetX = targetEntity!!.xy().x - centerX
            offsetY = targetEntity!!.xy().y - centerY
            this.level = level
            super.update()
        }
    }

    override fun nextStep(from: XY, to: Entity): XY? {
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

    override fun nextStep(from: Entity, to: XY): XY? {
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

    override fun canReach(to: Entity) = to == targetEntity

}

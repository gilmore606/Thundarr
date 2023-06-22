package world.path

import actors.Actor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.*
import world.Entity
import world.level.Level
import java.lang.RuntimeException

object Pather {

    private val maps = mutableListOf<StepMap>()

    private val coroutineContext = newSingleThreadAsyncContext("pather")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var worker: Job? = null
    private var jobs = mutableSetOf<Job>()

    init {
        relaunchWorker()
    }

    private fun relaunchWorker() {
        worker?.cancel()
        worker = coroutineScope.launch {
            while (!App.isExiting) {
                var doneMap: StepMap? = null
                for (i in 0 until maps.size) {
                    if (i < maps.size) {
                        val map = maps[i]
                        if (map.expired) doneMap = map
                        else if (map.dirty) {
                            map.update()
                        }
                    }
                }
                doneMap?.also {
                    it.dispose()
                    KtxAsync.launch {
                        maps.remove(it)
                    }
                }
                delay(1L)
            }
        }
    }

    fun nextStep(from: XY, to: Entity): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }
    fun nextStep(from: Actor, to: XY): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }
    fun nextStep(from: Actor, to: Rect): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }
    fun nextStep(from: Actor, to: Actor): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }
    fun nextStepAwayFrom(from: Actor, to: Actor): XY? = maps.firstNotNullOfOrNull { it.nextStepAwayFrom(from, to) }

    fun buildPath(from: XY, to: Entity) = maps.firstOrNull { false }.let { map ->
        mutableListOf<XY>().apply {
            val feet = XY(from.x, from.y)
            while (feet.x != to.xy().x || feet.y != to.xy().y) {
                nextStep(feet, to)?.also {
                    add(it)
                    feet.x = it.x
                    feet.y = it.y
                } ?: run { return this }
            }
            return this
        }
    }

    fun subscribe(walker: Actor, target: XY, range: Int) {
        if (!maps.hasOneWhere { it is PointStepMap && it.target == target && it.walkerID == walker.id }) {
            val map = PointStepMap().apply {
                init(walker, range, target)
            }
            maps.add(map)
        }
    }

    fun subscribe(walker: Actor, target: Rect, range: Int) {
        if (!maps.hasOneWhere { it is AreaStepMap && it.target == target && it.walkerID == walker.id }) {
            val map = AreaStepMap().apply {
                init(walker, range, target)
            }
            maps.add(map)
        }
    }

    fun subscribe(walker: Actor, target: Actor, range: Int) {
        if (!maps.hasOneWhere { it is ActorStepMap && it.targetID == target.id && it.walkerID == walker.id }) {
            val map = ActorStepMap().apply {
                init(walker, range, target)
            }
            maps.add(map)
        }
    }

    fun unsubscribe(walker: Actor, target: XY) {
        maps.firstOrNull { it is PointStepMap && it.target == target && it.walkerID == walker.id }?.also {
            it.expired = true
        }
    }

    fun unsubscribe(walker: Actor, target: Actor) {
        maps.firstOrNull { it is ActorStepMap && it.targetID == target.id && it.walkerID == walker.id }?.also {
            it.expired = true
        }
    }

    fun unsubscribe(walker: Actor, target: Rect) {
        maps.firstOrNull { it is AreaStepMap && it.target == target && it.walkerID == walker.id }?.also {
            it.expired = true
        }
    }

    fun unsubscribeAll(subscriber: Actor) {
        jobs.add(coroutineScope.launch {
            maps.forEach { if (it.walkerID == subscriber.id) it.expired = true }
        })
    }

    private suspend fun waitForActorLock(level: Level) {
        while (level.director.actorsLocked) {
            log.info("...Pather waiting for actors lock...")
            delay(0L)
        }
    }

    // Hooks for hearing about relevant world changes.

    fun onActorMove(actor: Actor) {
        if (worker?.isActive == false || worker == null) throw RuntimeException("Pather has crashed!")

        coroutineScope.launch {
            maps.safeForEach { it.onActorMove(actor) }
        }
    }

}

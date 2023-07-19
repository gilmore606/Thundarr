package path

import actors.actors.Actor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.*
import world.Entity
import java.lang.RuntimeException

object Pather {

    private var maps = mutableListOf<StepMap>()

    private val coroutineContext = newSingleThreadAsyncContext("pather")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var worker: Job? = null
    private var jobs = mutableSetOf<Job>()

    init {
        relaunchWorker()
    }

    private fun relaunchWorker() {
        worker?.cancel()
        worker = KtxAsync.launch {
            while (!App.isExiting) {
                val newMaps = mutableListOf<StepMap>()
                var updates = 0
                maps.forEach { map ->
                    if (map.isActive()) {
                        newMaps.add(map)
                        if (map.needsUpdated()) {
                            updates++
                            coroutineScope.launch { map.update() }
                        }
                    } else {
                        map.dispose()
                    }
                }
                maps = newMaps
                if (updates > 0) {
                    log.info("PATHER: updated $updates maps (${maps.size} alive)")
                    //log.info("PATHER: $maps")
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
            while (feet != to.xy()) {
                nextStep(feet, to)?.also {
                    add(it)
                    feet.setTo(it)
                } ?: run { return this }
            }
            return this
        }
    }

    fun subscribe(walker: Actor, target: XY, range: Int) {
        if (!maps.hasOneWhere { it is PointStepMap && it.target == target && it.walkerID == walker.id }) {
            val map = PointStepMap(target).apply {
                initialize(walker, range)
            }
            maps.add(map)
        }
    }

    fun subscribe(walker: Actor, target: Rect, range: Int) {
        if (!maps.hasOneWhere { it is AreaStepMap && it.target == target && it.walkerID == walker.id }) {
            val map = AreaStepMap(target).apply {
                initialize(walker, range)
            }
            maps.add(map)
        }
    }

    fun subscribe(walker: Actor, target: Actor, range: Int) {
        if (!maps.hasOneWhere { it is ActorStepMap && it.targetID == target.id && it.walkerID == walker.id }) {
            val map = ActorStepMap(target.id).apply {
                initialize(walker, range)
            }
            maps.add(map)
        }
    }

    fun unsubscribe(walker: Actor, target: XY) {
        maps.firstOrNull { it is PointStepMap && it.target == target && it.walkerID == walker.id }?.also {
            it.expire()
        }
    }

    fun unsubscribe(walker: Actor, target: Actor) {
        maps.firstOrNull { it is ActorStepMap && it.targetID == target.id && it.walkerID == walker.id }?.also {
            it.expire()
        }
    }

    fun unsubscribe(walker: Actor, target: Rect) {
        maps.firstOrNull { it is AreaStepMap && it.target == target && it.walkerID == walker.id }?.also {
            it.expire()
        }
    }

    fun unsubscribeAll(subscriber: Actor) {
        maps.safeForEach { if (it.walkerID == subscriber.id) it.expire() }
    }

    fun saveActorMaps(subscriber: Actor) {
        subscriber.savedStepMaps.clear()
        KtxAsync.launch {
            maps.safeForEach { map ->
                if (map.walkerID == subscriber.id) {
                    subscriber.savedStepMaps.add(map.getClone())
                    map.expire()
                }
            }
        }
    }

    fun restoreActorMaps(subscriber: Actor) {
        if (subscriber.savedStepMaps.isNotEmpty()) {
            KtxAsync.launch {
                subscriber.savedStepMaps.forEach { map ->
                    if (!maps.hasOneWhere { it == map }) {
                        map.onRestore(subscriber)
                        maps.add(map)
                    }
                }
                subscriber.savedStepMaps.clear()
            }
        }
    }

    // Hooks for hearing about relevant world changes.

    fun onActorMove(actor: Actor) {
        if (worker?.isActive == false || worker == null) throw RuntimeException("Pather has crashed!")
        maps.safeForEach { it.onActorMove(actor) }
    }

}

package path

import actors.actors.Actor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.*
import java.lang.RuntimeException

object Pather {

    private var maps = mutableListOf<StepMap>()

    private val coroutineContext = newSingleThreadAsyncContext("pather")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var worker: Job? = null
    private var jobs = mutableSetOf<Job>()
    var lastUpdated: Int = 0

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
                            map.updating = true
                            coroutineScope.launch {
                                map.updateAge()
                                map.update()
                            }
                        } else {
                            coroutineScope.launch {
                                map.updateAge()
                            }
                        }
                    } else {
                        map.dispose()
                    }
                }
                maps = newMaps
                if (updates > 0) {
                    //log.info("PATHER: updated $updates maps (${maps.size} alive)")
                    //log.info("PATHER: $maps")
                    if (updates > 10) {
                        log.info("PATHER: updated $updates maps (${maps.size} alive)")
                    }
                }
                lastUpdated = updates
                delay(1L)
            }
        }
    }

    fun nextStep(from: Actor, to: XY): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) } ?: newRequest(from, to)
    fun nextStep(from: Actor, to: Rect): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) } ?: newRequest(from, to)
    fun nextStep(from: Actor, to: Actor): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) } ?: newRequest(from, to)
    fun nextStepAwayFrom(from: Actor, to: Actor): XY? = maps.firstNotNullOfOrNull { it.nextStepAwayFrom(from, to) } ?: newRequest(from, to, true)

    private fun newRequest(from: Actor, to: XY): XY? {
        if (maps.hasOneWhere { it is PointStepMap && it.walkerID == from.id && it.target == to}) return null
        val map = PointStepMap(to).apply {
            initialize(from, 32)
        }
        maps.add(map)
        return fakeFirstStep(from, to)
    }

    private fun newRequest(from: Actor, to: Rect): XY? {
        if (maps.hasOneWhere { it is AreaStepMap && it.walkerID == from.id && it.target == to }) return null
        val map = AreaStepMap(to).apply {
            initialize(from, 64)
        }
        maps.add(map)
        return fakeFirstStep(from, to.center())
    }

    private fun newRequest(from: Actor, to: Actor, isAway: Boolean = false): XY? {
        if (maps.hasOneWhere { it is ActorStepMap && it.walkerID == from.id && it.targetID == to.id }) return null
        val map = ActorStepMap(to.id).apply {
            initialize(from, 48)
        }
        maps.add(map)
        return fakeFirstStep(from, to.xy, isAway)
    }

    private fun fakeFirstStep(from: Actor, to: XY, isAway: Boolean = false): XY? {
        from.level?.also { level ->
            DIRECTIONS.between(from.xy, to)?.also { toDir ->
                val dir = if (isAway) toDir.flipped() else toDir
                if (level.isWalkableFrom(from, from.xy, dir)) return dir
                else dir.dirPlusDiagonals().forEach { altDir ->
                    if (level.isWalkableFrom(from, from.xy, altDir)) return altDir
                }
            }
        }
        return null
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

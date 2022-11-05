package world.path

import actors.Actor
import actors.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.newSingleThreadAsyncContext
import util.RayCaster
import util.XY
import util.log
import world.Entity
import java.lang.RuntimeException

object Pather {

    private val maps = mutableSetOf<StepMap>()
    const val maxRange = 100f

    private val coroutineContext = newSingleThreadAsyncContext("pather")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var worker: Job? = null
    private var jobs = mutableSetOf<Job>()

    private val caster = RayCaster()

    private var playerMap: StepMap? = null

    init {
        relaunchWorker()
    }

    private fun relaunchWorker() {
        worker?.cancel()
        worker = coroutineScope.launch {
            while (true) {
                var doneMap: StepMap? = null
                maps.forEach { map ->
                    if (map.done) doneMap = map
                    else if (map.outOfDate) {
                        map.update(caster)
                    }
                }
                doneMap?.also {
                    log.info("pruning done map $it")
                    it.dispose()
                    maps.remove(it)
                }
                delay(1L)
            }
        }
    }

    fun nextStep(from: Entity, to: Entity): XY? = from.xy()?.let { xy -> maps.firstNotNullOfOrNull { it.nextStep(xy, to) } }

    fun nextStep(from: XY, to: Entity): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }

    fun nextStep(from: Entity, to: XY): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }

    fun buildPath(from: XY, to: Entity) = maps.firstOrNull { it.canReach(to) }.let { map ->
        mutableListOf<XY>().apply {
            val feet = XY(from.x, from.y)
            while (feet.x != to.xy()!!.x || feet.y != to.xy()!!.y) {
                nextStep(feet, to)?.also {
                    add(it)
                    feet.x = it.x
                    feet.y = it.y
                } ?: run { return this }
            }
            return this
        }
    }

    fun entitiesSeenBy(entity: Entity, matching: ((Entity)->Boolean)? = null): Map<Entity, Float> {
        maps.firstNotNullOfOrNull { it.entitiesSeenBy(entity) }?.also { allVisible ->
            matching?.also { matching ->
                val filtered = mutableMapOf<Entity, Float>()
                allVisible.keys.forEach { if (matching(it)) filtered[it] = allVisible[it]!! }
                return filtered
            } ?: run { return allVisible }
        }
        return mutableMapOf()
    }

    fun subscribe(subscriber: Entity, target: Entity, range: Float) {
        jobs.add(coroutineScope.launch {
            log.info("subscribing $subscriber @ $range for target $target")
            val map = maps.firstOrNull { it.targetEntity == target } ?: StepMap().apply {
                setTargetToEntity(target)
                changeRange(range)
                maps.add(this@apply)
                if (target is Player) {
                    playerMap = this@apply
                }
            }
            map.addSubscriber(subscriber, range)
        })
    }

    fun unsubscribe(subscriber: Entity, target: Entity) {
        jobs.add(coroutineScope.launch {
            maps.firstOrNull { it.targetEntity == target }?.also {
                it.removeSubscriber(subscriber)
            }
        })
    }

    fun unsubscribeAll(subscriber: Entity) {
        jobs.add(coroutineScope.launch {
            log.info("unsubscribing $subscriber")
            maps.forEach { it.removeSubscriber(subscriber) }
        })
    }


    // Hooks for hearing about relevant world changes.

    fun onActorMove(actor: Actor) {
        if (worker?.isActive == false || worker == null) throw RuntimeException("Pather has crashed!")

        coroutineScope.launch {
            maps.forEach { it.onActorMove(actor) }
        }
    }

    fun debugStepAt(x: Int, y: Int): Int = playerMap?.debugStepAt(x, y) ?: 0
}

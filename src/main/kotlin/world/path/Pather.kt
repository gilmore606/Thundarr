package world.path

import actors.Actor
import actors.Player
import actors.Ratman
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.RayCaster
import util.XY
import util.log
import util.safeForEach
import world.Entity
import world.level.Level
import java.lang.RuntimeException

object Pather {

    private val maps = mutableListOf<StepMap>()

    const val maxRange = 100f

    private val coroutineContext = newSingleThreadAsyncContext("pather")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var worker: Job? = null
    private var jobs = mutableSetOf<Job>()

    private var playerMap: StepMap? = null

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
                        if (map.done) doneMap = map
                        else if (map.outOfDate) {
                            map.update()
                        }
                    }
                }
                doneMap?.also {
                    log.info("pruning done map $it")
                    it.dispose()
                    KtxAsync.launch {
                        maps.remove(it)
                    }
                }
                delay(1L)
            }
        }
    }

    fun nextStep(from: Entity, to: Entity): XY? = from.xy().let { xy -> maps.firstNotNullOfOrNull { it.nextStep(xy, to) } }

    fun nextStep(from: XY, to: Entity): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }

    fun nextStep(from: Entity, to: XY): XY? = maps.firstNotNullOfOrNull { it.nextStep(from, to) }

    fun buildPath(from: XY, to: Entity) = maps.firstOrNull { it.canReach(to) }.let { map ->
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

    fun subscribe(subscriber: Entity, target: Entity, range: Float) {
        //log.info("subscribing $subscriber @ $range for target $target")
        val map = maps.firstOrNull { it.targetEntity == target } ?: StepMap().apply {
            log.info("adding stepmap to $target")
            setTargetToEntity(target)
            changeRange(range)
            maps.add(this@apply)
            if (target is Player) {
                playerMap = this@apply
            }
        }
        map.addSubscriber(subscriber, range)
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

    fun debugStepAt(x: Int, y: Int): Int = maps.firstOrNull { it.targetEntity is Ratman }?.debugStepAt(x, y) ?: 0
}

package actors

import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.DIRECTIONS
import util.XY
import util.distanceBetween
import util.log

@Serializable
sealed class NPC : Actor() {

    enum class Awareness { HIBERNATED, UNAWARE, AWARE }
    @Transient val awareRadius = 40f
    @Transient val hibernateRadius = 50f
    @Transient val forgetPlayerTime = 10.0

    var awareness = Awareness.HIBERNATED
    var lastPlayerSeenTime = 0.0

    open fun pickAction(): Action = Wait(1f)

    final override fun canAct() = juice > 0f
    final override fun isActing() = awareness != Awareness.HIBERNATED
    final override fun defaultAction(): Action {
        if (distanceToPlayer() > hibernateRadius) {
            hibernate()
        }
        return pickAction()
    }

    fun hibernate() { awareness = Awareness.HIBERNATED }
    fun unHibernate() { awareness = Awareness.UNAWARE }
    fun noticePlayer() { awareness = Awareness.AWARE }
    fun forgetPlayer() { if (awareness == Awareness.AWARE) awareness = Awareness.UNAWARE }

    protected fun distanceToPlayer() = if (level == App.player.level) distanceBetween(xy.x, xy.y, App.player.xy.x, App.player.xy.y) else 1000f

    fun wander(): Action? {
        val dirs = mutableListOf<XY>()
        level?.also { level ->
            DIRECTIONS.forEach { dir ->
                if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y)) {
                    dirs.add(dir)
                }
            }
        }
        if (dirs.isNotEmpty()) {
            val dir = dirs.random()
            return Move(dir)
        }
        return null
    }

    override fun advanceTime(delta: Float) {
        if (awareness != Awareness.HIBERNATED) {
            if (level?.visibilityAt(xy.x, xy.y) == 1f) {
                lastPlayerSeenTime = App.time
                if (awareness != Awareness.AWARE) noticePlayer()
            } else if (awareness == Awareness.AWARE && App.time - lastPlayerSeenTime > forgetPlayerTime) {
                forgetPlayer()
            }
        }
    }
}

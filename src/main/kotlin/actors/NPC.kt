package actors

import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import actors.animations.Hop
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.sparks.Speak
import render.tilesets.Glyph
import ui.panels.Console
import util.*

@Serializable
sealed class NPC : Actor() {

    enum class Awareness { HIBERNATED, UNAWARE, AWARE }
    @Transient val awareRadius = 40f
    @Transient val hibernateRadius = 50f
    @Transient val forgetPlayerTime = 10.0

    var awareness = Awareness.HIBERNATED
    var lastPlayerSeenTime = 0.0
    var hostile = false

    open fun pickAction(): Action = Wait(1f)

    open fun converseLines(): List<String> = listOf()

    open fun isHostile(): Boolean = hostile
    open fun becomeHostileMsg(): List<String> = listOf("%DN bellows with rage!", "%DN turns angrily toward you!")

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

    override fun onConverse(actor: Actor): Boolean {
        val converseLines = converseLines()
        if (converseLines.isNotEmpty()) {
            Console.announce(level, xy.x, xy.y, Console.Reach.AUDIBLE, this.dnamec() + " says, \"" + converseLines.random() + "\"")
            level?.addSpark(Speak().at(xy.x, xy.y))
            return true
        }
        return false
    }

    override fun statusGlyph(): Glyph? {
        if (isHostile()) return Glyph.HOSTILE_ICON
        return null
    }

    override fun examineDescription(): String {
        var d = description()
        if (hostile) d += "  " + this.gender().ps.capitalize() + " seems very angry at you!"
        return d
    }

    override fun receiveAttack(attacker: Actor) {
        super.receiveAttack(attacker)
        if (attacker is Player && !hostile) {
            hostile = true
            val m = becomeHostileMsg().random()
            Console.sayAct("", m, this, attacker, null, Console.Reach.AUDIBLE)
        }
    }
}

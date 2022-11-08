package actors

import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import actors.stats.Speed
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.sparks.Speak
import render.tilesets.Glyph
import ui.panels.Console
import util.*
import world.level.Level
import world.path.Pather

@Serializable
sealed class NPC : Actor() {

    enum class Awareness { HIBERNATED, UNAWARE, AWARE }
    @Transient val awareRadius = 40f
    @Transient val hibernateRadius = 50f
    @Transient val forgetPlayerTime = 10.0

    var awareness = Awareness.HIBERNATED
    var lastPlayerSeenTime = 0.0
    var hostile = false

    fun spawnAt(level: Level, x: Int, y: Int): NPC {
        onSpawn()
        moveTo(level, x, y)
        return this
    }

    open fun onSpawn() { }

    open fun pickAction(): Action = Wait(1f)

    open fun converseLines(): List<String> = listOf()

    open fun isHostile(): Boolean = hostile
    override fun willAggro(target: Actor) = isHostile() && target is Player

    override fun visualRange() = 8f + Speed.get(this)

    open fun becomeHostileMsg(): List<String> = listOf("%Dn bellows with rage!", "%Dn turns angrily toward you!")

    final override fun hasActionJuice() = juice > 0f
    final override fun wantsToAct() = awareness != Awareness.HIBERNATED
    final override fun defaultAction(): Action {
        if (distanceToPlayer() > hibernateRadius) {
            hibernate()
        }
        return pickAction()
    }

    override fun onRestore() {
        super.onRestore()
        if (awareness != Awareness.HIBERNATED) {
            startPathing()
        }
    }

    private fun startPathing() { Pather.subscribe(this, this, 24f) }

    fun hibernate() {
        awareness = Awareness.HIBERNATED
        Pather.unsubscribe(this, this)
    }

    fun unHibernate() {
        if (awareness == Awareness.HIBERNATED) {
            startPathing()
            awareness = Awareness.UNAWARE
        }
    }

    fun noticePlayer() { awareness = Awareness.AWARE }
    fun forgetPlayer() { if (awareness == Awareness.AWARE) awareness = Awareness.UNAWARE }

    protected fun distanceToPlayer() = if (level == App.player.level) distanceBetween(xy.x, xy.y, App.player.xy.x, App.player.xy.y) else 1000f

    fun wander(): Action? {
        val dirs = mutableListOf<XY>()
        level?.also { level ->
            DIRECTIONS.forEach { dir ->
                if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y) && level.actorAt(xy.x + dir.x, xy.y + dir.y) == null) {
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
        super.advanceTime(delta)
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

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        super.drawStatusGlyphs(drawIt)
        if (isHostile()) drawIt(Glyph.HOSTILE_ICON)
    }

    override fun examineDescription(): String {
        var d = description()
        if (hostile) d += "  " + this.gender().ps.capitalize() + " seems very angry at you!"
        if (awareness == Awareness.HIBERNATED) d += "  " + this.gender().ps.capitalize() + " looks very sleepy."
        if (awareness == Awareness.UNAWARE) d += "  " + this.gender().ps.capitalize() + " doesn't seem to notice you."
        return d
    }

    override fun receiveAggression(attacker: Actor) {
        if (attacker is Player && !hostile) {
            hostile = true
            val m = becomeHostileMsg().random()
            Console.sayAct("", m, this, attacker, null, Console.Reach.AUDIBLE)
        }
    }
}

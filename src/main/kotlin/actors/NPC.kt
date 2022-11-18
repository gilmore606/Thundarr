package actors

import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import actors.states.Attacking
import actors.states.Fleeing
import actors.states.Hibernated
import actors.states.State
import actors.stats.Speed
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.sparks.Speak
import render.tilesets.Glyph
import ui.panels.Console
import util.*
import world.level.Level

@Serializable
sealed class NPC : Actor() {

    @Transient val unhibernateRadius = 40f
    @Transient val hibernateRadius = 80f

    var state: State = Hibernated()
    var hostile = false

    fun spawnAt(level: Level, x: Int, y: Int): NPC {
        onSpawn()
        moveTo(level, x, y)
        return this
    }

    open fun onSpawn() { }

    open fun converseLines(): List<String> = listOf()

    open fun isHostile(): Boolean = hostile
    override fun willAggro(target: Actor) = isHostile() && target is Player

    override fun visualRange() = 8f + Speed.get(this)

    open fun becomeHostileMsg(): String = listOf("%Dn bellows with rage!", "%Dn turns angrily toward you!").random()

    open fun hostileResponseState(targetId: String) = Attacking(targetId)   // State change on hostile sighted
    open fun willSeek() = false  // Seek after losing hostile target?

    final override fun hasActionJuice() = juice > 0f
    final override fun wantsToAct() = state.wantsToAct()
    final override fun defaultAction(): Action {
        considerState()
        return state.pickAction(this)
    }

    override fun onRestore() {
        super.onRestore()
        state.onRestore(this)
    }

    protected fun distanceToPlayer() = if (level == App.player.level) distanceBetween(xy.x, xy.y, App.player.xy.x, App.player.xy.y) else 1000f

    // Observe our situation and possibly change states.
    open fun considerState() {
        if (distanceToPlayer() > hibernateRadius) {
            changeState(Hibernated())
        } else {
            state.considerState(this)
        }
    }

    fun changeState(newState: State) {
        state.leave(this)
        state = newState
        enterStateMsg(newState)?.also { Console.sayAct("", it, this) }
        log.info("NPC $this becomes $newState")
        newState.enter(this)
    }

    open fun enterStateMsg(newState: State): String? = when (newState) {
        is Attacking -> "%Dn rushes toward you!"
        is Fleeing -> "%Dn turns to flee!"
        else -> null
    }

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
        return d
    }

    override fun receiveAggression(attacker: Actor) {
        if (attacker is Player && !hostile) {
            hostile = true
            Console.sayAct("", becomeHostileMsg(), this, attacker, null, Console.Reach.AUDIBLE)
        }
    }
}

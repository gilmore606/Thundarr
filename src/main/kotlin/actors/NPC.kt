package actors

import actors.actions.Action
import actors.actions.Move
import actors.states.*
import actors.stats.Speed
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.sparks.Speak
import render.tilesets.Glyph
import things.NPCDen
import ui.panels.Console
import util.*
import world.level.Level

@Serializable
sealed class NPC : Actor() {

    enum class Tag {
        NPC_AUROX,
        NPC_MUSKOX,
        NPC_CYCLOX,
        NPC_TUSKER,
        NPC_TUSKLET,
        NPC_VOLTELOPE,
        NPC_VOLTELOPE_FAWN,
        NPC_SALAMAN,
        NPC_TORTLE,
        NPC_PIDGEY,
        NPC_PIDGEY_BRUTE,
    }

    companion object {
        fun create(tag: Tag): NPC = when (tag) {
            Tag.NPC_AUROX -> Aurox()
            Tag.NPC_MUSKOX -> MuskOx()
            Tag.NPC_CYCLOX -> Cyclox()
            Tag.NPC_TUSKER -> Tusker()
            Tag.NPC_TUSKLET -> Tusklet()
            Tag.NPC_VOLTELOPE -> Voltelope()
            Tag.NPC_VOLTELOPE_FAWN -> VoltelopeFawn()
            Tag.NPC_SALAMAN -> Salaman()
            Tag.NPC_TORTLE -> Tortle()
            Tag.NPC_PIDGEY -> Pidgey()
            Tag.NPC_PIDGEY_BRUTE -> PidgeyBrute()
        }
    }

    @Transient val unhibernateRadius = 30f
    @Transient val hibernateRadius = 40f

    var state: State = Hibernated()
    val stateStack = Stack<State>()

    var hostile = false
    var metPlayer = false
    val placeMemory = mutableMapOf<String,XY>()
    @Transient var den: NPCDen? = null

    fun spawnAt(level: Level, x: Int, y: Int): NPC {
        onSpawn()
        moveTo(level, x, y)
        return this
    }

    fun spawnInRoom(level: Level, room: Rect): NPC {
        placeMemory["myRoom0"] = XY(room.x0, room.y0)
        placeMemory["myRoom1"] = XY(room.x1, room.y1)
        val x = Dice.range(room.x0, room.x1)
        val y = Dice.range(room.y0, room.y1)
        return spawnAt(level, x, y)
    }

    open fun onSpawn() { }

    open fun converseLines(): List<String> = listOf()
    open fun meetPlayerMsg(): String? = null

    open fun isHostile(): Boolean = hostile
    override fun willAggro(target: Actor) = isHostile() && target is Player

    override fun visualRange() = 8f + Speed.get(this)

    open fun becomeHostileMsg(): String = listOf("%Dn bellows with rage!", "%Dn turns angrily toward you!").random()

    open fun idleState(): Idle = IdleDoNothing()
    open fun hostileResponseState(targetId: String) = Attacking(targetId)   // State change on hostile sighted
    open fun hostileLossState(targetId: String) = idleState()  // Seek after losing hostile target?

    final override fun hasActionJuice() = juice > 0f
    final override fun wantsToAct() = state.wantsToAct()
    final override fun defaultAction(): Action {
        doConsiderState()
        return state.pickAction(this)
    }

    override fun onRestore() {
        super.onRestore()
        state.onRestore(this)
    }

    protected fun distanceToPlayer() = if (level == App.player.level) distanceBetween(xy.x, xy.y, App.player.xy.x, App.player.xy.y) else 1000f

    // Observe our situation and possibly change states.
    private fun doConsiderState() {
        if (distanceToPlayer() > hibernateRadius) {
            changeState(Hibernated())
        } else {
            val oldState = state
            statuses.forEach { it.considerState(this) }
            if (oldState == state) {
                considerState()
                if (oldState == state) {
                    state.considerState(this)
                }
            }
        }
    }

    // Do any special state changes for this kind of NPC
    open fun considerState() { }

    fun changeState(newState: State) {
        state.leave(this)
        state = newState
        enterStateMsg(newState)?.also { Console.sayAct("", it, this) }
        log.info("NPC $this becomes $newState")
        newState.enter(this)
    }

    fun pushState(newState: State) {
        stateStack.push(state)
        changeState(newState)
    }

    fun popState() {
        stateStack.pop()?.also {
            changeState(it)
        } ?: run {
            changeState(idleState())
        }
    }

    open fun enterStateMsg(newState: State): String? = when (newState) {
        is Attacking -> "%Dn rushes toward you!"
        is Fleeing -> "%Dn turns to flee!"
        else -> null
    }

    fun say(text: String?) {
        text?.also { text ->
            Console.sayAct("", this.dnamec() + " says, \""  + text + "\"", this)
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
        return d
    }

    override fun receiveAggression(attacker: Actor) {
        if (attacker is Player && !hostile) {
            hostile = true
            Console.sayAct("", becomeHostileMsg(), this, attacker, null, Console.Reach.AUDIBLE)
        }
    }

    override fun die() {
        den?.onDie(this)
        super.die()
    }
}

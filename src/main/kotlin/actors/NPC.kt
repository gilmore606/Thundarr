package actors

import actors.actions.Action
import actors.actions.events.Event
import actors.states.*
import actors.stats.Speed
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.sparks.Speak
import render.tilesets.Glyph
import things.NPCDen
import ui.panels.Console
import util.*
import world.Chunk
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
        NPC_GRIZZLER,
        NPC_HERMIT,
        NPC_GATOR,
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
            Tag.NPC_GRIZZLER -> Grizzler()
            Tag.NPC_HERMIT -> Hermit()
            Tag.NPC_GATOR -> Gator()
        }
    }

    @Transient val unhibernateRadius = 65f
    @Transient val hibernateRadius = 90f

    var state: State = Hibernated()
    val stateStack = Stack<State>()

    enum class Opinion { HATE, NEUTRAL, LOVE }
    val opinions = mutableMapOf<String, Opinion>()
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

    open fun canSpawnAt(chunk: Chunk, x: Int, y: Int): Boolean = true
    open fun onSpawn() { }

    open fun converseLines(): List<String> = state.converseLines(this) ?: listOf()
    open fun meetPlayerMsg(): String? = null

    open fun isHostileTo(target: Actor): Boolean = (opinionOf(target) == Opinion.HATE)

    override fun willAggro(target: Actor) = isHostileTo(target)

    override fun visualRange() = 8f + Speed.get(this)

    open fun idleState(): Idle = IdleDoNothing()
    open fun hostileResponseState(enemy: Actor): State? = Attacking(enemy.id)   // State change on hostile sighted

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
            // Statuses get a chance to change our state
            statuses.forEach { it.considerState(this) }
            if (oldState == state) {
                // If no change, we get a chance to change our state
                considerState()
                if (oldState == state) {
                    // If no change, current state gets a chance to change our state
                    state.considerState(this)
                }
            }
        }
    }

    // Do any special state changes for this kind of NPC
    open fun considerState() { }

    fun changeState(newState: State) {
        val oldState = state
        state.leave(this)
        state = newState
        enterStateMsg(newState)?.also { Console.sayAct("", it, this) }
        log.info("NPC $this was $oldState, becomes $newState")
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

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        state.witnessEvent(this, culprit, event, location)
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
        state.drawStatusGlyphs(drawIt)
    }

    override fun examineDescription(): String {
        var d = description()
        if (isHostileTo(App.player)) d += "  " + this.gender().ps.capitalize() + " seems very angry at you!"
        return d
    }

    override fun receiveAggression(attacker: Actor) {
        if (!isHostileTo(attacker)) {
            downgradeOpinionOf(attacker)
            factions.forEach { factionID ->
                App.factions.byID(factionID)?.onMemberAttacked(attacker)
            }
        }
    }

    fun opinionOf(actor: Actor): Opinion {
        if (opinions.containsKey(actor.id)) return opinions[actor.id]!! else {
            var loved = false
            factions.forEach { id ->
                val opinion = App.factions.byID(id)?.opinionOf(actor)
                if (opinion == Opinion.HATE) return opinion
                else if (opinion == Opinion.LOVE) loved = true
            }
            return if (loved) Opinion.LOVE else Opinion.NEUTRAL
        }
    }

    fun upgradeOpinionOf(actor: Actor) {
        if (opinions[actor.id] == Opinion.HATE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = Opinion.LOVE
        }
    }

    fun downgradeOpinionOf(actor: Actor) {
        if (opinions[actor.id] == Opinion.LOVE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = Opinion.HATE
        }
    }

    override fun die() {
        den?.onDie(this)
        super.die()
    }
}

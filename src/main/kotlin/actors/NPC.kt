package actors

import actors.actions.Action
import actors.actions.events.Event
import actors.states.*
import actors.stats.Speed
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.sparks.Speak
import render.tilesets.Glyph
import things.NPCDen
import ui.modals.ConverseModal
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

    open fun commentLines(): List<String> = state.commentLines(this) ?: listOf()
    open fun meetPlayerMsg(): String? = null
    open fun portraitGlyph(): Glyph? = null

    open fun isHostileTo(target: Actor): Boolean = (opinionOf(target) == Opinion.HATE)

    override fun willAggro(target: Actor) = isHostileTo(target)

    override fun visualRange() = 8f + Speed.get(this)
    override fun canSee() = super.canSee() && state.canSee()

    open fun idleState(): Idle = IdleDoNothing()
    open fun hostileResponseState(enemy: Actor): State? = Attacking(enemy.id)   // State change on hostile sighted

    final override fun hasActionJuice() = juice > 0f
    final override fun wantsToAct() = state.wantsToAct()
    final override fun defaultAction(): Action {
        doConsiderState()
        this.pickAction()?.also { return it }
        return state.pickAction(this)
    }
    open fun pickAction(): Action? = null

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

    fun changeState(newState: State, suppressMessages: Boolean = false) {
        val oldState = state
        state.leave(this)
        state = newState
        if (!suppressMessages) enterStateMsg(newState)?.also { say(it) }
        //log.info("NPC $this was $oldState, becomes $newState")
        newState.enter(this)
    }

    fun pushState(newState: State) {
        stateStack.push(state)
        changeState(newState, true)
    }

    fun popState() {
        stateStack.pop()?.also {
            changeState(it, true)
        } ?: run {
            changeState(idleState(), true)
        }
    }

    open fun enterStateMsg(newState: State): String? = when (newState) {
        is Attacking -> ":rushes toward you!"
        is Fleeing -> ":turns to flee!"
        is Seeking -> ":looks around, hunting."
        else -> null
    }

    open fun seekTargetMsg() = if (isHuman()) "Couldn't have gone far..." else ":looks frustrated."
    open fun lostTargetMsg() = if (isHuman()) "Damn, lost em." else ":looks dejected."

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        state.witnessEvent(this, culprit, event, location)
    }

    open fun hasConversation() = false
    open fun conversationSources(): List<ConverseModal.Source> = listOf()
    open fun willTrade() = false
    open fun tradeMsg() = "I have wares to trade, if barbarian has coin."

    override fun onConverse(actor: Actor): Boolean {
        if (actor is Player && hasConversation()) {
            startConversation()
        }
        val converseLines = commentLines()
        if (converseLines.isNotEmpty()) {
            Console.announce(level, xy.x, xy.y, Console.Reach.AUDIBLE, this.dnamec() + " says, \"" + converseLines.random() + "\"")
            level?.addSpark(Speak().at(xy.x, xy.y))
            return true
        }
        return false
    }

    fun startConversation() {
        Screen.addModal(ConverseModal(this))
    }

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        super.drawStatusGlyphs(drawIt)
        if (hasConversation()) drawIt(Glyph.QUESTION_ICON)
        if (willTrade()) drawIt(Glyph.TRADE_ICON)
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
        state.receiveAggression(this, attacker)
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

    fun couldLearnFrom(npc: NPC): Set<String> = mutableSetOf<String>().apply {
        npc.opinions.keys.forEach { person ->
            if (!opinions.contains(person)) add(person)
        }
    }

    override fun die() {
        den?.onDie(this)
        super.die()
    }
}

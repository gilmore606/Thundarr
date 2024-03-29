package actors.actors

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
import util.*
import world.Chunk
import world.level.Level

@Serializable
sealed class NPC : Actor() {

    enum class Tag(val spawn: ()->NPC) {
        AUROX({ Aurox() }),
        BOAR({ Boar() }),
        CACTOID({ Cactoid() }),
        CHARMAN({ Charman() }),
        CYCLOX({ Cyclox() }),
        FROG({ Frog() }),
        GATOR({ Gator() }),
        GECKOID({ Geckoid() }),
        GLOCUST({ Glocust() }),
        GRIZZLER({ Grizzler() }),
        GRUB({ Grub() }),
        HEDGE_WITCH({ HedgeWitch() }),
        HERDER({ Herder() }),
        HERMIT({ Hermit() }),
        HYENAMAN({ Hyenaman() }),
        JERIF({ Jerif() }),
        KILLDAISY({ Killdaisy() }),
        LAMPREY({ Lamprey() }),
        LOSTRICH({ Lostrich() }),
        MOLEGULL({ Molegull() }),
        MUSKOX({ MuskOx() }),
        PEEPER({ Peeper() }),
        PENGO({ Pengo() }),
        PIDGEY({ Pidgey() }),
        PIDGEY_BRUTE({ PidgeyBrute() }),
        PINCER_BEETLE({ PincerBeetle() }),
        PORTAL({ MagicPortal() }),
        RAM({ Ram() }),
        RATLORD({ Ratlord("") }),
        RATMAN({ Ratman("") }),
        RATTHING({ Ratthing() }),
        SALAMAN({ Salaman() }),
        SCORPION({ Scorpion() }),
        STICKLEBOAR({ Stickleboar() }),
        THRALL({ Thrall("") }),
        TICK({ Tick() }),
        TORTLE({ Tortle() }),
        TORTLE_YOUNG({ YoungTortle() }),
        TORTLE_BULL({ BullTortle() }),
        TUSKER({ Tusker() }),
        TUSKLET({ Tusklet() }),
        VILLAGE_GUARD({ Tick() }), // no free spawn!
        VILLAGER({ Tick() }), // no free spawn!
        VOLTELOPE({ Voltelope() }),
        VOLTELOPE_FAWN({ VoltelopeFawn() }),
        WASTED_ONE({ WastedOne() }),
        WOLFMAN({ Wolfman() }),
        WOOD_SPIDER({ WoodSpider() }),
    }

    abstract val tag: Tag

    companion object {
        fun create(tag: Tag): NPC = tag.spawn()
    }

    @Transient val unhibernateRadius = 65f
    @Transient val hibernateRadius = 90f

    var state: State = Hibernated()
    val stateStack = Stack<State>()

    var conversationGlyph: Glyph? = null

    enum class Opinion { HATE, NEUTRAL, LOVE }
    @Serializable data class OpinionRecord(val opinion: Opinion, val time: Double = App.gameTime.time)

    val opinions = mutableMapOf<String, OpinionRecord>()
    var metPlayer = false

    val placeMemory = mutableMapOf<String,XY>()
    @Transient var den: NPCDen? = null

    fun spawnAt(level: Level, x: Int, y: Int): NPC {
        hp = hpMax()
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

    open fun spawnsInDen() = false
    open fun canSpawnAt(chunk: Chunk, x: Int, y: Int): Boolean = true
    open fun onSpawn() { }

    open fun meetPlayerMsg(): String? = null
    open fun portraitGlyph(): Glyph? = null

    val idleBounceOffset = Dice.zeroTo(800)
    override fun animOffsetY(): Float {
        val off = super.animOffsetY()
        return if (off == 0f) state.animOffsetY(this) else off
    }

    open fun isHostileTo(target: Actor): Boolean = (opinionOf(target) == Opinion.HATE)
    override fun willAggro(target: Actor) = isHostileTo(target)
    open fun aggroRange() = visualRange()
    open fun defendSpecies() = false
    open fun speciesBrothers() = setOf(tag)

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

    override fun advanceTime(delta: Float) {
        super.advanceTime(delta)
        updateConversationGlyph()
    }

    open fun updateConversationGlyph() { }
    open fun conversationSources(): List<ConverseModal.Source> = listOf()
    open fun willConverse() = conversationSources().size > 1 && state.allowsConversation()

    override fun onConverse(actor: Actor): Boolean {
        if (actor is Player && willConverse()) {
            level?.addSpark(Speak().at(xy.x, xy.y))
            Screen.addModal(ConverseModal(this))
            return true
        }
        if (state.allowsConversation() && spoutComment()) return true
        return false
    }

    open fun spoutComment(): Boolean {
        state.commentLine()?.also {
            say(it)
            return true
        }
        val lines = commentLines()
        if (lines.isNotEmpty()) {
            say(lines.random())
            return true
        }
        return false
    }

    open fun commentLines(): List<String> = listOf()

    override fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean {
        if (super.drawStatusGlyph(drawIt)) return true
        if (state.drawStatusGlyph(drawIt)) return true
        conversationGlyph?.also {
            drawIt(it)
            return true
        }
        return false
    }

    override fun examineDescription(): String {
        var d = description()
        if (isHostileTo(App.player)) d += "  " + this.gender().ps.capitalize() + " seems very angry at you!"
        return d
    }

    override fun receiveAggression(attacker: Actor) {
        super.receiveAggression(attacker)
        if (!isHostileTo(attacker)) {
            downgradeOpinionOf(attacker)
            factions.forEach { factionID ->
                App.factions.byID(factionID)?.onMemberAttacked(attacker)
            }
        }
        state.receiveAggression(this, attacker)
    }

    open fun opinionOf(actor: Actor): Opinion {
        if (opinions.containsKey(actor.id)) return opinions[actor.id]!!.opinion else {
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
        if (opinions[actor.id]?.opinion == Opinion.HATE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = OpinionRecord(Opinion.LOVE)
        }
    }

    fun downgradeOpinionOf(actor: Actor) {
        if (opinions[actor.id]?.opinion == Opinion.LOVE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = OpinionRecord(Opinion.HATE)
        }
    }

    fun couldLearnFrom(npc: NPC): Set<String> = mutableSetOf<String>().apply {
        npc.opinions.keys.forEach { person ->
            if (!opinions.contains(person)) add(person)
        }
    }

    override fun die() {
        den?.onDie(this)
        App.player.gainXP(effectiveXPvalue())
        super.die()
    }

    private fun effectiveXPvalue(): Int {
        var xp = xpValue()
        if (xpLevel < App.player.xpLevel) xp = (xp * 0.5).toInt()
        return xp
    }

    open fun xpValue() = 10
}

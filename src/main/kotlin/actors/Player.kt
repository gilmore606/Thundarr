package actors

import actors.actions.Action
import actors.actions.Move
import actors.actions.AutoMove
import actors.stats.Brains
import actors.stats.Heart
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.*
import actors.statuses.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktx.async.KtxAsync
import render.Screen
import render.sparks.GlyphRise
import render.sparks.Smoke
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import ui.panels.TimeButtons
import util.*
import world.Entity
import world.gen.Metamap
import world.journal.GameTime
import world.journal.Journal
import world.level.EnclosedLevel
import world.level.WorldLevel
import world.stains.Fire

@Serializable
open class Player : Actor() {

    companion object {
        private const val caloriesMax = 2000f
        private const val caloriesEatMax = 1800f
        private const val caloriesPerDay = 2800f
        private const val caloriesHunger = -2000f
        private const val caloriesStarving = -4000f

        private val xpForLevel = listOf(
            0,
            200,
            500,
            1000,
            2000,
            4000,
            8000,
            16000,
            32000,
            64000,
            104000,
            156000,
            228000,
            325000,
            452000,
            619000,
            831000,
            1093000,
            1410000,
            1787000,
            2230000,
            2745000,
            3338000,
            4015000,
            4782000,
            5639000,
            6596000,
            7663000,
            8850000,
            9999999
        )
    }

    var journal: Journal = Journal()
    var dangerMode: Boolean = false
    var thrownTag: Thing.Tag? = null
    override fun glyph() = Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian.  His good looks are somewhat marred by a permanent scowl."
    override fun dname() = name()
    override fun iname() = name()

    override fun initialFactions() = mutableSetOf(App.factions.humans)

    override fun visualRange() = 40f
    override fun hasActionJuice() = queuedActions.isNotEmpty()
    override fun wantsToAct() = true
    override fun defaultAction(): Action? = null

    var xp = 0
    var levelUpsAvailable = 0
    var lastChunkThreatLevel = 0

    var calories = 1000f

    val autoPickUpTypes = mutableListOf<Thing.Tag>()

    init {
        //Pather.subscribe(this, this, 60f)
    }

    open fun onSpawn() {
        Strength.set(this, 10f + Dice.zeroTo(4).toFloat())
        Brains.set(this, 8f + Dice.zeroTo(4).toFloat())
        Speed.set(this, 9f + Dice.zeroTo(3).toFloat())
        Heart.set(this, 9f)
        Fight.set(this, 1f)
        Throw.set(this, 1f)
        Survive.set(this, 1f)
    }

    override fun die() {
        super.die()
        KtxAsync.launch {
            delay(1000L)
            Screen.brightnessTarget = 0f
            App.saveStateAndReturnToMenu()
        }
    }

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        super.drawStatusGlyphs(drawIt)
        if (dangerMode) drawIt(Glyph.HOSTILE_ICON)
    }

    fun tryMove(dir: XY) {
        level?.also { level ->
            if (level.isWalkableFrom(this, xy, dir)) {
                if (level.stainsAt(xy.x + dir.x, xy.y + dir.y)?.hasOneWhere { it is Fire } == true && !dangerMode) {
                    Console.say("You reconsider your dangerous idea of running into a burning fire.")
                    return
                }
                queue(Move(dir))
            } else {
                level.bumpActionTo(xy.x, xy.y, dir)?.also { queue(it) }
            }
        }
    }

    fun tryAutoMove(dir: XY) {
        queue(AutoMove(dir))
    }

    override fun willAggro(target: Actor) = dangerMode

    fun toggleAggro() {
        if (dangerMode) {
            Console.say("You opt for a more careful approach.")
        } else {
            Console.say("You throw caution to the wind!  (You'll now attack anyone you bump into, and can walk into danger.)")
        }
        dangerMode = !dangerMode
    }

    fun toggleSleep() {
        level?.addSpark(Smoke().at(xy.x, xy.y))
        if (hasStatus(Status.Tag.ASLEEP)) {
            wakeFromSleep()
            TimeButtons.changeState(TimeButtons.State.PAUSE)
        } else {
            fallAsleep()
            TimeButtons.changeState(TimeButtons.State.PLAY)
        }
    }

    fun readyForThrowing(tag: Thing.Tag) {
        thrownTag = tag
        Console.say("You'll throw " + tag.pluralName + " now.")
    }

    fun getThrown(): Thing? = thrownTag?.let { tag -> contents.firstOrNull { it.tag == tag } }

    override fun onMove() {
        super.onMove()
        if (level is WorldLevel) Metamap.markChunkVisitedAt(xy.x, xy.y)
    }

    override fun advanceTime(delta: Float) {
        super.advanceTime(delta)
        calories -= (caloriesPerDay * delta / GameTime.TURNS_PER_DAY).toFloat()
        if (calories < caloriesEatMax) {
            removeStatus(Status.Tag.SATIATED)
        }
        if (calories <= caloriesStarving) {
            calories = caloriesStarving
            removeStatus(Status.Tag.HUNGRY)
            if (!hasStatus(Status.Tag.STARVING)) {
                Console.say("You're starving to death.")
                addStatus(Starving())
            }
        } else {
            if (calories <= caloriesHunger) {
                if (!hasStatus(Status.Tag.HUNGRY)) {
                    Console.say("Your stomach grumbles loudly.  Time for a meal.")
                    addStatus(Hungry())
                }
            }
        }
    }

    override fun ingestCalories(cal: Int) {
        calories += cal.toFloat()
        if (calories > caloriesMax) {
            calories = caloriesMax
            addStatus(Satiated())
        }
        if (calories > caloriesHunger) {
            if (hasStatus(Status.Tag.HUNGRY) || hasStatus(Status.Tag.STARVING)) {
                removeStatus(Status.Tag.HUNGRY)
                removeStatus(Status.Tag.STARVING)
                Console.say("That hit the spot.  You feel satisfied.")
            }
        } else if (calories > caloriesStarving) {
            if (hasStatus(Status.Tag.STARVING)) {
                removeStatus(Status.Tag.STARVING)
                Console.say("That took the edge off.  You don't feel quite so hungry.")
            }
        }
    }

    override fun wantsToPickUp(thing: Thing) = isAutoActionSafe() && autoPickUpTypes.contains(thing.tag)

    open fun addAutoPickUpType(type: Thing.Tag) {
        autoPickUpTypes.add(type)
        Console.say("You remind yourself to pick up any " + type.pluralName + " you find.")
    }
    open fun removeAutoPickUpType(type: Thing.Tag) {
        autoPickUpTypes.remove(type)
        Console.say("You give up on your $type collection.")
    }

    override fun canSee(entity: Entity?) = entity != null && entity.level() == level && level!!.visibilityAt(entity.xy()!!.x, entity.xy()!!.y) == 1f

    // Is it safe to do auto-actions like auto-pickup, shift-move, etc?
    // If we can see hostiles, it's not safe.
    fun isAutoActionSafe(): Boolean {
        if (entitiesSeen { it is NPC && it.isHostileTo(this) }.isNotEmpty()) {
            return false
        }
        return true
    }

    fun debugMove(dir: XY) {
        moveTo(level, xy.x + (dir.x * 20), xy.y + (dir.y * 20))
    }

    fun threatLevel(): Int {
        level?.also { level ->
            val threat = when {
                (level is WorldLevel) -> Metamap.metaAtWorld(App.player.xy.x, App.player.xy.y).threatLevel
                (level is EnclosedLevel) -> level.threatLevel
                else -> 1
            }
            return threat - xpLevel
        }
        return 0
    }

    fun gainXP(added: Int) {
        val effLevel = xpLevel + levelUpsAvailable
        if (effLevel >= xpForLevel.size - 1) return
        xp += added
        if (xp >= xpForLevel[effLevel]) {
            earnLevelUp()
        }
    }

    private fun earnLevelUp() {
        levelUpsAvailable++
        Console.say("You feel your inner potential has grown!")
        level?.addSpark(GlyphRise(Glyph.PLUS_ICON_BLUE).at(xy.x, xy.y))
    }

    fun levelUp() {
        if (levelUpsAvailable < 1) return
        levelUpsAvailable--
        xpLevel++
        Console.say("You feel your inner potential has been realized!")
        level?.addSpark(GlyphRise(Glyph.PLUS_ICON_BLUE).at(xy.x, xy.y))

        Heart.improve(this, fullLevel = true)
        hpMax += 5
        hp  = hpMax
    }
}

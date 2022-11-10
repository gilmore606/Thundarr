package actors

import actors.actions.Action
import actors.actions.Move
import actors.stats.Brains
import actors.stats.Strength
import actors.stats.skills.*
import actors.statuses.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktx.async.KtxAsync
import render.Screen
import render.sparks.Smoke
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import ui.panels.TimeButtons
import util.*
import world.Entity
import world.journal.GameTime
import world.journal.Journal
import world.path.Pather
import world.stains.Fire

@Serializable
open class Player : Actor() {

    var journal: Journal = Journal()
    var dangerMode: Boolean = false
    var thrownTag: String = ""
    override fun glyph() = Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian in a leather tunic."
    override fun dname() = name()
    override fun iname() = name()

    override fun hasActionJuice() = queuedActions.isNotEmpty()
    override fun wantsToAct() = true
    override fun defaultAction(): Action? = null

    var calories = 1000f
    private val caloriesMax = 2000f
    private val caloriesEatMax = 1800f
    private val caloriesPerDay = 2500f
    private val caloriesHunger = -2000f
    private val caloriesStarving = -4000f

    val autoPickUpTypes = mutableListOf<String>()

    init {
        Pather.subscribe(this, this, 100f)
    }

    open fun onSpawn() {
        Strength.set(this, 14f)
        Brains.set(this, 9f)
        Dig.set(this, 2f)
        Fight.set(this, 1f)
        Throw.set(this, 4f)
        Build.set(this, 1f)
        Survive.set(this, 2f)

        Sunsword().moveTo(this)
        repeat (20) { Meat().moveTo(this) }
        Torch().moveTo(this)
        Apple().moveTo(this)
        HornetHelmet().moveTo(this)
        HardHat().moveTo(this)
        RiotHelmet().moveTo(this)
        Pickaxe().moveTo(this)
        Axe().moveTo(this)
        Lighter().moveTo(this)
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

    override fun onMove() {
        super.onMove()
        level?.also { level ->
            val things = level.thingsAt(xy.x, xy.y).filter { it !is Scenery }
            if (things.isNotEmpty()) {
                Console.say("You see " + things.englishList() + " here.")
            }
        }
    }

    fun tryMove(dir: XY) {
        level?.also { level ->
            if (level.isWalkableFrom(xy, dir)) {
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
            removeStatus(Status.Tag.ASLEEP)
            TimeButtons.changeState(TimeButtons.State.PAUSE)
            Console.say("You wake up.")
        } else {
            Console.say("You lie down to sleep.")
            addStatus(Asleep())
            TimeButtons.changeState(TimeButtons.State.PLAY)
        }
    }

    fun readyForThrowing(tag: String) {
        thrownTag = tag
        Console.say("You'll throw " + thrownTag.plural() + " now.")
    }

    fun getThrown(): Thing? {
        if (thrownTag == "") return null
        return contents.firstOrNull { it.thingTag() == thrownTag }
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

    fun couldEat() = calories < caloriesEatMax

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

    override fun wantsToPickUp(thing: Thing) = autoPickUpTypes.contains(thing.thingTag())

    open fun addAutoPickUpType(type: String) {
        autoPickUpTypes.add(type)
        Console.say("You remind yourself to pick up any " + type.plural() + " you find.")
    }
    open fun removeAutoPickUpType(type: String) {
        autoPickUpTypes.remove(type)
        Console.say("You give up on your $type collection.")
    }

}

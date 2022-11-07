package actors

import actors.actions.Action
import actors.actions.Move
import actors.stats.Brains
import actors.stats.Strength
import actors.stats.skills.*
import actors.statuses.Hungry
import actors.statuses.Starving
import actors.statuses.Status
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import util.XY
import util.englishList
import util.plural
import world.Entity
import world.journal.GameTime
import world.journal.Journal
import world.path.Pather
import java.lang.Float.min

@Serializable
open class Player : Actor() {

    var journal: Journal = Journal()
    var willAggro: Boolean = false
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

    var calories = -1000f
    private val caloriesMax = 2000f
    private val caloriesEatMax = 1800f
    private val caloriesPerDay = 2500f
    private val caloriesHunger = -2000f
    private val caloriesStarving = -5000f

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
        repeat (20) { EnergyDrink().moveTo(this) }
        Torch().moveTo(this)
        Apple().moveTo(this)
        HornetHelmet().moveTo(this)
        HardHat().moveTo(this)
        RiotHelmet().moveTo(this)
        Pickaxe().moveTo(this)
        Axe().moveTo(this)
    }

    override fun statusGlyph(): Glyph? {
        if (willAggro) {
            return Glyph.HOSTILE_ICON
        }
        return null
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
                queue(Move(dir))
            } else {
                level.bumpActionTo(xy.x, xy.y, dir)?.also { queue(it) }
            }
        }
    }

    override fun willAggro(target: Actor) = willAggro

    fun toggleAggro() {
        if (willAggro) {
            Console.say("You calm down.")
        } else {
            Console.say("You boil over with rage, ready to smash the next creature you approach!")
        }
        willAggro = !willAggro
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
        if (calories <= caloriesStarving) {
            removeStatus(Status.Tag.HUNGRY)
            if (!hasStatus(Status.Tag.STARVING)) {
                Console.say("You're starving to death.")
                addStatus(Starving())
            }
        } else {
            calories -= (caloriesPerDay * delta / GameTime.TURNS_PER_DAY).toFloat()
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
            Console.say("Oof, you're stuffed.")
        }
        if (calories > caloriesHunger) {
            if (hasStatus(Status.Tag.HUNGRY) || hasStatus(Status.Tag.STARVING)) {
                removeStatus(Status.Tag.HUNGRY)
                removeStatus(Status.Tag.STARVING)
                Console.say("That hit the spot.  You don't feel hungry anymore.")
            }
        } else if (calories > caloriesStarving) {
            if (hasStatus(Status.Tag.STARVING)) {
                removeStatus(Status.Tag.STARVING)
                Console.say("That took the edge off.  You don't feel quite so hungry.")
            }
        }
    }
}

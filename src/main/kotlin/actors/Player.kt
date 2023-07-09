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
import world.terrains.Terrain
import java.lang.Float.min

@Serializable
open class Player : Actor() {

    companion object {
        private const val caloriesMax = 2000f
        private const val caloriesEatMax = 1800f
        private const val caloriesPerDay = 2800f
        private const val caloriesHunger = -2000f
        private const val caloriesStarving = -4000f

        private const val healSpeed = 0.3f

        class Lvl(val xp: Int, val name: String)
        val levels = listOf(
            Lvl(0, "Runaway"),
            Lvl(200, "Beggar"),
            Lvl(500,"Lost"),
            Lvl(1000,"Mendicant"),
            Lvl(2000,"Wanderer"),
            Lvl(4000,"Seeker"),
            Lvl(8000,"Tracker"),
            Lvl(16000,"Woodman"),
            Lvl(32000,"Hunter"),
            Lvl(64000,"Explorer"),
            Lvl(104000,"Delver"),
            Lvl(156000,"Intrepid"),
            Lvl(228000,"Fearless"),
            Lvl(325000,""),
            Lvl(452000,""),
            Lvl(619000,""),
            Lvl(831000,""),
            Lvl(1093000,""),
            Lvl(1410000,""),
            Lvl(1787000,""),
            Lvl(2230000,""),
            Lvl(2745000,""),
            Lvl(3338000,""),
            Lvl(4015000,""),
            Lvl(4782000,"Protector"),
            Lvl(5639000,"Hero"),
            Lvl(6596000,"Champion"),
            Lvl(7663000,"Warlord"),
            Lvl(8850000,"Conqueror"),
            Lvl(9999999,"Barbarian"),
        )
    }

    var journal: Journal = Journal()
    var dangerMode: Boolean = false
    var thrownTag: Thing.Tag? = null
    var tempInvisible: Boolean = false
    override fun glyph() = if (tempInvisible) Glyph.BLANK else Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian.  His good looks are somewhat marred by a permanent scowl."
    override fun dname() = name()
    override fun iname() = name()

    override fun initialFactions() = mutableSetOf(App.factions.humans)

    override fun visualRange() = 30f
    override fun hasActionJuice() = queuedActions.isNotEmpty()
    override fun wantsToAct() = true
    override fun defaultAction(): Action? = null
    override fun canSwimShallow() = true

    var xp = 0
    var levelUpsAvailable = 0
    var lastChunkThreatLevel = 0

    var temperature = 70
    var feltTemperature = 70

    var calories = 1000f

    val autoPickUpTypes = mutableListOf<Thing.Tag>()
    val ignoredAutoPickupTypes = mutableListOf<Thing.Tag>()

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
            Screen.brightnessTarget = 0f
            delay(1000L)
            App.wipeStateAndReturnToMenu()
        }
    }

    override fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean {
        if (dangerMode) {
            drawIt(Glyph.HOSTILE_ICON)
            return true
        }
        return super.drawStatusGlyph(drawIt)
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
        level?.also { level ->
            if (CARDINALS.contains(dir) && level.freeCardinalMovesFrom(xy + dir, this).size <= 2) {
                queue(AutoMove(dir, isHallway = true))
            } else {
                queue(AutoMove(dir))
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
        updateTemperature()
        updateCalories(delta)
    }

    private fun updateCalories(delta: Float) {
        var burn = (caloriesPerDay * delta / GameTime.TURNS_PER_DAY).toFloat()
        statuses.forEach { burn *= it.calorieBurnMod() }
        calories -= burn

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

    override fun onSleep(delta: Float) {
        var comfort = 0f
        var inBed = false
        statuses.forEach { comfort += it.comfort() }
        level?.thingsAt(xy.x, xy.y)?.forEach {
            if (!inBed) {
                comfort += it.sleepComfort()
                inBed = it is Bed
            }
        }
        if (!inBed) {
            comfort += Terrain.get(level?.getTerrain(xy.x, xy.y) ?: Terrain.Type.BLANK).sleepComfort()
        }
        if (level?.isRoofedAt(xy.x, xy.y) == false) comfort += -0.3f

        if (Dice.chance(0.1f)) {
            Console.say(when {
                comfort <= -0.5f -> "You toss and turn uncomfortably."
                else -> "You dream of large women."
            })
            log.info("sleep comfort: $comfort")
        }

        if (hp < hpMax && comfort > -0.5f) {
            val healChance = (0.5f + (comfort * 0.5f)) * healSpeed
            if (Dice.chance(healChance)) {
                healDamage((hpMax / 20f), null)
            }
        }
    }

    override fun wantsToPickUp(thing: Thing) = isAutoActionSafe() && autoPickUpTypes.contains(thing.tag)

    open fun addAutoPickUpType(type: Thing.Tag) {
        autoPickUpTypes.add(type)
        ignoredAutoPickupTypes.remove(type)
        Console.say("You remind yourself to pick up any " + type.pluralName + " you find.")
    }
    open fun removeAutoPickUpType(type: Thing.Tag) {
        autoPickUpTypes.remove(type)
        ignoredAutoPickupTypes.add(type)
        Console.say("You give up on your $type collection.")
    }

    override fun canSee(entity: Entity?) = entity != null && entity.level() == level && level!!.visibilityAt(entity.xy().x, entity.xy().y) == 1f

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

    fun addWetness(amount: Float, max: Float = 1f) {
        val wetStatus = status(Status.Tag.WET) as Wet?
        val current = wetStatus?.let { (it as Wet).wetness } ?: 0f
        if (current <= 0f) {
            addStatus(Wet().apply { wetness = amount })
        } else {
            wetStatus?.addWetness(amount)
        }
    }

    private fun updateTemperature() {
        level?.also { level ->
            temperature = level.temperatureAt(xy)
            feltTemperature = temperature + App.weather.feltTemperature()
            feltTemperature += App.player.status(Status.Tag.WET)?.let { (it as Wet).temperatureMod() } ?: 0
            forXY(-3, -3, 3, 3) { ix, iy ->

            }

            var clothing = 0
            gear.values.forEach { gear ->
                if (gear is Clothing) {
                    clothing += gear.wornTemperature()
                }
            }
            if (clothing > 0) {
                if (temperature > 68) {
                    feltTemperature += clothing / 2
                } else {
                    feltTemperature += clothing
                }
            } else if (clothing < 0) {
                if (temperature < 50) {
                    feltTemperature += clothing
                } else {
                    feltTemperature += clothing / 2
                }
            }
            if (hasStatus(Status.Tag.ASLEEP)) {
                level.thingsAt(xy.x, xy.y).firstOrNull { it is Bed }?.also {
                    if (temperature > 68) {
                        feltTemperature -= 5
                    } else if (temperature < 50) {
                        feltTemperature += 15
                    }
                } ?: run {
                    if (temperature < 50) {
                        feltTemperature -= 5
                    }
                }
                if (!level.isRoofedAt(xy.x, xy.y)) {
                    feltTemperature += if (temperature < 50) -8 else 0
                }
            }

            if (feltTemperature > Heatstroke.threshold) {
                removeStatus(Status.Tag.HOT)
                removeStatus(Status.Tag.COLD)
                removeStatus(Status.Tag.FREEZING)
                if (!hasStatus(Status.Tag.HEATSTROKE)) {
                    Console.say("You begin to slowly succumb to the roasting heat.")
                    addStatus(Heatstroke())
                }
            } else if (feltTemperature > Hot.threshold) {
                var told = false
                if (removeStatus(Status.Tag.HEATSTROKE)) {
                    Console.say("The heat no longer feels life threatening.")
                    told = true
                }
                removeStatus(Status.Tag.COLD)
                removeStatus(Status.Tag.FREEZING)
                if (!hasStatus(Status.Tag.HOT)) {
                    if (!told) Console.say("You feel sluggish in the oppressive heat.")
                    addStatus(Hot())
                }
            } else if (feltTemperature < Freezing.threshold) {
                removeStatus(Status.Tag.HOT)
                removeStatus(Status.Tag.HEATSTROKE)
                removeStatus(Status.Tag.COLD)
                if (!hasStatus(Status.Tag.FREEZING)) {
                    Console.say("You don't feel so cold now.  You feel peaceful.")
                    addStatus(Freezing())
                }
            } else if (feltTemperature < Cold.threshold) {
                removeStatus(Status.Tag.HOT)
                removeStatus(Status.Tag.HEATSTROKE)
                var told = false
                if (removeStatus(Status.Tag.FREEZING)) {
                    Console.say("You snap out of your reverie.  Must...push on...")
                    told = true
                }
                if (!hasStatus(Status.Tag.COLD)) {
                    if (!told) Console.say("You shiver uncontrollably.")
                    addStatus(Cold())
                }
            } else {
                if (removeStatus(Status.Tag.HOT)) Console.say("You wipe the sweat from your brow.")
                if (removeStatus(Status.Tag.HEATSTROKE)) Console.say("You wipe your brow with great relief.")
                if (removeStatus(Status.Tag.COLD)) Console.say("You don't feel so cold.")
                if (removeStatus(Status.Tag.FREEZING)) Console.say("Ah, blessed warmth!")
            }
        }
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
        if (effLevel >= levels.size - 1) return
        xp += added
        if (xp >= levels[effLevel].xp) {
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
        Console.say("You feel your potential realized.  You are Thundarr the ${levels[xpLevel].name.capitalize()}.")
        level?.addSpark(GlyphRise(Glyph.PLUS_ICON_BLUE).at(xy.x, xy.y))

        Heart.improve(this, fullLevel = true)
        hpMax += 5
        hp  = hpMax
    }
}

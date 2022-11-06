package actors

import actors.actions.*
import actors.animations.Animation
import actors.animations.Step
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.skills.Dodge
import actors.stats.skills.Skill
import actors.statuses.StatEffector
import actors.statuses.Status
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.sparks.Gore
import render.sparks.HealthUp
import render.sparks.Scoot
import render.sparks.Spark
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import ui.panels.StatusPanel
import util.*
import world.Entity
import world.Level
import world.path.Pather
import world.stains.Blood
import world.terrains.Terrain
import java.lang.Float.max
import java.lang.Integer.min

@Serializable
sealed class Actor : Entity, ThingHolder, LightSource, Temporal {

    companion object {
        val fist: MeleeWeapon = Fist()
    }

    var isUnloading = false
    val xy = XY(0,0)
    var juice = 0f // How many turns am I owed?
    val contents = mutableListOf<Thing>()

    val stats = mutableMapOf<Stat.Tag, Stat.Value>()
    val statuses = mutableListOf<Status>()

    @Transient val queuedActions: MutableList<Action> = mutableListOf()

    var hp = 20
    var hpMax = 20

    @Transient
    override var level: Level? = null
    @Transient
    val gear = mutableMapOf<Gear.Slot, Gear?>()

    @Transient
    var animation: Animation? = null
        set(value) {
            field = value
            value?.onStart()
        }

    open fun speed() = 0.5f + Speed.get(this) * 0.05f
    open fun visualRange() = 22f

    open fun bleedChance() = 0.6f
    open fun stepAnimation(dir: XY) = Step(dir)
    open fun stepSpark(dir: XY): Spark? = Scoot(dir)
    open fun stepSound(dir: XY): Speaker.SFX? = level?.let { level ->
        Terrain.get(level.getTerrain(xy.x + dir.x, xy.y + dir.y)).stepSound(this)
    }

    override fun level() = level
    override fun xy() = xy
    override fun contents() = contents
    override fun glyphBatch() = Screen.actorBatch
    override fun uiBatch() = Screen.uiActorBatch
    var mirrorGlyph = false

    abstract fun hasActionJuice(): Boolean
    abstract fun wantsToAct(): Boolean

    open fun onRestore() {
        contents.forEach { it.onRestore(this) }
        isUnloading = false
    }

    fun moveTo(level: Level?, x: Int, y: Int) {
        val oldX = this.xy.x
        val oldY = this.xy.y
        val oldLevel = this.level
        this.level = level
        this.xy.x = x
        this.xy.y = y
        Pather.onActorMove(this)
        oldLevel?.onActorMovedFrom(this, oldX, oldY)
        if (oldLevel != level) oldLevel?.director?.detachActor(this)
        level?.onActorMovedTo(this, x, y)
        if (oldLevel != level) level?.director?.attachActor(this)
        onMove()
    }

    open fun onMove() { }

    open fun onConverse(actor: Actor): Boolean = false

    open fun statusGlyph(): Glyph? = null

    open fun willAggro(target: Actor) = false

    // What will I do right now?
    open fun nextAction(): Action? = if (queuedActions.isNotEmpty()) {
        val action = queuedActions[0]
        if (!action.shouldContinueFor(this)) {
            queuedActions.remove(action)
        }
        action
    } else defaultAction()

    // With nothing queued, what should I decide to do now?
    abstract fun defaultAction(): Action?

    // Queue an action to be executed next.
    fun queue(action: Action) {
        if (action.canQueueFor(this)) queuedActions.add(action)
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        if (thing is LightSource && thing.light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
    }

    override fun add(thing: Thing) {
        contents.add(thing)
        if (thing is LightSource && light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
    }

    override fun light(): LightColor? {
        var light: LightColor? = null
        contents.forEach { thing ->
            if (thing is LightSource) {
                val thingLight = thing.light()
                if (thingLight != null) {
                    if (light == null) light = LightColor(0f,0f,0f)
                    light!!.r += thingLight.r
                    light!!.g += thingLight.g
                    light!!.b += thingLight.b
                }
            }
        }
        return light
    }

    fun animOffsetX() = animation?.offsetX() ?: 0f
    fun animOffsetY() = animation?.offsetY() ?: 0f

    final override fun onRender(delta: Float) {
        animation?.also { if (it.done) animation = null else it.onRender(delta) }
        doOnRender(delta)
    }
    open fun doOnRender(delta: Float) { }

    fun renderShadow(doDraw: (Double,Double,Double,Double)->Unit) {
        val extra = (shadowWidth() - 1f) * 0.5f
        val offset = shadowXOffset()
        val ox = animation?.shadowOffsetX()?.toDouble() ?: 0.0
        val oy = animation?.shadowOffsetY()?.toDouble() ?: 0.0
        val x0 = xy.x.toDouble() + ox - extra + offset
        val y0 = xy.y.toDouble() + oy
        val x1 = x0 + 1.0 + extra + offset
        val y1 = y0 + 1.0
        doDraw(x0, y0, x1, y1)
    }
    open fun shadowWidth() = 1f
    open fun shadowXOffset() = 0f

    fun knownSkills() = stats.keys.map { Stat.get(it) }.filterIsInstance<Skill>()

    fun equippedOn(slot: Gear.Slot): Gear? = gear[slot]

    fun equipGear(gear: Gear) {
        equippedOn(gear.slot)?.also { current ->
            queue(Unequip(current))
        }
        queue(Equip(gear))
    }

    fun onEquip(gear: Gear) {
        gear.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
    }

    fun unequipGear(gear: Gear) {
        if (gear.equipped) {
            queue(Unequip(gear))
        }
    }

    fun onUnequip(gear: Gear) {
        gear.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
    }

    fun gainHealth(amount: Float) {
        hp = min(hpMax, (hp + amount).toInt())
        level?.addSpark(HealthUp().at(xy.x, xy.y))
    }

    // Take damage and react to attacker if any.  Return damage that actually got through.
    fun receiveDamage(raw: Float, attacker: Actor? = null): Float {
        val armor = armorTotal()
        val amount = raw - armor
        if (amount > 0f) {
            if (Dice.chance(bleedChance() * amount * 0.35f)) {
                level?.addStain(Blood(), xy.x, xy.y)
                if (Dice.chance(0.2f * amount)) {
                    level?.addStain(Blood(), xy.x - 1 + Dice.zeroTo(2), xy.y - 1 + Dice.zeroTo(2))
                }
            }
            hp = max(0f, hp - amount).toInt()
        }
        if (hp < 1) die() else attacker?.also { receiveAggression(it) }
        return amount
    }

    open fun receiveAggression(attacker: Actor) { }

    open fun die() {
        level?.also { level ->
            val corpse = corpse()
            corpse.moveTo(level, xy.x, xy.y)
            repeat (6) {
                level.addSpark(Gore().at(xy.x, xy.y))
            }
            onDeath(corpse)
            contents.forEach { it.moveTo(corpse) }
        } ?: run {
            contents.forEach { it.moveTo(null) }
        }

        this.animation = null
        Pather.unsubscribeAll(this)
        moveTo(null, 0, 0)
    }

    open fun corpse() = Corpse()
    open fun onDeath(corpse: Corpse) { }

    override fun advanceTime(delta: Float) {
        statuses.forEach {
            it.advanceTime(this, delta)
        }
        statuses.filterAnd({ it.done }) { onRemoveStatus(it) }
    }

    fun addStatus(status: Status) {
        statuses.forEach {
            if (it.tag == status.tag) {
                it.onAddStack(this, status)
                return
            }
        }
        statuses.add(status)
        status.onAdd(this)
        status.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
        if (this is Player) StatusPanel.refillCache()
    }

    fun removeStatus(statusTag: Status.Tag) {
        statuses.firstOrNull { it.tag == statusTag }?.also { status ->
            statuses.remove(status)
            onRemoveStatus(status)
        }
    }

    fun statEffectors(stat: Stat) = ArrayList<StatEffector>().apply {
        addAll(statuses.filter { it.statEffects().containsKey(stat.tag) })
        addAll(gear.values.filter { it != null && it.statEffects().containsKey(stat.tag) }.map { it as StatEffector })
    }

    private fun onRemoveStatus(status: Status) {
        status.onRemove(this)
        status.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
        if (this is Player) StatusPanel.refillCache()
    }

    ///// combat info

    fun tryDodge(attacker: Actor, weapon: MeleeWeapon, bonus: Float): Float {
        // skip this if we're under certain statuses
        val roll = Dodge.resolve(this, 0f - bonus)
        var result = 0f
        if (roll > 0f) {
            if (roll > 3f) Console.sayAct("You dodge unpredictably.", "%Dn dodges unpredictably.", this)
            result = 0f - (roll * 0.5f)
        } else if (roll < -6f) {
            Console.sayAct("You stumble, exposing yourself.", "%Dn stumbles, creating an opening.", this)
            result = 4f
        }
        return result
    }

    open fun meleeWeapon(): MeleeWeapon = equippedOn(Gear.Slot.MELEE)?.let { it as MeleeWeapon } ?: fist

    open fun armorTotal(): Float {
        var total = 0f
        gear.values.forEach { if (it is Clothing) total += it.armor() }
        return total
    }


    ///// action generators

    fun useThing(thing: Thing, useTag: Thing.UseTag): Use? =
        thing.uses()[useTag]?.let { use ->
            if (use.canDo(this)) Use(thing, use.duration, use.toDo) else null
        }

    fun stepToward(target: Entity): Move? {
        Pather.nextStep(this, target)?.also { s ->
            return Move(XY(s.x - xy.x, s.y - xy.y))
        }
        return null
    }

    ///// useful utilities

    protected fun doWeHave(thingTag: String): Thing? {
        for (i in 0 until contents.size) if (contents[i].thingTag() == thingTag) return contents[i]
        return null
    }

    protected fun doWeHave(test: (Thing)->Boolean): Thing? {
        for (i in 0 until contents.size) if (test(contents[i])) return contents[i]
        return null
    }

    protected fun howManyWeHave(thingTag: String): Int {
        var c = 0
        for (i in 0 until contents.size) if (contents[i].thingTag() == thingTag) c++
        return c
    }

    protected fun entitiesSeen(matching: ((Entity)->Boolean)? = null) = Pather.entitiesSeenBy(this, matching)

    protected fun entitiesNextToUs(): Set<Entity> {
        val entities = mutableSetOf<Entity>()
        level?.also { level ->
            DIRECTIONS.forEach { dir ->
                level.actorAt(xy.x + dir.x, xy.y + dir.y)?.also { entities.add(it) }
                entities.addAll(level.thingsAt(xy.x + dir.x, xy.y + dir.y))
            }
        }
        return entities
    }

    protected fun canStep(dir: XY) = level?.isWalkableFrom(xy, dir) ?: false

    protected fun distanceTo(entity: Entity) = distanceBetween(entity.xy()?.x ?: 0, entity.xy()?.y ?: 0, xy.x, xy.y)

    protected fun forCardinals(doThis: (tx: Int, ty: Int, dir: XY)->Unit) {
        CARDINALS.forEach { dir ->
            val tx = xy.x + dir.x
            val ty = xy.y + dir.y
            doThis(tx, ty, dir)
        }
    }
}

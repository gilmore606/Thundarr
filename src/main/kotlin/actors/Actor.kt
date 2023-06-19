package actors

import actors.actions.*
import actors.animations.Animation
import actors.animations.Step
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Skill
import actors.statuses.*
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.sparks.*
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import ui.panels.StatusPanel
import util.*
import world.Entity
import world.level.Level
import world.journal.JournalEntry
import world.path.Pather
import world.stains.Blood
import world.stains.Stain
import world.terrains.Terrain
import java.lang.Float.max
import java.lang.Float.min

@Serializable
sealed class Actor : Entity, ThingHolder, LightSource, Temporal {

    companion object {
        val fist: MeleeWeapon = Fist()
        val caster = RayCaster()
    }

    val id = UUID()
    var isUnloading = false
    val xy = XY(0,0)
    var juice = 0f // How many turns am I owed?
    val contents = mutableListOf<Thing>()
    val lastSeenLocation = XY(-9999,-9999)

    val stats = mutableMapOf<Stat.Tag, Stat.Value>()
    val statuses = mutableListOf<Status>()

    open fun initialFactions() = mutableSetOf<String>().apply {
        if (isHuman()) add(App.factions.humans)
        if (isMonster()) add(App.factions.monsters)
    }
    val factions = initialFactions()

    @Transient val queuedActions: MutableList<Action> = mutableListOf()

    @Transient var seen = mutableMapOf<Entity, Float>()
    @Transient var seenUpdatedAt = 0.0

    var hp: Float = 20f
    var hpMax: Float = 20f

    @Transient
    override var level: Level? = null
    @Transient
    val gear = mutableMapOf<Gear.Slot, Gear?>()
    @Transient
    val gearDrawList = ArrayList<Gear>()

    @Transient
    var animation: Animation? = null
        set(value) {
            level?.also { level ->
                if (level.visibilityAt(xy.x, xy.y) == 1f) {
                    field = value
                    value?.onStart()
                }
            }
        }

    open fun hasProperName() = false
    override fun dname() = if (hasProperName()) (name()) else ("the " + name())
    override fun dnamec() = if (hasProperName()) (name()) else ("The " + name())
    override fun iname() = if (hasProperName()) (name()) else (name().aOrAn())
    override fun inamec() = if (hasProperName()) (name()) else (name().aOrAn().capitalize())

    open fun isSentient() = true
    open fun isHuman() = false
    open fun isMonster() = false
    open fun canOpenDoors() = isHuman()
    open fun canWalkOn(terrain: Terrain) = true
    open fun canSwimShallow() = false
    open fun canSwimDeep() = false
    open fun canSwimLava() = false
    open fun canFly() = false
    open fun actionSpeed() = 1.5f - Speed.get(this) * 0.05f
    open fun visualRange() = 10f

    open fun bleedChance() = 0.6f
    open fun stepAnimation(dir: XY): Animation = Step(dir)
    open fun stepSpark(dir: XY): Spark? = level?.let { level ->
        Terrain.get(level.getTerrain(xy.x + dir.x, xy.y + dir.y)).stepSpark(this, dir)
    }
    open fun stepSound(dir: XY): Speaker.SFX? = level?.let { level ->
        Terrain.get(level.getTerrain(xy.x + dir.x, xy.y + dir.y)).stepSound(this)
    }
    open fun talkSound(actor: Actor): Speaker.SFX? = null

    override fun level() = level
    override fun xy() = xy
    override fun contents() = contents
    override fun glyphBatch() = Screen.actorBatch
    override fun uiBatch() = Screen.uiActorBatch
    var mirrorGlyph = false
    var rotateGlyph = false

    open fun corpse(): Container? = Corpse(name())
    open fun bloodstain(): Stain? = Blood()
    open fun gore(): Spark? = BloodGore()

    open fun wantsToPickUp(thing: Thing): Boolean = false

    abstract fun hasActionJuice(): Boolean
    abstract fun wantsToAct(): Boolean
    fun isActing() = queuedActions.isNotEmpty()

    open fun onRestore() {
        contents.forEach {
            it.onRestore(this)
            if (it is Temporal) level?.linkTemporal(it)
        }
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

    open fun onMove() {
        level?.thingsAt(xy.x, xy.y)?.forEach { thing ->
            if (wantsToPickUp(thing)) queue(Get(thing))
        }
    }

    open fun onConverse(actor: Actor): Boolean = false

    open fun drawStatusGlyphs(drawIt: (Glyph)->Unit) {
        statuses.forEach { it.statusGlyph(this)?.also { drawIt(it) } }
    }

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

    fun cancelAction() {
        if (queuedActions.isNotEmpty()) {
            val action = queuedActions.removeAt(0)
            action.onCancel(this)
        }
    }

    fun doAction(action: Action) {
        level?.also {  level ->
            statuses.forEach { status ->
                if (status.preventedAction(action, this)) return
            }
            action.convertTo(this, level)?.also {
                it.execute(this, level)
            } ?: run {
                action.execute(this, level)
            }
        }
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        if (thing is LightSource && thing.light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
        updateEncumbrance()
    }

    override fun add(thing: Thing) {
        contents.add(thing)
        if (thing is LightSource && light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
        updateEncumbrance()
    }

    private fun updateEncumbrance() {
        val carried = contents.total { it.weight() }
        val capacity = carryingCapacity()
        if (carried > capacity) {
            removeStatus(Status.Tag.ENCUMBERED)
            if (!hasStatus(Status.Tag.BURDENED)) {
                addStatus(Burdened())
                if (this is Player) Console.say("You couldn't possibly carry any more.")
            }
        } else if (carried > capacity * 0.5f) {
            if (hasStatus(Status.Tag.BURDENED)) {
                removeStatus(Status.Tag.BURDENED)
                if (this is Player) Console.say("Your burden feels somewhat lighter.")
            } else if (this is Player && !hasStatus(Status.Tag.ENCUMBERED)) Console.say("You feel weighed down by possessions.")
            addStatus(Encumbered())
        } else {
            if (hasStatus(Status.Tag.ENCUMBERED) || hasStatus(Status.Tag.BURDENED)) Console.say("Your burden feels comfortable now.")
            removeStatus(Status.Tag.ENCUMBERED)
            removeStatus(Status.Tag.BURDENED)
        }
    }

    fun carryingCapacity() = 10f + Strength.get(this) * 2f

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

    open fun animOffsetX() = animation?.offsetX() ?: 0f
    open fun animOffsetY() = animation?.offsetY() ?: if (rotateGlyph) 0.3f else 0f

    final override fun onRender(delta: Float) {
        if (level?.visibilityAt(xy.x, xy.y) == 1f) {
            animation?.also { if (it.done) animation = null else it.onRender(delta) }
            doOnRender(delta)
        } else {
            animation = null
        }
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

    fun setGearSlot(slot: Gear.Slot, newGear: Gear?) {
        gear[slot]?.also { gearDrawList.remove(it) }
        gear[slot] = newGear
        newGear?.also { newGear ->
            gearDrawList.add(newGear)
            gearDrawList.sortByDescending { it.slot.drawOrder }
        }
    }

    fun gainHealth(amount: Float) {
        hp = min(hpMax, (hp + amount))
        level?.addSpark(HealthUp().at(xy.x, xy.y))
    }

    // Take damage and react to attacker if any.  Return damage that actually got through.
    fun receiveDamage(raw: Float, attacker: Actor? = null, internal: Boolean = false): Float {
        val armor = armorTotal()
        val amount = if (internal) raw else raw - armor
        if (amount > 0f) {
            if (Dice.chance(bleedChance() * amount * 0.35f)) {
                bloodstain()?.also { stain ->
                    level?.addStain(stain, xy.x, xy.y)
                    if (Dice.chance(0.2f * amount)) {
                        bloodstain()?.also { level?.addStain(it, xy.x - 1 + Dice.zeroTo(2), xy.y - 1 + Dice.zeroTo(2)) }
                    }
                }
            }
            repeat (amount.toInt()) {
                gore()?.also { level?.addSpark(it.at(xy.x, xy.y)) }
            }
            hp = max(0f, hp - amount)
            if (Dice.chance(amount * 0.1f)) {
                addStatus(Stunned())
            }
        }
        if (hp < 1) {
            die()
            attacker?.also { onKilledBy(it) }
        } else attacker?.also { receiveAggression(it) }
        return amount
    }

    fun healDamage(heal: Float, healer: Actor? = null) {
        hp = min(hpMax, hp + heal)
        healer?.also { receiveAssistance(it) }
    }

    open fun receiveAggression(attacker: Actor) { }
    open fun receiveAssistance(assister: Actor) { }

    open fun onKilledBy(killer: Actor) {
        if (isHuman() && killer is Player) {
            killer.journal.achieve(JournalEntry(
                "Mortality.",
                "Today, I was forced to...kill a man.  I pray to the Lords of Light that it will be the last time.  But in my heart, I know it will not.  Not by a long shot."
            ))
        }
    }

    open fun die() {
        level?.also { level ->
            makeCorpse(level)
        } ?: run {
            contents.forEach { it.moveTo(null) }
        }
        this.animation = null
        Pather.unsubscribeAll(this)
        moveTo(null, 0, 0)
    }

    protected fun makeCorpse(level: Level) {
        repeat (6) {
            gore()?.also { level.addSpark(it.at(xy.x, xy.y)) }
        }
        corpse()?.also { corpse ->
            corpse.moveTo(level, xy.x, xy.y)
            onDeath(corpse)
            contents.safeForEach { it.moveTo(corpse) }
        } ?: run {
            onDeath(null)
            contents.safeForEach { it.moveTo(xy.x, xy.y) }
        }
    }

    open fun onDeath(corpse: Container?) { }

    open fun ingestCalories(cal: Int) { }

    override fun advanceTime(delta: Float) {
        statuses.safeForEach { it.advanceTime(this, delta) }
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
        if (status.proneGlyph()) updateRotateGlyph()
        if (this is Player) StatusPanel.refillCache()
    }

    fun removeStatus(statusTag: Status.Tag) {
        statuses.firstOrNull { it.tag == statusTag }?.also { status ->
            statuses.remove(status)
            onRemoveStatus(status)
            if (status.proneGlyph()) updateRotateGlyph()
        }
    }

    fun fallAsleep() {
        addStatus(Asleep())
        Console.sayAct("You fall asleep.", "%N falls asleep.", this)
    }

    fun wakeFromSleep() {
        removeStatus(Status.Tag.ASLEEP)
        Console.sayAct("You wake up.", "%N wakes up.", this)
    }

    open fun onSleep() {
        if (hp < hpMax) {
            if (!hasStatus(Status.Tag.STARVING)) {
                if (Dice.chance(0.3f)) {
                    var healmax = 5 + if (hasStatus(Status.Tag.HUNGRY)) -2 else if (hasStatus(Status.Tag.SATIATED)) 1 else 0
                    healmax += (statuses.firstOrNull { it.tag == Status.Tag.BANDAGED } as Bandaged?)?.quality?.toInt() ?: -3
                    val heal = Dice.range(0, Math.max(1, healmax))
                    healDamage(heal.toFloat())
                }
            }
        }
    }

    private fun updateRotateGlyph() {
        this.rotateGlyph = false
        statuses.safeForEach { if (it.proneGlyph()) this.rotateGlyph = true }
    }

    fun hasStatus(statusTag: Status.Tag) = statuses.hasOneWhere { it.tag == statusTag }

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

    fun tryDodge(attacker: Actor, weapon: Thing, bonus: Float): Float {
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
            if (use.canDo(this, xy.x, xy.y, false)) Use(useTag, thing, use.duration) else null
        }

    fun stepToward(target: Entity): Move? {
        Pather.nextStep(this, target)?.also { s ->
            return Move(XY(s.x - xy.x, s.y - xy.y))
        }
        return null
    }

    fun stepToward(xy: XY): Move? {
        Pather.nextStep(this, xy)?.also { return Move(it) }
        return null
    }

    fun stepToward(rect: Rect): Move? {
        Pather.nextStep(this, rect)?.also { return Move(it) }
        return null
    }

    ///// useful utilities

    fun getActor(id: String) = level?.director?.getActor(id)

    protected fun doWeHave(thingTag: Thing.Tag): Thing? {
        for (i in 0 until contents.size) if (contents[i].tag == thingTag) return contents[i]
        return null
    }

    protected fun doWeHave(test: (Thing)->Boolean): Thing? {
        for (i in 0 until contents.size) if (test(contents[i])) return contents[i]
        return null
    }

    protected fun howManyWeHave(thingTag: Thing.Tag): Int {
        var c = 0
        for (i in 0 until contents.size) if (contents[i].tag == thingTag) c++
        return c
    }

    open fun canSee(entity: Entity?): Boolean {
        if (level == null) return false
        if (entity == null) return false
        if (entity.level() != level) return false
        entity.xy().also { entityXY ->
            if (distanceBetween(entityXY, xy) > visualRange()) return false
        }

        return entitiesSeen { it == entity }.isNotEmpty()
    }

    fun entitiesNextToUs(matching: ((Entity)->Boolean)? = null): Set<Entity> {
        val entities = mutableSetOf<Entity>()
        level?.also { level ->
            DIRECTIONS.from(xy.x, xy.y) { dx, dy, _ ->
                level.actorAt(dx, dy)?.also { entities.add(it) }
                entities.addAll(level.thingsAt(dx, dy))
            }
            matching?.also { matching ->
                val filtered = mutableSetOf<Entity>()
                entities.forEach { if (matching(it)) filtered.add(it) }
                return filtered
            }
        }
        return entities
    }

    fun entitiesSeen(matching: ((Entity)->Boolean)? = null): Map<Entity, Float> {
        if (seenUpdatedAt < App.time) updateSeen()
        matching?.also { matching ->
            val filtered = mutableMapOf<Entity, Float>()
            seen.keys.forEach { if (matching(it)) filtered[it] = seen[it]!! }
            return filtered
        }
        return seen
    }

    private fun updateSeen() {
        seen.clear()
        caster.populateSeenEntities(seen, this)
        seenUpdatedAt = App.time
    }

    protected fun canStep(dir: XY) = level?.isWalkableFrom(this, xy, dir) ?: false

    protected fun distanceTo(entity: Entity) = distanceBetween(entity.xy().x, entity.xy().y, xy.x, xy.y)

    protected fun forCardinals(doThis: (tx: Int, ty: Int, dir: XY)->Unit) {
        CARDINALS.forEach { dir ->
            val tx = xy.x + dir.x
            val ty = xy.y + dir.y
            doThis(tx, ty, dir)
        }
    }

    fun joinFaction(factionID: String) {
        factions.add(factionID)
    }

}

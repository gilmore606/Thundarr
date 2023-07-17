package actors.actors

import actors.actions.*
import actors.actions.events.Event
import actors.animations.Animation
import actors.animations.Step
import actors.bodyparts.*
import actors.stats.Senses
import actors.stats.Speed
import actors.stats.Stat
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Skill
import actors.stats.skills.Sneak
import actors.statuses.*
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.sparks.*
import render.tilesets.Glyph
import things.*
import ui.panels.Console
import util.*
import world.Entity
import world.level.Level
import world.journal.JournalEntry
import path.Pather
import path.StepMap
import world.stains.Blood
import world.stains.Fire
import world.stains.Stain
import world.terrains.Terrain
import java.lang.Float.max
import java.lang.Float.min
import java.lang.Math.abs

@Serializable
sealed class Actor : Entity, ThingHolder, LightSource, Temporal {

    companion object {
        val fist: MeleeWeapon = Fist()
        val caster = RayCaster()
    }

    val id = UUID()
    override fun getHolderKey() = ThingHolder.Key(ThingHolder.Type.ACTOR, actorKey = id)

    override fun toString() = "${name()} ($id)"

    var isUnloading = false
    val xy = XY(0,0)
    var juice = 0f // How many turns am I owed?
    val contents = mutableListOf<Thing>()
    val lastSeenLocation = XY(-9999,-9999)
    val keyIDs = mutableSetOf<String>()

    var cash = 0
    var xpLevel = 1
    val stats = mutableMapOf<Stat.Tag, Stat.Value>()
    val statuses = mutableListOf<Status>()

    var savedStepMaps: MutableList<StepMap> = mutableListOf()

    open fun initialFactions() = mutableSetOf<String>().apply {
        if (isHuman()) add(App.factions.humans)
        if (isMonster()) add(App.factions.monsters)
    }
    val factions = initialFactions()

    val queuedActions: MutableList<Action> = mutableListOf()


    var hp: Float = 20f
    var hpMax: Float = 20f

    val bodyparts: Set<Bodypart> = initialBodyparts()
    open fun initialBodyparts() = Bodypart.humanoid()

    open fun skinArmor() = 0f
    open fun skinArmorMaterial() = Clothing.Material.HIDE
    open fun skinDeflect() = 0f

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

    @Transient var currentSenses: Float = 0f
    @Transient var currentSneak: Float = 0f

    @Transient var seen = mutableMapOf<Entity, Float>()
    @Transient var seenUpdatedAt = 0.0
    @Transient var seenPrevious = mutableMapOf<Entity, Float>()

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
        Pather.restoreActorMaps(this)
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
        level?.getTerrain(x, y)?.also { terrain ->
            Terrain.get(terrain).onStep(this, x, y, level.getTerrainData(x, y))
        }
        onMove()
    }

    open fun onMove() {
        level?.thingsAt(xy.x, xy.y)?.forEach { thing ->
            if (wantsToPickUp(thing)) queue(Get(thing.getKey()))
        }
    }

    open fun onConverse(actor: Actor): Boolean = false

    open fun drawStatusGlyph(drawIt: (Glyph)->Unit): Boolean {
        var drawn = false
        statuses.forEach { it.statusGlyph(this)?.also {
            if (!drawn) {
                drawIt(it)
                drawn = true
            }
        } }
        return drawn
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
            queue(Unequip(current.getKey()))
        }
        queue(Equip(gear.getKey()))
    }

    fun onEquip(gear: Gear) {
        gear.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
    }

    fun unequipGear(gear: Gear) {
        if (gear.equipped) {
            queue(Unequip(gear.getKey()))
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

    fun say(text: String?) {
        text?.also { text ->
            val t = if (text.startsWith(":")) {
                "${dnamec()} ${text.drop(1)}"
            } else {
                "${dnamec()} says, \"$text\""
            }
            Console.sayAct("", t, this)
            level?.addSpark(Speak().at(xy.x, xy.y))
            Speaker.world(talkSound(App.player), source = xy)
        }
    }

    fun gainHealth(amount: Float) {
        hp = min(hpMax, (hp + amount))
        level?.addSpark(GlyphRise(Glyph.PLUS_ICON_GREEN).at(xy.x, xy.y))
    }

    fun receiveDamage(amount: Float, attacker: Actor? = null, internal: Boolean = false) {
        if (!internal) {
            if (Dice.chance(bleedChance() * amount * 0.35f)) {
                bloodstain()?.also { stain ->
                    level?.addStain(stain, xy.x, xy.y)
                    if (Dice.chance(0.2f * amount)) {
                        bloodstain()?.also {
                            level?.addStain(
                                it,
                                xy.x - 1 + Dice.zeroTo(2),
                                xy.y - 1 + Dice.zeroTo(2)
                            )
                        }
                    }
                }
            }
            repeat(amount.toInt()) {
                gore()?.also { level?.addSpark(it.at(xy.x, xy.y)) }
            }
        }
        hp = max(0f, hp - amount)
        if (hp < 1) {
            die()
            attacker?.also { onKilledBy(it) }
        }
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
    }

    fun removeStatus(statusTag: Status.Tag): Boolean {
        statuses.firstOrNull { it.tag == statusTag }?.also { status ->
            statuses.remove(status)
            onRemoveStatus(status)
            if (status.proneGlyph()) updateRotateGlyph()
            return true
        }
        return false
    }

    fun fallAsleep() {
        addStatus(Asleep())
        Console.sayAct("You fall asleep.", "%N falls asleep.", this)
    }

    fun wakeFromSleep() {
        removeStatus(Status.Tag.ASLEEP)
        Console.sayAct("You wake up.", "%N wakes up.", this)
    }

    open fun onSleep(delta: Float) {
        if (hp < hpMax) {
            if (Dice.chance(0.3f)) {
                var healmax = 3
                val heal = Dice.range(0, Math.max(1, healmax))
                healDamage(heal.toFloat())
            }
        }
    }

    private fun updateRotateGlyph() {
        this.rotateGlyph = false
        statuses.safeForEach { if (it.proneGlyph()) this.rotateGlyph = true }
    }

    fun hasStatus(statusTag: Status.Tag) = statuses.hasOneWhere { it.tag == statusTag }

    fun status(statusTag: Status.Tag) = statuses.firstOrNull { it.tag == statusTag }

    fun statEffectors(stat: Stat) = ArrayList<StatEffector>().apply {
        addAll(statuses.filter { it.statEffects().containsKey(stat.tag) })
        addAll(gear.values.filter { it != null && it.statEffects().containsKey(stat.tag) }.map { it as StatEffector })
    }

    private fun onRemoveStatus(status: Status) {
        status.onRemove(this)
        status.statEffects().forEach { (tag, _) ->
            Stat.get(tag).touch(this)
        }
    }

    open fun witnessEvent(culprit: Actor?, event: Event, location: XY) { }

    ///// combat info

    fun canActivelyDefend(attacker: Actor? = null): Boolean {
        statuses.forEach { if (it.preventActiveDefense()) return false }
        attacker?.also { if (!canSee(it)) return false }
        return true
    }

    open fun meleeWeapon(): MeleeWeapon = equippedOn(Gear.Slot.MELEE)?.let { it as MeleeWeapon } ?: fist

    open fun randomBodypart(): Bodypart {
        val range = bodyparts.sumOf { it.size }
        val roll = Dice.float(0f, range)
        var acc = 0f
        bodyparts.forEach {
            acc += it.size
            if (roll <= acc) return it
        }
        return bodyparts.random()
    }

    open fun skinReduceDamage(target: Actor, type: Damage, rawDamage: Float): Float =
        max(0f, rawDamage - skinArmorMaterial().modify(type, skinArmor()))

    ///// action generators

    fun useThing(thing: Thing, useTag: Thing.UseTag): Use? =
        thing.uses()[useTag]?.let { use ->
            if (use.canDo(this, xy.x, xy.y, false)) Use(useTag, thing.getKey(), use.duration) else null
        }

    fun stepToward(target: Actor): Move? {
        Pather.nextStep(this, target)?.also { return Move(it) }
        return null
    }

    fun stepAwayFrom(target: Actor): Move? {
        Pather.nextStepAwayFrom(this, target)?.also { return Move(it) }
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

    fun hasA(thingTag: Thing.Tag): Boolean = doWeHave(thingTag) != null

    // Can we see at all?
    open fun canSee(): Boolean = statuses.hasNoneWhere { it.preventVision() }
    open fun canSeeInDark(): Boolean = false

    // Is Entity in our seen list?
    open fun canSee(entity: Entity?): Boolean {
        if (level == null) return false
        if (entity == null) return false
        if (entity.level() != level) return false
        return entitiesSeen { it == entity }.isNotEmpty()
    }

    // Given our senses and lighting, can we see XY?
    // updateSeen() uses this with checkOcclusion = false
    open fun canSee(targetXY: XY, checkOcclusion: Boolean = true): Boolean {
        if (level == null) return false
        if (!canSee()) return false
        if (distanceBetween(targetXY, xy) > visualRange()) return false
        if (checkOcclusion) {
            var occluded = false
            drawLine(targetXY, xy) { tx, ty ->
                if (level?.isOpaqueAt(tx, ty) == true) occluded = true
            }
            if (occluded) return false
        }
        if (canSeeInDark()) return true
        val light = level?.lightAt(targetXY.x, targetXY.y)?.brightness() ?: 1f
        if (light < 0.3f) return false
        if (light > 0.75f) return true
        val diff = 2 - (light / 0.1f) // 1 = lowlight 4 = high
        if (currentSenses > diff) return true
        return false
    }

    // TODO : Implement this for real
    open fun canHear(targetXY: XY, volume: Float): Boolean {
        if (level == null) return false
        if (distanceBetween(targetXY, xy) > visualRange()) return false
        return true
    }

    open fun sneakCheck(perceiver: Actor, distance: Float): Boolean {
        val sneak = this.currentSneak -
                (if (perceiver.seenPrevious.containsKey(this)) 6 else 0) +
                (distance / 3f) - 5
        return (sneak.toInt() - perceiver.currentSenses) > 0
    }

    fun isNextTo(actor: Actor) = abs(actor.xy.x - xy.x) <= 1 && abs(actor.xy.y - xy.y) <= 1

    fun isNextToFire(): Boolean {
        var found = false
        level?.also { level ->
            forXY(xy.x-1,xy.y-1,xy.x+1,xy.y+1) { dx,dy ->
                if (level.stainsAt(dx,dy).hasOneWhere { it is Fire }) found = true
            }
        }
        return found
    }

    fun entitiesNextToUs(matching: ((Entity)->Boolean) = { _ -> true }): Set<Entity> {
        val entities = mutableSetOf<Entity>()
        level?.also { level ->
            DIRECTIONS.from(xy.x, xy.y) { dx, dy, _ ->
                level.actorAt(dx, dy)?.also { if (matching(it)) entities.add(it) }
                level.thingsAt(dx, dy).forEach { if (matching(it)) entities.add(it) }
            }
        }
        return entities
    }

    fun entitiesSeen(matching: ((Entity)->Boolean)? = null): Map<Entity, Float> {
        if (seenUpdatedAt < App.time) {
            updateSenseRolls()
            updateSeen()
        }
        matching?.also { matching ->
            val filtered = mutableMapOf<Entity, Float>()
            seen.keys.forEach { if (matching(it)) filtered[it] = seen[it]!! }
            return filtered
        }
        return seen
    }

    private fun updateSeen() {
        seenPrevious = seen
        seen = mutableMapOf()
        caster.populateSeenEntities(seen, this)
        seenUpdatedAt = App.time
    }

    private fun updateSenseRolls() {
        currentSenses = Senses.resolve(this, 0f, true)

        val terrain = Terrain.get(level?.getTerrain(xy.x, xy.y) ?: Terrain.Type.TERRAIN_DIRT)
        currentSneak = Sneak.resolve(this, terrain.sneakDifficulty - 6, true)
    }

    protected fun canStep(dir: XY) = level?.isWalkableFrom(this, xy, dir) ?: false

    fun freeCardinalMoves(): Set<XY> = level?.freeCardinalMovesFrom(xy, this) ?: setOf()

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

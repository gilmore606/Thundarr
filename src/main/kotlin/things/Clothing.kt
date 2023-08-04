package things

import actors.actors.Actor
import actors.stats.Brains
import actors.stats.Stat
import actors.stats.skills.Build
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Damage.*
import ui.modals.ExamineModal.StatLine
import util.Dice
import world.Entity
import java.lang.Float.max

@Serializable
sealed class Clothing : Gear() {

    enum class Material(
        val displayName: String,
        private val vsCRUSH: Float,
        private val vsCUT: Float,
        private val vsPIERCE: Float,
        private val vsBURN: Float,
        private val vsSHOCK: Float,
        private val vsCORRODE: Float
    ) {
        CLOTH("cloth", 0.1f, 0.1f, 0f, 0f, 0.2f, 0f),
        HIDE("hide", 1f, 1f, 1f, 1f, 1f, 1f),
        FUR("fur", 1f, 1f, 1f, 0.7f, 1.2f, 1.2f),
        SCALES("scales", 0.8f, 1.2f, 1.2f, 1f, 1f, 1f),
        SHELL("shell", 0.5f, 1.5f, 1.5f, 1.5f, 2f, 1.5f),
        PLASTIC("plastic", 0.7f, 1f, 0.5f, 0.7f, 1.3f, 0.4f),
        METAL("metal", 1f, 1.4f, 1f, 1f, 0.2f, 1.5f),
        WOOD("wood", 1f, 0.5f, 0.7f, 0f, 1f, 0.5f),
        ;
        fun modify(type: Damage, amount: Float) = when (type) {
            CRUSH -> vsCRUSH * amount
            CUT -> vsCUT * amount
            PIERCE -> vsPIERCE * amount
            BURN -> vsBURN * amount
            SHOCK -> vsSHOCK * amount
            CORRODE -> vsCORRODE * amount
            else -> amount
        }
    }

    override fun equipSelfMsg() = "You put on your %d."
    override fun unequipSelfMsg() = "You take off your %d."
    override fun equipOtherMsg() = "%Dn puts on %id."
    override fun unequipOtherMsg() = "%Dn takes off %p %d."
    override fun value() = 1

    open fun material(): Material = Material.CLOTH
    open fun armor() = 0f
    open fun deflect() = 0f

    open fun coldProtection() = 0f
    open fun heatProtection() = 0f
    open fun weatherProtection() = 0f

    fun armorVs(type: Damage) = material().modify(type, armor())

    fun reduceDamage(target: Actor, type: Damage, rawDamage: Float): Float =
        max(0f, rawDamage - armorVs(type).let { Dice.float(it * 0.5f, it) })

    override fun examineStats(compareTo: Entity?) = mutableListOf<StatLine>().apply {
        add(StatLine(isSpacer = true))
        add(StatLine(
            "deflect", deflect(), showPlus = true,
            compare = compareTo?.let { (it as Clothing).deflect() }
        ))
        add(StatLine(
            "made of", valueString = material().displayName
        ))
        add(StatLine(isSpacer = true))
        Damage.values().forEach { damage ->
            add(StatLine(
                "vs ${damage.displayName}", armorVs(damage),
                compare = compareTo?.let { (it as Clothing).armorVs(damage) }
            ))
        }
        add(StatLine(isSpacer = true))
        add(StatLine(
            "insulation", coldProtection(),
            compare = compareTo?.let { (it as Clothing).coldProtection() }
        ))
        add(StatLine(
            "cooling", heatProtection(),
            compare = compareTo?.let { (it as Clothing).heatProtection() }
        ))
        add(StatLine(
            "weather", weatherProtection(),
            compare = compareTo?.let { (it as Clothing).weatherProtection() }
        ))
    }
}


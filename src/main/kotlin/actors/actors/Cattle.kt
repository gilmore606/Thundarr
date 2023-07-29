package actors.actors

import actors.states.Idle
import actors.states.IdleHerd
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*
import util.Dice

@Serializable
sealed class GenericCattle : NPC() {
    override fun glyph() = Glyph.CATTLE
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.2f
    override fun hpMax() = 6f
    override fun unarmedWeapons() = setOf(horns, hooves)
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.HIDE

    override fun idleState(): Idle = IdleHerd(
        0.4f, 10, true,
        Dice.float(19f, 21f),
        Dice.float(5f, 7f),
    )
}

@Serializable
class Aurox : GenericCattle() {
    override val tag = Tag.AUROX
    override fun name() = "aurox"
    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."
    override fun onSpawn() {
        initStats(14, 8, 5, 9, 8, 0, 0)
    }
    override fun skinArmor() = 1.5f

    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        repeat(Dice.oneTo(2)) { add(RawMeat()) }
        if (Dice.chance(0.5f)) add(Hide())
    }
}

@Serializable
class MuskOx : GenericCattle() {
    override val tag = Tag.MUSKOX
    override fun hue() = 4.3f
    override fun name() = "muskox"
    override fun description() = "Predictably, it smells awful."
    override fun onSpawn() {
        initStats(15, 9, 5, 10, 9, 1, 0)
    }
    override fun skinArmor() = 3f

    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        repeat(Dice.oneTo(3)) { add(RawMeat()) }
        add(Hide())
    }
}

@Serializable
class Cyclox : GenericCattle() {
    override val tag = Tag.CYCLOX
    override fun glyph() = Glyph.CYCLOX
    override fun name() = "cyclox"
    override fun description() = "A large spheroid ruminant with wrinkly gray skin, and one giant eye."
    override fun onSpawn() {
        initStats(15, 10, 6, 14, 10, 2, 0)
    }
    override fun skinArmor() = 3f
    override fun canSeeInDark() = true

    override fun idleState() = IdleWander(0.5f)

    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        add(RawMeat())
        add(Hide())
    }
}

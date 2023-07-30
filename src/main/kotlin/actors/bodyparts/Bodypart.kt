package actors.bodyparts

import actors.actors.Actor
import kotlinx.serialization.Serializable
import things.Clothing
import things.Damage
import things.Gear
import java.lang.Float.max
import java.lang.Float.min

@Serializable
sealed class Bodypart(
    val name: String,
    val size: Float,
    val toHit: Int,
    val gearSlot: Gear.Slot? = null
) {

    companion object {
        fun humanoid() = setOf(
            Head(), Torso(), Hands(), Legs(), Feet()
        )
        fun quadruped() = setOf(
            Head(), Torso(), Legs()
        )
        fun serpent() = setOf(
            Head(), Torso()
        )
        fun blob() = setOf(
            Body()
        )
        fun tree() = setOf(
            Trunk(), Branches()
        )
        fun bird() = setOf(
            Head(), Wings(), Feet()
        )
    }

    fun clothingOn(actor: Actor): Clothing? = gearSlot?.let { actor.gear[it]?.let { it as Clothing } }

    fun getDeflect(actor: Actor): Float = clothingOn(actor)?.deflect() ?: actor.skinDeflect()

    fun reduceDamage(target: Actor, type: Damage, rawDamage: Float): Float {
        var damage = clothingOn(target)?.reduceDamage(target, type, rawDamage) ?: rawDamage
        if (damage > 0f) {
            damage = target.skinReduceDamage(target, type, rawDamage)
        }
        return max(0f, damage)
    }
}

@Serializable
class Head(
    val count: Int = 1
) : Bodypart(
    if (count < 2) "head" else "heads", 0.2f, -3, Gear.Slot.HEAD
)

@Serializable
class Torso : Bodypart(
    "torso", 0.5f, -1, Gear.Slot.TORSO
)

@Serializable
class Hands : Bodypart(
    "hands", 0.05f, -4, Gear.Slot.HANDS
)

@Serializable
class Legs(
    val count: Int = 2
) : Bodypart(
    if (count < 2) "leg" else "legs", 0.2f, -2, Gear.Slot.LEGS
)

@Serializable
class Feet : Bodypart(
    "feet", 0.05f, -3, Gear.Slot.FEET
)

@Serializable
class Body : Bodypart(
    "body", 1f, 0, Gear.Slot.TORSO
)

@Serializable
class Trunk : Bodypart(
    "trunk", 1f, 0, Gear.Slot.TORSO
)

@Serializable
class Branches : Bodypart(
    "branches", 1f, -1, Gear.Slot.HANDS
)

@Serializable
class Petals : Bodypart(
    "petals", 1f, 0, Gear.Slot.HANDS
)

@Serializable
class Wings : Bodypart(
    "wings", 1f, 0, Gear.Slot.HANDS
)

@Serializable
class Eyeball : Bodypart(
    "eyeball", 1f, 0, Gear.Slot.HEAD
)

@Serializable
class Stinger : Bodypart(
    "stinger", 0.5f, -1, Gear.Slot.NECK
)

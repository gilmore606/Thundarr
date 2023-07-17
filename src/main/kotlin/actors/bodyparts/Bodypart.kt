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
class Head : Bodypart(
    "head", 0.2f, -3, Gear.Slot.HEAD
)

@Serializable
class Torso : Bodypart(
    "torso", 0.4f, -1, Gear.Slot.TORSO
)

@Serializable
class Hands : Bodypart(
    "hands", 0.1f, -4, Gear.Slot.HANDS
)

@Serializable
class Legs : Bodypart(
    "legs", 0.2f, -2, Gear.Slot.LEGS
)

@Serializable
class Feet : Bodypart(
    "feet", 0.1f, -3, Gear.Slot.FEET
)

@Serializable
class Body : Bodypart(
    "body", 1f, 0, Gear.Slot.TORSO
)

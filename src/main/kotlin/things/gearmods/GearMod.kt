package things.gearmods

import kotlinx.serialization.Serializable

@Serializable
sealed class GearMod {
    enum class Tag(
        val get: GearMod
    ) {
        BENT(Bent), RUSTY(Rusty), LIGHT(Light), HEAVY(Heavy), FINE(Fine), MASTER(Master)
    }

    abstract val tag: Tag
    abstract fun prefix(): String
    open fun weight(): Float = 0f
    open fun valueMod(): Float = 1f

}

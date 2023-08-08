package things.gearmods

import kotlinx.serialization.Serializable

@Serializable
sealed class WeaponMod : GearMod() {
    open fun damage(): Float = 0f
    open fun accuracy(): Float = 0f
    open fun speed(): Float = 0f
}

@Serializable
object Bent : WeaponMod() {
    override val tag = Tag.BENT
    override fun prefix() = "bent "
    override fun accuracy() = -1f
    override fun valueMod() = 0.6f
}

@Serializable
object Rusty : WeaponMod() {
    override val tag = Tag.RUSTY
    override fun prefix() = "rusty "
    override fun damage() = -1f
    override fun valueMod() = 0.6f
}

@Serializable
object Light : WeaponMod() {
    override val tag = Tag.LIGHT
    override fun prefix() = "light "
    override fun accuracy() = 1f
    override fun weight() = -0.5f
    override fun valueMod() = 1.4f
}

@Serializable
object Heavy : WeaponMod() {
    override val tag = Tag.HEAVY
    override fun prefix() = "heavy "
    override fun damage() = 1f
    override fun accuracy() = -1f
    override fun weight() = 0.5f
    override fun valueMod() = 1.6f
}

@Serializable
object Fine : WeaponMod() {
    override val tag = Tag.FINE
    override fun prefix() = "fine "
    override fun accuracy() = 2f
    override fun valueMod() = 2f
}

@Serializable
object Master : WeaponMod() {
    override val tag = Tag.MASTER
    override fun prefix() = "master "
    override fun accuracy() = 1f
    override fun damage() = 1f
    override fun valueMod() = 3f
}

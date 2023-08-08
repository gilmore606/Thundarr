package things.gearmods

import kotlinx.serialization.Serializable

@Serializable
sealed class WeaponMod {
    abstract fun prefix(): String
    open fun damage(): Float = 0f
    open fun accuracy(): Float = 0f
    open fun weight(): Float = 0f
    open fun valueMod(): Float = 1f
    open fun xpLevel(): Int = 0
}

@Serializable
object Bent : WeaponMod() {
    override fun prefix() = "bent "
    override fun accuracy() = -1f
    override fun valueMod() = 0.6f
    override fun xpLevel() = -1
}

@Serializable
object Rusty : WeaponMod() {
    override fun prefix() = "rusty "
    override fun damage() = -1f
    override fun valueMod() = 0.6f
    override fun xpLevel() = -1
}

@Serializable
object Light : WeaponMod() {
    override fun prefix() = "light "
    override fun accuracy() = 1f
    override fun weight() = -0.5f
    override fun valueMod() = 1.4f
    override fun xpLevel() = 1
}

@Serializable
object Heavy : WeaponMod() {
    override fun prefix() = "heavy "
    override fun damage() = 1f
    override fun accuracy() = -1f
    override fun weight() = 0.5f
    override fun valueMod() = 1.6f
    override fun xpLevel() = 1
}

@Serializable
object Fine : WeaponMod() {
    override fun prefix() = "fine "
    override fun accuracy() = 2f
    override fun valueMod() = 2f
    override fun xpLevel() = 2
}

@Serializable
object Master : WeaponMod() {
    override fun prefix() = "master "
    override fun accuracy() = 1f
    override fun damage() = 1f
    override fun valueMod() = 3f
    override fun xpLevel() = 2
}

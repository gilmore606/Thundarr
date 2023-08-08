package things

import actors.stats.skills.Spears
import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
class Fist : UnarmedWeapon() {
    override val tag = Tag.FIST
    override fun baseName() = "fist"
    override fun description() = "Bare knuckles."
    override fun hitSelfMsg() = "You punch %dd in the %part!"
    override fun hitOtherMsg() = "%Dn punches %dd in the %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Teeth : UnarmedWeapon() {
    override val tag = Tag.TEETH
    override fun baseName() = "teeth"
    override fun description() = "Sharp teeth."
    override fun hitSelfMsg() = "You bite %dd's %part!"
    override fun hitOtherMsg() = "%Dn bites %dd's %part!"
    override fun damageType() = Damage.CUT
}

@Serializable
class Claws : UnarmedWeapon() {
    override val tag = Tag.CLAWS
    override fun baseName() = "claws"
    override fun description() = "Sharp claws."
    override fun hitSelfMsg() = "You tear at %dd's %part with your claws!"
    override fun hitOtherMsg() = "%Dn claws %dd's %part!"
    override fun damageType() = Damage.CUT
}

@Serializable
class Mandibles : UnarmedWeapon() {
    override val tag = Tag.MANDIBLES
    override fun baseName() = "mandibles"
    override fun description() = "Sharp mandibles."
    override fun hitSelfMsg() = "You bite %dd's %part with your mandibles!"
    override fun hitOtherMsg() = "%Dn's mandibles tear at %dd's %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Horns : UnarmedWeapon() {
    override val tag = Tag.HORNS
    override fun baseName() = "horns"
    override fun description() = "Sharp horns."
    override fun hitSelfMsg() = "You ram your horns into %dd's %part!"
    override fun hitOtherMsg() = "%Dn stabs %p horns into %dd's %part!"
    override fun damageType() = Damage.PIERCE
}

@Serializable
class Hooves : UnarmedWeapon() {
    override val tag = Tag.HOOVES
    override fun baseName() = "horns"
    override fun description() = "Crushing hooves."
    override fun hitSelfMsg() = "You trample %dd's %part!"
    override fun hitOtherMsg() = "%Dn tramples %dd's %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Beak : UnarmedWeapon() {
    override val tag = Tag.BEAK
    override fun baseName() = "beak"
    override fun description() = "Sharp beak."
    override fun hitSelfMsg() = "You peck at %dd's %part!"
    override fun hitOtherMsg() = "%Dn pecks at %dd's %part!"
    override fun damageType() = Damage.PIERCE
}

@Serializable
class Branches : UnarmedWeapon() {
    override val tag = Tag.BRANCHES
    override fun baseName() = "branches"
    override fun description() = "Woody branches."
    override fun hitSelfMsg() = "You flail your branches at %dd's %part!"
    override fun hitOtherMsg() = "%Dn flails %p branches at %dd's %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class UnarmedSpear : UnarmedWeapon() {
    override val tag = Tag.WOODSPEAR
    override fun baseName() = "wood spear"
    override fun description() = "A wooden pike, its tip hardened in fire."
    override fun hitSelfMsg() = "You stab %dd's %part!"
    override fun hitOtherMsg() = "%Dn stabs %p spear into %dd's %part!"
    override fun damageType() = Damage.PIERCE
}

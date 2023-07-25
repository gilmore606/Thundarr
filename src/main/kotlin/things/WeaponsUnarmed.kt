package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
class Fist : UnarmedWeapon() {
    override val tag = Tag.FIST
    override fun glyph() = Glyph.BLANK
    override fun name() = "fist"
    override fun description() = "Bare knuckles."
    override fun hitSelfMsg() = "You punch %dd in the %part!"
    override fun hitOtherMsg() = "%Dn punches %dd in the %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Teeth : UnarmedWeapon() {
    override val tag = Tag.TEETH
    override fun glyph() = Glyph.BLANK
    override fun name() = "teeth"
    override fun description() = "Sharp teeth."
    override fun hitSelfMsg() = "You bite %dd's %part!"
    override fun hitOtherMsg() = "%Dn bites %dd's %part!"
    override fun damageType() = Damage.CUT
}

@Serializable
class Claws : UnarmedWeapon() {
    override val tag = Tag.CLAWS
    override fun glyph() = Glyph.BLANK
    override fun name() = "claws"
    override fun description() = "Sharp claws."
    override fun hitSelfMsg() = "You tear at %dd's %part with your claws!"
    override fun hitOtherMsg() = "%Dn claws %dd's %part!"
    override fun damageType() = Damage.CUT
}

@Serializable
class Mandibles : UnarmedWeapon() {
    override val tag = Tag.MANDIBLES
    override fun glyph() = Glyph.BLANK
    override fun name() = "mandibles"
    override fun description() = "Sharp mandibles."
    override fun hitSelfMsg() = "You bite %dd's %part with your mandibles!"
    override fun hitOtherMsg() = "%Dn's mandibles tear at %dd's %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Horns : UnarmedWeapon() {
    override val tag = Tag.HORNS
    override fun glyph() = Glyph.BLANK
    override fun name() = "horns"
    override fun description() = "Sharp horns."
    override fun hitSelfMsg() = "You ram your horns into %dd's %part!"
    override fun hitOtherMsg() = "%Dn stabs %p horns into %dd's %part!"
    override fun damageType() = Damage.PIERCE
}

@Serializable
class Hooves : UnarmedWeapon() {
    override val tag = Tag.HOOVES
    override fun glyph() = Glyph.BLANK
    override fun name() = "horns"
    override fun description() = "Crushing hooves."
    override fun hitSelfMsg() = "You trample %dd's %part!"
    override fun hitOtherMsg() = "%Dn tramples %dd's %part!"
    override fun damageType() = Damage.CRUSH
}

@Serializable
class Beak : UnarmedWeapon() {
    override val tag = Tag.BEAK
    override fun glyph() = Glyph.BLANK
    override fun name() = "beak"
    override fun description() = "Sharp beak."
    override fun hitSelfMsg() = "You peck at %dd's %part!"
    override fun hitOtherMsg() = "%Dn pecks at %dd's %part!"
    override fun damageType() = Damage.PIERCE
}

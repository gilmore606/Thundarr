package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
class Fist : MeleeWeapon() {
    override val tag = Tag.FIST
    override fun glyph() = Glyph.BLANK
    override fun name() = "fist"
    override fun description() = "Bare knuckles."
    override fun hitSelfMsg() = "You punch %dd in the %part!"
    override fun hitOtherMsg() = "%Dn punches %dd in the %part!"
    override fun damageType() = Damage.CRUSH
    override fun damage() = 1f
}

@Serializable
class Teeth : MeleeWeapon() {
    override val tag = Tag.TEETH
    override fun glyph() = Glyph.BLANK
    override fun name() = "teeth"
    override fun description() = "Sharp teeth."
    override fun hitSelfMsg() = "You bite %dd's %part!"
    override fun hitOtherMsg() = "%Dn bites %dd's %part!"
    override fun damageType() = Damage.CUT
    override fun damage() = 1.5f
}

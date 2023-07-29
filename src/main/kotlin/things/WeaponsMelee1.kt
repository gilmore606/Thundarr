package things

import actors.actors.Actor
import actors.stats.Heart
import actors.stats.Stat
import actors.stats.skills.Blades
import actors.stats.skills.Spears
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor

// Basic melee weapons


@Serializable
class Gladius : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SWORD_WORN)
    }
    override val tag = Tag.GLADIUS
    override fun name() = "gladius"
    override fun description() = "A double edged iron short sword."
    override fun glyphTransform() = glyphTransform
    override fun skill() = Blades
    override fun glyph() = Glyph.SWORD
    override fun weight() = 0.5f
    override fun damageType() = Damage.CUT
    override fun damage() = 5f
}

@Serializable
class Sunsword : MeleeWeapon(), LightSource {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SUNSWORD_LIT)
        val lightColor = LightColor(0.1f, 0.3f, 0.3f)
    }
    override val tag = Tag.SUNSWORD
    override fun glyph() = if (equipped) Glyph.SUNSWORD_LIT else Glyph.SUNSWORD
    override fun glyphTransform() = glyphTransform
    override fun name() = "sunsword"
    override fun description() = "The legendary Sunsword holds the power of sunlight.  Weirdly effective against robots."
    override fun skill() = Blades
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Heart.tag] = 1f
    }
    override fun light() = if (equipped) lightColor else null
    override fun damageType() = Damage.SHOCK
    override fun damage() = 12f

    override fun onEquip(actor: Actor) {
        actor.level?.addLightSource(actor.xy.x, actor.xy.y, actor)
    }

    override fun onUnequip(actor: Actor) {
        actor.level?.removeLightSource(actor)
    }
}

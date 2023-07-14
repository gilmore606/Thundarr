package things

import actors.Actor
import actors.NPC
import actors.Player
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.LightColor

@Serializable
class Shrine : LitThing() {
    init {
        active = true
    }

    var lastPrayedDate = 0

    override val tag = Tag.SHRINE
    override fun name() = "shrine"
    override fun description() = "A stone shrine erected to honor the Lords of Light."
    override fun glyph() = Glyph.SHRINE
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking(actor: Actor) = true
    override fun ambientSound() = Speaker.PointAmbience(Speaker.Ambience.SHRINECHORD, 24f, 1f)
    override val lightColor = LightColor(0.1f, 0.1f, 0.4f)

    override fun uses() = mapOf(
        UseTag.USE to Use("pray to " + name(), 2.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y -> doPray(actor) }
        )
    )

    fun doPray(actor: Actor, priest: NPC? = null) {
        Console.sayAct("You whisper a prayer to the Lords of Light.",  "%DN kneels before %dd and whispers a prayer.", actor, this)
        if (actor is Player) {
            if (actor.levelUpsAvailable > 0) {
                Speaker.world(Speaker.SFX.PRAYER, source = xy())
                actor.levelUp()
                return
            }
            if (lastPrayedDate == App.gameTime.date) {
                Console.say("A voice speaks from deep inside the shrine: \"The Lords help those who help themselves...\"")
                return
            }
            lastPrayedDate = App.gameTime.date
            Speaker.world(Speaker.SFX.PRAYER, source = xy())
            if (actor.hp < actor.hpMax) {
                Console.say("You feel a warm glow spread out from your heart, through your body, healing every wound as it flows.")
                actor.healDamage(actor.hpMax - actor.hp)
            }
        }
    }

}

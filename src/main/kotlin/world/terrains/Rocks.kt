package world.terrains

import actors.Actor
import actors.stats.skills.Survive
import audio.Speaker
import render.tilesets.Glyph
import things.Rock
import things.Thing
import ui.panels.Console

object Rocks : Floor(Type.TERRAIN_ROCKS, Glyph.ROCKS, true, 1f) {
    override fun name() = "rocky ground"
    override fun moveSpeed(actor: Actor) = 1.4f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun fertilityBonus() = -0.4f

    override fun uses() = mapOf(
        Thing.UseTag.TRANSFORM to Thing.Use("find a rock", 4f,
            canDo = { actor,x,y,targ -> true },
            toDo = { actor,level,x,y ->
                scavengeRock(actor)
            })
    )

    private fun scavengeRock(actor: Actor) {
        if (Survive.resolve(actor, 1f) >= 0) {
            Console.sayAct("You find a good sized rock in the stony ground.", "%Dn picks up a rock.", actor)
            Rock().moveTo(actor)
        } else {
            Console.sayAct("You fail to find a decent sized rock.", "%Dn pokes around in the rocks.", actor)
        }
    }
}

object CaveRocks : Floor(Type.TERRAIN_CAVE_ROCKS, Glyph.CAVE_ROCKS, false, 1f) {
    override fun name() = "rocky ground"
    override fun moveSpeed(actor: Actor) = 1.4f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun uses() = Rocks.uses()
}

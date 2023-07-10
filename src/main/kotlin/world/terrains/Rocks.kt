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

    override fun scavengeProduct() = Rock()
    override fun scavengeCommand() = "find a rock"
    override fun scavengeDifficulty() = -1f
    override fun scavengeMsg() = "You find a good sized rock in the stony ground."
    override fun scavengeFailMsg() = "You root around a while, but fail to find a decent sized rock."
}

object CaveRocks : Floor(Type.TERRAIN_CAVE_ROCKS, Glyph.CAVE_ROCKS, false, 1f) {
    override fun name() = "rocky ground"
    override fun moveSpeed(actor: Actor) = 1.4f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD

    override fun scavengeProduct() = Rock()
    override fun scavengeCommand() = "find a rock"
    override fun scavengeDifficulty() = 0f
    override fun scavengeMsg() = "You find a good sized rock among the stones."
    override fun scavengeFailMsg() = "You root around a while, but fail to find a decent sized rock."
}

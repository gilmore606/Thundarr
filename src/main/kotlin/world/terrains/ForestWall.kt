package world.terrains

import render.tilesets.Glyph
import things.Log
import things.Stick
import things.Thing
import util.Dice

sealed class ForestWall(
    type: Terrain.Type,
    glyph: Glyph
) : Wall(type, glyph, 4f) {
    override fun isDiggable() = false
    override fun pruneVerticalOrphans() = true

    override fun scavengeProduct(): Thing = if (Dice.flip()) Log() else Stick()
    override fun scavengeCommand() = "find wood"
    override fun scavengeDifficulty() = 0f
    override fun scavengeMsg() = "You root around in the forest and find %it."
    override fun scavengeFailMsg() = "You root around in the forest, but fail to find any good wood."
}

object TemperateForestWall : ForestWall(Type.TERRAIN_TEMPERATE_FORESTWALL, Glyph.TEMPERATE_FOREST_WALL) {
    override fun name() = "trees"
    override fun bumpMsg() = "The forest is too thick to pass this way."
}

object PineForestWall : ForestWall(Type.TERRAIN_PINE_FORESTWALL, Glyph.PINE_FOREST_WALL) {
    override fun name() = "pine trees"
    override fun bumpMsg() = "The forest is too thick to pass this way."
}

object TropicalForestWall : ForestWall(Type.TERRAIN_TROPICAL_FORESTWALL, Glyph.TROPICAL_FOREST_WALL) {
    override fun name() = "jungle trees"
    override fun bumpMsg() = "The jungle is too thick to pass this way."
}

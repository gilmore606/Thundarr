package world.terrains

import kotlinx.serialization.Serializable
import util.Glyph

@Serializable
sealed class Terrain(
    private val glyph: Glyph,
    private val walkable: Boolean,
    private val flyable: Boolean,
    private val opaque: Boolean,
    private val msgWalkOn: String = ""
) {

    companion object {
        fun create(type: Type) = when (type) {
            Type.TERRAIN_BRICKWALL -> BrickWall()
            Type.TERRAIN_STONEFLOOR -> StoneFloor()
        }
    }

    enum class Type { TERRAIN_BRICKWALL, TERRAIN_STONEFLOOR }

    open fun glyph() = this.glyph

    open fun isWalkable() = this.walkable

    open fun isOpaque() = this.opaque
}

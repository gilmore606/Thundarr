package util

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    WALL,
    FLOOR,
    OPEN_DOOR,
    CLOSED_DOOR,

    CURSOR,

    PLAYER,
    SKELETON,
    SKULL
}

package util

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    WALL,
    FLOOR,
    OPEN_DOOR,
    CLOSED_DOOR,

    CURSOR,
    BOX_BG,
    BOX_BORDER,
    BOX_SHADOW,

    PLAYER,
    SKELETON,
    SKULL
}

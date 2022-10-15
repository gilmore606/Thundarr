package render.tilesets

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
    LOGO_MOON,
    LOGO_OOKLA,

    PLAYER,
    SKELETON,
    SKULL,

    TREE,
    LIGHTBULB
}

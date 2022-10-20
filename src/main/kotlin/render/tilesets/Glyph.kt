package render.tilesets

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    BLANK,

    BRICK_WALL,
    STONE_FLOOR,
    DIRT,
    GRASS,
    PORTAL_DOOR,

    CURSOR,
    BOX_BG,
    BOX_BORDER,
    BOX_SHADOW,
    LOGO_MOON,
    LOGO_OOKLA,
    OCCLUSION_SHADOWS_V,
    OCCLUSION_SHADOWS_H,
    PANEL_SHADOW,

    PLAYER,
    SKELETON,
    SKULL,

    TREE,
    LIGHTBULB
}

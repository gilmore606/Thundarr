package render.tilesets

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    BLANK,

    BRICK_WALL,
    STONE_FLOOR,
    DIRT,
    GRASS,
    WATER,
    PORTAL_DOOR,

    CURSOR,
    BOX_BG,
    BOX_BORDER,
    BOX_SHADOW,
    LOGO_MOON,
    LOGO_OOKLA,
    OCCLUSION_SHADOWS_V,
    OCCLUSION_SHADOWS_H,
    SURF_V,
    SURF_H,
    PANEL_SHADOW,

    PLAYER,
    MOK,
    HORSE,
    CATTLE,
    PEASANT,
    CITIZEN,
    TRIBAL,

    TREE,
    LIGHTBULB,
    AXE,
    SIGN,
    CHEST,
    CACTUS,
    DEAD_TREE,
    TOMBSTONE,
    BIG_ROCK,
    BLADE,
    HILT,
    PINE_TREE,
    PALM_TREE,
    BOW,
    BOTTLE,
    FRUIT
}

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
    WALL_DAMAGE,

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
    BUTTON_INVENTORY,
    BUTTON_MAP,
    BUTTON_SYSTEM,
    BUTTON_JOURNAL,
    BUTTON_GEAR,
    BUTTON_SKILLS,
    BUTTON_PAUSE,
    BUTTON_PLAY,
    BUTTON_FFWD,
    BUTTON_BLANK,
    COLOR_BARS,
    WINDOW_SHADE,

    PLAYER,
    MOK,
    HORSE,
    CATTLE,
    PEASANT,
    CITIZEN,
    TRIBAL,
    HERDER,
    WOLFMAN,
    MOB_SHADOW,
    SNAKEMAN,
    KARROK,
    THRALL,
    RATMAN,
    RATTHING,

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
    HILT_LIT,
    PINE_TREE,
    PALM_TREE,
    BOTTLE,
    FRUIT,
    TORCH,
    TORCH_LIT,
    SMOKE_PUFF,
    DUST_PUFF,
    HEALTH_ICON,
    SLEEP_ICON,
    ANGRY_THUNDARR,
    SPEECH_BUBBLE,
    POW_ICON,
    HOSTILE_ICON,
    CORPSE,
    MEAT,
    FILING_CABINET,
    HARD_HAT,
    HORNED_HAT,
    HELMET,
    LOG,
    BRICK,
    RAINDROP,
    LIGHTER,
    CAMPFIRE,
    DOOR_CLOSED,
    DOOR_OPEN,
    TABLE,
    BOARD,
    BROWN_BOOTS,
    BROWN_BOOTS_WORN,
    GRAY_BOOTS,
    GRAY_BOOTS_WORN,
    RED_TUNIC,
    RED_TUNIC_WORN,
    RED_JACKET,
    RED_JACKET_WORN,
    NECK_CHARM,
    NECK_CHARM_WORN,
    CEILING_LIGHT,

    BLOODSTAIN,
    SCORCHMARK
}
